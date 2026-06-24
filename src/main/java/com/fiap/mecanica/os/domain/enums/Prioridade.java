package com.fiap.mecanica.os.domain.enums;

import lombok.Getter;

@Getter
public enum Prioridade {
  BAIXA(0), NORMAL(1), ALTA(2), URGENTE(3);

  private final int nivel;

  Prioridade(int nivel) {
    this.nivel = nivel;
  }
}
