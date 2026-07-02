package com.fiap.mecanica.os.presentation.controller;

import com.fiap.mecanica.os.application.service.OrdemServicoService;
import com.fiap.mecanica.os.domain.enums.TipoItem;
import com.fiap.mecanica.os.domain.model.OrdemServico;
import com.fiap.mecanica.os.presentation.dto.AbrirOsRequest;
import com.fiap.mecanica.os.presentation.dto.AdicionarItemRequest;
import com.fiap.mecanica.os.presentation.dto.OrdemServicoResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordens-servico")
@RequiredArgsConstructor
public class OrdemServicoController {

  private final OrdemServicoService service;

  @PostMapping
  public ResponseEntity<OrdemServicoResponse> abrir(@Valid @RequestBody AbrirOsRequest req) {
    OrdemServico os = service.abrir(req.clienteId(), req.veiculoId(), req.mecanicoId());
    return ResponseEntity
        .created(URI.create("/api/ordens-servico/" + os.getId()))
        .body(OrdemServicoResponse.from(os));
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrdemServicoResponse> buscar(@PathVariable UUID id) {
    return ResponseEntity.ok(OrdemServicoResponse.from(service.buscarPorId(id)));
  }

  @GetMapping
  public ResponseEntity<Page<OrdemServicoResponse>> listar(Pageable pageable) {
    return ResponseEntity.ok(service.listarTodas(pageable).map(OrdemServicoResponse::from));
  }

  @PutMapping("/{id}/iniciar-diagnostico")
  public ResponseEntity<OrdemServicoResponse> iniciarDiagnostico(@PathVariable UUID id) {
    return ResponseEntity.ok(OrdemServicoResponse.from(service.iniciarDiagnostico(id)));
  }

  @PostMapping("/{id}/itens")
  public ResponseEntity<OrdemServicoResponse> adicionarItem(
      @PathVariable UUID id, @Valid @RequestBody AdicionarItemRequest req) {
    OrdemServico os = service.adicionarItem(
        id, req.referenciaId(), TipoItem.valueOf(req.tipo()),
        req.descricao(), req.valorUnitario(), req.quantidade());
    return ResponseEntity.ok(OrdemServicoResponse.from(os));
  }

  @PutMapping("/{id}/emitir-orcamento")
  public ResponseEntity<OrdemServicoResponse> emitirOrcamento(@PathVariable UUID id) {
    return ResponseEntity.ok(OrdemServicoResponse.from(service.emitirOrcamento(id)));
  }

  @PutMapping("/{id}/aprovar")
  public ResponseEntity<OrdemServicoResponse> aprovar(@PathVariable UUID id) {
    return ResponseEntity.ok(OrdemServicoResponse.from(service.aprovar(id)));
  }

  @PutMapping("/{id}/cancelar")
  public ResponseEntity<OrdemServicoResponse> cancelar(@PathVariable UUID id) {
    return ResponseEntity.ok(OrdemServicoResponse.from(service.cancelar(id)));
  }
}
