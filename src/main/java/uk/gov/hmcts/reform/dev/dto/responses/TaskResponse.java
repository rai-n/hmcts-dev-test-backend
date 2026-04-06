package uk.gov.hmcts.reform.dev.dto.responses;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;

/**
 * DTO for task responses. Added HATEOAS links for client simplicity
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    /**
     * Tasks resource type. Resource not fully abstracted into attributes/ relationships
     */
    @Builder.Default
    private String type = "tasks";

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Short version;

    /**
     * HATEOAS links. E.g.
     * {"self": "v1/tasks/123", "update": "v1/tasks/123", "delete": "v1/tasks/123"}
     */
    private Map<String, String> links;
}
