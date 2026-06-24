package com.fiap.mecanica.os.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "saga_state")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStateEntity {

  @Id
  private UUID id;

  @Column(name = "os_id", nullable = false)
  private UUID osId;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "orcamento_id")
  private UUID orcamentoId;

  @Column(name = "payment_url", length = 500)
  private String paymentUrl;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
