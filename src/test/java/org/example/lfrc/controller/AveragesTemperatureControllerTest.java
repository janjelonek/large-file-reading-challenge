package org.example.lfrc.controller;

import org.example.lfrc.MockMvcConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {MockMvcConfig.class})
class AveragesTemperatureControllerTest {

    private static final String URL_TEMPLATE = "/api/v1/cities/{city}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnCorrectAverageTemperatureForWarsaw() throws Exception {
        performCityTemperatureTest("Warszawa", List.of("2018"), List.of(6.3));
    }

    @Test
    void shouldReturnCorrectAverageTemperatureForKrakow() throws Exception {
        performCityTemperatureTest("Kraków", List.of("2018", "2019"), List.of(5.3, 6.3));
    }

    @Test
    void shouldReturnBadRequestWhenCityDoesNotExist() throws Exception {
        String city = "Bielsko-Biała";

        mockMvc.perform(get(URL_TEMPLATE, city))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .isEqualTo(String.format("City not found: %s", city)));
    }

    private void performCityTemperatureTest(String city, List<String> years, List<Double> temperatures) throws Exception {
        var resultActions = mockMvc.perform(get(URL_TEMPLATE, city))
                .andExpect(status().isOk());

        for (int i = 0; i < years.size(); i++) {
            resultActions
                    .andExpect(jsonPath(String.format("$[%d].year", i)).value(years.get(i)))
                    .andExpect(jsonPath(String.format("$[%d].averageTemperature", i)).value(temperatures.get(i)));
        }
    }
}

