package com.fiap.mecanica.os.infra.seeding.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mecanicos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MecanicoSeedEntity {

  @Id
  private UUID id;

  @Column(name = "cpf", nullable = false, unique = true)
  private String cpf;

  @Column(name = "especialidade", nullable = false)
  private String especialidade;
}
