package com.fiap.mecanica.os.infra.seeding.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "veiculos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoSeedEntity {

  @Id
  private UUID id;

  @Column(name = "cliente_id", nullable = false)
  private UUID clienteId;

  @Column(name = "placa", nullable = false, unique = true)
  private String placa;

  @Column(name = "marca", nullable = false)
  private String marca;

  @Column(name = "modelo", nullable = false)
  private String modelo;

  @Column(name = "ano", nullable = false)
  private Integer ano;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
