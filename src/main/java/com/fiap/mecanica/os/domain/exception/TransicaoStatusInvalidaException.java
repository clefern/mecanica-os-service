package com.fiap.mecanica.os.domain.exception;

public class TransicaoStatusInvalidaException extends RuntimeException {

  public TransicaoStatusInvalidaException(String message) {
    super(message);
  }

  public TransicaoStatusInvalidaException(String de, String para) {
    super("Transição de status inválida: " + de + " → " + para);
  }
}
