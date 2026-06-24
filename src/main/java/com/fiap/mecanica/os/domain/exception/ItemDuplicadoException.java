package com.fiap.mecanica.os.domain.exception;

import java.util.UUID;

public class ItemDuplicadoException extends RuntimeException {

  public ItemDuplicadoException(String descricao, UUID referenciaId) {
    super("Item já adicionado à OS: " + descricao + " (id=" + referenciaId + ")");
  }
}
