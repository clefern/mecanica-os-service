package com.fiap.mecanica.os.infra.persistence.adapter;

import com.fiap.mecanica.os.application.port.out.OrdemServicoRepositoryPort;
import com.fiap.mecanica.os.domain.model.OrdemServico;
import com.fiap.mecanica.os.infra.persistence.entity.OrdemServicoEntity;
import com.fiap.mecanica.os.infra.persistence.mapper.OrdemServicoMapper;
import com.fiap.mecanica.os.infra.persistence.repository.OrdemServicoJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrdemServicoRepositoryAdapter implements OrdemServicoRepositoryPort {

  private final OrdemServicoJpaRepository jpaRepository;
  private final OrdemServicoMapper mapper;

  @Override
  public OrdemServico salvar(OrdemServico os) {
    OrdemServicoEntity entity = mapper.toEntity(os);
    OrdemServicoEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<OrdemServico> buscarPorId(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Page<OrdemServico> listarTodas(Pageable pageable) {
    return jpaRepository.findAll(pageable).map(mapper::toDomain);
  }
}
