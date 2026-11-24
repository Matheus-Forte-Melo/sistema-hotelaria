# Pernoite — Divisão de atividades por integrante

**Proposta para revisão** · Baseada na [redefinição de escopo](redefinicao-de-escopo.md).

As [issues de frontend](issues-frontend.md) detalham a parte visual de cada entrega. Cada issue entrega uma funcionalidade completa no monorepo, da operação na tela à leitura ou gravação no banco. Os pacotes Java abaixo ficam em `br.com.hotelaria.hotelaria` e seguem a organização atual. Para o frontend, a proposta é agrupar as telas por funcionalidade em `front/src/features/`. Os nomes dos componentes e serviços servem como esqueleto.

| Issue | Responsável | Entrega demonstrável |
|---|---|---|
| 1 | Jeferson Eduardo | Cadastrar e listar andares |
| 2 | Luan Elizeu | Cadastrar quartos e consultar disponibilidade |
| 3 | Matheus Kuchenbecker | Criar e editar reservas com validação |
| 4 | Geison | Localizar, consultar e cancelar reservas |
| 5 | Matheus Forte de Melo | Navegar no mapa temporal de reservas |

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

**Responsável:** Jeferson Eduardo

**Objetivo.** A recepção cadastra um andar pelo número e o encontra na lista usada pelas telas de quartos e do mapa.

**Onde trabalhar.** No backend, usar `model/Andar`, `repository/AndarRepository` e um `AndarController` no pacote `controller`. No frontend, usar `front/src/features/andares/`.

**Esqueleto.** Campo “Número do andar” → botão “Cadastrar” → lista de andares em ordem numérica. O backend associa o andar ao bloco fixo interno exigido pelo modelo.

**Aceite.**

- [ ] Cadastrar o andar 2 pela tela, recarregar e encontrá-lo na lista.
- [ ] O cadastro funciona sem criar ou selecionar bloco e avisa se o andar já existe.

## Issue 2 — Cadastrar quartos e consultar disponibilidade

**Responsável:** Luan Elizeu

**Objetivo.** A recepção cadastra os quartos de cada andar e encontra aqueles que comportam um grupo durante todo o período solicitado.

**Onde trabalhar.** No backend, usar `model/Quarto`, os repositórios de quartos e reservas em `repository`, um `QuartoController` em `controller` e um `DisponibilidadeService` em `service`. No frontend, usar `front/src/features/quartos/`.

**Esqueleto.** O formulário de quarto recebe andar, identificação, capacidade e diária padrão. A consulta recebe entrada, saída e quantidade de pessoas. A lista de resultados mostra andar, quarto, capacidade e diária. O formulário de reserva da issue 3 pode reutilizar essa consulta.

**Aceite.**

- [ ] Cadastrar no mesmo andar um quarto de duas vagas e outro de quatro. Uma busca para três pessoas apresenta apenas o de quatro quando ambos estão livres.
- [ ] Uma reserva vigente de 10 a 12 retira o quarto da busca de 11 a 13. O quarto volta a aparecer para entrada no dia 12 se o novo período estiver livre.
- [ ] Reservas canceladas liberam o quarto na busca. A consulta orienta a corrigir períodos inválidos ou uma quantidade de pessoas menor que um.

## Issue 3 — Criar e editar reservas

**Responsável:** Matheus Kuchenbecker

**Objetivo.** A recepção registra uma reserva e consegue mudar suas datas, quarto ou quantidade de pessoas preservando as regras de disponibilidade.

**Onde trabalhar.** No backend, usar `model/Reserva`, `repository/ReservaRepository`, `repository/QuartoRepository` e um `ReservaService` em `service`. No pacote `controller`, expor as operações de criação e edição. No frontend, usar `front/src/features/reservas/formulario/`.

**Esqueleto.** Formulário de criação e edição → nome do hóspede, entrada, saída, quantidade de pessoas, quarto e diária acordada → total previsto → salvar. Adequar o modelo para guardar o nome de referência da reserva. Ao salvar, o backend confere período, capacidade e conflitos mesmo que o quarto estivesse livre na busca inicial.

**Aceite.**

- [ ] Criar uma reserva de 10 a 12 para duas pessoas usando apenas o nome de referência do hóspede. O total corresponde a duas diárias e preserva o preço combinado se a diária padrão do quarto mudar.
- [ ] Recusar saída anterior ou igual à entrada, quantidade inferior a um ou superior à capacidade e sobreposição de reservas. Em duas tentativas simultâneas para o mesmo quarto e período, apenas uma é confirmada.
- [ ] Ao editar, desconsiderar a própria reserva na busca de conflitos. Uma mudança válida atualiza o período e o total. Uma tentativa inválida mantém a reserva anterior.

## Issue 4 — Localizar, consultar e cancelar reservas

**Responsável:** Geison

**Objetivo.** A recepção encontra uma reserva pelo nome do hóspede, consulta o que foi combinado e, se necessário, cancela a reserva.

**Onde trabalhar.** No backend, usar `model/Reserva` e `repository/ReservaRepository`. Nos pacotes `controller` e `service`, implementar a busca, a consulta de detalhes e o cancelamento. No frontend, usar `front/src/features/reservas/consulta/`.

**Esqueleto.** Busca por nome → lista com quarto, período e situação → detalhes da reserva. Nos detalhes, “Editar” abre o formulário da issue 3. “Cancelar reserva” pede confirmação e atualiza a situação na tela.

**Aceite.**

- [ ] Buscar pelo nome e distinguir duas reservas do mesmo hóspede pelo quarto e pelo período. Os detalhes mostram quantidade de pessoas, diária acordada e total previsto.
- [ ] Desistir do cancelamento mantém a reserva vigente. Confirmar permite encontrá-la novamente com a situação “Cancelada”.
- [ ] Após o cancelamento, a consulta de disponibilidade libera o quarto para uma nova reserva no mesmo período.

## Issue 5 — Visualizar reservas no mapa temporal

**Responsável:** Matheus Forte de Melo

**Objetivo.** A recepção escolhe um andar e uma data para ver quais quartos estão reservados naquela noite e abrir os detalhes de uma reserva.

**Onde trabalhar.** No backend, usar os repositórios de quartos e reservas em `repository`, um `MapaService` em `service` e um `MapaController` em `controller`. No frontend, usar `front/src/features/mapa/`.

**Esqueleto.** Seletor de andar e navegação “Dia anterior / Hoje / Próximo dia”, com escolha direta de data → resumo do andar → grade de quartos → painel do quarto selecionado. A consulta deve trazer todos os quartos do andar, inclusive os livres. Cada cartão mostra a identificação do quarto e a situação “Livre” ou “Reservado” por texto e cor.

**Aceite.**

- [ ] Com uma reserva de 10 a 12 e outra de 12 a 14 no mesmo quarto, o painel mostra a primeira nos dias 10 e 11 e a segunda no dia 12.
- [ ] Mudar a data atualiza grade, resumo e painel para a mesma noite, mantendo o andar. O botão “Hoje” retorna à data atual. Trocar de andar limita a grade e a contagem aos quartos daquele andar.
- [ ] Após editar ou cancelar uma reserva, uma nova consulta reflete a mudança no cartão e no total de quartos reservados. Em datas passadas, a legenda continua indicando a situação da reserva.

## Pontos de integração

A lista de andares da issue 1 alimenta o cadastro de quartos e o mapa. A issue 2 fornece a busca usada no formulário da issue 3. As issues 3 e 4 compartilham os campos da reserva e as situações “Vigente” e “Cancelada” exibidas na interface. As consultas de disponibilidade e do mapa seguem a mesma convenção de pernoite usada na gravação: a noite da entrada está incluída e a noite da saída fica livre para outra reserva.
