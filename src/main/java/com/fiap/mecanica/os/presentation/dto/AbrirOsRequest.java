package com.fiap.mecanica.os.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AbrirOsRequest(
    @NotNull UUID clienteId,
    @NotNull UUID veiculoId,
    @NotNull UUID mecanicoId) {}
