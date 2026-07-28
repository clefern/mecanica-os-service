# Architecture Decision Records (ADR)

Registro das decisões arquiteturais da **Fase 4 — Mecânica API** (Grupo 14SOAT).
Cada ADR documenta o contexto, a decisão tomada, as consequências e as
alternativas consideradas. Formato baseado em [MADR](https://adr.github.io/madr/).

| # | Decisão | Status |
|---|---------|--------|
| [0001](0001-arquitetura-microsservicos-c4.md) | Arquitetura de microsserviços (C4 + topologia) | Aceito |
| [0002](0002-saga-orquestrada-rabbitmq.md) | Coordenação da transação distribuída — Saga orquestrada via RabbitMQ | Aceito |
| [0003](0003-mongodb-workshop.md) | Persistência poliglota — MongoDB no workshop-service | Aceito |
| [0004](0004-banco-por-servico-rds-compartilhado.md) | Banco por serviço em instância RDS compartilhada | Aceito |

> Uma síntese destas decisões, pronta para apresentação, está em
> [`../apresentacao-arquitetura.pdf`](../apresentacao-arquitetura.pdf).
