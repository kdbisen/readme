package com.banking.onboarding.controller;

import com.banking.onboarding.dto.ProcessEntityResponse;
import com.banking.onboarding.model.OnboardingProcess;
import com.banking.onboarding.model.RequestType;
import com.banking.onboarding.service.OnboardingProcessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.integration.support.MessageChannel;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OnboardingController.class)
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OnboardingProcessService processService;

    @MockBean
    private MessageChannel processEntityChannel;

    @Test
    void testProcessEntity() throws Exception {
        // Given
        String xmlPayload = "<entity><id>123</id><name>Test Entity</name></entity>";
        RequestType requestType = RequestType.ADD_KYC;

        OnboardingProcess process = OnboardingProcess.builder()
                .processId("test-process-id")
                .correlationId("test-correlation-id")
                .requestType(requestType)
                .status(OnboardingProcess.ProcessStatus.RECEIVED)
                .createdAt(LocalDateTime.now())
                .build();

        when(processService.createProcess(anyString(), anyString(), any(RequestType.class))).thenReturn(process);

        // When & Then
        mockMvc.perform(post("/onboarding/process-entity/ADD_KYC")
                .contentType(MediaType.APPLICATION_XML)
                .content(xmlPayload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.processId").value("test-process-id"))
                .andExpect(jsonPath("$.correlationId").value("test-correlation-id"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void testGetProcessStatus() throws Exception {
        // Given
        OnboardingProcess process = OnboardingProcess.builder()
                .processId("test-process-id")
                .correlationId("test-correlation-id")
                .status(OnboardingProcess.ProcessStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        when(processService.getProcessById("test-process-id")).thenReturn(Optional.of(process));

        // When & Then
        mockMvc.perform(get("/onboarding/status/test-process-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processId").value("test-process-id"))
                .andExpect(jsonPath("$.correlationId").value("test-correlation-id"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.progressPercentage").value(100));
    }

    @Test
    void testGetProcessStatusNotFound() throws Exception {
        // Given
        when(processService.getProcessById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/onboarding/status/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testHealthCheck() throws Exception {
        // Given
        when(processService.getProcessCountByStatus(any())).thenReturn(0L);

        // When & Then
        mockMvc.perform(get("/onboarding/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
