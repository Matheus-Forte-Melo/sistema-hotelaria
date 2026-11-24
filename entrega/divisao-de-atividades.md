# Pernoite — Divisão de atividades por integrante

Cada funcionalidade tem uma issue de backend e outra de frontend, ligadas entre si. O backend segue a [arquitetura em camadas](../docs/documentacao-tecnica.md#3-arquitetura-e-modelagem-de-dados). Consulte também o [escopo](redefinicao-de-escopo.md), os [critérios de frontend](issues-frontend.md) e o [guia de contribuição](../docs/contribuicao.md).

| Backend | Frontend | Responsável | Funcionalidade |
|---|---|---|---|
| [#1](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/1) | [#6](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/6) | Jeferson Eduardo | Cadastrar e listar andares |
| [#2](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/2) | [#7](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/7) | Luan Elizeu | Cadastrar quartos e consultar disponibilidade |
| [#3](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/3) | [#8](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/8) | Matheus Kuchenbecker | Criar e editar reservas com validação |
| [#4](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/4) | [#9](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/9) | Geison | Localizar, consultar e cancelar reservas |
| [#5](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/5) | [#10](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/10) | Matheus Forte de Melo | Navegar no mapa temporal de reservas |

## Ordem recomendada de desenvolvimento

Antes de abrir as branches das funcionalidades, combinar os campos de andar, quarto e reserva que as telas receberão da API. A home e os contextos já estão em `front/`. A sugestão é que Matheus Forte de Melo coordene as rotas de navegação. Depois disso, cada integrante pode trabalhar na pasta da sua tela.

| Ordem de integração | Quem e o que integrar | O que pode avançar em paralelo |
|---|---|---|
| 1. Andares | Jeferson entrega a issue 1. A lista de andares passa a alimentar o cadastro de quartos e o mapa. | Luan prepara o formulário de quartos. Matheus Forte já pode desenhar a grade e a navegação por datas com dados de exemplo. |
| 2. Quartos e disponibilidade | Luan integra a issue 2 usando os andares cadastrados. A busca passa a fornecer os quartos disponíveis para o formulário de reserva. | Matheus Kuchenbecker prepara as regras e o formulário de reserva. Geison monta a tela de consulta com um exemplo dos campos combinados. |
| 3. Criação e edição | Matheus Kuchenbecker integra a issue 3, incluindo os ajustes de nome do hóspede em `Reserva` e a validação ao salvar. | Matheus Forte conecta andares e quartos ao mapa. Geison prepara a navegação entre consulta e edição. |
| 4. Consulta e cancelamento | Geison atualiza sua branch com a issue 3 e integra a issue 4. A equipe já consegue percorrer o CRUD completo. | Matheus Forte liga o mapa às reservas gravadas e confere a troca de hóspedes no dia de saída. |
| 5. Mapa temporal | Matheus Forte integra a issue 5 e demonstra como criar, editar e cancelar uma reserva altera a grade e o resumo do andar. | A equipe confere os cenários de aceite com os mesmos quartos e reservas. |

O principal ponto de conflito é o arquivo `ReservaRepository.java`, usado nas issues 2 a 5. Cada integrante deve reaproveitar as consultas já disponíveis. Se duas issues precisarem acrescentar consultas nesse arquivo, integrar a primeira e atualizar a branch da seguinte antes da segunda edição. A mesma ordem vale para mudanças no serviço ou controlador de reservas compartilhado por Matheus Kuchenbecker e Geison.

## Issue 1 — Cadastrar e listar andares

**Responsável:** Jeferson Eduardo · [Backend #1](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/1) · [Frontend #6](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/6)

**Implementação.** Receber o número do andar no controller, associar o bloco fixo internamente e persistir usando AndarRepository. Retornar os andares ordenados e informar duplicidade pela API.

No backend, usar `model/Andar`, `repository/AndarRepository` e um `AndarController` no pacote `controller`.

### Critérios de aceite

- [ ] Criar e consultar o andar 2 pela API. A resposta da listagem está ordenada pelo número.
- [ ] O backend associa o bloco fixo e rejeita um número de andar duplicado.

- [ ] Cobrir o comportamento entregue com testes unitários.

## Issue 2 — Cadastrar quartos e consultar disponibilidade

**Responsável:** Luan Elizeu · [Backend #2](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/2) · [Frontend #7](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/7)

**Implementação.** Receber andar, identificação, capacidade e diária no cadastro. Consultar disponibilidade por entrada, saída e pessoas considerando todas as noites do período.

No backend, usar `model/Quarto`, os repositórios de quartos e reservas em `repository`, um `QuartoController` em `controller` e um `DisponibilidadeService` em `service`.

### Critérios de aceite

- [ ] Cadastrar no mesmo andar um quarto de duas vagas e outro de quatro. Uma busca para três pessoas apresenta apenas o de quatro quando ambos estão livres.
- [ ] Uma reserva vigente de 10 a 12 retira o quarto da busca de 11 a 13. O quarto volta a aparecer para entrada no dia 12 se o novo período estiver livre.
- [ ] Reservas canceladas liberam o quarto na busca. A consulta orienta a corrigir períodos inválidos ou uma quantidade de pessoas menor que um.

- [ ] Cobrir o comportamento entregue com testes unitários.

## Issue 3 — Criar e editar reservas

**Responsável:** Matheus Kuchenbecker · [Backend #3](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/3) · [Frontend #8](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/8)

**Implementação.** Persistir nome de referência, quarto, período, pessoas e diária acordada. Validar capacidade e conflito na mesma transação da gravação, também na edição.

No backend, usar `model/Reserva`, `repository/ReservaRepository`, `repository/QuartoRepository` e um `ReservaService` em `service`. No pacote `controller`, expor as operações de criação e edição.

### Critérios de aceite

- [ ] Criar uma reserva de 10 a 12 para duas pessoas usando apenas o nome de referência do hóspede. O total corresponde a duas diárias e preserva o preço combinado se a diária padrão do quarto mudar.
- [ ] Recusar saída anterior ou igual à entrada, quantidade inferior a um ou superior à capacidade e sobreposição de reservas. Em duas tentativas simultâneas para o mesmo quarto e período, apenas uma é confirmada.
- [ ] Ao editar, desconsiderar a própria reserva na busca de conflitos. Uma mudança válida atualiza o período e o total. Uma tentativa inválida mantém a reserva anterior.

- [ ] Cobrir o comportamento entregue com testes unitários.

## Issue 4 — Localizar, consultar e cancelar reservas

**Responsável:** Geison · [Backend #4](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/4) · [Frontend #9](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/9)

**Implementação.** Expor busca por nome, detalhes por identificador e cancelamento. Preservar o registro cancelado e liberar o período para outra reserva.

No backend, usar `model/Reserva` e `repository/ReservaRepository`. Nos pacotes `controller` e `service`, implementar a busca, a consulta de detalhes e o cancelamento.

### Critérios de aceite

- [ ] Buscar duas reservas de mesmo nome e distingui-las por quarto e período. Consultar quantidade de pessoas, diária e total.
- [ ] Cancelar por identificador e consultar novamente a reserva com a situação Cancelada.
- [ ] Após o cancelamento, permitir nova reserva no período liberado.

- [ ] Cobrir o comportamento entregue com testes unitários.

## Issue 5 — Visualizar reservas no mapa temporal

**Responsável:** Matheus Forte de Melo · [Backend #5](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/5) · [Frontend #10](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/10)

**Implementação.** Receber andar e data e retornar todos os quartos do andar com situação, detalhes da reserva vigente e total de reservados para a noite consultada.

No backend, usar os repositórios de quartos e reservas em `repository`, um `MapaService` em `service` e um `MapaController` em `controller`.

### Critérios de aceite

- [ ] Incluir os quartos livres e reservados do andar consultado e o total coerente com a lista.
- [ ] Para reservas sucessivas de 10 a 12 e de 12 a 14, retornar a primeira no dia 11 e a segunda no dia 12.
- [ ] Refletir edições e cancelamentos nas consultas seguintes e usar o mesmo critério de pernoite para qualquer data.

- [ ] Cobrir o comportamento entregue com testes unitários.

## Pontos de integração

A lista de andares da issue 1 alimenta o cadastro de quartos e o mapa. A issue 2 fornece a busca usada no formulário da issue 3. As issues 3 e 4 compartilham os campos da reserva e as situações “Vigente” e “Cancelada” exibidas na interface. As consultas de disponibilidade e do mapa seguem a mesma convenção de pernoite usada na gravação: a noite da entrada está incluída e a noite da saída fica livre para outra reserva.
