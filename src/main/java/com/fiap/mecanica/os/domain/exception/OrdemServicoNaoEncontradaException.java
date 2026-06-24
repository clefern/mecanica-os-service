package com.fiap.mecanica.os.domain.exception;

import java.util.UUID;

public class OrdemServicoNaoEncontradaException extends RuntimeException {

  public OrdemServicoNaoEncontradaException(UUID id) {
    super("Ordem de serviço não encontrada: " + id);
  }
}
