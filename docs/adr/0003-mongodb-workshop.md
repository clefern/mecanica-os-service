# ADR 0003 — Persistência poliglota: MongoDB no workshop-service

- **Status:** Aceito
- **Data:** 2026-07
- **Relacionado:** [ADR 0001](0001-arquitetura-microsservicos-c4.md), [ADR 0004](0004-banco-por-servico-rds-compartilhado.md)

## Contexto

Com **database-per-service**, cada serviço escolhe a tecnologia de persistência
que melhor atende ao seu modelo. O **workshop-service** registra a execução
física do reparo: um agregado auto-contido por execução, com estrutura flexível e
aninhada (checklist de itens, apontamentos do mecânico, histórico de status,
eventuais anexos) que evolui com o tempo e varia entre tipos de serviço.

Esse dado é lido/gravado sempre pelo próprio agregado (a execução), **não exige
joins** com outros contextos e não participa de relatórios relacionais.

Os demais serviços (os, billing, inventory) têm dados **relacionais e
transacionais** — entidades bem definidas, integridade referencial, agregações e
consultas por múltiplos critérios — bem servidos por PostgreSQL.

## Decisão

Adotar **persistência poliglota**:

- **MongoDB** (orientado a documentos) no **workshop-service**.
- **PostgreSQL** nos demais (os, billing, inventory).

No workshop, coleções `execucoes`, `mecanicos` e `processed_commands` (esta
última para idempotência de comandos da Saga — ver [ADR 0002](0002-saga-orquestrada-rabbitmq.md)).

## Justificativa

- **Aderência ao modelo:** documento único mapeia o agregado de execução sem
  ORM/joins; campos flexíveis acomodam variações sem migrações rígidas.
- **Requisito do desafio:** demonstra explicitamente o uso de banco **NoSQL**
  além do relacional, evidenciando a liberdade tecnológica dos microsserviços.
- **Simplicidade de escrita/leitura** do agregado por `os_id`.

## Consequências

**Positivas**
- Modelo de dados natural para a execução; evolução de schema sem fricção.
- Isolamento tecnológico: escolha do workshop não impacta os demais serviços.

**Negativas**
- Mais um motor de banco para operar/monitorar (poliglota aumenta a superfície).
- Sem integridade referencial nativa entre coleções — garantida na aplicação.
- Time precisa dominar dois paradigmas (relacional + documento).

## Alternativas consideradas

- **Tudo em PostgreSQL (coluna `JSONB`)** — viável, mas descaracteriza o requisito
  de NoSQL e mistura paradigmas num mesmo motor sem ganho para este agregado.
- **DynamoDB** — bom fit gerenciado, porém restrito/custoso no AWS Academy e sem
  o mesmo suporte local no docker-compose para desenvolvimento.

## Nota de implantação

Em cluster o MongoDB roda **in-cluster** via Helm (Bitnami, release
`mecanica-mongodb`), evitando custo/atrito de um serviço gerenciado no ambiente
limitado do AWS Academy. Detalhes em [ADR 0004](0004-banco-por-servico-rds-compartilhado.md).
