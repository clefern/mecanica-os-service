# language: pt
Funcionalidade: Saga de aprovação — fluxo feliz
  Como sistema de orquestração
  Quero que a Saga complete com pagamento aprovado
  Para entregar o veículo ao cliente

  Contexto:
    Dado que estou autenticado como "admin@mecanica.com" com senha "123456"

  Cenário: Saga completa — OS chega a ENTREGUE
    Dado uma OS no status "AGUARDANDO_APROVACAO"
    Quando o billing confirma o pagamento da OS
    E o inventory confirma a reserva de peças da OS
    E o workshop conclui a execução do reparo da OS
    Então a OS deve estar com status "ENTREGUE"

  Cenário: Fluxo completo via endpoints até AGUARDANDO_APROVACAO
    Quando abro uma OS para o cliente "00000000-0000-0000-0000-000000000010" e veículo "00000000-0000-0000-0000-000000000020" e mecânico "00000000-0000-0000-0000-000000000002"
    E adiciono o item com referência "10000000-0000-0000-0000-000000000001" tipo "PECA" valor 45.90 quantidade 2
    E inicio o diagnóstico da OS
    E emito o orçamento da OS
    Então a OS deve estar com status "AGUARDANDO_APROVACAO"
