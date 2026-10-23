# TODO — entrega e desenvolvimento

**Revisão:** 23/10/2026. Marcar conclusão somente com artefato ou validação correspondente.

## Etapa 1 — identificação e concepção

- [ ] Confirmar nome técnico/comercial do produto.
- [ ] Preencher equipe no README: até cinco integrantes e exatamente um líder.
- [ ] Preencher link da aplicação publicada antes da submissão.
- [x] Documentar problema, público-alvo e objetivo.
- [x] Enumerar RF e regras de negócio em `requisitos.md`.
- [x] Documentar RNF de segurança, banco e padrões REST.
- [ ] Acrescentar RNF de desempenho com métrica, carga, volume de dados e ambiente acordados.
- [x] Organizar documento técnico com o sumário da atividade.
- [ ] Revisar e aprovar concepção e decisões com a equipe.

## Etapa 1 — arquitetura e modelagem

- [x] Registrar backend REST e frontend separado.
- [ ] Definir classes e representação de reserva/hospedagem.
- [ ] Produzir UML com atributos tipados, métodos principais, relacionamentos, cardinalidades e opcionalidade.
- [ ] Conferir UML contra requisitos, histórico e indisponibilidades.
- [ ] Preencher dicionário: tipos Java/SQL, nulabilidade, PK, FK, unicidade e restrições.
- [ ] Documentar anotações JPA, lado proprietário, cascatas e exclusão de cada associação.

## Etapa 1 — persistência JPA

- [x] Incluir dependências JPA e H2.
- [x] Configurar H2 em memória no perfil `dev`.
- [ ] Implementar no mínimo cinco entidades de domínio com `@Entity` e identificadores.
- [ ] Mapear os relacionamentos necessários com anotações JPA adequadas.
- [ ] Criar repositórios com `JpaRepository` para as entidades persistidas.
- [ ] Implementar restrições de integridade coerentes com a preservação de histórico.
- [ ] Validar gravação, leitura, associações e restrições com testes de integração relevantes.
- [ ] Demonstrar persistência e manter UML, dicionário e código consistentes.

## Etapa 1 — execução e revisão

- [x] Criar healthcheck público `/api/health`.
- [x] Documentar execução no Eclipse e IntelliJ IDEA.
- [ ] Restaurar `.mvn/wrapper/maven-wrapper.properties` e demais arquivos necessários ao Wrapper.
- [ ] Validar execução a partir de clone limpo, com JDK 21 e perfil `dev`.
- [ ] Executar e registrar testes após implementar persistência.
- [ ] Revisar identificação da equipe, links e artefatos obrigatórios antes da submissão.

## Evolução do produto — além do foco da Etapa 1

- [ ] Definir contratos REST, paginação, filtros, erros e formatos de datas.
- [ ] Implementar login por e-mail, hash de senhas, logout e autorização por perfil.
- [ ] Escolher autenticação do frontend e configurar CORS e CSRF conforme essa decisão.
- [ ] Implementar cadastros de estrutura física, funcionários e hóspedes.
- [ ] Implementar disponibilidade, reservas e bloqueios com proteção contra conflitos concorrentes.
- [ ] Validar capacidade, ocupantes e presença de adulto.
- [ ] Implementar check-in, check-out, cancelamento e correções históricas.
- [ ] Implementar consultas do mapa temporal, resumo do andar e detalhes.
- [ ] Construir e integrar frontend separado.
- [ ] Adotar banco persistente, migrações, backup e restauração.
- [ ] Validar cenários de aceite e preparar implantação e documentação operacional.
