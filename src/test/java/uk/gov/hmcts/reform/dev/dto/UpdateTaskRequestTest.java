package uk.gov.hmcts.reform.dev.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.dev.dto.requests.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;

class UpdateTaskRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should validate valid UpdateTaskRequest")
    void shouldValidateValidRequest() {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
            .status(TaskStatus.SUBMITTED)
            .build();

        Set<ConstraintViolation<UpdateTaskRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject null status")
    void shouldRejectNullStatus() {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
            .build();

        Set<ConstraintViolation<UpdateTaskRequest>> violations = validator.validate(request);
        assertThat(violations)
            .hasSize(1)
            .first()
            .satisfies(v -> assertThat(v.getMessage()).isEqualTo("Status is required"));
    }

    @Test
    @DisplayName("Should accept all valid status values")
    void shouldAcceptAllValidStatusValues() {
        for (TaskStatus status : TaskStatus.values()) {
            UpdateTaskRequest request = UpdateTaskRequest.builder()
                .status(status)
                .build();

            Set<ConstraintViolation<UpdateTaskRequest>> violations = validator.validate(request);
            assertThat(violations)
                .withFailMessage("Status " + status + " should be valid")
                .isEmpty();
        }
    }
}
