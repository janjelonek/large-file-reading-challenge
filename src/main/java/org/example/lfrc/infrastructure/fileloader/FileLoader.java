package org.example.lfrc.infrastructure.fileloader;

import org.example.lfrc.domain.task.TemperatureProcessingTask;
import org.example.lfrc.domain.temperature.AverageTemperaturesCache;
import org.example.lfrc.infrastructure.csvprocessor.CSVFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FileLoader {

    private static final Logger log = LoggerFactory.getLogger(FileLoader.class);

    private final CSVFileProcessor csvFileProcessor;
    private final AverageTemperaturesCache averageTemperaturesCache;

    public FileLoader(CSVFileProcessor csvFileProcessor,
                      AverageTemperaturesCache averageTemperaturesCache) {
        this.csvFileProcessor = csvFileProcessor;
        this.averageTemperaturesCache = averageTemperaturesCache;
    }

    public void loadFile(Path filePath) {
        try {
            log.info("Loading file {}", filePath);
            Map<String, Map<Integer, Double>> averageTemperaturesMap = csvFileProcessor.process(filePath, new TemperatureProcessingTask());
            log.info("Successfully loaded file {}", filePath);
            averageTemperaturesCache.setAverageTemperaturesMap(averageTemperaturesMap);
        } catch (IOException | ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
