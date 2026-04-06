package uk.gov.hmcts.reform.dev;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Map;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class TaskApiFunctionalTest {

    @Value("${TEST_URL:http://localhost:4000}")
    private String url;

    private static final String TASKS_ENDPOINT = "/v1/tasks";

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = url;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    @DisplayName("Should create task and retrieve it successfully")
    void shouldCreateAndRetrieveTask() {
        Map<String, Object> createRequest = Map.of(
            "title", "Functional Test",
            "description", "Test description",
            "status", "DRAFT",
            "dueDate", LocalDateTime.now().plusDays(1).toString()
        );

        Response createResponse = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post(TASKS_ENDPOINT)
            .then()
            .extract().response();

        assertThat(createResponse.statusCode()).isEqualTo(201);
        assertThat(createResponse.jsonPath().getString("title")).isEqualTo("Functional Test");
        assertThat(createResponse.jsonPath().getMap("links")).containsKey("self");

        Long taskId = createResponse.jsonPath().getLong("id");

        Response getResponse = given()
            .when()
            .get(TASKS_ENDPOINT + "/{id}", taskId)
            .then()
            .extract().response();

        assertThat(getResponse.statusCode()).isEqualTo(200);
        assertThat(getResponse.jsonPath().getLong("id")).isEqualTo(taskId);
    }

    @Test
    @DisplayName("Should return paginated tasks with HATEOAS links")
    void shouldReturnPaginatedTasksWithHateoasLinks() {
        Response response = given()
            .queryParam("page", 0)
            .queryParam("size", 10)
            .when()
            .get(TASKS_ENDPOINT)
            .then()
            .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getMap("links")).containsKeys("self", "first", "last");
        assertThat(response.jsonPath().getMap("pageDetails")).containsKeys("totalElements",
               "totalPages", "currentPage", "pageSize");
        assertThat(response.jsonPath().getList("data")).isNotNull();
    }

    @Test
    @DisplayName("Should update task status through valid transitions")
    void shouldUpdateTaskStatusThroughValidTransitions() {
        Map<String, Object> createRequest = Map.of(
            "title", "Status Transition Task",
            "status", "DRAFT",
            "dueDate", LocalDateTime.now().plusDays(1).toString()
        );

        Response createResponse = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .post(TASKS_ENDPOINT);

        Long taskId = createResponse.jsonPath().getLong("id");

        Map<String, Object> updateRequest = Map.of("status", "SUBMITTED");

        Response updateResponse = given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .patch(TASKS_ENDPOINT + "/{id}", taskId);

        assertThat(updateResponse.statusCode()).isEqualTo(200);
        assertThat(updateResponse.jsonPath().getString("status")).isEqualTo("SUBMITTED");
    }

    @Test
    @DisplayName("Should reject invalid status transition")
    void shouldRejectInvalidStatusTransition() {
        Map<String, Object> createRequest = Map.of(
            "title", "Invalid Transition Task",
            "status", "DRAFT",
            "dueDate", LocalDateTime.now().plusDays(1).toString()
        );

        Response createResponse = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .post(TASKS_ENDPOINT);

        Long taskId = createResponse.jsonPath().getLong("id");

        Map<String, Object> updateRequest = Map.of("status", "DISPOSED");

        Response updateResponse = given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .patch(TASKS_ENDPOINT + "/{id}", taskId);

        assertThat(updateResponse.statusCode()).isEqualTo(400);
        assertThat(updateResponse.jsonPath().getString("errorCode")).isEqualTo("error.tasks.status.invalid.transition");
    }

    @Test
    @DisplayName("Should delete task successfully")
    void shouldDeleteTaskSuccessfully() {
        Map<String, Object> createRequest = Map.of(
            "title", "Delete Test Task",
            "status", "DRAFT",
            "dueDate", LocalDateTime.now().plusDays(1).toString()
        );

        Response createResponse = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .post(TASKS_ENDPOINT);

        Long taskId = createResponse.jsonPath().getLong("id");

        Response deleteResponse = given()
            .delete(TASKS_ENDPOINT + "/{id}", taskId);

        assertThat(deleteResponse.statusCode()).isEqualTo(204);

        Response getResponse = given()
            .get(TASKS_ENDPOINT + "/{id}", taskId);

        assertThat(getResponse.statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("Should return 404 for non-existent task")
    void shouldReturn404ForNonExistentTask() {
        Response response = given()
            .get(TASKS_ENDPOINT + "/{id}", 999999);

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("errorCode")).isEqualTo("error.tasks.not.found");
    }

    @Test
    @DisplayName("Should return validation error for missing required fields")
    void shouldReturnValidationErrorForMissingRequiredFields() {
        Map<String, Object> invalidRequest = Map.of(
            "title", "",
            "status", "DRAFT"
        );

        Response response = given()
            .contentType(ContentType.JSON)
            .body(invalidRequest)
            .post(TASKS_ENDPOINT);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("errorCode")).isEqualTo("error.tasks.validation.failed");
    }

    @Test
    @DisplayName("Should handle disposed task without update link")
    void shouldHandleDisposedTaskWithoutUpdateLink() {
        Map<String, Object> createRequest = Map.of(
            "title", "Disposed Task Test",
            "status", "DRAFT",
            "dueDate", LocalDateTime.now().plusDays(1).toString()
        );

        Response createResponse = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .post(TASKS_ENDPOINT);

        Long taskId = createResponse.jsonPath().getLong("id");

        given().contentType(ContentType.JSON).body(Map.of("status", "SUBMITTED"))
            .patch(TASKS_ENDPOINT + "/{id}", taskId);
        given().contentType(ContentType.JSON).body(Map.of("status", "PREPARING"))
            .patch(TASKS_ENDPOINT + "/{id}", taskId);
        given().contentType(ContentType.JSON).body(Map.of("status", "BOOKED"))
            .patch(TASKS_ENDPOINT + "/{id}", taskId);

        Response disposeResponse = given()
            .contentType(ContentType.JSON)
            .body(Map.of("status", "DISPOSED"))
            .patch(TASKS_ENDPOINT + "/{id}", taskId);

        assertThat(disposeResponse.statusCode()).isEqualTo(200);
        assertThat(disposeResponse.jsonPath().getString("status")).isEqualTo("DISPOSED");

        Map<String, String> links = disposeResponse.jsonPath().getMap("links");
        assertThat(links).containsKey("self");
        assertThat(links).containsKey("delete");
        assertThat(links).doesNotContainKey("update");
    }
}
