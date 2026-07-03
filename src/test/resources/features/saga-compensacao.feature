# language: pt
Funcionalidade: Saga de compensação
  Como sistema de orquestração
  Quero que a Saga compense corretamente em falhas
  Para manter a consistência dos dados

  Contexto:
    Dado que estou autenticado como "admin@mecanica.com" com senha "123456"

  Cenário: OS é cancelada quando pagamento é recusado
    Dado uma OS no status "AGUARDANDO_APROVACAO"
    Quando o billing recusa o pagamento da OS
    Então a OS deve estar com status "CANCELADA"

  Cenário: OS é cancelada quando o workshop falha na execução
    Dado uma OS no status "AGUARDANDO_APROVACAO"
    Quando o billing confirma o pagamento da OS
    E o inventory confirma a reserva de peças da OS
    E o workshop informa falha na execução da OS
    Então a OS deve estar com status "CANCELADA"
