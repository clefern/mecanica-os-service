package com.fiap.mecanica.os.application.saga.command;

import java.util.UUID;

public record IniciarExecucaoCommand(UUID sagaId, UUID osId) {}
