# Documento de Concepção e Requisitos

**Produto:** nome a definir — Sistema de Gestão de Hospedagens (nome provisório).  
**Versão:** 1.1 — proposta inicial.  
**Data:** 23/10/2026.

## 1. Objetivo geral

Desenvolver um sistema web para administração interna de um pequeno hotel ou pousada, centralizando reservas, hóspedes e hospedagens. O diferencial será um mapa temporal por blocos, andares e quartos, com navegação por datas e indicadores de ocupação realizada, atual e prevista.

Todos os requisitos abaixo integram a primeira versão proposta, respeitando os limites do [escopo](escopo.md).

## 2. Perfis de acesso

| Operação | Administrador | Recepcionista |
|---|---|---|
| Consultar mapa, indicadores, hóspedes e histórico | Sim | Sim |
| Cadastrar e atualizar hóspedes | Sim | Sim |
| Criar, alterar e cancelar reservas | Sim | Sim |
| Registrar check-in e check-out | Sim | Sim |
| Gerenciar funcionários e perfis | Sim | Não |
| Configurar blocos, andares, quartos e indisponibilidades | Sim | Não |
| Corrigir datas efetivas de estadias encerradas, mediante justificativa | Sim | Não |

## 3. Requisitos funcionais

### RF01 — Autenticação e autorização

Permitir entrada com e-mail e senha e encerramento da sessão. Restringir operações pelo perfil do funcionário, inclusive na API. Usuários desativados não poderão continuar acessando operações protegidas.

### RF02 — Gerenciamento de funcionários

Permitir ao administrador cadastrar, editar e desativar funcionários e atribuir os perfis administrador ou recepcionista. Impedir a desativação do último administrador ativo.

### RF03 — Estrutura física

Permitir cadastrar blocos e andares, vinculando cada andar a um bloco e cada quarto a um andar. A identificação do quarto deve ser única no bloco. Preservar referências históricas, aplicando as restrições de alteração definidas nas regras de negócio.

### RF04 — Gerenciamento de quartos

Cadastrar identificação, categoria, capacidade máxima e valor padrão da diária. Permitir desativação para novas reservas sem apagar reservas ou estadias existentes. Categoria será um atributo simples, sem gestão própria de tarifas.

### RF05 — Indisponibilidade por período

Permitir ao administrador registrar e consultar bloqueios de quartos com início, fim e motivo. Impedir bloqueios conflitantes com reservas ativas ou estadias. A interface deve orientar a resolver o conflito antes de efetivar o bloqueio.

### RF06 — Cadastro de hóspedes

Cadastrar nome, data de nascimento e dados de contato. Permitir informar tipo e número de documento de qualquer hóspede, sem diferenciar papéis entre os ocupantes. A data de nascimento é obrigatória para validar a composição do quarto. Quando informado, o documento deve ser utilizado para evitar duplicação. Possíveis coincidências de nome e nascimento devem ser sinalizadas para conferência, sem fusão automática.

### RF07 — Consulta de disponibilidade

Consultar quartos para um intervalo de entrada e saída e uma quantidade de pessoas. Considerar capacidade, reservas ativas, hospedagens em andamento, desativação e bloqueios aplicáveis ao período.

### RF08 — Gerenciamento de reservas

Criar, consultar, alterar e cancelar reservas com quarto, hóspedes conhecidos, quantidade prevista de pessoas, datas e diária acordada. Revalidar disponibilidade e capacidade a cada alteração. Registrar cancelamento com data e motivo. Reservas canceladas permanecem consultáveis e deixam de bloquear disponibilidade.

### RF09 — Registro de ocupantes

Associar à reserva uma lista de hóspedes, sem papéis distintos entre eles. Permitir completar a lista antes do check-in. Impedir duplicidade da mesma pessoa na lista e exigir que a quantidade identificada corresponda à quantidade efetiva de ocupantes no check-in. Exibir o aviso: “Cada quarto deve ter pelo menos um hóspede com 18 anos ou mais. Não é permitida hospedagem apenas de crianças e/ou adolescentes.” Com a lista completa, impedir salvar uma composição sem adulto na data prevista de entrada. Com a lista incompleta e sem adulto identificado, sinalizar a validação como pendente; pessoas não identificadas não comprovam a presença de adulto.

### RF10 — Check-in

Registrar data e hora efetivas de entrada e alterar a situação da reserva para em andamento. Exigir identificação de todos os ocupantes, validar a capacidade e impedir check-in em quarto ainda ocupado. Revalidar as idades na entrada efetiva e bloquear o check-in se não houver pelo menos um hóspede com 18 anos ou mais, apresentando o aviso definido no RF09. Uma entrada antecipada deve revalidar o período afetado antes da confirmação.

### RF11 — Check-out

Registrar data e hora efetivas de saída e finalizar a hospedagem. Preservar ocupantes, quarto e período realizado para consultas históricas. Saídas antecipadas atualizam a ocupação realizada. Prorrogações exigem alteração validada da saída prevista e não podem sobrepor outra reserva ou bloqueio.

### RF12 — Valor previsto da hospedagem

Calcular quantidade de diárias multiplicada pelo valor acordado na reserva. Alterações do preço padrão do quarto não modificam reservas existentes. Alterações autorizadas de período recalculam a previsão; o sistema não realiza cobrança, quitação ou apuração financeira final.

### RF13 — Mapa visual do hotel

Oferecer navegação clicável: selecionar o bloco apresenta seus andares; selecionar um andar apresenta os quartos em uma grade fixa, semelhante a um mapa de reserva de cinema. Cada quarto será um quadrado clicável com apenas número, cor e ícone da situação e ocupação no formato “2/4 pessoas”. Exibir legenda para livre, reservado, ocupado e indisponível, respeitando a referência temporal e o estado “sem registros de ocupação” previsto na RN07. Limitar os controles de seleção a bloco, andar e data, sem filtro por situação. A grade não representa a planta real do hotel.

### RF14 — Navegação temporal

Disponibilizar no topo do mapa seletor de data, avanço e retorno de um dia e ação “Hoje”. Atualizar quadrados, resumo do andar e painel de detalhes para a mesma data de referência, preservando bloco e andar selecionados. Destacar a data e rotular a visão como histórica, atual ou prevista. Aplicar as regras temporais RN01 a RN07.

### RF15 — Detalhes de quarto na data

Ao clicar em um quarto, abrir um painel de detalhes sem sair do mapa, apresentando os registros relacionados à data: estadia realizada ou em andamento, reserva prevista, hóspedes, entradas, saídas e motivo de indisponibilidade. Distinguir saída de uma estadia e entrada de outra no mesmo dia. Oferecer acesso às ações de reservar, fazer check-in ou check-out conforme situação, data e permissões, mantendo as validações dos respectivos fluxos. Datas futuras permitem iniciar uma reserva, mas não realizar check-in ou check-out futuro; datas passadas permitem somente consulta nesse painel. Correções de histórico permanecem no fluxo específico do RF20.

### RF16 — Indicadores de capacidade e ocupação

Exibir apenas um resumo do andar selecionado: quantidade de quartos ocupados e quantidade de pessoas em relação à capacidade operacional, por exemplo “3 quartos ocupados · 8/20 pessoas”. Em datas futuras, identificar essas quantidades como previstas. Não apresentar percentuais ou totais consolidados por bloco e hotel. Calcular o resumo com a mesma referência temporal da grade. Em hoje, contar a ocupação real, mantendo reservas ainda sem entrada identificadas pela situação dos quartos e nos detalhes.

### RF17 — Distribuição etária

Exibir a distribuição de crianças, adolescentes e adultos apenas no painel de detalhes do quarto selecionado, com idade calculada na data de referência. Em previsões, apresentar também a quantidade de pessoas ainda não identificadas. Não exibir totais etários no painel principal ou agregações por andar, bloco ou hotel. Identificar quando os dados são previstos, sem sugerir que equivalem a hóspedes presentes.

### RF18 — Consulta de hóspedes e histórico

Pesquisar por nome ou documento e filtrar hóspedes presentes, com reservas futuras ou com estadias anteriores. No perfil, exibir dados cadastrais atuais e registros de hospedagem com datas, quarto, bloco e andar, sem atribuir papéis aos hóspedes. Reservas canceladas devem ser identificadas separadamente das estadias realizadas.

### RF19 — Entradas, saídas e pendências do dia

Listar entradas e saídas previstas ou realizadas na data selecionada. Na visão atual, sinalizar reservas cuja entrada prevista passou sem check-in e hospedagens cuja saída prevista passou sem check-out. Não efetuar check-in, check-out ou cancelamento automaticamente.

### RF20 — Correção de histórico

Permitir ao administrador corrigir datas e horários efetivos de estadias encerradas, com justificativa, registro do autor e preservação dos valores anteriores desses campos. Validar cronologia, capacidade e conflitos antes de salvar e recalcular as consultas afetadas. Esse registro limitado não representa auditoria completa do sistema.

## 4. Regras de negócio e interpretação temporal

| Código | Regra |
|---|---|
| **RN01** | Todas as datas e os agrupamentos diários usam a zona de horário configurada para o hotel. |
| **RN02** | Para reservas e ocupação diária de passado/futuro, utilizar o intervalo de pernoite `[entrada, saída)`: entrada incluída, saída excluída. Saída deve ser posterior à entrada. |
| **RN03** | Em datas passadas, contar ocupação efetivamente realizada a partir de check-in e check-out. Para estadias ainda abertas, considerar sua permanência efetiva até o presente. Reserva sem check-in não conta como estadia realizada. |
| **RN04** | Em hoje, contar como presentes apenas hóspedes com check-in registrado e sem check-out. Mostrar entradas e saídas previstas separadamente. Ao sair da visão de hoje, indicar que o mapa passa a representar pernoites históricos ou previstos. |
| **RN05** | Em datas futuras, contar reservas confirmadas e estadias em andamento até sua saída prevista. Usar somente a informação mais recente disponível e rotular os números como previsão. |
| **RN06** | Uma hospedagem com saída vencida continua ocupando o quarto na visão atual e gera pendência. A previsão futura continua baseada nas datas registradas e deve alertar que existem saídas vencidas a regularizar. Nunca permitir nova entrada enquanto o quarto permanecer efetivamente ocupado. |
| **RN07** | Consultas passadas mostram fatos conforme os registros atuais e correções autorizadas; não reproduzem versões antigas do planejamento. Uma data sem registros deve aparecer como “sem registros de ocupação”, sem afirmar que há comprovação de quarto livre. |
| **RN08** | Cada reserva envolve um quarto e uma lista de hóspedes, sem responsável ou acompanhante. Todos compartilham o período da estadia. |
| **RN09** | Impedir sobreposição de reservas ativas, estadias e bloqueios para o mesmo quarto. Garantir essa integridade também em operações simultâneas. |
| **RN10** | A quantidade de pessoas deve ser positiva e não pode exceder a capacidade do quarto. Antes do check-in, a previsão pode incluir pessoas ainda não identificadas. Cada quarto deve conter pelo menos um adulto: validar na entrada prevista ao completar ou alterar a lista de hóspedes ou o período da reserva, e novamente na entrada efetiva ao realizar check-in. Uma lista completa formada exclusivamente por menores de 18 anos deve ser rejeitada com aviso, inclusive quando mistura crianças e adolescentes. |
| **RN11** | Situações da reserva: confirmada, em andamento, finalizada e cancelada. Cancelamento aplica-se à reserva confirmada; estadias iniciadas são encerradas por check-out. Ausência sem entrada gera pendência para tratamento manual. |
| **RN12** | Faixas adotadas para indicadores internos: crianças de 0 a 11 anos; adolescentes de 12 a 17; adultos a partir de 18 anos, sem limite superior. Não há categoria separada de idosos nem tratamento operacional distinto por idade entre adultos. São categorias estatísticas do projeto, sem finalidade de classificação jurídica. |
| **RN13** | Calcular idade na data consultada. Datas de nascimento futuras em relação ao cadastro não são aceitas. Pessoa sem idade válida para a referência histórica deve aparecer como dado pendente, sem classificação presumida. |
| **RN14** | Pessoas ainda não identificadas em uma reserva futura compõem a previsão de vagas, mas ficam em uma categoria separada dos totais por faixa etária e não satisfazem a exigência de adulto no quarto. Enquanto a lista estiver incompleta, a verificação sem adulto identificado permanece pendente e o check-in fica bloqueado. |
| **RN15** | Capacidade operacional corresponde à capacidade total dos quartos ativos na data, descontados os bloqueados no período. Guardar a data de ativação e eventual desativação para não aplicar a configuração atual indevidamente ao passado. |
| **RN16** | O resumo considera apenas o andar selecionado e informa quantidade de quartos ocupados e pessoas/capacidade operacional, sem calcular percentuais. Em previsão, usar as respectivas quantidades previstas. Com capacidade operacional zero, exibir “sem capacidade disponível”, sem efetuar divisão. |
| **RN17** | Localização e capacidade de quarto que tenha reservas ou estadias associadas não podem ser alteradas. Desativação não pode invalidar reservas existentes; conflitos devem ser resolvidos antes. Identificadores usados no histórico devem ser preservados. |
| **RN18** | Não excluir fisicamente pela interface hóspedes, quartos ou usuários referenciados em reservas e estadias. Desativação não apaga vínculos históricos. |
| **RN19** | Bloqueios usam início incluído e fim excluído. Alterações de bloqueios passados exigem administrador e justificativa, com registro dos valores anteriores, pois afetam indicadores históricos. |
| **RN20** | O mapa utiliza uma grade fixa de quartos do andar selecionado, semelhante à seleção de assentos de cinema. Bloco e andar funcionam como navegação; os quartos são os elementos clicáveis. A disposição é lógica, sem planta arquitetônica, posicionamento livre ou editor de layout. |

## 5. Requisitos não funcionais

| Código | Categoria | Definição e critério de verificação |
|---|---|---|
| **RNF01** | Usabilidade | Data selecionada, legenda e tipo de visão devem permanecer visíveis. Principais ações do mapa devem poder ser utilizadas por teclado e as situações não podem depender exclusivamente de cores. Validar por roteiro manual. |
| **RNF02** | Compatibilidade | Interface adaptada a computadores e tablets, a partir de 768 px de largura. Validar nas versões estáveis disponíveis de Chrome, Firefox e Edge no momento da homologação. |
| **RNF03** | Segurança de acesso | Armazenar senhas com hash próprio para senhas; exigir HTTPS em produção; validar autorização no servidor; invalidar sessões no logout e impedir acesso de usuários desativados. |
| **RNF04** | Validação e proteção | Validar entradas no servidor, utilizar consultas parametrizadas, limitar tentativas de autenticação e impedir exposição de senhas ou documentos completos em logs de aplicação. |
| **RNF05** | Dados pessoais | Restringir dados de hóspedes aos funcionários autorizados. Cartões e totais do mapa não devem expor documentos ou contatos completos; esses dados ficam nos detalhes com acesso controlado. |
| **RNF06** | Banco de dados | Utilizar banco relacional com chaves estrangeiras, migrações versionadas e transações nas operações críticas. PostgreSQL é a opção proposta; a escolha final será confirmada no planejamento técnico. |
| **RNF07** | Integridade transacional | Executar as operações de criação, alteração e cancelamento de reservas em transações, com validação das regras de disponibilidade no backend e reversão das alterações em caso de falha. |
| **RNF08** | API REST | Usar JSON, métodos HTTP coerentes com a operação, códigos de resposta adequados e formato consistente de erros. Listagens devem oferecer paginação e filtros. Documentar endpoints e formatos de datas. |
| **RNF09** | Consistência temporal | Usar uma referência de data e horário consistente por consulta. Cartões e agregações devem refletir o mesmo conjunto de dados; mudanças de dia devem descartar respostas atrasadas de consultas anteriores. |
| **RNF11** | Manutenção e validação | Manter instruções de instalação, configuração e execução. Automatizar testes das regras críticas de disponibilidade, capacidade, transições e cálculo temporal; validar visualmente o mapa por roteiro de aceite. |

## 6. Cenários essenciais de aceite

| Cenário | Resultado esperado |
|---|---|
| Reserva de 10 a 12 e outra de 12 a 14 no mesmo quarto | Ambas são permitidas. A consulta de pernoite do dia 12 conta apenas a segunda, com saída da primeira identificada nos detalhes. |
| Duas reservas simultâneas para o mesmo quarto e período | Apenas uma é confirmada; a outra recebe mensagem de conflito. |
| Consulta passada de reserva cancelada antes da entrada | A reserva não integra a ocupação realizada. |
| Check-out realizado antes do previsto | O histórico usa a saída efetiva; a reserva preserva a informação do período originalmente previsto quando aplicável. |
| Consulta atual com quarto ainda sem check-out | O quarto permanece ocupado, mesmo depois da saída prevista, e uma nova entrada é impedida. |
| Reserva futura de quatro pessoas, com duas identificadas | A previsão de vagas conta quatro; nos detalhes do quarto, o perfil etário classifica duas e informa duas ainda não identificadas. |
| Hóspede que completa 60 anos entre duas datas consultadas | Continua contado como adulto em ambas as datas, sem categoria ou tratamento distinto. |
| Lista completa apenas com crianças, apenas com adolescentes ou com ambos | O sistema exibe aviso e rejeita a composição do quarto, pois não há pessoa com 18 anos ou mais. |
| Quarto com menores de idade e pelo menos um adulto | A composição é aceita, desde que capacidade e demais regras sejam atendidas, sem atribuir responsabilidade a nenhum hóspede. |
| Único adulto removido da lista de hóspedes | A alteração é rejeitada se deixar a lista completa apenas com menores. Se a lista ficar incompleta, a verificação fica pendente e o check-in é bloqueado. |
| Hóspede que completa 18 anos na entrada prevista | Conta como adulto nessa data; uma antecipação para antes do aniversário exige nova validação e bloqueia o check-in caso não haja outro adulto. |
| Andar com todos os quartos bloqueados | Quadrados indicam indisponibilidade e o resumo informa “sem capacidade disponível”, sem percentuais. |
| Desativação de quarto com histórico | O passado continua consultável; a capacidade futura respeita a data de desativação. |
| Seleção de bloco, andar e quarto | O bloco apresenta seus andares; o andar apresenta a grade; clicar no quarto abre detalhes sem sair do mapa. |
| Leitura da grade | Cada quadrado mostra número, cor e ícone da situação e pessoas/capacidade. Hóspedes, datas e faixas etárias ficam nos detalhes. |
| Consulta de quarto em data passada | O painel mostra o histórico sem oferecer ações de reserva, check-in ou check-out. |
| Avanço rápido entre várias datas | A tela termina exibindo quadrados, resumo do andar e detalhes da última data escolhida, mantendo bloco e andar selecionados, sem mistura de respostas. |
| Correção autorizada de estadia passada | O histórico e os indicadores são atualizados e a correção mantém autor, justificativa e valores anteriores. |

## 7. Decisões pendentes

- Nome comercial do produto.
- Identidade visual e aprovação do protótipo do mapa.
- Stack de aplicação e ambiente de hospedagem.
- Responsáveis, calendário, orçamento e critérios operacionais de implantação.
- Política de retenção e tratamento de dados pessoais antes do uso com dados reais.

Essas pendências não impedem a prototipação. Alterações que ampliem os limites funcionais devem ser registradas e avaliadas quanto ao impacto no projeto.
