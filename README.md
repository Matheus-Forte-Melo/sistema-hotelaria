# Pernoite — Sistema de Gestão de Hospedagens

API REST para administração interna de pequenos hotéis e pousadas. O produto pretende centralizar quartos, hóspedes, reservas, check-in e check-out, oferecendo um mapa temporal da ocupação por bloco, andar e data. Os usuários previstos são administradores e recepcionistas.

O monorepo reúne a API Spring Boot em `back/` e a aplicação React, Vite e Tailwind CSS em `front/`. A entrega atual concentra-se em reservas e no mapa por andar e data. Consulte a [redefinição de escopo](entrega/redefinicao-de-escopo.md) e as [issues do frontend](entrega/issues-frontend.md).

## Iniciar o projeto

A partir da raiz, use dois terminais. No primeiro, com JDK 21 e Maven:

```sh
cd back
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

No segundo, com Node 22.12+ e npm:

```sh
cd front
npm ci
npm run dev
```

Abra a URL impressa pelo Vite. A home exibe dados de demonstração e verifica a conexão ao abrir. O botão **Verificar conexão** repete a chamada a `/api/health`. O proxy de desenvolvimento encaminha `/api` ao Spring em `http://127.0.0.1:8080`. Para outra porta, defina `BACKEND_URL` em `front/.env.local` e reinicie o Vite. Em hospedagem estática, configure o servidor para encaminhar `/api` ao backend.

## Fontes de dados do frontend

Os componentes leem `useEstrutura()` e `useReservas()`. O [provider](front/src/contexts/HotelContexts.jsx) recebe dois repositórios com métodos assíncronos. `estrutura.listar()` retorna `{ andares, quartos }` e `reservas.listar()` retorna uma lista. Os campos e vínculos estão no [repositório de demonstração](front/src/data/mockRepositories.js).

Quando uma API de negócio estiver pronta, implemente o método correspondente com HTTP e passe esse repositório ao provider em [main.jsx](front/src/main.jsx). Cada fonte pode ser substituída individualmente. Atualize o rótulo de demonstração da home conforme as fontes integradas. O healthcheck já consulta o Spring mesmo com os dados de negócio simulados.

Dentro de `front/`, execute `npm run build` para gerar a versão de produção em `dist/`. Os comandos Maven abaixo devem ser executados em `back/`.

## Estado atual

A camada de persistência da Etapa 1 está implementada: nove entidades JPA com relacionamentos, repositórios `JpaRepository` e testes de integração no H2. O diagrama de classes UML e o dicionário de dados estão no [documento técnico](docs/documentacao-tecnica.md#3-arquitetura-e-modelagem-de-dados). Serviços e endpoints de negócio ainda não existem. Consulte o [TODO](docs/todo.md) e o [contexto](docs/context.md).

## Identificação da equipe

A equipe é composta por cinco integrantes:

| Nome completo | Papel |
|---|---|
| Matheus Forte de Melo | Líder |
| Geison Carlos Melo | Integrante |
| Luan Elizeu Santos | Integrante |
| Jeferson Machado | Integrante |
| Matheus Kuchenbecker | Integrante |

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

O healthcheck confirma que a API responde; não verifica consultas ao banco. A raiz `/` não contém frontend. A segurança libera `/api/health` (e `/h2-console` no perfil `dev`) e exige autenticação nas demais rotas; login por e-mail e perfis de usuário ainda não estão implementados.

## Banco, terminal e testes

`application-dev.properties` configura `jdbc:h2:mem:hotelaria` e `ddl-auto=create-drop`. O Hibernate gera as tabelas a partir das entidades ao iniciar, e a classe `DadosIniciaisDev` carrega dados fictícios de demonstração. Para consultar as tabelas, abra `http://localhost:8080/h2-console` com JDBC URL `jdbc:h2:mem:hotelaria`, usuário `sa` e senha vazia. O console só fica disponível no perfil `dev`. Os dados desaparecem ao encerrar a aplicação; essa configuração não atende à preservação de histórico da versão final.

Com JDK 21 e Maven instalado:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Para executar os testes existentes:

```bash
mvn test -Dspring.profiles.active=dev
```

**Pendência do ambiente:** os scripts `mvnw` e `mvnw.cmd` existem, mas `.mvn/wrapper` não estava presente na revisão de 23/10/2026. Restaure o Maven Wrapper completo antes de usar esses scripts. As IDEs podem utilizar seu Maven integrado. O terminal da revisão usava Java 17, abaixo do Java 21 definido pelo projeto, e não tinha Maven no PATH. Confira `java -version` e `mvn -version` no ambiente utilizado.

## Dados mock e conexão pelo DBeaver

Inicie a aplicação com o perfil Spring `dev` (`--spring.profiles.active=dev` na IDE). A classe `DadosIniciaisDev` carrega automaticamente 2 funcionários, 2 blocos, 3 andares, 8 quartos, 5 hóspedes, 4 reservas e 1 indisponibilidade. Há exemplos de estadia finalizada, em andamento, reserva futura e cancelada, com datas relativas ao dia atual.

No DBeaver, crie uma conexão **H2**, selecione a configuração por URL e informe:

| Campo | Valor |
|---|---|
| URL JDBC | `jdbc:h2:tcp://localhost:9092/mem:hotelaria` |
| Usuário | `sa` |
| Senha | Vazia |
| Schema | `PUBLIC` |

Use o driver H2 compatível com a versão resolvida pelo Maven. Mantenha a aplicação rodando enquanto usa o DBeaver. O servidor TCP só inicia no perfil `dev` e aceita clientes locais, sem `-tcpAllowOthers`. A porta pode ser alterada em `app.h2.tcp-port` no arquivo `application-dev.properties`; ajuste a URL do DBeaver se mudar essa porta.

Não use `jdbc:h2:mem:hotelaria` no DBeaver: isso criaria outro banco na JVM do cliente. O acesso ao banco em memória da aplicação precisa ser via TCP, conforme a [documentação do H2](https://h2database.com/html/features.html#in_memory_databases).

Para conferir os dados:

```sql
SELECT * FROM PUBLIC.QUARTO;
SELECT * FROM PUBLIC.HOSPEDE;
SELECT * FROM PUBLIC.RESERVA;
```

O banco é temporário: os dados são recriados a cada reinicialização. Os funcionários fictícios `admin@hotel.local` / `admin123` e `recepcao@hotel.local` / `recepcao123` são registros de demonstração, não credenciais de conexão ao banco; o login da aplicação ainda precisa ser implementado.

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
