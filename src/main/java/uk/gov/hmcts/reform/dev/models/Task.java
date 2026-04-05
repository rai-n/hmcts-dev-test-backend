package uk.gov.hmcts.reform.dev.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tasks")
@Getter
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(length = 100, nullable = false)
  private String title;

  @Column(length = 2500)
  private String description;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private TaskStatus status;

  /** UK only app */
  @Column(nullable = false)
  private LocalDateTime dueDate;

  /**
   * optimistic locking - preventing overwrites from different stakeholders helpful if description/
   * comments are updated with renewed evidence
   */
  @Version private short version;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate private LocalDateTime updatedAt;

  public enum TaskStatus {
    DRAFT,
    SUBMITTED,
    PREPARING,
    BOOKED,
    DISPOSED,
    ADJOURNED
  }
}
