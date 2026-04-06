package uk.gov.hmcts.reform.dev.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dev.models.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findAll(Pageable pageable);
}
