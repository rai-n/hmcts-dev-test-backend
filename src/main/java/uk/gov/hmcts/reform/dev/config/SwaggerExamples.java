package uk.gov.hmcts.reform.dev.config;

/**
 * Swagger example payloads for API documentation.
 */
public final class SwaggerExamples {

    private SwaggerExamples() {
        // Utility class
    }

    public static final String TASK_RESPONSE = """
        {
          "type": "tasks",
          "id": 1,
          "title": "Case AAAA - Initial Review",
          "description": "Review initial evidence for case A123",
          "status": "DRAFT",
          "dueDate": "2026-05-01T10:00:00",
          "createdAt": "2026-04-06T11:58:00.219115",
          "updatedAt": "2026-04-06T11:58:00.219115",
          "version": 0,
          "links": {
            "self": "/v1/tasks/1",
            "update": "/v1/tasks/1",
            "delete": "/v1/tasks/1"
          }
        }
        """;

    public static final String UPDATED_TASK_RESPONSE = """
        {
          "type": "tasks",
          "id": 1,
          "title": "Case AAAA - Initial Review",
          "description": "Review initial evidence for case A123",
          "status": "SUBMITTED",
          "dueDate": "2026-05-01T10:00:00",
          "createdAt": "2026-04-06T11:58:00.219115",
          "updatedAt": "2026-04-06T12:00:00.123456",
          "version": 1,
          "links": {
            "self": "/v1/tasks/1",
            "update": "/v1/tasks/1",
            "delete": "/v1/tasks/1"
          }
        }
        """;

    public static final String PAGED_TASK_RESPONSE = """
        {
          "links": {
            "next": "/v1/tasks?page=1&size=10",
            "last": "/v1/tasks?page=1&size=10",
            "self": "/v1/tasks?page=0&size=10",
            "first": "/v1/tasks?page=0&size=10"
          },
          "pageDetails": {
            "totalElements": 18,
            "totalPages": 2,
            "currentPage": 0,
            "pageSize": 10
          },
          "data": [
            {
              "type": "tasks",
              "id": 1,
              "title": "Case AAAA - Initial Review",
              "description": "Review initial evidence for case A123",
              "status": "DRAFT",
              "dueDate": "2026-05-01T10:00:00",
              "createdAt": "2026-04-06T11:58:00.219115",
              "updatedAt": "2026-04-06T11:58:00.219115",
              "version": 0,
              "links": {
                "self": "/v1/tasks/1",
                "update": "/v1/tasks/1",
                "delete": "/v1/tasks/1"
              }
            },
            {
              "type": "tasks",
              "id": 2,
              "title": "Case BBBB - Submitted",
              "description": "Case submitted for processing",
              "status": "SUBMITTED",
              "dueDate": "2026-05-15T14:00:00",
              "createdAt": "2026-04-06T11:58:00.219115",
              "updatedAt": "2026-04-06T11:58:00.219115",
              "version": 2,
              "links": {
                "self": "/v1/tasks/2",
                "update": "/v1/tasks/2",
                "delete": "/v1/tasks/2"
              }
            }
          ]
        }
        """;

}
