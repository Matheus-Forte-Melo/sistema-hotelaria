# Pernoite — Redefinição de escopo da entrega

**Data da revisão** — 24/11/2026

Após consulta ao professor Leonardo Vitazik, a equipe redefiniu o escopo para viabilizar a entrega acadêmica. A orientação recebida foi concentrar o projeto em um CRUD com regras de negócio e uma visualização mais avançada. Para o Pernoite, a entrega passa a ser **gerenciar reservas de quartos e consultar essas reservas em um mapa temporal por andar**.

Este documento define o que será demonstrado e avaliado nesta entrega. O [escopo original](../docs/escopo.md) e os [requisitos iniciais](../docs/legado/requisitos.md) registram a proposta anterior.

## Artefatos da revisão

Os [requisitos vigentes](../docs/requisitos.md) associam cada funcionalidade às issues de backend e frontend. Os [diagramas UML revisados](../docs/uml/README.md) representam os casos de uso da recepção, as classes do domínio e a sequência de consulta do mapa temporal. O [índice do legado](../docs/legado/README.md) explica a mudança e reúne os requisitos e diagramas originais.

## Funcionalidades da entrega

| Área | O que a recepção poderá fazer |
|---|---|
| Estrutura do hotel | Cadastrar andares e quartos. Cada quarto pertence a um andar e possui identificação, capacidade e valor padrão da diária. A navegação começa pelo andar. |
| Disponibilidade | Informar entrada, saída e quantidade de pessoas para consultar os quartos que comportam o grupo e estão livres durante todo o período. |
| Reservas | Criar, consultar, editar e cancelar uma reserva. Registrar o nome do hóspede para localizar a reserva, quarto, datas, quantidade de pessoas e diária acordada. O cancelamento preserva o registro para consulta. |
| Mapa temporal | Escolher um andar e uma data, avançar ou voltar um dia e retornar a hoje. A grade identifica cada quarto como livre ou reservado por texto e cor. Ao selecionar um quarto, a recepção vê os dados da reserva correspondente à data. O resumo informa quantos quartos do andar estão reservados. |

## Regras de negócio

1. Cada reserva corresponde a um quarto. A quantidade de pessoas deve ser positiva e respeitar a capacidade do quarto.
2. A saída deve ser posterior à entrada. A reserva ocupa as noites a partir da entrada até a véspera da saída. Uma reserva de 10 a 12 permite outra entrada no dia 12.
3. Duas reservas vigentes do mesmo quarto não podem ocupar a mesma noite. A regra vale também para solicitações simultâneas. Ao editar quarto, datas ou quantidade de pessoas, o sistema revalida os dados, desconsiderando a própria reserva na busca de conflitos.
4. O cancelamento libera o período para nova reserva. O registro cancelado continua consultável e deixa de compor a grade e o total de quartos reservados.
5. O valor previsto corresponde ao número de noites multiplicado pela diária acordada. Alterar a diária padrão do quarto preserva o preço já combinado.
6. Para qualquer data, inclusive passada ou atual, o mapa mostra a situação das **reservas** segundo os registros disponíveis na consulta. A presença efetiva de hóspedes exigiria registros de entrada e saída, que ficam fora desta entrega.

## Redução em relação à proposta inicial

| Tema da proposta original | Decisão para esta entrega |
|---|---|
| Blocos (RF03, RF13) | A estrutura visível passa a ser andar → quarto. Ficam excluídos o cadastro de novos blocos e o seletor de bloco. O modelo existente pode manter um bloco fixo interno para vincular os andares. |
| Hóspedes e faixas etárias (RF06, RF09, RF17, RF18) | A reserva usa identificação simples do hóspede e quantidade de pessoas. Saem o cadastro individual de todos os ocupantes, o histórico por pessoa, a exigência de adulto por quarto e os cálculos de idade. |
| Indisponibilidades agendadas (RF05) | Saem os bloqueios por período, seus motivos e correções. A disponibilidade para reserva depende da capacidade do quarto e das reservas vigentes no período. |
| Operação da estadia (RF10, RF11, RF19, RF20) | Check-in, check-out, presença real, pendências operacionais e correções de estadias encerradas ficam fora da demonstração. |
| Administração de acesso (RF01, RF02) | Gestão de funcionários e implementação de perfis de acesso ficam fora dos critérios desta entrega. |

## Critérios de aceite para a demonstração

- Cadastrar um andar e dois quartos de capacidades diferentes sem precisar criar ou escolher um bloco.
- Criar uma reserva de 10 a 12 para duas pessoas. Conferir o valor de duas diárias e localizar a reserva pelo nome do hóspede.
- Tentar reservar o mesmo quarto de 11 a 13 e verificar a recusa por conflito. Criar outra reserva com entrada no dia 12 e verificar que a troca é permitida.
- Tentar mover uma reserva para um quarto de capacidade insuficiente e verificar que a alteração é recusada e a reserva original é preservada.
- Navegar pelos dias 10, 11 e 12. Conferir na grade, no resumo do andar e no painel do quarto qual reserva corresponde a cada noite.
- Alterar o período de uma reserva e conferir a atualização no mapa. Cancelá-la, consultar o registro cancelado e usar o período liberado em uma nova reserva.
