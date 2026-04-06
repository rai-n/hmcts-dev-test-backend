package uk.gov.hmcts.reform.dev.controllers;

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
import uk.gov.hmcts.reform.dev.dto.responses.PagedTaskResponse;
import uk.gov.hmcts.reform.dev.dto.responses.TaskResponse;
import uk.gov.hmcts.reform.dev.services.TaskService;
import uk.gov.hmcts.reform.dev.utils.HateoasLinkBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

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

    @GetMapping
    public ResponseEntity<PagedTaskResponse> getTasks(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        PagedTaskResponse pagedTaskResponse = taskService.getTasks(page, size);
        return ResponseEntity.ok(pagedTaskResponse);

    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTaskStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateTaskRequest request) {

        TaskResponse updateTaskStatus = taskService.updateTaskStatus(id, request);
        TaskResponse taskResponse = HateoasLinkBuilder.addHateoasLinks(updateTaskStatus);

        return ResponseEntity.ok(taskResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
