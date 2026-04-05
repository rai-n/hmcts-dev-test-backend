package uk.gov.hmcts.reform.dev.services;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dev.dto.requests.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.requests.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.responses.TaskResponse;
import uk.gov.hmcts.reform.dev.exceptions.InvalidStateTransitionException;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.mappers.TaskMapper;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;
import uk.gov.hmcts.reform.dev.utils.TaskTransitions;

/**
 * Service implementation for case worker task operations.
 * Handles task lifecycle management with basic state validation.
 */
@Service
public class CaseWorkerTaskService implements TaskService {

    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;

    public CaseWorkerTaskService(TaskMapper taskMapper, TaskRepository taskRepository) {
        this.taskMapper = taskMapper;
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest createTaskRequest) {
        Task task = taskMapper.toEntity(createTaskRequest);
        Task createdTask = taskRepository.save(task);
        return taskMapper.toResponse(createdTask);
    }

    @Override
    public TaskResponse getTask(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
        return taskMapper.toResponse(task);
    }

    @Override
    public List<TaskResponse> getTasks() {
        return taskRepository.findAll().stream()
            .map(taskMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long id, UpdateTaskRequest updateTaskRequest) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));

        TaskStatus currentStatus = task.getStatus();
        TaskStatus newStatus = updateTaskRequest.getStatus();

        if (!TaskTransitions.isValidTransition(currentStatus, newStatus)) {
            throw new InvalidStateTransitionException(id, currentStatus, newStatus);
        }

        taskMapper.updateEntityFromRequest(updateTaskRequest, task);
        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }
}
