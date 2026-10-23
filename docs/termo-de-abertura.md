# Termo de Abertura do Projeto

**Projeto:** Sistema de Gestão de Hospedagens com Mapa Temporal.  
**Nome comercial:** a definir.  
**Versão:** 1.1.  
**Data de elaboração:** 23/10/2026.  
**Situação:** minuta para revisão; aprovação ainda não registrada.

## 1. Identificação e responsáveis

| Papel | Responsável |
|---|---|
| Patrocinador ou solicitante | A definir |
| Responsável pelo projeto | A definir |
| Responsável pela validação funcional | A definir |
| Equipe de desenvolvimento | A definir |
| Representante dos usuários da recepção | A definir |

## 2. Justificativa

Pequenos hotéis e pousadas precisam controlar reservas, identificar hóspedes e acompanhar a disponibilidade dos quartos. Quando essas informações estão dispersas, a recepção tem dificuldade para enxergar a ocupação do estabelecimento, consultar visitas anteriores e planejar entradas e saídas.

O projeto propõe centralizar essas atividades em uma aplicação web interna. Seu diferencial é apresentar a estrutura do hotel de forma visual e permitir navegar entre datas, conectando a operação presente ao histórico realizado e ao planejamento futuro.

## 3. Objetivo geral

Entregar um sistema de gestão de hospedagens para um único hotel ou pousada, com gerenciamento de hóspedes, reservas, check-in e check-out e um mapa temporal de ocupação organizado por blocos, andares e quartos.

## 4. Objetivos específicos e sucesso esperado

| Objetivo | Evidência de sucesso |
|---|---|
| Centralizar a rotina de hospedagem | Executar um fluxo completo de reserva, identificação dos ocupantes, check-in e check-out. |
| Tornar a ocupação visualmente compreensível | Selecionar bloco e andar, consultar a grade de quartos com legenda e resumo do andar e abrir detalhes ao clicar em um quarto. |
| Permitir consulta em diferentes datas | Demonstrar ocupação passada, presença atual e previsão futura, identificando claramente cada tipo de informação. |
| Evitar inconsistências de disponibilidade | Aprovar os cenários de conflito de reservas, inclusive concorrentes, e limite de capacidade. |
| Preservar a relação com os hóspedes | Consultar estadias anteriores de cada hóspede utilizando o mesmo cadastro em visitas futuras. |

## 5. Descrição da solução e diferencial

A solução terá acesso autenticado para administradores e recepcionistas. Os usuários poderão cadastrar a estrutura física, consultar disponibilidade, registrar reservas e acompanhar estadias.

O **mapa temporal do hotel será a entrega prioritária de experiência e apresentação**, com uma visualização simples, semelhante a um mapa de reserva de cinema. Selecionar um bloco apresenta seus andares; clicar em um andar apresenta uma grade fixa de quartos. Cada quarto é um quadrado clicável com número, cor e ícone da situação e ocupação, como “2/4 pessoas”.

Ao clicar no quarto, um painel exibe hóspedes, datas, faixas etárias e ações disponíveis, sem sair do mapa. Os controles serão limitados a bloco, andar e data, com navegação de dia anterior, próximo dia e “Hoje”. O resumo principal mostrará apenas quartos ocupados e pessoas/capacidade do andar selecionado, sem percentuais, totais por bloco e hotel ou totais etários. A troca da data atualizará a grade, o resumo e os detalhes; datas passadas permitirão consulta histórica nesse painel.

O mapa distinguirá fatos históricos de previsões: passado baseado nas estadias realizadas; hoje baseado na presença efetiva e nas movimentações esperadas; futuro baseado nas reservas e saídas previstas. A navegação não implica reconstruir todas as versões antigas do planejamento.

## 6. Escopo resumido

### Incluído

- Autenticação e dois perfis de acesso.
- Funcionários, blocos, andares, quartos e indisponibilidades por período.
- Cadastro de hóspedes, sem distinção de papéis entre os ocupantes, e validação de pelo menos um adulto por quarto.
- Reservas, disponibilidade, check-in, check-out e valor previsto de diárias.
- Mapa temporal com seleção de bloco e andar, grade clicável de quartos, painel de detalhes e navegação por datas.
- Resumo de quartos ocupados e pessoas/capacidade do andar selecionado; faixas etárias apenas nos detalhes do quarto: crianças, adolescentes e adultos, sem categoria separada de idosos.
- Histórico de hospedagens por hóspede.
- Documentação, validação e instruções de execução e recuperação de dados.

### Excluído

- Acesso de hóspedes, canais externos de reserva e pagamentos.
- Gestão financeira, fiscal, de estoque, restaurante ou serviços extras.
- Redes de hotéis, reservas de múltiplos quartos e mudanças de quarto durante a estadia.
- Mapas 3D, plantas arquitetônicas e aplicativo móvel nativo.
- Auditoria completa e reprodução do planejamento como era conhecido no passado.

Os limites detalhados constam no [documento de escopo](escopo.md), e as funcionalidades e regras estão no [documento de requisitos](requisitos.md).

## 7. Entregas e marcos

| Marco | Entrega e condição de conclusão | Prazo |
|---|---|---|
| M1 — Definição | Escopo e requisitos revisados; responsáveis identificados. | A definir |
| M2 — Protótipo visual | Mapa demonstrável com seleção de bloco e andar, grade clicável, painel de detalhes e navegação por passado, hoje e futuro, validado pelo responsável funcional. | A definir |
| M3 — Base operacional | Acesso, estrutura física, hóspedes e reservas implementados, com validação de disponibilidade. | A definir |
| M4 — Hospedagens e mapa | Check-in, check-out, navegação temporal, indicadores e histórico integrados. | A definir |
| M5 — Homologação | Cenários essenciais aprovados e falhas impeditivas resolvidas. | A definir |
| M6 — Entrega | Aplicação executável no ambiente acordado, documentação e demonstração de restauração disponibilizadas. | A definir |

O protótipo do mapa precede a implementação completa para validar cedo o principal diferencial do projeto. Datas e duração serão acordadas após a definição da equipe e de sua disponibilidade.

## 8. Premissas

- O produto atenderá um estabelecimento por instalação.
- A equipe do hotel será responsável por registrar corretamente reservas, ocupantes, entradas e saídas.
- A visualização histórica depende dos registros existentes; períodos sem dados não terão ocupação presumida.
- O estabelecimento terá acesso à aplicação por navegador em computador ou tablet.
- Haverá um representante disponível para validar regras e o protótipo.
- Dados fictícios serão suficientes para desenvolvimento e demonstração inicial.

## 9. Restrições

- Manter o escopo centrado na recepção, no mapa temporal e no histórico de hóspedes.
- Não depender de integrações externas para concluir os fluxos principais.
- Aplicar uma convenção diária de pernoite em consultas passadas e futuras; não incluir uma linha do tempo por hora.
- Não tratar entradas e saídas individuais de hóspedes do mesmo quarto, hospedagens no mesmo dia ou transferências de quarto.
- Proteger vínculos históricos restringindo alterações estruturais de quartos já utilizados.
- Orçamento, equipe, prazo final e infraestrutura ainda não definidos; este termo não representa compromisso financeiro ou de entrega em uma data específica.

## 10. Riscos iniciais e respostas

| Risco | Impacto | Resposta planejada |
|---|---|---|
| Ampliação excessiva de funcionalidades | Atraso e perda de foco | Avaliar mudanças contra o objetivo do mapa e registrar impactos antes de incluí-las. |
| Confusão entre ocupação real e prevista | Decisões incorretas da recepção | Usar rótulos explícitos, legenda e validação do protótipo com cenários de passado, presente e futuro. |
| Sobreposição de reservas concorrentes | Dupla reserva do quarto | Aplicar controle transacional e teste de concorrência. |
| Dados incompletos dos ocupantes | Indicadores etários incorretos | Exigir identificação e data de nascimento no check-in, validar pelo menos um adulto por quarto e mostrar pendências nas previsões. |
| Saídas não registradas ou datas incorretas | Divergência entre mapa e operação | Sinalizar pendências e disponibilizar correção controlada de datas efetivas. |
| Alterações estruturais afetarem o histórico | Totais históricos inconsistentes | Preservar referências e restringir mudanças de localização e capacidade. |
| Interface visual ficar carregada | Dificuldade de uso | Exibir uma grade simples do andar selecionado e detalhes sob demanda, com controles apenas de bloco, andar e data; validar em computador e tablet. |
| Ausência de definições de equipe e prazo | Planejamento sem base | Definir responsáveis e estimativas antes de assumir uma data de entrega. |

## 11. Recursos e orçamento

Recursos previstos: equipe para análise, design, desenvolvimento e validação; repositório de código; ambiente de desenvolvimento e teste; banco relacional; e ambiente de hospedagem para disponibilização acordada.

O orçamento será estimado após a escolha da infraestrutura e a definição da equipe. Nenhum valor ou contratação está aprovado por este documento.

## 12. Gestão e controle de mudanças

O responsável pelo projeto coordenará atividades, acompanhará marcos e registrará decisões. O responsável pela validação funcional verificará o comportamento do sistema conforme o escopo e os requisitos.

Pedidos de mudança devem registrar descrição, justificativa e impactos em esforço, prazo e critérios de aceite. A inclusão será acordada com o solicitante, com atualização dos documentos afetados. Ajustes de implementação que não alterem o escopo podem ser decididos pela equipe técnica.

## 13. Critérios de encerramento

- Entregáveis acordados disponíveis no ambiente definido.
- Cenários essenciais do documento de requisitos aprovados.
- Mapa temporal validado como principal interface de acompanhamento da ocupação.
- Nenhuma falha impeditiva aberta nos fluxos de reserva, check-in, check-out ou cálculo temporal.
- Documentação de execução, configuração, backup e restauração entregue.
- Pendências não impeditivas registradas e aceite final documentado pelo responsável funcional.

## 14. Aprovação

Esta minuta consolida a proposta do projeto. O início formal, os responsáveis e os compromissos de prazo e orçamento dependem de definição e registro pelas partes envolvidas.

| Papel | Nome | Data | Registro de aprovação |
|---|---|---|---|
| Solicitante ou patrocinador | A definir | A definir | Pendente |
| Responsável pelo projeto | A definir | A definir | Pendente |
| Responsável pela validação funcional | A definir | A definir | Pendente |
