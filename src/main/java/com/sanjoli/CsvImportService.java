package com.sanjoli;

import com.sanjoli.entity.Person;
import com.sanjoli.PersonRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final PersonRepository personRepository;

    private static final int BATCH_SIZE = 500;
    private static final int THREAD_COUNT = 6;

    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    public void importCsv(String filePath) throws IOException, CsvException, InterruptedException, ExecutionException {

        List<String[]> lines;
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            lines = reader.readAll();
        }

        // skip header
        List<String[]> data = lines.subList(1, lines.size());

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < data.size(); i += BATCH_SIZE) {
            int start = i;
            int end = Math.min(i + BATCH_SIZE, data.size());
            List<String[]> batch = data.subList(start, end);

            futures.add(executor.submit(() -> {
                List<Person> people = new ArrayList<>();
                for (String[] row : batch) {
                    people.add(mapRowToPerson(row));
                }
                personRepository.saveAll(people);
            }));
        }

        // wait for all tasks
        for (Future<?> future : futures) {
            future.get();
        }

    }

    private Person mapRowToPerson(String[] row) {
        Person p = new Person();
        p.setPlayerID(row[0]);
        p.setBirthYear(parseInt(row[1]));
        p.setBirthMonth(parseInt(row[2]));
        p.setBirthDay(parseInt(row[3]));
        p.setBirthCountry(row[4]);
        p.setBirthState(row[5]);
        p.setBirthCity(row[6]);
        p.setDeathYear(parseInt(row[7]));
        p.setDeathMonth(parseInt(row[8]));
        p.setDeathDay(parseInt(row[9]));
        p.setDeathCountry(row[10]);
        p.setDeathState(row[11]);
        p.setDeathCity(row[12]);
        p.setNameFirst(row[13]);
        p.setNameLast(row[14]);
        p.setNameGiven(row[15]);
        p.setWeight(parseInt(row[16]));
        p.setHeight(parseInt(row[17]));
        p.setBats(row[18]);
        p.setThrowsHand(row[19]);
        p.setDebut(row[20]);
        p.setFinalGame(row[21]);
        p.setRetroID(row[22]);
        p.setBbrefID(row[23]);
        return p;
    }

    private Integer parseInt(String s) {
        try {
            return s.isEmpty() ? null : Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}


