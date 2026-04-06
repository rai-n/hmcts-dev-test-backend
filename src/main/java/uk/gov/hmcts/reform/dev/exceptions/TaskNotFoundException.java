package uk.gov.hmcts.reform.dev.exceptions;

/**
 * Exception thrown when a requested task cannot be found.
 */
public class TaskNotFoundException extends TaskException {

    private static final String EXCEPTION_MESSAGE = "Task not found with id: %d";

    public TaskNotFoundException(Long taskId) {
        super(String.format(EXCEPTION_MESSAGE, taskId), taskId);
    }
}
