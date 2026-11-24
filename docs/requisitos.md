# Pernoite — Requisitos após a revisão de escopo

**Referência da entrega acadêmica de 24/11/2026.** A recepção de um estabelecimento cadastra a estrutura, administra reservas e consulta a situação dos quartos por noite. Os identificadores RF foram preservados para manter a rastreabilidade com as issues. A numeração com intervalos reflete a redução aprovada no [documento de entrega](../entrega/redefinicao-de-escopo.md). A [especificação anterior](legado/requisitos.md) registra a proposta da Etapa 1.

## Requisitos funcionais

| ID | Comportamento esperado | Backend / frontend |
|---|---|---|
| RF03 | Cadastrar andares pelo número, impedir duplicidade, listá-los em ordem e associar cada quarto a um andar. A recepção inicia pelo andar. O bloco fixo, se necessário ao banco existente, é interno. | [#1](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/1) / [#6](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/6) |
| RF04 | Cadastrar quarto com andar, identificação, capacidade e diária padrão. | [#2](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/2) / [#7](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/7) |
| RF07 | Buscar quartos para entrada, saída e quantidade de pessoas. Retornar os que comportam o grupo durante todas as noites e indicar quando nenhum atende. | #2 / #7 |
| RF08 | Criar e editar reservas com nome de referência, quarto, entrada, saída, pessoas e diária acordada. Buscar pelo nome, identificar cada reserva por quarto e período, consultar detalhes e cancelar mediante confirmação na tela. Preservar o registro cancelado. | [#3](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/3) / [#8](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/8) e [#4](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/4) / [#9](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/9) |
| RF12 | Calcular o total previsto pelo número de noites e pela diária acordada, inclusive ao editar o período. | #3 / #8 |
| RF13 | Exibir todos os quartos do andar escolhido em uma grade, com identificação e situação “Livre” ou “Reservado” por texto e cor. | [#5](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/5) / [#10](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/10) |
| RF14 | Selecionar data, avançar ou voltar um dia e retornar a hoje. Manter o andar selecionado e atualizar grade, resumo e detalhes para a mesma noite. | #5 / #10 |
| RF15 | Ao selecionar um quarto, exibir a reserva vigente naquela noite ou indicar que está livre. | #5 / #10 |
| RF16 | Exibir o total de quartos reservados no andar e na data consultados. | #5 / #10 |

## Regras de negócio

1. Cada reserva ocupa um quarto. A quantidade de pessoas deve ser maior que zero e caber no quarto.
2. A saída deve ser posterior à entrada. A primeira noite é a da entrada, e a noite da saída fica disponível para outra reserva.
3. Criação e edição revalidam capacidade e disponibilidade ao salvar. A edição desconsidera a própria reserva. Solicitações simultâneas para o mesmo quarto e noite podem confirmar no máximo uma reserva. Uma alteração recusada preserva os valores gravados.
4. Cancelar libera o período. O registro permanece pesquisável com situação “Cancelada” e deixa de compor a grade de reservas vigentes e seu total.
5. O valor previsto é o número de noites multiplicado pela diária acordada. Alterar a tarifa padrão do quarto preserva o preço combinado.
6. O mapa retrata as reservas conforme os registros disponíveis na consulta, inclusive para datas passadas. A presença física de hóspedes fica fora desta entrega.

## Cenários de aceite

| Situação | Resultado esperado |
|---|---|
| Cadastrar os andares 1 e 2 e repetir o número 2 | A lista exibe 1 e 2 uma vez cada e informa a duplicidade. |
| Consultar três vagas com quartos livres de duas e quatro vagas | Apenas o quarto de quatro vagas aparece. |
| Reservar de 10 a 12 por R$ 180 a diária e tentar estender até 13, quando outra reserva começa no dia 12 | A reserva original mantém a saída no dia 12 e o total de R$ 360. |
| Duas pessoas tentam reservar simultaneamente a mesma noite do mesmo quarto | Apenas uma gravação é confirmada. |
| Ana reserva o 101 de 10 a 12 e Bruno de 12 a 14 | O mapa mostra Ana na noite do dia 11 e Bruno na noite do dia 12. |
| Cancelar a reserva de Bruno e consultar novamente o dia 12 | O 101 fica livre, o total do andar diminui em um e a busca por Bruno ainda encontra a reserva cancelada. |

Os [diagramas UML revisados](uml/README.md) mostram o domínio e a consulta temporal. O cadastro de blocos e ocupantes individuais, as regras etárias, os bloqueios agendados, o check-in, o check-out e a administração de funcionários ficaram na proposta anterior.
