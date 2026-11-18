package com.noah.ticketbox.api.venue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VenueController.class)
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueRepository venueRepository;

    @Test
    void list_isSortedByName_andIncludesCapacity() throws Exception {
        // Stubbed only for Sort.by("name"): any other sort would return an empty list and fail the test.
        when(venueRepository.findAll(Sort.by("name"))).thenReturn(List.of(
                new VenueEntity("Hoa Binh Theater", "240 Ba Thang Hai, HCMC", 20, 25)));

        mockMvc.perform(get("/api/v1/venues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Hoa Binh Theater"))
                .andExpect(jsonPath("$[0].capacity").value(500)); // 20 rows x 25 seats
    }
}
