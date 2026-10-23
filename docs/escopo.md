# Documento de Escopo do Projeto

**Produto:** nome a definir — Sistema de Gestão de Hospedagens (nome provisório).  
**Versão:** 1.1 — proposta inicial.  
**Data:** 23/10/2026.

## 1. Visão do produto

Sistema web de uso interno para pequenas pousadas e hotéis, destinado à administração e à recepção de um único estabelecimento. Centraliza quartos, hóspedes, reservas e hospedagens, com destaque para uma representação visual da ocupação ao longo do tempo.

O principal diferencial é o **mapa temporal do hotel**: uma interface organizada por blocos, andares e quartos, na qual o usuário pode escolher uma data, avançar ou voltar dias e consultar a ocupação realizada, atual ou prevista.

O mapa deve permitir responder rapidamente: quais quartos estão ocupados, quem está hospedado, quantas vagas estão sendo utilizadas, quais entradas e saídas são esperadas e como a ocupação se distribui pelo hotel.

## 2. Objetivo e público-alvo

Facilitar a operação da recepção, reduzir conflitos de reservas e oferecer uma visão clara da utilização do hotel, substituindo controles dispersos em cadernos e planilhas.

Usuários previstos:

- **Administrador:** configura a estrutura do hotel, gerencia funcionários e acessa todas as operações.
- **Recepcionista:** consulta o mapa, cadastra hóspedes, gerencia reservas e registra entradas e saídas.

Hóspedes não terão acesso ao sistema nesta versão.

## 3. Escopo incluído

### 3.1. Acesso e estrutura física

- Autenticação com e-mail e senha e controle de acesso por perfil.
- Cadastro e desativação de funcionários pelo administrador.
- Cadastro de blocos, andares e quartos, com possibilidade de usar apenas um bloco.
- Definição da categoria, capacidade e diária de cada quarto.
- Registro de períodos de indisponibilidade de quartos, com motivo.

### 3.2. Hóspedes, reservas e hospedagens

- Cadastro único de hóspedes, reutilizado em visitas futuras.
- Registro de todos os hóspedes de cada hospedagem, sem papéis distintos entre os ocupantes.
- Consulta de disponibilidade por período e capacidade.
- Criação, alteração e cancelamento de reservas.
- Check-in e check-out, com registro das datas e dos horários efetivos.
- Cálculo do valor previsto das diárias, preservando o preço acordado na reserva.
- Consulta de hóspedes presentes, reservas futuras e histórico de estadias.

### 3.3. Mapa temporal — principal entrega

- Navegação clicável por bloco e andar: selecionar um bloco exibe seus andares; selecionar um andar exibe sua grade de quartos.
- Quartos exibidos como quadrados em uma grade fixa, no estilo de um mapa de reserva de cinema, sem representar a planta real do hotel.
- Cada quadrado mostra apenas o número do quarto, cor e ícone da situação e ocupação no formato “2/4 pessoas”.
- Seletor de data, botões de dia anterior e próximo dia e ação “Hoje”.
- Identificação explícita da data consultada e do caráter histórico, atual ou previsto da informação.
- Indicação de quarto livre, reservado, ocupado ou indisponível, conforme a data e a natureza do registro.
- Resumo apenas do andar selecionado, com quartos ocupados e pessoas/capacidade, conforme a data consultada.
- Controles de seleção limitados a bloco, andar e data, sem filtro adicional de situação.
- Clique no quarto abre um painel de detalhes sem sair do mapa, com hóspedes, entradas, saídas e ações de reserva, check-in ou check-out, conforme situação e permissões. Datas passadas são consultadas como histórico, sem ações operacionais nesse painel.
- Cores acompanhadas de textos ou ícones para que a leitura não dependa apenas de cor.

### 3.4. Indicadores e histórico

- Resumo do andar selecionado com quantidade de quartos ocupados ou previstos e pessoas em relação à capacidade operacional, sem percentuais ou totais consolidados por bloco e hotel.
- Distribuição por crianças, adolescentes e adultos apenas nos detalhes do quarto, calculada pela data de nascimento na data consultada. Pessoas com 18 anos ou mais integram a mesma categoria de adultos, sem separação de idosos. Não haverá totais etários no painel principal.
- Identificação de hóspedes ainda não cadastrados nos detalhes do quarto, sem classificá-los indevidamente por idade.
- Página do hóspede com dados cadastrais atuais e histórico de hospedagens de cada pessoa.

## 4. Significado da navegação temporal

“Atemporal”, neste projeto, significa poder navegar entre datas passadas, presentes e futuras. A interface será denominada **mapa temporal**, pois cada consulta tem uma data de referência.

| Consulta | Informação apresentada |
|---|---|
| Data passada | Ocupação realizada, apurada a partir das entradas e saídas efetivas registradas. Reservas canceladas ou sem check-in não representam ocupação realizada. |
| Hoje | Presença real no momento da consulta, com entradas e saídas previstas identificadas separadamente. |
| Data futura | Projeção com base nas reservas ativas e nas saídas previstas de hospedagens em andamento. Não representa presença confirmada. |

Para datas passadas e futuras, a ocupação diária usa a convenção de pernoite: **a data de entrada está incluída e a data de saída está excluída**. As entradas e saídas do dia também aparecem nos detalhes, inclusive quando não compõem a contagem de pernoite.

Exemplo: uma estadia de 10 a 12 ocupa as noites de 10 e 11. Outra reserva pode começar no dia 12. Ao consultar o dia 12, o mapa pode mostrar a saída da primeira estadia e a entrada da segunda, sem somar ambas como ocupação da noite.

O histórico retrata fatos registrados, incluindo correções autorizadas. **Não será reconstruída a tela exatamente como era conhecida em uma data anterior**, nem o histórico de todas as versões das reservas. Uma reserva futura alterada passa a compor a previsão atualizada.

## 5. Limites para manter o projeto viável

- Um estabelecimento por instalação e uma única zona de horário configurada para o hotel.
- Uma reserva corresponde a um quarto e a uma lista de hóspedes, sem designação de responsável ou acompanhante.
- Todos os hóspedes do quarto compartilham o período de estadia; entradas e saídas individuais não serão tratadas.
- Ocupantes devem estar identificados antes da conclusão do check-in. Cada quarto deve ter pelo menos um hóspede com 18 anos ou mais na entrada efetiva. O sistema deve avisar sobre essa regra e impedir hospedagens compostas exclusivamente por menores de 18 anos, sejam crianças, adolescentes ou ambos.
- Sem troca de quarto durante a hospedagem nesta versão.
- Sem hospedagens de algumas horas ou entrada e saída no mesmo dia.
- Bloco, andar e capacidade de quartos com reservas ou histórico associado não poderão ser alterados; o quarto poderá ser desativado para novas reservas, preservando o histórico.
- Registros utilizados em hospedagens não serão excluídos fisicamente pela interface.
- Dados cadastrais exibidos no perfil serão os atuais; não haverá versionamento completo de nomes, endereços ou contatos.
- Os indicadores estarão disponíveis na interface, sem ferramenta genérica de criação de relatórios.

## 6. Fora do escopo

- Portal do hóspede, reservas públicas e aplicativo móvel nativo.
- Integrações com Booking, Airbnb ou outros canais externos.
- Pagamentos on-line, caixa, contabilidade, cobrança e emissão fiscal.
- Restaurante, frigobar, estoque, consumo e serviços adicionais.
- Distribuição de tarefas de limpeza ou manutenção; apenas bloqueio de quartos por período.
- Gestão de redes de hotéis e múltiplos estabelecimentos.
- Tarifação dinâmica, cupons, programas de fidelidade e reservas coletivas de vários quartos.
- Plantas arquitetônicas, mapas 3D, posicionamento livre dos quartos e editor gráfico de edifícios.
- Import

## 7. Critérios de aceite do escopo

- Executar o fluxo completo de cadastro, reserva, check-in, consulta visual e check-out.
- Impedir sobreposição de reservas e ocupação acima da capacidade, inclusive em solicitações concorrentes.
- Navegar por passado, presente e futuro com identificação clara do significado de cada visão.
- Demonstrar saída e entrada no mesmo dia sem duplicar a ocupação diária.
- Selecionar bloco e andar, visualizar a grade e abrir detalhes de um quarto sem sair do mapa.
- Manter coerência entre quadrados, detalhes e resumo do andar selecionado ao trocar a data.
- Consultar estadias anteriores de um hóspede sem perder o vínculo com os demais hóspedes da estadia e com o quarto.
- Preservar o histórico após cancelamento de reservas futuras ou desativação de cadastros.

## 8. Documentos relacionados

- [Requisitos funcionais, não funcionais e regras de negócio](requisitos.md).
- [Termo de abertura do projeto](termo-de-abertura.md).
