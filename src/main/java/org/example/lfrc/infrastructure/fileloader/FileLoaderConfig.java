package org.example.lfrc.infrastructure.fileloader;

import jakarta.annotation.PostConstruct;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.nio.file.Path;

@Configuration
@EnableScheduling
public class FileLoaderConfig {

    private static final Logger log = LoggerFactory.getLogger(FileLoaderConfig.class);

    private final FileLoader fileLoader;
    private final String filePath;
    private final int interval;

    private Path path;

    public FileLoaderConfig(FileLoader fileLoader,
                            @Value("${file-loader.file-to-process-path}") String filePath,
                            @Value("${file-loader.check-for-file-changes-interval-ms}") int interval) {
        this.fileLoader = fileLoader;
        this.filePath = filePath;
        this.interval = interval;
    }

    @PostConstruct
    public void init() {
        this.path = Path.of(filePath);
        loadFileAtStartup();
    }

    private void loadFileAtStartup() {
        log.info("Loading file {} on startup", path);
        fileLoader.loadFile(path);
    }

    @Bean
    public FileAlterationMonitor fileAlterationMonitor() throws Exception {
        FileAlterationObserver observer = new FileAlterationObserver(new File(path.getParent().toString()));
        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onFileChange(File file) {
                log.info("Detected file modification: {}", file.getAbsolutePath());
                fileLoader.loadFile(path);
            }
        });

        FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        monitor.start();
        return monitor;
    }
}
