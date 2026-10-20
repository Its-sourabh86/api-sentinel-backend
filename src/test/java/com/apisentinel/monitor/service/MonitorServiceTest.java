package com.apisentinel.monitor.service;

import com.apisentinel.common.exception.ResourceNotFoundException;
import com.apisentinel.monitor.dto.request.CreateMonitorRequest;
import com.apisentinel.monitor.dto.request.UpdateMonitorRequest;
import com.apisentinel.monitor.dto.response.MonitorResponse;
import com.apisentinel.monitor.entity.Monitor;
import com.apisentinel.monitor.enums.HttpMethod;
import com.apisentinel.monitor.repository.MonitorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitorServiceTest {

    @Mock
    private MonitorRepository monitorRepository;

    @InjectMocks
    private MonitorService monitorService;

    private Monitor sampleMonitor() {
        return new Monitor("Old name", "https://example.com/api",
                HttpMethod.GET, 60, 3000, 200);
    }

    // ---------- create ----------

    @Test
    @DisplayName("create: method aur expectedStatus na ho toh defaults (GET, 200) lagne chahiye")
    void create_appliesDefaults() {
        // Arrange
        CreateMonitorRequest request = new CreateMonitorRequest(
                "Inventory API", "https://example.com/inventory",
                null, 60, 3000, null);
        when(monitorRepository.save(any(Monitor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MonitorResponse response = monitorService.create(request);

        // Assert
        assertThat(response.method()).isEqualTo(HttpMethod.GET);
        assertThat(response.expectedStatus()).isEqualTo(200);
        assertThat(response.name()).isEqualTo("Inventory API");
    }

    @Test
    @DisplayName("create: diye gaye values defaults ko override karne chahiye")
    void create_usesProvidedValues() {
        CreateMonitorRequest request = new CreateMonitorRequest(
                "Price API", "https://example.com/price",
                HttpMethod.POST, 30, 2000, 201);
        when(monitorRepository.save(any(Monitor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MonitorResponse response = monitorService.create(request);

        assertThat(response.method()).isEqualTo(HttpMethod.POST);
        assertThat(response.expectedStatus()).isEqualTo(201);
        assertThat(response.intervalSeconds()).isEqualTo(30);
    }

    // ---------- get ----------

    @Test
    @DisplayName("getById: monitor na mile toh ResourceNotFoundException")
    void getById_notFound_throws() {
        when(monitorRepository.getByIdOrThrow(999L))
                .thenThrow(new ResourceNotFoundException("Monitor", 999L));

        assertThatThrownBy(() -> monitorService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ---------- update ----------

    @Test
    @DisplayName("update: sirf bheje gaye fields badlen, baaki same rahen (partial update)")
    void update_changesOnlyProvidedFields() {
        Monitor existing = sampleMonitor();
        when(monitorRepository.getByIdOrThrow(1L)).thenReturn(existing);
        when(monitorRepository.save(any(Monitor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateMonitorRequest request = new UpdateMonitorRequest(
                null, null, null, 30, null, null);

        MonitorResponse response = monitorService.update(1L, request);

        assertThat(response.intervalSeconds()).isEqualTo(30);   // badla
        assertThat(response.name()).isEqualTo("Old name");      // same
        assertThat(response.url()).isEqualTo("https://example.com/api"); // same
        assertThat(response.timeoutMs()).isEqualTo(3000);       // same
    }

    @Test
    @DisplayName("update: monitor na mile toh exception aur save kabhi call na ho")
    void update_notFound_throwsAndDoesNotSave() {
        when(monitorRepository.getByIdOrThrow(999L))
                .thenThrow(new ResourceNotFoundException("Monitor", 999L));

        UpdateMonitorRequest request = new UpdateMonitorRequest(
                "New", null, null, null, null, null);

        assertThatThrownBy(() -> monitorService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(monitorRepository, never()).save(any());
    }

    // ---------- pause / resume ----------

    @Test
    @DisplayName("pause: enabled false hona chahiye aur save call hona chahiye")
    void pause_disablesMonitor() {
        Monitor existing = sampleMonitor();
        when(monitorRepository.getByIdOrThrow(1L)).thenReturn(existing);
        when(monitorRepository.save(any(Monitor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MonitorResponse response = monitorService.pause(1L);

        assertThat(response.enabled()).isFalse();
        verify(monitorRepository).save(existing);
    }

    @Test
    @DisplayName("pause: dobara call karne par bhi result same (idempotent)")
    void pause_isIdempotent() {
        Monitor existing = sampleMonitor();
        existing.setEnabled(false); // pehle se paused
        when(monitorRepository.getByIdOrThrow(1L)).thenReturn(existing);
        when(monitorRepository.save(any(Monitor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MonitorResponse response = monitorService.pause(1L);

        assertThat(response.enabled()).isFalse();
    }

    @Test
    @DisplayName("resume: enabled true hona chahiye")
    void resume_enablesMonitor() {
        Monitor existing = sampleMonitor();
        existing.setEnabled(false);
        when(monitorRepository.getByIdOrThrow(1L)).thenReturn(existing);
        when(monitorRepository.save(any(Monitor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MonitorResponse response = monitorService.resume(1L);

        assertThat(response.enabled()).isTrue();
    }

    // ---------- delete ----------

    @Test
    @DisplayName("delete: sahi monitor repository.delete ko pass hona chahiye")
    void delete_deletesMonitor() {
        Monitor existing = sampleMonitor();
        when(monitorRepository.getByIdOrThrow(1L)).thenReturn(existing);

        monitorService.delete(1L);

        ArgumentCaptor<Monitor> captor = ArgumentCaptor.forClass(Monitor.class);
        verify(monitorRepository).delete(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Old name");
    }

    @Test
    @DisplayName("delete: monitor na mile toh delete kabhi call na ho")
    void delete_notFound_neverDeletes() {
        when(monitorRepository.getByIdOrThrow(999L))
                .thenThrow(new ResourceNotFoundException("Monitor", 999L));

        assertThatThrownBy(() -> monitorService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(monitorRepository, never()).delete(any());
    }
}