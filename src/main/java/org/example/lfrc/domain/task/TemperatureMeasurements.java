package org.example.lfrc.domain.task;

public class TemperatureMeasurements {

    double sumOfTemperatures;
    int count;

    public void addTemperature(double temperature) {
        sumOfTemperatures += temperature;
        count++;
    }

    public double average() {
        return count == 0 ? 0 : sumOfTemperatures / count;
    }
}
