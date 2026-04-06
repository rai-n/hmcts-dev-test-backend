package uk.gov.hmcts.reform.dev.exceptions;

import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;

/**
 * Exception thrown when an invalid task status transition is attempted.
 */
public class InvalidStateTransitionException extends TaskException {

    private static final String EXCEPTION_MESSAGE =
        "Task %d - Invalid status transition from '%s' to '%s'";

    private final TaskStatus fromStatus;
    private final TaskStatus toStatus;

    public InvalidStateTransitionException(Long taskId, TaskStatus fromStatus, TaskStatus toStatus) {
        super(String.format(EXCEPTION_MESSAGE, taskId, fromStatus, toStatus), taskId);
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    public TaskStatus getFromStatus() {
        return fromStatus;
    }

    public TaskStatus getToStatus() {
        return toStatus;
    }
}
