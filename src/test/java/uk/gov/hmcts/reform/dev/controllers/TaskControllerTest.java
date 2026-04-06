package uk.gov.hmcts.reform.dev.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.hmcts.reform.dev.dto.requests.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.requests.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.responses.PagedTaskResponse;
import uk.gov.hmcts.reform.dev.dto.responses.TaskResponse;
import uk.gov.hmcts.reform.dev.exceptions.InvalidStateTransitionException;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;
import uk.gov.hmcts.reform.dev.services.TaskService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @Test
    @DisplayName("Should create task and return 201 with HATEOAS links")
    void shouldCreateTaskAndReturn201() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Test Task")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();

        TaskResponse response = TaskResponse.builder()
            .id(1L)
            .title("Test Task")
            .status(TaskStatus.DRAFT)
            .links(Map.of("self", "/v1/tasks/1", "delete", "/v1/tasks/1"))
            .build();

        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/v1/tasks/1"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Test Task"))
            .andExpect(jsonPath("$.links.self").value("/v1/tasks/1"));
    }

    @Test
    @DisplayName("Should return 400 when create task validation fails")
    void shouldReturn400WhenCreateTaskValidationFails() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();

        mockMvc.perform(post("/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("error.tasks.validation.failed"));
    }

    @Test
    @DisplayName("Should get tasks with pagination and return 200")
    void shouldGetTasksWithPagination() throws Exception {
        PagedTaskResponse pagedResponse = PagedTaskResponse.builder()
            .links(Map.of("self", "/v1/tasks?page=0&size=10", "first", "/v1/tasks?page=0&size=10"))
            .pageDetails(PagedTaskResponse.PageDetails.builder()
                .totalElements(2)
                .totalPages(1)
                .currentPage(0)
                .pageSize(10)
                .build())
            .data(List.of(
                TaskResponse.builder().id(1L).title("Task 1").build(),
                TaskResponse.builder().id(2L).title("Task 2").build()
            ))
            .build();

        when(taskService.getTasks(anyInt(), anyInt())).thenReturn(pagedResponse);

        mockMvc.perform(get("/v1/tasks")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.links.self").value("/v1/tasks?page=0&size=10"));
    }

    @Test
    @DisplayName("Should update task status and return 200")
    void shouldUpdateTaskStatusAndReturn200() throws Exception {
        Long taskId = 1L;
        UpdateTaskRequest request = UpdateTaskRequest.builder()
            .status(TaskStatus.SUBMITTED)
            .build();

        TaskResponse response = TaskResponse.builder()
            .id(taskId)
            .title("Test Task")
            .status(TaskStatus.SUBMITTED)
            .links(Map.of("self", "/v1/tasks/1", "update", "/v1/tasks/1"))
            .build();

        when(taskService.updateTaskStatus(any(Long.class), any(UpdateTaskRequest.class)))
            .thenReturn(response);

        mockMvc.perform(patch("/v1/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(taskId))
            .andExpect(jsonPath("$.status").value("SUBMITTED"))
            .andExpect(jsonPath("$.links.update").value("/v1/tasks/1"));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent task")
    void shouldReturn404WhenUpdatingNonExistentTask() throws Exception {
        Long taskId = 999L;
        UpdateTaskRequest request = UpdateTaskRequest.builder()
            .status(TaskStatus.SUBMITTED)
            .build();

        when(taskService.updateTaskStatus(any(Long.class), any(UpdateTaskRequest.class)))
            .thenThrow(new TaskNotFoundException(taskId));

        mockMvc.perform(patch("/v1/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("error.tasks.not.found"));
    }

    @Test
    @DisplayName("Should return 400 for invalid state transition")
    void shouldReturn400ForInvalidStateTransition() throws Exception {
        Long taskId = 1L;
        UpdateTaskRequest request = UpdateTaskRequest.builder()
            .status(TaskStatus.DRAFT)
            .build();

        when(taskService.updateTaskStatus(any(Long.class), any(UpdateTaskRequest.class)))
            .thenThrow(new InvalidStateTransitionException(taskId, TaskStatus.DISPOSED, TaskStatus.DRAFT));

        mockMvc.perform(patch("/v1/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("error.tasks.status.invalid.transition"));
    }

    @Test
    @DisplayName("Should delete task and return 204")
    void shouldDeleteTaskAndReturn204() throws Exception {
        Long taskId = 1L;

        mockMvc.perform(delete("/v1/tasks/{id}", taskId))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent task")
    void shouldReturn404WhenDeletingNonExistentTask() throws Exception {
        Long taskId = 9999L;

        doThrow(new TaskNotFoundException(taskId)).when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/v1/tasks/{id}", taskId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("error.tasks.not.found"));
    }
}
