package com.fiap.mecanica.os.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record AdicionarItemRequest(
    @NotNull UUID referenciaId,
    @NotBlank String tipo,
    @NotBlank String descricao,
    @NotNull BigDecimal valorUnitario,
    @Min(1) int quantidade) {}
