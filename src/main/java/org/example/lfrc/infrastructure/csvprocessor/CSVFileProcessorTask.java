package org.example.lfrc.infrastructure.csvprocessor;

import org.apache.commons.csv.CSVRecord;

import java.util.List;

public interface CSVFileProcessorTask<M, R, S> {

    M map(CSVRecord csvRecord);

    R reduce(List<M> mappedResults);

    S reduceAll(List<R> reducedResults);
}
