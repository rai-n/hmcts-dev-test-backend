package uk.gov.hmcts.reform.dev.controllers;

import static org.springframework.http.ResponseEntity.ok;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

    @GetMapping(value = "/tasks", produces = "application/json")
    public ResponseEntity<String> getExampleCase() {
        return ok("hello");
    }
}
