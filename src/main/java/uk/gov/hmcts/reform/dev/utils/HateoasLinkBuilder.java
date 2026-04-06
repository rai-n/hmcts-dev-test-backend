package uk.gov.hmcts.reform.dev.utils;

import org.springframework.data.domain.Page;
import uk.gov.hmcts.reform.dev.dto.responses.TaskResponse;
import uk.gov.hmcts.reform.dev.models.Task;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for building HATEOAS links for tasks.
 * Generating links to decouple client url generation.
 */
public class HateoasLinkBuilder {

    private static final String BASE_PATH = "v1/tasks";

    private HateoasLinkBuilder() {

    }

    private static Map<String, String> buildLinks(Long taskId, Task.TaskStatus taskStatus) {
        Map<String, String> links = new HashMap<>();

        String selfLink = BASE_PATH + "/" + taskId;
        links.put("self", selfLink);

        if (taskStatus != Task.TaskStatus.DISPOSED) {
            links.put("update", selfLink);
        }

        links.put("delete", selfLink);

        return links;
    }

    public static Map<String, String> buildLinksWithPagination(Page<Task> page, int size) {
        Map<String, String> paginationLinks = new HashMap<>();

        paginationLinks.put("self", String.format("%s?page=%d&size=%d", BASE_PATH, page.getNumber(), size));
        paginationLinks.put("first", String.format("%s?page=0&size=%d", BASE_PATH, size));

        if (page.hasPrevious()) {
            paginationLinks.put("prev", String.format("%s?page=%d&size=%d", BASE_PATH, page.getNumber() - 1, size));
        }

        if (page.hasNext()) {
            paginationLinks.put("next", String.format("%s?page=%d&size=%d", BASE_PATH, page.getNumber() + 1, size));
        }

        paginationLinks.put("last", String.format("%s?page=%d&size=%d", BASE_PATH, page.getTotalPages(), size));

        return paginationLinks;
    }

    public static TaskResponse addHateoasLinks(TaskResponse taskResponse) {
        Map<String, String> links = HateoasLinkBuilder.buildLinks(taskResponse.getId(), taskResponse.getStatus());

        return TaskResponse.builder()
            .id(taskResponse.getId())
            .title(taskResponse.getTitle())
            .description(taskResponse.getDescription())
            .status(taskResponse.getStatus())
            .dueDate(taskResponse.getDueDate())
            .version(taskResponse.getVersion())
            .createdAt(taskResponse.getCreatedAt())
            .updatedAt(taskResponse.getUpdatedAt())
            .links(links)
            .build();
    }
}
