# ADR 0004 — Banco por serviço em instância RDS compartilhada

- **Status:** Aceito
- **Data:** 2026-07
- **Relacionado:** [ADR 0001](0001-arquitetura-microsservicos-c4.md), [ADR 0003](0003-mongodb-workshop.md)

## Contexto

O desafio exige que cada microsserviço tenha **seu próprio banco de dados**. O
ambiente de avaliação é o **AWS Academy Learner Lab**, com **quotas limitadas**,
credenciais temporárias e sessões curtas — o custo/tempo de provisionamento
importa. Os três serviços SQL (os, billing, inventory) usam PostgreSQL; o
workshop usa MongoDB ([ADR 0003](0003-mongodb-workshop.md)).

## Decisão

**Isolamento lógico numa única instância RDS PostgreSQL** para os serviços SQL:

- Uma instância `db.t3.small` (Terraform `fiap-tc-mecanica-infra-db`) hospeda três
  bancos **isolados**: `os_service`, `billing_service`, `inventory_service`. Cada
  serviço conecta apenas ao seu banco.
- Os bancos são criados por um **Job Kubernetes idempotente** (`db-init-job.yaml`)
  que roda **dentro do cluster** — o RDS é privado (`publicly_accessible = false`)
  e o Job, estando na mesma VPC, alcança o banco sem expô-lo à internet.
- **MongoDB in-cluster** via Helm (release `mecanica-mongodb`) para o workshop.

"Próprio banco de dados" = banco lógico próprio + credenciais/conexão próprias,
sem compartilhamento de schema entre serviços.

## Justificativa

- **Mais barato e rápido de subir** que 3 instâncias — decisivo no lab limitado.
- **Sem ginástica de rede:** o Job in-cluster evita tornar o RDS público só para
  o Terraform (provider `postgresql`) criar bancos a partir de fora da VPC.
- **Idempotente:** o Job só cria o banco se ainda não existir — seguro reexecutar.

## Consequências

**Positivas**
- Um único `terraform apply`, uma instância para operar; custo mínimo.
- Isolamento por banco + credenciais atende ao requisito.

**Negativas / limites**
- Isolamento **lógico**, não físico — CPU/IO/armazenamento são compartilhados
  (aceitável para avaliação; não é a topologia de produção ideal).
- Criação de bancos fica fora do estado do Terraform (é um Job de runtime).

## Alternativas consideradas

- **3 instâncias RDS separadas** — isolamento físico e "própria infra" mais
  forte, porém ~3× custo e risco de estourar quota do AWS Academy. Trocável no
  futuro sem mudar os serviços (só o host por serviço).
- **Provider `postgresql` no Terraform** — criaria os bancos como IaC, mas o
  runner (laptop/CI) não alcança o RDS privado; exigiria expor o RDS
  publicamente. Rejeitado por segurança/atrito.
- **PostgreSQL in-cluster (StatefulSet)** — remove o RDS, mas perde o uso de banco
  gerenciado (menos aderente ao objetivo de IaC de infra gerenciada).

## Implementação

- `k8s/db-init-job.yaml` — Job que cria os 3 bancos (validado localmente: criação
  + idempotência contra `postgres:16`).
- `k8s/secrets-setup.sh` — `rds-admin-secret` (usado pelo Job) e um `db-url` por
  serviço apontando para o mesmo host e o banco correspondente; `mongodb-uri`
  aponta para o serviço in-cluster.
- Ver [`../../k8s/README.md`](../../k8s/README.md) para o fluxo de bootstrap.
