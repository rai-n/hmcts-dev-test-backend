package uk.gov.hmcts.reform.dev.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dev.dto.requests.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.requests.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.responses.PagedTaskResponse;
import uk.gov.hmcts.reform.dev.dto.responses.TaskResponse;
import uk.gov.hmcts.reform.dev.exceptions.InvalidStateTransitionException;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;

/**
 * Integration tests for CaseWorkerTaskService.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class CaseWorkerTaskServiceIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @DisplayName("Should create and retrieve task with auditing")
    void shouldCreateAndRetrieveTaskWithAuditing() {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Integration test task")
            .description("Test Description")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().plusDays(7))
            .build();

        TaskResponse created = taskService.createTask(request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Integration test task");
        assertThat(created.getStatus()).isEqualTo(TaskStatus.DRAFT);
        assertThat(created.getCreatedAt()).isNotNull();

        TaskResponse retrieved = taskService.getTask(created.getId());
        assertThat(retrieved.getId()).isEqualTo(created.getId());
    }

    @Test
    @DisplayName("Should update task status through valid transitions")
    void shouldUpdateTaskStatusThroughValidTransitions() {
        CreateTaskRequest createRequest = CreateTaskRequest.builder()
            .title("Update status transition task")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().plusDays(7))
            .build();

        TaskResponse task = taskService.createTask(createRequest);
        Long taskId = task.getId();

        TaskResponse submitted = updateStatus(taskId, TaskStatus.SUBMITTED);
        assertThat(submitted.getStatus()).isEqualTo(TaskStatus.SUBMITTED);

        TaskResponse preparing = updateStatus(taskId, TaskStatus.PREPARING);
        assertThat(preparing.getStatus()).isEqualTo(TaskStatus.PREPARING);

        TaskResponse booked = updateStatus(taskId, TaskStatus.BOOKED);
        assertThat(booked.getStatus()).isEqualTo(TaskStatus.BOOKED);
    }

    @Test
    @DisplayName("Should reject invalid status transition")
    void shouldRejectInvalidStatusTransition() {
        CreateTaskRequest createRequest = CreateTaskRequest.builder()
            .title("Invalid Transition Task")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().plusDays(7))
            .build();

        TaskResponse task = taskService.createTask(createRequest);
        Long taskId = task.getId();

        UpdateTaskRequest invalidRequest = UpdateTaskRequest.builder()
            .status(TaskStatus.DISPOSED)
            .build();

        assertThatThrownBy(() -> taskService.updateTaskStatus(taskId, invalidRequest))
            .isInstanceOf(InvalidStateTransitionException.class)
            .hasMessageContaining("Invalid status transition from 'DRAFT' to 'DISPOSED'");
    }

    @Test
    @DisplayName("Should return empty list when no tasks exist")
    @SuppressWarnings("unchecked")
    void shouldReturnEmptyListWhenNoTasksExist() {
        Page<Task> emptyPage = mock(Page.class);

        when(taskRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PagedTaskResponse pagedTaskResponse = taskService.getTasks(0, 20);
        assertThat(pagedTaskResponse.getData()).isEmpty();
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException for non-existent task")
    void shouldThrowExceptionForNonExistentTask() {
        assertThatThrownBy(() -> taskService.getTask(99999L))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task not found with id: 99999");
    }

    @Test
    @DisplayName("Should delete task and verify removal")
    void shouldDeleteTaskAndVerifyRemoval() {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Task to Delete")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().plusDays(7))
            .build();

        TaskResponse created = taskService.createTask(request);
        Long taskId = created.getId();

        taskService.deleteTask(taskId);

        assertThatThrownBy(() -> taskService.getTask(taskId))
            .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("Should handle adjournment and resumption")
    void shouldHandleAdjournmentAndResumption() {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Adjournment test")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().plusDays(7))
            .build();

        TaskResponse task = taskService.createTask(request);
        Long taskId = task.getId();

        updateStatus(taskId, TaskStatus.SUBMITTED);
        updateStatus(taskId, TaskStatus.PREPARING);
        updateStatus(taskId, TaskStatus.BOOKED);

        TaskResponse adjourned = updateStatus(taskId, TaskStatus.ADJOURNED);
        assertThat(adjourned.getStatus()).isEqualTo(TaskStatus.ADJOURNED);

        TaskResponse resumed = updateStatus(taskId, TaskStatus.PREPARING);
        assertThat(resumed.getStatus()).isEqualTo(TaskStatus.PREPARING);
    }

    private TaskResponse updateStatus(Long taskId, TaskStatus newStatus) {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
            .status(newStatus)
            .build();
        return taskService.updateTaskStatus(taskId, request);
    }
}
