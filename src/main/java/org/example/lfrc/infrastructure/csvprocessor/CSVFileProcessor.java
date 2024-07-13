package org.example.lfrc.infrastructure.csvprocessor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

@Service
public class CSVFileProcessor {

    private final int numberOfThreads;
    private final int bufferSize;

    public CSVFileProcessor(@Value("${csv-file-processor.number-of-threads}") int numberOfThreads,
                            @Value("${csv-file-processor.buffer-size}") int bufferSize) {
        this.numberOfThreads = numberOfThreads;
        this.bufferSize = bufferSize;
    }

    public <M, R, S> S process(Path filePath, CSVFileProcessorTask<M, R, S> task) throws IOException, ExecutionException, InterruptedException {
        ThreadFactory virtualThreadFactory = Thread.ofVirtual().factory();
        List<Future<R>> futures;

        try (ExecutorService executorService = Executors.newThreadPerTaskExecutor(virtualThreadFactory)) {
            futures = prepareFutures(filePath, task, executorService);
        }

        List<R> results = new ArrayList<>();

        for (Future<R> future : futures) {
            results.add(future.get());
        }

        return task.reduceAll(results);
    }

    private <M, R, S> List<Future<R>> prepareFutures(Path filePath, CSVFileProcessorTask<M, R, S> task, ExecutorService executorService) throws IOException {
        List<Future<R>> futures = new ArrayList<>();

        try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            long fileSize = fileChannel.size();
            long chunkSize = Math.max(fileSize / numberOfThreads, bufferSize);
            int requiredNumberOfThreads = (int) Math.ceil((double) fileSize / chunkSize);

            long start = 0;
            for (int i = 0; i < requiredNumberOfThreads; i++) {
                long end = (i == requiredNumberOfThreads - 1) ? fileSize : findNextLineEnd(fileChannel, start + chunkSize);

                MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, start, end - start);
                futures.add(executorService.submit(() -> createTask(buffer, task)));

                start = end;
            }
        }

        return futures;
    }

    private long findNextLineEnd(FileChannel fileChannel, long position) throws IOException {
        fileChannel.position(position);
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        long fileSize = fileChannel.size();
        long currentPosition = position;

        while (currentPosition < fileSize) {
            buffer.clear();
            int bytesRead = fileChannel.read(buffer);
            if (bytesRead == -1) {
                break;
            }

            buffer.flip();
            while (buffer.hasRemaining()) {
                if (buffer.get() == '\n') {
                    return currentPosition + buffer.position();
                }
            }
            currentPosition += bytesRead;
        }

        return fileSize;
    }

    private <M, R, S> R createTask(MappedByteBuffer buffer, CSVFileProcessorTask<M, R, S> task) {
        try (CSVParser parser = createCSVParser(buffer)) {
            List<M> mappedRecords = parser.stream()
                    .map(task::map)
                    .filter(Objects::nonNull)
                    .toList();
            return task.reduce(mappedRecords);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CSVParser createCSVParser(MappedByteBuffer buffer) throws IOException {
        return CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .build()
                .parse(new StringReader(byteBufferToString(buffer)));
    }

    private String byteBufferToString(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
