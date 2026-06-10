package com.sanjoli;

import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImportController.class)
class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CsvImportService csvImportService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CsvImportService csvImportService() {
            return mock(CsvImportService.class);
        }
    }
    @Test
    void importEndpoint_returns200_whenImportSucceeds() throws Exception {
        doNothing().when(csvImportService).importCsv(anyString());

        mockMvc.perform(post("/api/person/import").param("filePath", "/some/file.csv"))
                .andExpect(status().isOk())
                .andExpect(content().string("CSV import completed successfully"));
    }

    @Test
    void importEndpoint_returns500_whenIoExceptionThrown() throws Exception {
        doThrow(new IOException("file not found")).when(csvImportService).importCsv(anyString());

        mockMvc.perform(post("/api/person/import").param("filePath", "/bad/path.csv"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("CSV import failed")));
    }

    @Test
    void importEndpoint_returns500_whenCsvExceptionThrown() throws Exception {
        doThrow(new CsvException("bad csv")).when(csvImportService).importCsv(anyString());

        mockMvc.perform(post("/api/person/import").param("filePath", "/bad/path.csv"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("CSV import failed")));
    }

    @Test
    void importEndpoint_requires_filePath_param() throws Exception {
        mockMvc.perform(post("/api/person/import"))
                .andExpect(status().isBadRequest());
    }
}
