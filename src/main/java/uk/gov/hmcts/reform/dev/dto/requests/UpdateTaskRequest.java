package uk.gov.hmcts.reform.dev.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;

/**
 * DTO for updating task status.
 * Status transitions are controlled via state machine in service layer.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    @NotNull(message = "Status is required")
    private TaskStatus status;
}
