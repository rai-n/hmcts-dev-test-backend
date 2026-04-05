package uk.gov.hmcts.reform.dev.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.dev.dto.requests.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;

class CreateTaskRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should correctly validate CreateTaskRequest")
    void shouldValidateValidRequest() {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Test Task")
            .description("Test Description")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();

        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should accept description as optional")
    void shouldAcceptDescriptionAsOptional() {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Test Task")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();

        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject blank title")
    void shouldRejectBlankTitle() {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();

        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertThat(violations)
            .hasSize(1)
            .first()
            .satisfies(v -> assertThat(v.getMessage()).isEqualTo("Title is required"));
    }

    @Test
    @DisplayName("Should reject title exceeding max length")
    void shouldRejectTitleExceedingMaxLength() {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title(".".repeat(110))
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();

        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertThat(violations)
            .hasSize(1)
            .first()
            .satisfies(v -> assertThat(v.getMessage())
                .isEqualTo("Title must not exceed 100 characters"));
    }

    @Test
    @DisplayName("Should reject null status")
    void shouldRejectNullStatus() {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Test Task")
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();

        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertThat(violations)
            .hasSize(1)
            .first()
            .satisfies(v -> assertThat(v.getMessage()).isEqualTo("Status is required"));
    }

    @Test
    @DisplayName("Should reject past due date")
    void shouldRejectPastDueDate() {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Test Task")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now().minusDays(1))
            .build();

        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertThat(violations)
            .hasSize(1)
            .first()
            .satisfies(v -> assertThat(v.getMessage())
                .isEqualTo("Due date must be in the future"));
    }
}
