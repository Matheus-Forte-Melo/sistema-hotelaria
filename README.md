# Sistema de Gestão de Hospedagens

API REST para administração interna de pequenos hotéis e pousadas. O produto pretende centralizar quartos, hóspedes, reservas, check-in e check-out, oferecendo um mapa temporal da ocupação por bloco, andar e data. Os usuários previstos são administradores e recepcionistas.

O nome é provisório. Este repositório contém o backend Spring Boot; o frontend será desenvolvido separadamente e consumirá JSON.

## Estado atual

A base permite iniciar o desenvolvimento, mas a Etapa 1 ainda não está concluída. Existem inicialização Spring Boot, configuração H2 e healthcheck público. Faltam UML, entidades de domínio, relacionamentos e repositórios JPA. Consulte o [TODO](docs/todo.md) e o [contexto](docs/context.md).

## Identificação da equipe

Preencher antes da submissão: máximo de cinco integrantes, sendo um líder. Remova linhas não utilizadas.

| Integrante | Nome completo | Matrícula | Papel |
|---|---|---|---|
| 1 | A preencher | A preencher | Líder |
| 2 | A preencher | A preencher | Integrante |
| 3 | A preencher | A preencher | Integrante |
| 4 | A preencher | A preencher | Integrante |
| 5 | A preencher | A preencher | Integrante |

**Link da aplicação publicada:** a preencher antes da submissão. O endereço localhost não substitui esse link.

## Tecnologias e pré-requisitos

- JDK 21 recomendado, conforme a versão Java definida no `pom.xml`.
- Spring Boot 4.1.2, Maven, Spring Web MVC, Spring Data JPA, Validation e Spring Security.
- H2 em memória, com perfil Spring `dev`.
- Eclipse com integração Maven (m2e) ou IntelliJ IDEA, da JetBrains.
- Internet na primeira importação para baixar as dependências.

Não é necessário instalar H2 ou Tomcat separadamente. Configure o JDK tanto na IDE quanto no executor Maven.

## Executar no Eclipse

1. Em **Window > Preferences > Java > Installed JREs**, adicione e selecione o JDK 21.
2. Selecione **File > Import > Maven > Existing Maven Projects**.
3. Escolha a pasta que contém o `pom.xml` e conclua a importação.
4. Aguarde as dependências. Se necessário, use **Maven > Update Project** e confira o JDK em **Properties > Java Build Path**.
5. Em **Run > Run Configurations > Java Application**, crie uma configuração para o projeto.
6. Selecione a classe principal `br.com.hotelaria.hotelaria.HotelariaApplication`.
7. Em **Arguments > Program arguments**, informe `--spring.profiles.active=dev`.
8. Na aba **JRE**, selecione o JDK 21 e clique em **Run**.

Com Spring Tools instalado, também é possível executar como **Spring Boot App**, mantendo o perfil `dev`. A configuração Java Application não depende desse plugin. A integração Maven é fornecida pelo [Eclipse m2e](https://eclipse.dev/m2e/documentation/m2e-faq.html).

## Executar no IntelliJ IDEA (JetBrains)

1. Em **File > Open**, abra a pasta do projeto ou o `pom.xml` como projeto Maven.
2. Em **File > Project Structure > Project SDK**, selecione o JDK 21.
3. Aguarde a sincronização Maven. Confira também o JDK do importador e do runner em **Settings > Build, Execution, Deployment > Build Tools > Maven**.
4. Abra `HotelariaApplication.java` e execute o método `main` pelo ícone ao lado dele.
5. Em **Run > Edit Configurations**, edite a configuração criada e adicione `--spring.profiles.active=dev` em **Program arguments**; o campo pode estar em **Modify options**.
6. Se usar uma configuração Spring Boot com o campo **Active profiles**, você pode preencher `dev` nesse campo em vez do argumento.
7. Execute novamente a configuração.

O perfil `dev` é do Spring, não do Maven. A configuração Java **Application** pode ser utilizada mesmo sem os recursos específicos de Spring da IDE. Consulte a [documentação da JetBrains](https://www.jetbrains.com/help/idea/run-debug-configuration-spring-boot.html) e os [perfis Spring](https://docs.spring.io/spring-boot/reference/features/profiles.html).

## Validar a execução

Confirme no console o perfil `dev` e a inicialização na porta 8080. Abra `http://localhost:8080/api/health` no navegador ou execute:

```bash
curl -i http://localhost:8080/api/health
```

Resposta esperada: HTTP `200`, com os campos abaixo, cuja ordem pode variar:

```json
{
  "status": "UP",
  "message": "API Funcional! :3"
}
```

O healthcheck confirma que a API responde; não verifica consultas ao banco. A raiz `/` não contém frontend. A segurança libera `/api/health` e exige autenticação nas demais rotas; login por e-mail e perfis de usuário ainda não estão implementados.

## Banco, terminal e testes

`application-dev.properties` configura `jdbc:h2:mem:hotelaria` e `ddl-auto=create-drop`. As tabelas serão geradas quando houver entidades. Os dados desaparecem ao encerrar a aplicação; essa configuração não atende à preservação de histórico e backup da versão final.

Com JDK 21 e Maven instalado:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Para executar os testes existentes:

```bash
mvn test -Dspring.profiles.active=dev
```

**Pendência do ambiente:** os scripts `mvnw` e `mvnw.cmd` existem, mas `.mvn/wrapper` não estava presente na revisão de 23/10/2026. Restaure o Maven Wrapper completo antes de usar esses scripts. As IDEs podem utilizar seu Maven integrado. O terminal da revisão usava Java 17, abaixo do Java 21 definido pelo projeto, e não tinha Maven no PATH. Confira `java -version` e `mvn -version` no ambiente utilizado.

## Documentação de desenvolvimento

| Documento | Conteúdo |
|---|---|
| [Documentação técnica](docs/documentacao-tecnica.md) | Sumário da Etapa 1 e pendências de modelagem |
| [Escopo](docs/escopo.md) | Funcionalidades, limites e critérios de aceite |
| [Concepção e requisitos](docs/requisitos.md) | RF, RNF, regras de negócio e cenários de aceite |
| [Termo de abertura](docs/termo-de-abertura.md) | Justificativa, entregáveis e riscos |
| [TODO](docs/todo.md) | Checklist da entrega e sequência de desenvolvimento |
| [Contexto](docs/context.md) | Decisões, estado do código e orientações para continuidade |

Antes de implementar, consulte as regras temporais e de concorrência. Atualize contexto e checklist conforme o código evoluir.
