package uk.gov.hmcts.reform.dev.services;

import uk.gov.hmcts.reform.dev.dto.requests.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.requests.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.responses.TaskResponse;

import java.util.List;

/**
 * Creating interface for future extensibility in mind.
 * Non caseworkers could be using the service.
 */
public interface TaskService {

    TaskResponse createTask(CreateTaskRequest createTaskRequest);

    TaskResponse getTask(Long id);

    List<TaskResponse> getTasks();

    TaskResponse updateTaskStatus(Long id, UpdateTaskRequest updateTaskRequest);

    void deleteTask(Long id);
}
