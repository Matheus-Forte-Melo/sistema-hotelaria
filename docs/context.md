# Contexto do trabalho

**Última revisão:** 23/10/2026.

## Objetivo e decisões

Sistema interno de gestão de hospedagens, com mapa temporal por bloco, andar e quarto. A Etapa 1 acadêmica foca em concepção, requisitos, arquitetura, UML e persistência JPA; não equivale à conclusão de todos os fluxos do produto.

- Backend REST em Java 21, Spring Boot 4.1.2 e Maven.
- Frontend separado; framework, repositório e hospedagem ainda não definidos. Ele poderá ser uma SPA.
- H2 em memória no perfil Spring `dev`, somente nesta fase.
- Spring Data JPA para persistência e Spring Security para segurança.
- PostgreSQL permanece uma proposta para o banco persistente, não uma configuração existente.
- Nome comercial, equipe, líder e URL publicada aguardam preenchimento.

Referências: [escopo](escopo.md), [requisitos](requisitos.md), [documento técnico](documentacao-tecnica.md) e [TODO](todo.md).

## O que existe

| Item | Evidência e limite |
|---|---|
| Inicialização | `HotelariaApplication`, pacote `br.com.hotelaria.hotelaria` |
| Dependências | Web MVC, JPA, H2, H2 Console, Validation, Security, DevTools e dependências de teste |
| Configuração geral | `application.properties` define o nome `hotelaria` |
| Perfil dev | H2 em memória, `create-drop`, SQL no log e `open-in-view=false` |
| Healthcheck | `GET /api/health`, com `status=UP` e `message=API Funcional! :3` |
| Segurança inicial | Libera `/api/health` e exige autenticação nas demais rotas; não implementa login por e-mail ou autorização por perfil |
| Teste inicial | Somente `contextLoads`, sem cobertura de regras de negócio ou persistência de entidades |

O usuário informou que a aplicação está rodando. Nesta revisão documental, o código foi inspecionado, mas não foi executado novo build ou teste: o terminal usa Java 17, não há Maven no PATH e falta `.mvn/wrapper`. O JDK e Maven da IDE podem ser diferentes.

A dependência H2 Console não significa que exista console configurado e liberado. O healthcheck é uma resposta estática, não uma verificação da conexão ao banco.

## O que falta

Ainda não existem entidades `@Entity`, interfaces `JpaRepository`, serviços de domínio, endpoints de negócio, UML ou dicionário de dados preenchido. Faltam também autenticação funcional, autorização por perfil, frontend, integração CORS, migrações e implantação.

As dependências de JPA instaladas não satisfazem, sozinhas, a exigência acadêmica de pelo menos cinco entidades persistidas com relacionamentos e repositórios.

## Contextos importantes para modelagem

- Uma reserva corresponde a um quarto e vários hóspedes, sem responsável/acompanhante.
- Intervalos de reserva usam `[entrada, saída)`. Hoje representa presença real; passado, fatos registrados; futuro, previsão.
- Preservar a diária acordada na reserva e usar `BigDecimal` para dinheiro.
- Separar datas previstas de horários efetivos. A zona de horário do hotel e o tipo dos instantes efetivos precisam ser definidos.
- Suportar bloqueios por período, ativação/desativação de quartos e preservação do histórico.
- Garantir conflitos de reserva transacionalmente, inclusive sob concorrência.
- Correções históricas exigem autor, justificativa e valores anteriores conforme os requisitos.
- Projetar DTOs para a API, evitando expor diretamente os relacionamentos JPA.

## Próximo passo

Fechar domínio e UML antes das entidades. Candidatos para discussão: funcionário, bloco, andar, quarto, hóspede, reserva e indisponibilidade. Decidir se hospedagem será evolução da reserva ou entidade própria, e como registrar ocupantes e correções. São propostas, não um modelo aprovado ou implementado.

Depois, implementar entidades/repositórios, validar persistência e relacionamentos e atualizar a documentação. Manter o checklist fiel às evidências, sem marcar como implementado algo apenas descrito.
