package uk.gov.hmcts.reform.dev.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dev.dto.requests.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.requests.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.responses.TaskResponse;
import uk.gov.hmcts.reform.dev.exceptions.InvalidStateTransitionException;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.mappers.TaskMapper;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;

/**
 * Testing business logic, state transitions, and exception handling.
 */
@ExtendWith(MockitoExtension.class)
class CaseWorkerTaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskRepository taskRepository;

    private CaseWorkerTaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new CaseWorkerTaskService(taskMapper, taskRepository);
    }

    @Test
    @DisplayName("Should create task successfully")
    void shouldCreateTaskSuccessfully() {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("New Task")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();

        Task task = mock(Task.class);
        Task savedTask = mock(Task.class);
        TaskResponse response = TaskResponse.builder()
            .id(1L)
            .title("New Task")
            .build();

        when(taskMapper.toEntity(request)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(savedTask);
        when(taskMapper.toResponse(savedTask)).thenReturn(response);

        TaskResponse result = taskService.createTask(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("New Task");
    }

    @Test
    @DisplayName("Should return task by id")
    void shouldReturnTaskById() {
        Long taskId = 1L;
        Task task = mock(Task.class);
        TaskResponse response = TaskResponse.builder()
            .id(taskId)
            .title("Test Task")
            .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(response);

        TaskResponse result = taskService.getTask(taskId);

        assertThat(result.getId()).isEqualTo(taskId);
        assertThat(result.getTitle()).isEqualTo("Test Task");
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when task not found")
    void shouldThrowExceptionWhenTaskNotFound() {
        Long taskId = 999L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask(taskId))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task not found with id: 999");
    }

    @Test
    @DisplayName("Should return all tasks")
    void shouldReturnAllTasks() {
        Task task1 = mock(Task.class);
        Task task2 = mock(Task.class);
        TaskResponse response1 = TaskResponse.builder().id(1L).build();
        TaskResponse response2 = TaskResponse.builder().id(2L).build();

        when(taskRepository.findAll()).thenReturn(Arrays.asList(task1, task2));
        when(taskMapper.toResponse(task1)).thenReturn(response1);
        when(taskMapper.toResponse(task2)).thenReturn(response2);

        List<TaskResponse> results = taskService.getTasks();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(1L);
        assertThat(results.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should return empty list when no tasks")
    void shouldReturnEmptyListWhenNoTasks() {
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());

        List<TaskResponse> results = taskService.getTasks();

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should update task status with valid transition")
    void shouldUpdateTaskStatusWithValidTransition() {
        Long taskId = 1L;
        UpdateTaskRequest request = UpdateTaskRequest.builder()
            .status(TaskStatus.SUBMITTED)
            .build();

        Task task = mock(Task.class);
        Task updatedTask = mock(Task.class);
        TaskResponse response = TaskResponse.builder()
            .id(taskId)
            .status(TaskStatus.SUBMITTED)
            .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(task.getStatus()).thenReturn(TaskStatus.DRAFT);
        when(taskRepository.save(task)).thenReturn(updatedTask);
        when(taskMapper.toResponse(updatedTask)).thenReturn(response);

        TaskResponse result = taskService.updateTaskStatus(taskId, request);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.SUBMITTED);
        verify(taskMapper).updateEntityFromRequest(request, task);
    }

    @Test
    @DisplayName("Should throw InvalidStateTransitionException for invalid transition")
    void shouldThrowExceptionForInvalidTransition() {
        Long taskId = 1L;
        UpdateTaskRequest request = UpdateTaskRequest.builder()
            .status(TaskStatus.DRAFT)
            .build();

        Task task = mock(Task.class);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(task.getStatus()).thenReturn(TaskStatus.DISPOSED);

        assertThatThrownBy(() -> taskService.updateTaskStatus(taskId, request))
            .isInstanceOf(InvalidStateTransitionException.class)
            .hasMessageContaining("Invalid status transition from 'DISPOSED' to 'DRAFT'");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should allow all valid status transitions")
    void shouldAllowAllValidStatusTransitions() {
        Long taskId = 1L;

        assertValidTransition(taskId, TaskStatus.DRAFT, TaskStatus.SUBMITTED);
        assertValidTransition(taskId, TaskStatus.SUBMITTED, TaskStatus.PREPARING);
        assertValidTransition(taskId, TaskStatus.PREPARING, TaskStatus.BOOKED);
        assertValidTransition(taskId, TaskStatus.PREPARING, TaskStatus.ADJOURNED);
        assertValidTransition(taskId, TaskStatus.BOOKED, TaskStatus.DISPOSED);
        assertValidTransition(taskId, TaskStatus.BOOKED, TaskStatus.ADJOURNED);
        assertValidTransition(taskId, TaskStatus.ADJOURNED, TaskStatus.PREPARING);
        assertValidTransition(taskId, TaskStatus.ADJOURNED, TaskStatus.BOOKED);
    }

    private void assertValidTransition(Long taskId, TaskStatus from, TaskStatus to) {
        Task task = mock(Task.class);
        Task updatedTask = mock(Task.class);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(task.getStatus()).thenReturn(from);
        when(taskRepository.save(task)).thenReturn(updatedTask);
        when(taskMapper.toResponse(updatedTask)).thenReturn(TaskResponse.builder().build());

        UpdateTaskRequest request = UpdateTaskRequest.builder().status(to).build();

        taskService.updateTaskStatus(taskId, request);

        verify(taskMapper).updateEntityFromRequest(request, task);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when updating non-existent task")
    void shouldThrowExceptionWhenUpdatingNonExistentTask() {
        Long taskId = 999L;
        UpdateTaskRequest request = UpdateTaskRequest.builder()
            .status(TaskStatus.SUBMITTED)
            .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTaskStatus(taskId, request))
            .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete task successfully")
    void shouldDeleteTaskSuccessfully() {
        Long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(true);

        taskService.deleteTask(taskId);

        verify(taskRepository).deleteById(taskId);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when deleting non-existent task")
    void shouldThrowExceptionWhenDeletingNonExistentTask() {
        Long taskId = 999L;
        when(taskRepository.existsById(taskId)).thenReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask(taskId))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task not found with id: 999");

        verify(taskRepository, never()).deleteById(taskId);
    }
}
