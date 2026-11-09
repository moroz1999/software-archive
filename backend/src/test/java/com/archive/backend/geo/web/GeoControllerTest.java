package com.archive.backend.geo.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.geo.app.ListCitiesHandler;
import com.archive.backend.geo.app.ListCitiesQuery;
import com.archive.backend.geo.app.ListCountriesHandler;
import com.archive.backend.geo.app.ListCountriesQuery;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.read.CityItem;
import com.archive.backend.geo.read.CountryItem;
import com.archive.backend.geo.web.mapper.GeoWebMapperImpl;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GeoController.class)
@Import({SecurityConfig.class, WebConfig.class, GeoWebMapperImpl.class, ApiExceptionHandler.class})
class GeoControllerTest {

    private static final UUID COUNTRY = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CITY = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListCountriesHandler countriesHandler;

    @MockitoBean
    private ListCitiesHandler citiesHandler;

    @Test
    void listsTheCountries() throws Exception {
        when(countriesHandler.handle(new ListCountriesQuery()))
                .thenReturn(List.of(new CountryItem(COUNTRY, "Finland")));

        mvc.perform(get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(COUNTRY.toString()))
                .andExpect(jsonPath("$[0].name").value("Finland"));
    }

    @Test
    void listsTheCitiesOfACountry() throws Exception {
        when(citiesHandler.handle(new ListCitiesQuery(new CountryId(COUNTRY))))
                .thenReturn(List.of(new CityItem(CITY, "Helsinki")));

        mvc.perform(get("/api/countries/{id}/cities", COUNTRY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(CITY.toString()))
                .andExpect(jsonPath("$[0].name").value("Helsinki"));
    }

    @Test
    void answers400ForAMalformedCountryId() throws Exception {
        mvc.perform(get("/api/countries/{id}/cities", "not-a-uuid")).andExpect(status().isBadRequest());
    }
}
