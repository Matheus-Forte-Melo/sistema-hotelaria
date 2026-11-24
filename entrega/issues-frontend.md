# Pernoite — Issues de frontend

Cada item detalha a parte React da issue correspondente do [plano de desenvolvimento](divisao-de-atividades.md). Cada item aponta para as duas issues no GitHub. A home e o healthcheck já servem de ponto de partida. As novas telas ficam em `front/src/features/`, usam Tailwind CSS e acessam os dados pelos contextos. Começar com os hooks e repositórios mockados e integrar cada operação quando a API estiver disponível. Recomendamos [TanStack Query e o fluxo com mocks](../docs/contribuicao.md#trabalhar-enquanto-a-api-está-em-desenvolvimento).

## FRONT-01 — Cadastrar e listar andares

**Jeferson Eduardo** · [Backend #1](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/1) · [Frontend #6](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/6) · `features/andares/`

- **Tela.** Campo de número, botão Cadastrar e lista ordenada. Usar o contexto de estrutura e acrescentar a operação de cadastro ao repositório.
- **Aceite.** Cadastrar o andar 2, ver a lista atualizada e receber uma mensagem ao repetir o número. Com a API integrada, o andar continua visível ao recarregar.

## FRONT-02 — Cadastrar quartos e buscar disponibilidade

**Luan Elizeu** · [Backend #2](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/2) · [Frontend #7](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/7) · `features/quartos/`

- **Tela.** Selecionar um andar da FRONT-01, informar identificação, capacidade e diária. Na busca, informar entrada, saída e número de pessoas. Mostrar os quartos retornados e uma mensagem quando a lista estiver vazia.
- **Aceite.** Buscar três vagas exclui o quarto de duas. Uma reserva de 10 a 12 impede entrada no dia 11 e permite entrada no dia 12. Disponibilizar a seleção de quarto para a FRONT-03.

## FRONT-03 — Criar e editar uma reserva

**Matheus Kuchenbecker** · [Backend #3](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/3) · [Frontend #8](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/8) · `features/reservas/formulario/`

- **Tela.** Reutilizar o formulário nos modos criar e editar. Exibir nome, período, pessoas, quarto, diária e total previsto. Usar a busca da FRONT-02 e encaminhar a gravação pelo contexto de reservas.
- **Aceite.** Uma reserva de duas noites mostra duas diárias. Ao editar, recuperar os valores vigentes. Se a API recusar por capacidade ou por concorrência, manter os campos preenchidos e mostrar o motivo.

## FRONT-04 — Buscar, consultar e cancelar reservas

**Geison** · [Backend #4](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/4) · [Frontend #9](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/9) · `features/reservas/consulta/`

- **Tela.** Busca pelo nome, lista com quarto e período e painel de detalhes. Abrir a FRONT-03 para edição e pedir confirmação antes de cancelar.
- **Aceite.** Duas reservas de um mesmo nome podem ser distinguidas. Desistir mantém a situação vigente. Confirmar exibe “Cancelada” após a resposta da API e preserva a reserva na consulta.

## FRONT-05 — Navegar no mapa temporal

**Matheus Forte de Melo** · [Backend #5](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/5) · [Frontend #10](https://github.com/Matheus-Forte-Melo/sistema-hotelaria/issues/10) · `features/mapa/`

- **Tela.** Seletor de andar e data, dia anterior, próximo dia e Hoje. Exibir grade com todos os quartos do andar, situação por texto e cor, total reservado e detalhes do quarto selecionado.
- **Aceite.** Com reservas de 10 a 12 e de 12 a 14 no mesmo quarto, mudar do dia 11 para o dia 12 troca o nome no painel. Cancelar pela FRONT-04 e consultar o mapa novamente atualiza grade e total.

As telas podem começar em paralelo usando os mocks. Para a integração real, seguir a sequência andares → quartos → gravação de reservas → consulta e cancelamento → mapa. Cada responsável amplia o repositório e o contexto apenas com as operações usadas pela sua tela.
