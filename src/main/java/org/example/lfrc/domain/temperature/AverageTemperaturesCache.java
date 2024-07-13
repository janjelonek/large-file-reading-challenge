package org.example.lfrc.domain.temperature;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AverageTemperaturesCache {

    private Map<String, Map<Integer, Double>> averageTemperaturesMap;

    public void setAverageTemperaturesMap(Map<String, Map<Integer, Double>> averageTemperaturesMap) {
        this.averageTemperaturesMap = averageTemperaturesMap;
    }

    public Map<Integer, Double> getAverageTemperaturesForCity(String city) {
        return averageTemperaturesMap.get(city);
    }
}
