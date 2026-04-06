package uk.gov.hmcts.reform.dev.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

import uk.gov.hmcts.reform.dev.dto.responses.TaskResponse;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;

class HateoasLinkBuilderTest {

    @Test
    @DisplayName("Should build links for non-disposed task with update link")
    void shouldBuildLinksForNonDisposedTaskWithUpdateLink() {
        TaskResponse taskResponse = TaskResponse.builder()
            .id(1L)
            .title("Test Task")
            .status(TaskStatus.DRAFT)
            .dueDate(LocalDateTime.now())
            .build();

        TaskResponse result = HateoasLinkBuilder.addHateoasLinks(taskResponse);

        assertThat(result.getLinks()).containsKey("self");
        assertThat(result.getLinks()).containsKey("update");
        assertThat(result.getLinks()).containsKey("delete");
        assertThat(result.getLinks().get("self")).isEqualTo("/v1/tasks/1");
        assertThat(result.getLinks().get("update")).isEqualTo("/v1/tasks/1");
        assertThat(result.getLinks().get("delete")).isEqualTo("/v1/tasks/1");
    }

    @Test
    @DisplayName("Should build links for disposed task without update link")
    void shouldBuildLinksForDisposedTaskWithoutUpdateLink() {
        TaskResponse taskResponse = TaskResponse.builder()
            .id(1L)
            .title("Disposed Task")
            .status(TaskStatus.DISPOSED)
            .dueDate(LocalDateTime.now())
            .build();

        TaskResponse result = HateoasLinkBuilder.addHateoasLinks(taskResponse);

        assertThat(result.getLinks()).containsKey("self");
        assertThat(result.getLinks()).doesNotContainKey("update");
        assertThat(result.getLinks()).containsKey("delete");
        assertThat(result.getLinks().get("self")).isEqualTo("/v1/tasks/1");
        assertThat(result.getLinks().get("delete")).isEqualTo("/v1/tasks/1");
    }

    @Test
    @DisplayName("Should build pagination links for first page")
    void shouldBuildPaginationLinksForFirstPage() {
        @SuppressWarnings("unchecked")
        Page<Task> page = mock(Page.class);
        when(page.getNumber()).thenReturn(0);
        when(page.getTotalPages()).thenReturn(3);
        when(page.hasPrevious()).thenReturn(false);
        when(page.hasNext()).thenReturn(true);

        Map<String, String> links = HateoasLinkBuilder.buildLinksWithPagination(page, 10);

        assertThat(links).containsKey("self");
        assertThat(links).containsKey("first");
        assertThat(links).containsKey("last");
        assertThat(links).containsKey("next");
        assertThat(links).doesNotContainKey("prev");
        assertThat(links.get("self")).isEqualTo("/v1/tasks?page=0&size=10");
        assertThat(links.get("first")).isEqualTo("/v1/tasks?page=0&size=10");
        assertThat(links.get("last")).isEqualTo("/v1/tasks?page=2&size=10");
        assertThat(links.get("next")).isEqualTo("/v1/tasks?page=1&size=10");
    }

    @Test
    @DisplayName("Should build pagination links for middle page")
    @SuppressWarnings("unchecked")
    void shouldBuildPaginationLinksForMiddlePage() {
        Page<Task> page = mock(Page.class);
        when(page.getNumber()).thenReturn(1);
        when(page.getTotalPages()).thenReturn(3);
        when(page.hasPrevious()).thenReturn(true);
        when(page.hasNext()).thenReturn(true);

        Map<String, String> links = HateoasLinkBuilder.buildLinksWithPagination(page, 10);

        assertThat(links).containsKey("self");
        assertThat(links).containsKey("first");
        assertThat(links).containsKey("last");
        assertThat(links).containsKey("next");
        assertThat(links).containsKey("prev");
        assertThat(links.get("self")).isEqualTo("/v1/tasks?page=1&size=10");
        assertThat(links.get("prev")).isEqualTo("/v1/tasks?page=0&size=10");
        assertThat(links.get("next")).isEqualTo("/v1/tasks?page=2&size=10");
    }

    @Test
    @DisplayName("Should build pagination links for last page")
    void shouldBuildPaginationLinksForLastPage() {
        @SuppressWarnings("unchecked")
        Page<Task> page = mock(Page.class);
        when(page.getNumber()).thenReturn(2);
        when(page.getTotalPages()).thenReturn(3);
        when(page.hasPrevious()).thenReturn(true);
        when(page.hasNext()).thenReturn(false);

        Map<String, String> links = HateoasLinkBuilder.buildLinksWithPagination(page, 10);

        assertThat(links).containsKey("self");
        assertThat(links).containsKey("first");
        assertThat(links).containsKey("last");
        assertThat(links).containsKey("prev");
        assertThat(links).doesNotContainKey("next");
        assertThat(links.get("self")).isEqualTo("/v1/tasks?page=2&size=10");
        assertThat(links.get("last")).isEqualTo("/v1/tasks?page=2&size=10");
        assertThat(links.get("prev")).isEqualTo("/v1/tasks?page=1&size=10");
    }

    @Test
    @DisplayName("Should build pagination links for single page result")
    void shouldBuildPaginationLinksForSinglePageResult() {
        @SuppressWarnings("unchecked")
        Page<Task> page = mock(Page.class);
        when(page.getNumber()).thenReturn(0);
        when(page.getTotalPages()).thenReturn(1);
        when(page.hasPrevious()).thenReturn(false);
        when(page.hasNext()).thenReturn(false);

        Map<String, String> links = HateoasLinkBuilder.buildLinksWithPagination(page, 10);

        assertThat(links).containsKey("self");
        assertThat(links).containsKey("first");
        assertThat(links).containsKey("last");
        assertThat(links).doesNotContainKey("next");
        assertThat(links).doesNotContainKey("prev");
        assertThat(links.get("self")).isEqualTo(links.get("first"));
        assertThat(links.get("self")).isEqualTo(links.get("last"));
    }
}
