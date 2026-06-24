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
@Table(name = "clientes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteSeedEntity {

  @Id
  private UUID id;

  @Column(name = "documento", nullable = false, unique = true)
  private String documento;

  @Column(name = "tipo_pessoa", nullable = false)
  private String tipoPessoa;

  @Column(name = "telefone")
  private String telefone;

  @Column(name = "endereco")
  private String endereco;
}
