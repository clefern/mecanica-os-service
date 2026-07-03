package com.fiap.mecanica.os.presentation.controller;

import com.fiap.mecanica.os.domain.exception.ItemDuplicadoException;
import com.fiap.mecanica.os.domain.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.os.domain.exception.TransicaoStatusInvalidaException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(TransicaoStatusInvalidaException.class)
  public ResponseEntity<Map<String, String>> handleTransicao(TransicaoStatusInvalidaException ex) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(Map.of("erro", ex.getMessage()));
  }

  @ExceptionHandler(ItemDuplicadoException.class)
  public ResponseEntity<Map<String, String>> handleItemDuplicado(ItemDuplicadoException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("erro", ex.getMessage()));
  }

  @ExceptionHandler(OrdemServicoNaoEncontradaException.class)
  public ResponseEntity<Map<String, String>> handleNaoEncontrada(OrdemServicoNaoEncontradaException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("erro", ex.getMessage()));
  }
}
