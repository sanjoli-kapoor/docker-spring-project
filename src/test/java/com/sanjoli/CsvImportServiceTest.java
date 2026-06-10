package com.sanjoli;

import com.sanjoli.entity.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CsvImportServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private CsvImportService csvImportService;

    @TempDir
    Path tempDir;

    private static final String HEADER = "playerID,birthYear,birthMonth,birthDay,birthCountry,birthState,birthCity," +
            "deathYear,deathMonth,deathDay,deathCountry,deathState,deathCity," +
            "nameFirst,nameLast,nameGiven,weight,height,bats,throws,debut,finalGame,retroID,bbrefID";

    private static final String SAMPLE_ROW =
            "aardsda01,1981,12,27,USA,CO,Denver,,,,,,,David,Aardsma,David Allan,220,75,R,R,2004-04-06,,aardd001,aardsda01";

    @Test
    void importCsv_skipsHeaderAndSavesAllRows() throws Exception {
        Path csv = writeCsv(HEADER, SAMPLE_ROW, SAMPLE_ROW, SAMPLE_ROW);

        csvImportService.importCsv(csv.toString());

        ArgumentCaptor<List<Person>> captor = ArgumentCaptor.forClass(List.class);
        verify(personRepository, atLeastOnce()).saveAll(captor.capture());

        int totalSaved = captor.getAllValues().stream().mapToInt(List::size).sum();
        assertThat(totalSaved).isEqualTo(3);
    }

    @Test
    void importCsv_mapsFieldsCorrectly() throws Exception {
        Path csv = writeCsv(HEADER, SAMPLE_ROW);

        csvImportService.importCsv(csv.toString());

        ArgumentCaptor<List<Person>> captor = ArgumentCaptor.forClass(List.class);
        verify(personRepository).saveAll(captor.capture());

        Person person = captor.getValue().get(0);
        assertThat(person.getPlayerID()).isEqualTo("aardsda01");
        assertThat(person.getBirthYear()).isEqualTo(1981);
        assertThat(person.getBirthMonth()).isEqualTo(12);
        assertThat(person.getBirthDay()).isEqualTo(27);
        assertThat(person.getBirthCountry()).isEqualTo("USA");
        assertThat(person.getNameFirst()).isEqualTo("David");
        assertThat(person.getNameLast()).isEqualTo("Aardsma");
        assertThat(person.getWeight()).isEqualTo(220);
        assertThat(person.getHeight()).isEqualTo(75);
        assertThat(person.getBats()).isEqualTo("R");
        assertThat(person.getThrowsHand()).isEqualTo("R");
        assertThat(person.getDebut()).isEqualTo("2004-04-06");
    }

    @Test
    void importCsv_handlesEmptyNumericFields() throws Exception {
        // deathYear/Month/Day and finalGame are empty in this row
        Path csv = writeCsv(HEADER, SAMPLE_ROW);

        csvImportService.importCsv(csv.toString());

        ArgumentCaptor<List<Person>> captor = ArgumentCaptor.forClass(List.class);
        verify(personRepository).saveAll(captor.capture());

        Person person = captor.getValue().get(0);
        assertThat(person.getDeathYear()).isNull();
        assertThat(person.getDeathMonth()).isNull();
        assertThat(person.getDeathDay()).isNull();
        assertThat(person.getFinalGame()).isEmpty();
    }

    @Test
    void importCsv_splitsIntoBatchesOf500() throws Exception {
        String[] rows = new String[1001];
        for (int i = 0; i < 1001; i++) {
            rows[i] = SAMPLE_ROW;
        }
        Path csv = writeCsv(HEADER, rows);

        csvImportService.importCsv(csv.toString());

        // 1001 rows → 3 batches: 500 + 500 + 1
        verify(personRepository, org.mockito.Mockito.times(3)).saveAll(anyList());
    }

    private Path writeCsv(String header, String... dataRows) throws IOException {
        Path file = tempDir.resolve("test.csv");
        StringBuilder sb = new StringBuilder(header).append("\n");
        for (String row : dataRows) {
            sb.append(row).append("\n");
        }
        Files.writeString(file, sb.toString());
        return file;
    }
}
