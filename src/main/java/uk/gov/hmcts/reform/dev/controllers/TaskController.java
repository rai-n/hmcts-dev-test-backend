package uk.gov.hmcts.reform.dev.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.hmcts.reform.dev.dto.requests.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.requests.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.responses.ErrorResponse;
import uk.gov.hmcts.reform.dev.dto.responses.PagedTaskResponse;
import uk.gov.hmcts.reform.dev.dto.responses.TaskResponse;
import uk.gov.hmcts.reform.dev.services.TaskService;
import uk.gov.hmcts.reform.dev.utils.HateoasLinkBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/tasks")
@Tag(name = "Tasks", description = "Task management API for case workers")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(
        summary = "Create a new task",
        description = "Creates a new task with the provided details."
            + "Returns 201 Created with the created task and HATEOAS links."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task created successfully",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
        @Valid @RequestBody CreateTaskRequest request) {

        TaskResponse createdTask = taskService.createTask(request);
        TaskResponse taskResponse = HateoasLinkBuilder.addHateoasLinks(createdTask);

        URI uri = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdTask.getId())
            .toUri();

        return ResponseEntity.created(uri).body(taskResponse);
    }

    @Operation(
        summary = "Get all tasks with pagination",
        description = "Retrieves a paginated list of tasks with HATEOAS links for navigation."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
            content = @Content(schema = @Schema(implementation = PagedTaskResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PagedTaskResponse> getTasks(
        @Parameter(description = "Page number (zero-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Number of items per page", example = "10")
        @RequestParam(defaultValue = "10") int size
    ) {
        PagedTaskResponse pagedTaskResponse = taskService.getTasks(page, size);
        return ResponseEntity.ok(pagedTaskResponse);

    }

    @Operation(
        summary = "Update task status",
        description = "Updates the status of an existing task. Valid status transitions are enforced."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task status updated successfully",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status transition or validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTaskStatus(
        @Parameter(description = "Task ID", example = "1")
        @PathVariable Long id,
        @Valid @RequestBody UpdateTaskRequest request) {

        TaskResponse updateTaskStatus = taskService.updateTaskStatus(id, request);
        TaskResponse taskResponse = HateoasLinkBuilder.addHateoasLinks(updateTaskStatus);

        return ResponseEntity.ok(taskResponse);
    }

    @Operation(
        summary = "Delete a task",
        description = "Deletes a task by its ID. Returns 204 No Content on success."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
        @Parameter(description = "Task ID", example = "1")
        @PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
