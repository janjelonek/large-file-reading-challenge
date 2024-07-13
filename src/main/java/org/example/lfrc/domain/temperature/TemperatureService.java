package org.example.lfrc.domain.temperature;

import org.example.lfrc.domain.temperature.exception.CityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TemperatureService {

    private final AverageTemperaturesCache averageTemperaturesCache;

    public TemperatureService(AverageTemperaturesCache averageTemperaturesCache) {
        this.averageTemperaturesCache = averageTemperaturesCache;
    }

    public List<AverageTemperaturesDTO> getAverageTemperaturesForCity(String city) {
        Map<Integer, Double> averageTemperaturesForCity = averageTemperaturesCache.getAverageTemperaturesForCity(city);

        if (averageTemperaturesForCity == null) {
            throw new CityNotFoundException(city);
        }

        return averageTemperaturesForCity.entrySet().stream()
                .map(entry -> new AverageTemperaturesDTO(entry.getKey().toString(), entry.getValue()))
                .toList();
    }
}
