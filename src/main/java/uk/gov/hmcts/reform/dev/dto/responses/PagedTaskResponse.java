package uk.gov.hmcts.reform.dev.dto.responses;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for task responses. Added HATEOAS links for client simplicity
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedTaskResponse {

    /**
     * Pagination links following json api format.
     * "self": "v1/tasks?page[offset]=0&page[limit]=10",
     * "next": "v1/tasks?page[offset]=20&page[limit]=10",
     * "prev": "v1/tasks?page[offset]=0&page[limit]=20",
     * "first": "v1/tasks?page[offset]=0&page[limit]=15"
     * "last": "v1/tasks?page[offset]=100&page[limit]=10"
     */
    private Map<String, String> links;

    private PageDetails pageDetails;

    private List<TaskResponse> data;

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageDetails {
        private long totalElements;
        private int totalPages;
        private int currentPage;
        private int pageSize;
    }
}
