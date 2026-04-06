package uk.gov.hmcts.reform.dev.services;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dev.dto.requests.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.requests.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.responses.PagedTaskResponse;
import uk.gov.hmcts.reform.dev.dto.responses.TaskResponse;
import uk.gov.hmcts.reform.dev.exceptions.InvalidStateTransitionException;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.mappers.TaskMapper;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;
import uk.gov.hmcts.reform.dev.utils.HateoasLinkBuilder;
import uk.gov.hmcts.reform.dev.utils.TaskTransitions;

/**
 * Service implementation for case worker task operations.
 * Performs basic state validation.
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
    public PagedTaskResponse getTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> taskPage = taskRepository.findAll(pageable);

        List<TaskResponse> taskResponses = taskPage.getContent()
            .stream()
            .map(taskMapper::toResponse)
            .map(HateoasLinkBuilder::addHateoasLinks)
            .toList();

        PagedTaskResponse.PageDetails pageDetails = PagedTaskResponse.PageDetails.builder()
            .totalElements(taskPage.getTotalElements())
            .pageSize(taskPage.getSize())
            .currentPage(taskPage.getNumber())
            .totalPages(taskPage.getTotalPages())
            .build();

        Map<String, String> linksWithPagination = HateoasLinkBuilder.buildLinksWithPagination(taskPage, size);

        return PagedTaskResponse.builder()
            .data(taskResponses)
            .links(linksWithPagination)
            .pageDetails(pageDetails)
            .build();


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
