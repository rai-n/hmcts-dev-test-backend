package uk.gov.hmcts.reform.dev.exceptions;

import lombok.Getter;

@Getter
public abstract class TaskException extends RuntimeException {

    private final Long taskId;

    protected TaskException(String message, Long taskId) {
        super(message);
        this.taskId = taskId;
    }

}
