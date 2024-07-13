package org.example.lfrc.domain.task;

import org.apache.commons.csv.CSVRecord;
import org.example.lfrc.infrastructure.csvprocessor.CSVFileProcessorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TemperatureProcessingTask
        implements CSVFileProcessorTask<TemperatureEntry,
                                        Map<String, Map<Integer, TemperatureMeasurements>>,
                                        Map<String, Map<Integer, Double>>> {

    private static final Logger log = LoggerFactory.getLogger(TemperatureProcessingTask.class);

    @Override
    public TemperatureEntry map(CSVRecord csvRecord) {
        if (csvRecord.size() != 3) {
            log.warn("Skipping an entry because of incorrect size. Size: {}", csvRecord.size());
            return null;
        }

        String city = csvRecord.get(0);

        String timestamp = csvRecord.get(1);
        int year = Integer.parseInt(timestamp.substring(0, 4));

        double temperature;

        try {
            temperature = Double.parseDouble(csvRecord.get(2));
        } catch (NumberFormatException e) {
            log.warn("Skipping an entry because of number format exception. Incorrect value: {}", csvRecord.get(2));
            return null;
        }

        return new TemperatureEntry(city, year, temperature);
    }

    @Override
    public Map<String, Map<Integer, TemperatureMeasurements>> reduce(List<TemperatureEntry> entries) {
        return entries.stream().collect(
            Collectors.groupingBy(
                TemperatureEntry::city,
                Collectors.groupingBy(
                    TemperatureEntry::year,
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            TemperatureMeasurements tm = new TemperatureMeasurements();
                            list.forEach(entry -> tm.addTemperature(entry.temperature()));
                            return tm;
                        }
                    )
                )
            )
        );
    }

    @Override
    public Map<String, Map<Integer, Double>> reduceAll(List<Map<String, Map<Integer, TemperatureMeasurements>>> reducedResults) {
         return reducedResults.stream()
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.flatMapping(
                    entry -> entry.getValue().entrySet().stream(),
                    Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.collectingAndThen(
                            Collectors.averagingDouble(e -> e.getValue().average()),
                            TemperatureProcessingTask::roundToOneDecimalPlace
                        )
                    )
                )
            ));
    }

    public static double roundToOneDecimalPlace(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
