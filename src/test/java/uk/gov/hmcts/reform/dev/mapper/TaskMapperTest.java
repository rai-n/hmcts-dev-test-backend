package uk.gov.hmcts.reform.dev.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.reform.dev.dto.requests.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.requests.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.responses.TaskResponse;
import uk.gov.hmcts.reform.dev.mappers.TaskMapper;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;

class TaskMapperTest {

    private final TaskMapper mapper = Mappers.getMapper(TaskMapper.class);

    @Test
    @DisplayName("Should map CreateTaskRequest to Task entity")
    void shouldMapCreateRequestToEntity() {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("New Task")
            .description("Task Description")
            .status(TaskStatus.DRAFT)
            .dueDate(dueDate)
            .build();

        Task task = mapper.toEntity(request);

        assertThat(task).isNotNull();
        assertThat(task.getTitle()).isEqualTo("New Task");
        assertThat(task.getDescription()).isEqualTo("Task Description");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DRAFT);
        assertThat(task.getDueDate()).isEqualTo(dueDate);
    }

    @Test
    @DisplayName("Should ignore audit fields when mapping CreateTaskRequest")
    void shouldIgnoreAuditFieldsWhenMappingCreateRequest() {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("New Task")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();

        Task task = mapper.toEntity(request);

        assertThat(task.getId()).isEqualTo(0L);
        assertThat(task.getVersion()).isEqualTo((short) 0);
        assertThat(task.getCreatedAt()).isNull();
        assertThat(task.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should map Task entity to TaskResponse")
    void shouldMapEntityToResponse() {
        Task task = new Task(
            "Task Title",
            "Task Description",
            TaskStatus.SUBMITTED,
            LocalDateTime.now().plusDays(5)
        );

        TaskResponse response = mapper.toResponse(task);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Task Title");
        assertThat(response.getDescription()).isEqualTo("Task Description");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.SUBMITTED);
        assertThat(response.getLinks()).isNull();
    }

    @Test
    @DisplayName("Should ignore links when mapping to TaskResponse")
    void shouldIgnoreLinksWhenMappingToResponse() {
        Task task = new Task(
            "Task",
            null,
            TaskStatus.DRAFT,
            LocalDateTime.now().plusDays(1)
        );

        TaskResponse response = mapper.toResponse(task);

        assertThat(response.getLinks()).isNull();
    }

    @Test
    @DisplayName("Should update status from UpdateTaskRequest")
    void shouldUpdateStatusFromUpdateRequest() {
        Task task = new Task(
            "Task Title",
            "Description",
            TaskStatus.SUBMITTED,
            LocalDateTime.now().plusDays(1)
        );
        UpdateTaskRequest request = UpdateTaskRequest.builder()
            .status(TaskStatus.SUBMITTED)
            .build();

        mapper.updateEntityFromRequest(request, task);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.SUBMITTED);
        assertThat(task.getTitle()).isEqualTo("Task Title");
        assertThat(task.getDescription()).isEqualTo("Description");
    }

    @Test
    @DisplayName("Should protect fields during update")
    void shouldProtectFieldsDuringUpdate() {
        LocalDateTime originalDueDate = LocalDateTime.now().plusDays(1);
        Task task = new Task(
            "Original Title",
            "Original Description",
            TaskStatus.DRAFT,
            originalDueDate
        );
        UpdateTaskRequest request = UpdateTaskRequest.builder()
            .status(TaskStatus.BOOKED)
            .build();

        mapper.updateEntityFromRequest(request, task);

        assertThat(task.getTitle()).isEqualTo("Original Title");
        assertThat(task.getDescription()).isEqualTo("Original Description");
        assertThat(task.getDueDate()).isEqualTo(originalDueDate);
        assertThat(task.getStatus()).isNotEqualTo(request.getStatus());
    }

    @Test
    @DisplayName("Should handle null description in CreateTaskRequest")
    void shouldHandleNullDescriptionInCreateRequest() {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Task without description")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();

        Task task = mapper.toEntity(request);

        assertThat(task.getDescription()).isNull();
    }

    @Test
    @DisplayName("Should map all TaskStatus values correctly")
    void shouldMapAllTaskStatusValues() {
        for (TaskStatus status : TaskStatus.values()) {
            CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Task")
                .status(status)
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();

            Task task = mapper.toEntity(request);

            assertThat(task.getStatus())
                .withFailMessage("Status " + status + " should be mapped correctly")
                .isEqualTo(status);
        }
    }
}
