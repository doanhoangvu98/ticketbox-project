package com.noah.ticketbox.api.report;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks the request validation of the revenue report. The SQL itself is not executed here
 * (the repository is a mock); running it for real needs a PostgreSQL test database (Testcontainers).
 */
@WebMvcTest(ReportController.class)
class ReportControllerTest {

    private static final String URL = "/api/v1/reports/revenue";
    private static final String FROM = "2026-01-01T00:00:00Z";
    private static final String TO = "2027-01-01T00:00:00Z";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RevenueReportRepository revenueReportRepository;

    @Test
    void validRequest_returnsRows_andLimitDefaultsTo10() throws Exception {
        when(revenueReportRepository.topEventsByRevenue(any(), any(), eq(10))).thenReturn(List.of(
                new RevenueRow(7L, "Rock Night", OffsetDateTime.parse("2026-12-01T20:00:00+07:00"),
                        100, 40, new BigDecimal("8000000"), new BigDecimal("40.0"))));

        mockMvc.perform(get(URL).param("from", FROM).param("to", TO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventName").value("Rock Night"))
                .andExpect(jsonPath("$[0].occupancyPercent").value(40.0));

        verify(revenueReportRepository).topEventsByRevenue(OffsetDateTime.parse(FROM), OffsetDateTime.parse(TO), 10);
    }

    @Test
    void fromNotBeforeTo_returns400_andDoesNotQuery() throws Exception {
        mockMvc.perform(get(URL).param("from", TO).param("to", FROM))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"));

        verifyNoInteractions(revenueReportRepository);
    }

    @Test
    void limitOutOfRange_returns400() throws Exception {
        mockMvc.perform(get(URL).param("from", FROM).param("to", TO).param("limit", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(revenueReportRepository);
    }

    @Test
    void missingRequiredParameter_returns400() throws Exception {
        mockMvc.perform(get(URL).param("from", FROM))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(revenueReportRepository);
    }
}
