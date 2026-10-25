# Contexto do trabalho

**Última revisão:** 25/10/2026.

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
| Modelo de domínio | Nove entidades em `model`: Funcionario, Bloco, Andar, Quarto, Hospede, Reserva, Indisponibilidade, CorrecaoHistorico e CorrecaoIndisponibilidade, além dos enums Perfil, SituacaoReserva, TipoDocumento e FaixaEtaria |
| Repositórios | Um `JpaRepository` por entidade em `repository`, com consultas de conflito, sobreposição e lock pessimista do quarto |
| Testes | `ReservaTest` (regras da reserva), `PersistenciaJpaTest` (`@DataJpaTest` no H2) e `contextLoads`: `IndisponibilidadeTest`: 26 testes passando em 25/10/2026 |
| Demonstração | No perfil `dev`, `DadosIniciaisDev` carrega dados fictícios e o console H2 fica em `/h2-console` |

O usuário informou que a aplicação está rodando. Nesta revisão documental, o código foi inspecionado, mas não foi executado novo build ou teste: o terminal usa Java 17, não há Maven no PATH e falta `.mvn/wrapper`. O JDK e Maven da IDE podem ser diferentes.

A dependência H2 Console não significa que exista console configurado e liberado. O healthcheck é uma resposta estática, não uma verificação da conexão ao banco.

## O que falta

Entidades, repositórios, UML e dicionário de dados existem (seção 3 do [documento técnico](documentacao-tecnica.md)). Ainda não existem serviços de domínio nem endpoints de negócio. Faltam também autenticação funcional, autorização por perfil, frontend, integração CORS, migrações e implantação.

Falta configurar a zona de horário do hotel (RN01); os dados de exemplo usam `America/Sao_Paulo`.

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

O domínio foi fechado em 25/10/2026: **reserva e hospedagem são a mesma entidade**, cuja situação evolui com check-in e check-out (RN11). Ocupantes são um `@ManyToMany` sem papéis. Correções de estadia ficam em `CorrecaoHistorico`, e alterações de bloqueios passados em `CorrecaoIndisponibilidade`. As decisões e o diagrama estão na seção 3.1 do [documento técnico](documentacao-tecnica.md); revisar com a equipe.

Regras que envolvem só uma reserva estão na entidade `Reserva`. Os serviços devem implementar as que comparam registros: travar o quarto com `QuartoRepository.findByIdParaAtualizacao`, verificar `ReservaRepository.existeConflito` e `IndisponibilidadeRepository.existeSobreposicao`, impedir check-in em quarto ocupado e validar a identificação do quarto no bloco. Ao alterar entidades, atualizar diagrama, dicionário e testes juntos.
