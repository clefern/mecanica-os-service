package com.fiap.mecanica.os.application.port.out;

import com.fiap.mecanica.os.domain.model.OrdemServico;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrdemServicoRepositoryPort {

  OrdemServico salvar(OrdemServico os);

  Optional<OrdemServico> buscarPorId(UUID id);

  Page<OrdemServico> listarTodas(Pageable pageable);
}
