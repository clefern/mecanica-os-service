# mecanica-os-service

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mecanica_os-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=mecanica_os-service)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=mecanica_os-service&metric=coverage)](https://sonarcloud.io/summary/new_code?id=mecanica_os-service)

> Orquestrador da Saga e gerenciador do ciclo de vida da Ordem de Serviço — coração da Fase 4 (Grupo 14SOAT).

## Responsabilidade na Saga

Este serviço é o **orquestrador central** da Saga Coreografada. Ele abre e avança a OS pelos seus estados, dispara comandos aos demais microsserviços via RabbitMQ e reage aos eventos de resposta para avançar ou compensar o fluxo:

```
RECEBIDA → EM_DIAGNOSTICO → AGUARDANDO_APROVACAO
  └── Saga: GerarOrcamento → ReservarPecas → IniciarExecucao → ENTREGUE
                                                              ↘ CANCELADA (compensação)
```

## Endpoints REST

| Método | Path | Descrição |
|--------|------|-----------|
| `POST` | `/api/auth/login` | Autenticação — retorna JWT |
| `POST` | `/api/ordens-servico` | Abrir nova OS |
| `GET` | `/api/ordens-servico` | Listar OSs (paginado) |
| `GET` | `/api/ordens-servico/{id}` | Buscar OS por ID |
| `POST` | `/api/ordens-servico/{id}/itens` | Adicionar item (peça/serviço) |
| `PUT` | `/api/ordens-servico/{id}/iniciar-diagnostico` | Avançar para EM_DIAGNOSTICO |
| `PUT` | `/api/ordens-servico/{id}/emitir-orcamento` | Emitir orçamento e disparar Saga |
| `PUT` | `/api/ordens-servico/{id}/aprovar` | Aprovar OS manualmente |
| `PUT` | `/api/ordens-servico/{id}/cancelar` | Cancelar OS |

Swagger: `http://localhost:8080/swagger-ui.html`

## Mensagens RabbitMQ

### Publica (comandos para os MS)
| Routing Key | Tipo | Destinatário |
|-------------|------|--------------|
| `billing.gerar-orcamento` | `GerarOrcamentoCommand` | billing-service |
| `inventory.reservar-pecas` | `ReservarPecasCommand` | inventory-service |
| `workshop.iniciar-execucao` | `IniciarExecucaoCommand` | workshop-service |

### Consome (eventos de resposta)
| Routing Key | Tipo | Ação |
|-------------|------|------|
| `os.orcamento-criado` | `OrcamentoCriadoEvent` | Saga avança |
| `os.pagamento-confirmado` | `PagamentoConfirmadoEvent` | Reserva peças |
| `os.pagamento-recusado` | `PagamentoRecusadoEvent` | Cancela OS |
| `os.pecas-reservadas` | `PecasReservadasEvent` | Inicia execução |
| `os.falha-reserva` | `FalhaNaReservaEvent` | Cancela OS |
| `os.execucao-finalizada` | `ExecucaoFinalizadaEvent` | OS → ENTREGUE |
| `os.falha-execucao` | `FalhaNaExecucaoEvent` | Cancela OS |

## Como rodar localmente

```bash
# Stack completa (todos os MS + infra)
cd ms-infra-ms/mecanica-fiap
docker compose -f docker-compose.full.yml up --build

# Login de teste
curl -s -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@mecanica.com","password":"123456"}'
```

## Testes

```bash
./mvnw test                       # unitários (domain + saga) + BDD
./mvnw test -Dtest="CucumberTest" # apenas BDD (10 cenários Gherkin)
```

O BDD usa **Testcontainers** (PostgreSQL real) — requer Docker em execução.

## Tech stack

| | |
|-|-|
| **Java** | 21 |
| **Framework** | Spring Boot 3.5.x |
| **Banco** | PostgreSQL 16 (porta 5432) |
| **Mensageria** | RabbitMQ 3.13 |
| **Migrations** | Flyway |
| **Segurança** | JWT (JJWT 0.12) |
| **Porta** | 8080 |
| **Cobertura** | JaCoCo ≥ 80% |
| **BDD** | Cucumber 7.21 + JUnit Platform Suite |
