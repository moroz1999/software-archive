package com.archive.backend.shared.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.shared.domain.error.ConflictException;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class ApiExceptionHandlerTest {

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
            .setControllerAdvice(new ApiExceptionHandler())
            .build();

    @Test
    void notFoundIs404WithCode() throws Exception {
        mvc.perform(get("/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("test.not-found"));
    }

    @Test
    void conflictIs409WithCode() throws Exception {
        mvc.perform(get("/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("test.conflict"));
    }

    @Test
    void ruleViolationIs422WithFieldError() throws Exception {
        mvc.perform(get("/rule-violation"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("test.title-taken"))
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[0].code").value("test.title-taken"));
    }

    @Test
    void ruleViolationWithoutFieldHasNoErrors() throws Exception {
        mvc.perform(get("/rule-violation-form"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("test.form-rule"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void aChangeLostToAnotherRunningAtOnceIs409WithCode() throws Exception {
        mvc.perform(get("/concurrent"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("conflict.concurrent"));
    }

    @Test
    void invalidValueIs400WithCode() throws Exception {
        mvc.perform(get("/invalid-value"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("test.invalid"));
    }

    @Test
    void malformedRequestIs400() throws Exception {
        mvc.perform(post("/body").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void unexpectedFailureIs500WithoutInternals() throws Exception {
        mvc.perform(get("/crash"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(content().string(not(containsString("secret"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/not-found")
        void notFound() {
            throw new NotFoundException("test.not-found", "Nothing here.");
        }

        @GetMapping("/conflict")
        void conflict() {
            throw new ConflictException("test.conflict", "Already changed.");
        }

        @GetMapping("/concurrent")
        void concurrent() {
            throw new CannotAcquireLockException("deadlock detected");
        }

        @GetMapping("/rule-violation")
        void ruleViolation() {
            throw new RuleViolationException("test.title-taken", "Title is taken.", "title");
        }

        @GetMapping("/rule-violation-form")
        void ruleViolationForm() {
            throw new RuleViolationException("test.form-rule", "Form rule.");
        }

        @GetMapping("/invalid-value")
        void invalidValue() {
            throw new InvalidValueException("test.invalid", "Invalid.");
        }

        @PostMapping("/body")
        void body(@RequestBody Payload payload) {}

        @GetMapping("/crash")
        void crash() {
            throw new IllegalStateException("secret internals");
        }
    }

    record Payload(String value) {}
}
