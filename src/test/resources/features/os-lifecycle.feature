# language: pt
Funcionalidade: Ciclo de vida da Ordem de Serviço
  Como atendente da mecânica
  Quero gerenciar o ciclo de vida de uma OS
  Para controlar o fluxo de atendimento ao cliente

  Contexto:
    Dado que estou autenticado como "admin@mecanica.com" com senha "123456"

  Cenário: Abrir uma nova OS com mecânico atribuído
    Quando abro uma OS para o cliente "00000000-0000-0000-0000-000000000010" e veículo "00000000-0000-0000-0000-000000000020" e mecânico "00000000-0000-0000-0000-000000000002"
    Então a OS é criada com status "RECEBIDA"
    E o código da OS começa com "OS-"

  Cenário: Adicionar peça a uma OS
    Dado uma OS no status "RECEBIDA"
    Quando adiciono o item com referência "10000000-0000-0000-0000-000000000002" tipo "PECA" valor 189.90 quantidade 1
    Então o valor total da OS é 189.90

  Cenário: Avançar OS para diagnóstico
    Dado uma OS no status "RECEBIDA"
    Quando inicio o diagnóstico da OS
    Então a OS avança para o status "EM_DIAGNOSTICO"

  Cenário: Emitir orçamento dispara a Saga
    Dado uma OS no status "EM_DIAGNOSTICO" com um item adicionado
    Quando emito o orçamento da OS
    Então a OS avança para o status "AGUARDANDO_APROVACAO"

  Cenário: Cancelar uma OS ativa
    Dado uma OS no status "RECEBIDA"
    Quando cancelo a OS
    Então a OS avança para o status "CANCELADA"

  Cenário: Não é possível adicionar item em OS aguardando aprovação
    Dado uma OS no status "AGUARDANDO_APROVACAO"
    Quando tento adicionar um item à OS
    Então recebo resposta com status HTTP 422
