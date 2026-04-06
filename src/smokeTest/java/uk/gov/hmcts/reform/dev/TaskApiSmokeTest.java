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

/**
 * Quick health check.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class TaskApiSmokeTest {

    @Value("${TEST_URL:http://localhost:4000}")
    private String testUrl;

    private static final String TASKS_ENDPOINT = "/v1/tasks";

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    @DisplayName("Application health check - root")
    void applicationHealthCheck() {
        Response response = given()
            .when()
            .get("/")
            .then()
            .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.asString()).contains("Welcome");
    }

    @Test
    @DisplayName("Task API is accessible - GET tasks returns 200")
    void taskApiIsAccessible() {
        Response response = given()
            .when()
            .get(TASKS_ENDPOINT)
            .then()
            .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getMap("pageDetails")).isNotNull();
        assertThat(response.jsonPath().getList("data")).isNotNull();
    }

    @Test
    @DisplayName("Task creation is working - POST task returns 201")
    void taskCreationIsWorking() {
        Map<String, Object> createRequest = Map.of(
            "title", "Smoke Test",
            "status", "DRAFT",
            "dueDate", LocalDateTime.now().plusDays(1).toString()
        );

        Response response = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post(TASKS_ENDPOINT)
            .then()
            .extract().response();

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getLong("id")).isNotNull();
        assertThat(response.jsonPath().getString("title")).isEqualTo("Smoke Test");
        assertThat(response.jsonPath().getMap("links")).containsKey("self");
    }

    @Test
    @DisplayName("Swagger UI is accessible")
    void swaggerUiIsAccessible() {
        Response response = given()
            .when()
            .get("/swagger-ui.html")
            .then()
            .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.asString()).contains("swagger-ui");
    }

    @Test
    @DisplayName("API docs endpoint is accessible")
    void apiDocsIsAccessible() {
        Response response = given()
            .when()
            .get("/v3/api-docs")
            .then()
            .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("openapi")).isNotNull();
        assertThat(response.jsonPath().getMap("paths")).containsKey("/v1/tasks");
    }

    @Test
    @DisplayName("Error handling is working - returns proper error response")
    void errorHandlingIsWorking() {
        Response response = given()
            .when()
            .get(TASKS_ENDPOINT + "/{id}", 999999999)
            .then()
            .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("errorCode")).isEqualTo("error.tasks.not.found");
        assertThat(response.jsonPath().getString("message")).isNotNull();
    }
}
