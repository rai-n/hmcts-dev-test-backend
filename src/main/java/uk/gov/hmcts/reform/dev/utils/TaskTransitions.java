package uk.gov.hmcts.reform.dev.utils;

import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;

/**
 * Utility class for task status transition validation.
 * Defines the state machine rules for task lifecycle.
 */
public final class TaskTransitions {

    private TaskTransitions() {

    }

    /**
     * Validates if a status transition is allowed.
     *
     * @param from current status
     * @param to   target status
     * @return true if transition is valid
     */
    public static boolean isValidTransition(TaskStatus from, TaskStatus to) {
        return switch (from) {
            case DRAFT -> to == TaskStatus.SUBMITTED;
            case SUBMITTED -> to == TaskStatus.PREPARING;
            case PREPARING -> to == TaskStatus.BOOKED || to == TaskStatus.ADJOURNED;
            case BOOKED -> to == TaskStatus.DISPOSED || to == TaskStatus.ADJOURNED;
            case ADJOURNED -> to == TaskStatus.PREPARING || to == TaskStatus.BOOKED;
            case DISPOSED -> false; // Terminal state - no transitions allowed
        };
    }
}
