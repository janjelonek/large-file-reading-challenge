package org.example.lfrc.controller;

import org.example.lfrc.domain.temperature.AverageTemperaturesDTO;
import org.example.lfrc.domain.temperature.TemperatureService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
public class AveragesTemperatureController {

    private final TemperatureService temperatureService;

    public AveragesTemperatureController(TemperatureService temperatureService) {
        this.temperatureService = temperatureService;
    }

    @GetMapping("/{city}")
    public List<AverageTemperaturesDTO> averageTemperatures(@PathVariable String city) {
        return temperatureService.getAverageTemperaturesForCity(city);
    }
}
