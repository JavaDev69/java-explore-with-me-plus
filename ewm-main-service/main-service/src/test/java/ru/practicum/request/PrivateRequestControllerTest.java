package ru.practicum.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.events.EventState;
import ru.practicum.requests.service.RequestsService;

import java.util.List;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class PrivateRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestsService requestsService;

    @Test
    void updateRequestStatus_Success() throws Exception {
        // Given
        Long userId = 1L;
        Long eventId = 2L;

        EventRequestStatusUpdateRequest request = new EventRequestStatusUpdateRequest();
        request.setRequestIds(List.of(1L, 2L));
        request.setStatus(EventState.CONFIRMED);

        ParticipationRequestDto confirmedDto = ParticipationRequestDto.builder()
                .id(1L)
                .status(EventState.CONFIRMED)
                .build();

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(List.of(confirmedDto));
        result.setRejectedRequests(List.of());

        when(requestsService.updateRequestStatuses(userId, eventId, request)).thenReturn(result);

        // When & Then
        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.rejectedRequests").isEmpty());
    }
}
