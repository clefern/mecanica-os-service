package com.fiap.mecanica.os.application.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

public record OsCriadaNotificacaoEvent(
    UUID osId,
    String osCodigo,
    LocalDateTime dataEntrada,
    UUID clienteId,
    String clienteNome,
    String clienteEmail,
    UUID veiculoId,
    String veiculoPlaca,
    String veiculoMarca,
    String veiculoModelo,
    Integer veiculoAno) {}
