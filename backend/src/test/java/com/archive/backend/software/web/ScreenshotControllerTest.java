package com.archive.backend.software.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import com.archive.backend.software.app.AddScreenshotsCommand;
import com.archive.backend.software.app.AddScreenshotsHandler;
import com.archive.backend.software.app.DeleteScreenshotCommand;
import com.archive.backend.software.app.DeleteScreenshotHandler;
import com.archive.backend.software.app.GetScreenshotFileHandler;
import com.archive.backend.software.app.GetScreenshotFileQuery;
import com.archive.backend.software.app.ListScreenshotsHandler;
import com.archive.backend.software.app.ListScreenshotsQuery;
import com.archive.backend.software.app.ReorderScreenshotsCommand;
import com.archive.backend.software.app.ReorderScreenshotsHandler;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ReleaseNotFoundException;
import com.archive.backend.software.domain.ScreenshotFormat;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.ScreenshotImages;
import com.archive.backend.software.domain.ScreenshotNotFoundException;
import com.archive.backend.software.domain.ScreenshotOrderOutdatedException;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.read.ScreenshotFile;
import com.archive.backend.software.read.SoftwareScreenshot;
import com.archive.backend.software.web.mapper.ScreenshotWebMapper;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScreenshotController.class)
@Import({SecurityConfig.class, WebConfig.class, ScreenshotWebMapper.class, ApiExceptionHandler.class})
class ScreenshotControllerTest {

    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID AY_MOD = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final UUID TITLE = UUID.fromString("00000000-0000-0000-0055-000000000001");
    private static final UUID GAME = UUID.fromString("00000000-0000-0000-0055-000000000002");
    private static final String OF_SOFTWARE = "/api/software/{softwareId}/screenshots";
    private static final String OF_RELEASE = "/api/software/{softwareId}/releases/{releaseId}/screenshots";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListScreenshotsHandler listHandler;

    @MockitoBean
    private AddScreenshotsHandler addHandler;

    @MockitoBean
    private DeleteScreenshotHandler deleteHandler;

    @MockitoBean
    private ReorderScreenshotsHandler reorderHandler;

    @MockitoBean
    private GetScreenshotFileHandler fileHandler;

    private static MockMultipartFile file(byte[] bytes) {
        return file("shot.png", bytes);
    }

    private static MockMultipartFile file(String name, byte[] bytes) {
        return new MockMultipartFile("files", name, "image/png", bytes);
    }

    @Test
    void listsTheScreenshotsOfSoftwareWithTheAddressesOfTheirPictures() throws Exception {
        when(listHandler.handle(new ListScreenshotsQuery(new SoftwareId(ELITE), null)))
                .thenReturn(List.of(new SoftwareScreenshot(TITLE), new SoftwareScreenshot(GAME)));

        mvc.perform(get(OF_SOFTWARE, ELITE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TITLE.toString()))
                .andExpect(jsonPath("$[0].url").value("/api/screenshots/" + TITLE))
                .andExpect(jsonPath("$[1].id").value(GAME.toString()));
    }

    @Test
    void listsTheScreenshotsOfARelease() throws Exception {
        when(listHandler.handle(new ListScreenshotsQuery(new SoftwareId(ELITE), new ReleaseId(AY_MOD))))
                .thenReturn(List.of(new SoftwareScreenshot(GAME)));

        mvc.perform(get(OF_RELEASE, ELITE, AY_MOD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].url").value("/api/screenshots/" + GAME));
    }

    @Test
    void answersNotFoundForUnknownSoftwareOrRelease() throws Exception {
        when(listHandler.handle(new ListScreenshotsQuery(new SoftwareId(ELITE), null)))
                .thenThrow(new SoftwareNotFoundException());
        when(listHandler.handle(new ListScreenshotsQuery(new SoftwareId(ELITE), new ReleaseId(AY_MOD))))
                .thenThrow(new ReleaseNotFoundException());

        mvc.perform(get(OF_SOFTWARE, ELITE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("software.not-found"));
        mvc.perform(get(OF_RELEASE, ELITE, AY_MOD))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("release.not-found"));
    }

    @Test
    void uploadsScreenshotsAndAnswersWithThem() throws Exception {
        when(addHandler.handle(any(AddScreenshotsCommand.class)))
                .thenReturn(List.of(new ScreenshotId(TITLE), new ScreenshotId(GAME)));

        mvc.perform(multipart(OF_SOFTWARE, ELITE)
                        .file(file("title.png", ScreenshotImages.png()))
                        .file(file("game.gif", ScreenshotImages.gif())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(TITLE.toString()))
                .andExpect(jsonPath("$[0].url").value("/api/screenshots/" + TITLE))
                .andExpect(jsonPath("$[1].id").value(GAME.toString()));

        ArgumentCaptor<AddScreenshotsCommand> sent = ArgumentCaptor.forClass(AddScreenshotsCommand.class);
        verify(addHandler).handle(sent.capture());
        assertThat(sent.getValue().actor()).isEqualTo(Actor.anonymous());
        assertThat(sent.getValue().softwareId()).isEqualTo(new SoftwareId(ELITE));
        assertThat(sent.getValue().releaseId()).isNull();
        assertThat(sent.getValue().images())
                .extracting(ScreenshotImage::format)
                .containsExactly(ScreenshotFormat.PNG, ScreenshotFormat.GIF);
        // What the archive keeps of each upload: the name of its file and its size.
        assertThat(sent.getValue().images())
                .extracting(image -> image.name().value(), ScreenshotImage::size)
                .containsExactly(tuple("title.png", (long) ScreenshotImages.png().length), tuple("game.gif", (long)
                        ScreenshotImages.gif().length));
        // The content is streamed from the uploaded part, whole.
        try (InputStream content = sent.getValue().images().get(1).content().open()) {
            assertThat(content.readAllBytes()).isEqualTo(ScreenshotImages.gif());
        }
    }

    @Test
    void uploadsScreenshotsOfARelease() throws Exception {
        when(addHandler.handle(any(AddScreenshotsCommand.class))).thenReturn(List.of(new ScreenshotId(TITLE)));

        mvc.perform(multipart(OF_RELEASE, ELITE, AY_MOD).file(file(ScreenshotImages.jpeg())))
                .andExpect(status().isCreated());

        ArgumentCaptor<AddScreenshotsCommand> sent = ArgumentCaptor.forClass(AddScreenshotsCommand.class);
        verify(addHandler).handle(sent.capture());
        assertThat(sent.getValue().releaseId()).isEqualTo(new ReleaseId(AY_MOD));
    }

    @Test
    void refusesAFileThatIsNoPictureUnderTheFiles() throws Exception {
        mvc.perform(multipart(OF_SOFTWARE, ELITE)
                        .file(file(ScreenshotImages.png()))
                        .file(file(ScreenshotImages.text())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("screenshot.format-unsupported"))
                .andExpect(jsonPath("$.errors[0].field").value("files"));
        verify(addHandler, never()).handle(any());
    }

    @Test
    void refusesAPictureOverTenMegabytes() throws Exception {
        mvc.perform(multipart(OF_SOFTWARE, ELITE).file(file(ScreenshotImages.pngOf(ScreenshotImage.MAX_BYTES + 1))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("screenshot.too-large"))
                .andExpect(jsonPath("$.errors[0].field").value("files"));
    }

    @Test
    void answersAnUploadWithoutFilesAsAnIncorrectRequest() throws Exception {
        mvc.perform(multipart(OF_SOFTWARE, ELITE)).andExpect(status().isBadRequest());
    }

    @Test
    void refusesAnUploadOfAVisitorOrOfSomeoneWhoMayNot() throws Exception {
        when(addHandler.handle(any(AddScreenshotsCommand.class)))
                .thenThrow(new SignInRequiredException())
                .thenThrow(new ChangeForbiddenException());

        mvc.perform(multipart(OF_SOFTWARE, ELITE).file(file(ScreenshotImages.png())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.required"));
        mvc.perform(multipart(OF_SOFTWARE, ELITE).file(file(ScreenshotImages.png())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("access.denied"));
    }

    @Test
    void reordersTheScreenshots() throws Exception {
        mvc.perform(put(OF_RELEASE + "/order", ELITE, AY_MOD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"order\":[\"" + GAME + "\",\"" + TITLE + "\"]}"))
                .andExpect(status().isNoContent());

        verify(reorderHandler)
                .handle(new ReorderScreenshotsCommand(
                        Actor.anonymous(),
                        new SoftwareId(ELITE),
                        new ReleaseId(AY_MOD),
                        List.of(new ScreenshotId(GAME), new ScreenshotId(TITLE))));
    }

    @Test
    void answersAnOutdatedOrderWithAConflict() throws Exception {
        doThrow(new ScreenshotOrderOutdatedException())
                .when(reorderHandler)
                .handle(any(ReorderScreenshotsCommand.class));

        mvc.perform(put(OF_SOFTWARE + "/order", ELITE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"order\":[\"" + TITLE + "\"]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("screenshot.order-outdated"));
    }

    @Test
    void deletesAScreenshot() throws Exception {
        mvc.perform(delete(OF_SOFTWARE + "/{screenshotId}", ELITE, TITLE)).andExpect(status().isNoContent());

        verify(deleteHandler)
                .handle(new DeleteScreenshotCommand(
                        Actor.anonymous(), new SoftwareId(ELITE), null, new ScreenshotId(TITLE)));
    }

    @Test
    void answersNotFoundForDeletingAnUnknownScreenshot() throws Exception {
        doThrow(new ScreenshotNotFoundException()).when(deleteHandler).handle(any(DeleteScreenshotCommand.class));

        mvc.perform(delete(OF_RELEASE + "/{screenshotId}", ELITE, AY_MOD, TITLE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("screenshot.not-found"));
    }

    @Test
    void servesThePictureAsStoredForAYear(@TempDir Path dir) throws Exception {
        Path picture = Files.write(dir.resolve("picture"), ScreenshotImages.gif());
        when(fileHandler.handle(new GetScreenshotFileQuery(new ScreenshotId(TITLE))))
                .thenReturn(new ScreenshotFile(picture, "image/gif"));

        mvc.perform(get("/api/screenshots/{id}", TITLE))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/gif"))
                .andExpect(header().string("Cache-Control", "max-age=31536000, public, immutable"))
                .andExpect(content().bytes(ScreenshotImages.gif()));
    }

    @Test
    void answersNotFoundForAnUnknownPictureAndAnIncorrectRequestForAMalformedId() throws Exception {
        when(fileHandler.handle(any(GetScreenshotFileQuery.class))).thenThrow(new ScreenshotNotFoundException());

        mvc.perform(get("/api/screenshots/{id}", TITLE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("screenshot.not-found"));
        mvc.perform(get("/api/screenshots/{id}", "not-an-id")).andExpect(status().isBadRequest());
    }
}
