# ADR 0001 — Arquitetura de microsserviços (C4 + topologia)

- **Status:** Aceito
- **Data:** 2026-07
- **Contexto do desafio:** Fase 4 — refatorar a aplicação em, no mínimo, 3
  microsserviços independentes, cada um com seu próprio repositório,
  infraestrutura e banco de dados.

## Contexto

A Fase 3 entregou um monólito (`fiap-tc-mecanica-app`) hexagonal/DDD em torno da
Ordem de Serviço (OS) de uma oficina mecânica. A Fase 4 exige decomposição em
microsserviços independentes, aplicando o **Saga Pattern** para manter a
consistência de um fluxo de negócio que cruza vários serviços e bancos.

Bounded contexts identificados no domínio:

- **Ordem de Serviço** — ciclo de vida da OS e orquestração do fluxo.
- **Faturamento** — orçamento e pagamento (Mercado Pago).
- **Estoque** — reserva e estorno de peças.
- **Execução (oficina)** — registro físico do reparo.

## Decisão

Decompor em **4 microsserviços**, um por bounded context, cada um em seu próprio
repositório, com CI/CD, infraestrutura e banco próprios:

| Serviço | Repositório | Responsabilidade | Banco |
|---------|-------------|------------------|-------|
| os-service | `mecanica-os-service` | Ciclo de vida da OS + **orquestrador da Saga** | PostgreSQL |
| billing-service | `mecanica-billing-service` | Orçamento + pagamento (Mercado Pago) | PostgreSQL |
| inventory-service | `mecanica-inventory-service` | Estoque, reserva e estorno de peças | PostgreSQL |
| workshop-service | `mecanica-workshop-service` | Execução física do reparo | MongoDB |

Componentes de apoio:

- **`mecanica-shared-kernel`** — Value Objects genéricos (CPF, CNPJ, Email,
  Endereco, PlacaVeiculo, TelefoneBr) publicados no GitHub Packages. Compartilha
  *tipos*, não estado nem banco — não cria acoplamento de runtime.
- **`fiap-tc-mecanica-lambda`** — autenticação CPF → JWT (AWS Lambda), reusada da
  Fase 3.

**Estilo interno:** cada serviço mantém Arquitetura Hexagonal + DDD (domínio
isolado de infraestrutura via ports/adapters).

**Comunicação:**
- **Síncrona (externa):** REST via **Traefik** como API Gateway (ponto único de
  entrada), roteando por `PathPrefix` para cada serviço.
- **Assíncrona (interna):** **RabbitMQ** para a Saga (comandos e eventos) — ver
  [ADR 0002](0002-saga-orquestrada-rabbitmq.md).

### C4 — Nível 1: Contexto

```mermaid
C4Context
  title Contexto do sistema — Mecânica API (Fase 4)
  Person(cliente, "Cliente", "Consulta OS e acompanha o reparo")
  Person(atendente, "Atendente / Mecânico", "Cria OS, gera diagnóstico e orçamento")
  System(mecanica, "Mecânica API", "Plataforma de gestão de ordens de serviço da oficina")
  System_Ext(mp, "Mercado Pago", "Gateway de pagamento")
  Rel(cliente, mecanica, "Acompanha OS", "HTTPS")
  Rel(atendente, mecanica, "Gerencia OS", "HTTPS")
  Rel(mecanica, mp, "Cria preferência / recebe webhook", "HTTPS")
```

### C4 — Nível 2: Contêineres

```mermaid
C4Container
  title Contêineres — Mecânica API (Fase 4)
  Person(user, "Cliente / Atendente")
  System_Ext(mp, "Mercado Pago")

  Container_Boundary(k8s, "Cluster EKS (namespace mecanica-ms)") {
    Container(gw, "Traefik", "API Gateway", "Ponto único de entrada; roteia /api/*")
    Container(os, "os-service", "Spring Boot / Java 21", "OS + orquestrador da Saga")
    Container(bill, "billing-service", "Spring Boot / Java 21", "Orçamento + pagamento")
    Container(inv, "inventory-service", "Spring Boot / Java 21", "Estoque e reserva")
    Container(shop, "workshop-service", "Spring Boot / Java 21", "Execução do reparo")
    ContainerQueue(mq, "RabbitMQ", "AMQP", "Exchange mecanica.direct — comandos/eventos da Saga")
    ContainerDb(pgos, "os_service", "PostgreSQL", "OS + saga_state")
    ContainerDb(pgbill, "billing_service", "PostgreSQL", "Orçamentos/pagamentos")
    ContainerDb(pginv, "inventory_service", "PostgreSQL", "Peças/reservas")
    ContainerDb(mongo, "workshop_service", "MongoDB", "Execuções")
  }

  Rel(user, gw, "REST", "HTTPS")
  Rel(gw, os, "/api/auth, /api/ordens-servico", "HTTP")
  Rel(gw, bill, "/api/billing", "HTTP")
  Rel(gw, inv, "/api/estoque", "HTTP")
  Rel(gw, shop, "/api/execucoes", "HTTP")

  Rel(os, mq, "Publica comandos / consome eventos", "AMQP")
  Rel(bill, mq, "Consome comandos / publica eventos", "AMQP")
  Rel(inv, mq, "Consome comandos / publica eventos", "AMQP")
  Rel(shop, mq, "Consome comandos / publica eventos", "AMQP")

  Rel(bill, mp, "SDK / webhook", "HTTPS")
  Rel(os, pgos, "JPA", "5432")
  Rel(bill, pgbill, "JPA", "5432")
  Rel(inv, pginv, "JPA", "5432")
  Rel(shop, mongo, "Spring Data", "27017")
```

### Topologia de implantação (AWS / EKS)

```mermaid
flowchart TB
  internet(("Internet"))
  subgraph aws["AWS — Terraform (fiap-tc-mecanica-infra-*)"]
    nlb["NLB (internet-facing)"]
    subgraph eks["EKS — namespace mecanica-ms"]
      traefik["Traefik (IngressRoute)"]
      os["os-service"]
      bill["billing-service"]
      inv["inventory-service"]
      shop["workshop-service"]
      rmq["RabbitMQ (Helm)"]
      mongo["MongoDB (Helm)"]
      dbinit["Job db-init"]
    end
    rds[("RDS PostgreSQL\n3 bancos lógicos")]
    ecr["ECR (4 repos)"]
    sm["Secrets Manager"]
  end

  internet --> nlb --> traefik
  traefik --> os & bill & inv & shop
  os <--> rmq
  bill <--> rmq
  inv <--> rmq
  shop <--> rmq
  os --> rds
  bill --> rds
  inv --> rds
  shop --> mongo
  dbinit -. cria os_service/billing_service/inventory_service .-> rds
```

## Consequências

**Positivas**
- Deploy, escala e evolução independentes por serviço (repos + CI/CD próprios).
- Isolamento de falhas e de dados (database-per-service).
- Fronteiras de domínio explícitas; times podem trabalhar em paralelo.

**Negativas / custos**
- Complexidade operacional maior (rede, mensageria, observabilidade distribuída).
- Consistência **eventual** — sem transação ACID cruzando serviços; exige Saga e
  compensações ([ADR 0002](0002-saga-orquestrada-rabbitmq.md)).
- Depuração distribuída exige correlação de logs/traces.

## Alternativas consideradas

- **Manter o monólito modular** — rejeitado: não atende ao requisito de
  microsserviços independentes com repositório/infra/banco próprios.
- **Apenas 3 serviços** (juntar workshop ao os) — rejeitado: workshop tem modelo
  de dados e ciclo próprios (execução física), e a separação evidencia melhor a
  Saga e a persistência poliglota.
