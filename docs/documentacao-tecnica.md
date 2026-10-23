# Documentação técnica — Etapa 1

**Revisão:** 23/10/2026. **Situação:** em elaboração; modelagem e persistência pendentes.

Identificação da equipe e URL da aplicação: preencher no [README](../README.md), com no máximo cinco integrantes e um líder.

## 1. Visão Geral do Projeto

### 1.1 Nome do Produto

Sistema de Gestão de Hospedagens — nome técnico provisório. Nome comercial a definir.

### 1.2 Declaração do Problema e Justificativa

Pequenos hotéis e pousadas precisam acompanhar reservas, disponibilidade e presença de hóspedes. Controles dispersos em cadernos e planilhas dificultam consultas e favorecem conflitos de reserva. O sistema pretende centralizar as informações e apresentar a ocupação por quarto e data, preservando o histórico de estadias.

O [termo de abertura](termo-de-abertura.md) detalha justificativa, riscos e entregáveis. O [escopo](escopo.md) delimita o produto e os critérios de aceite.

### 1.3 Público-Alvo e Objetivos do Sistema

Uso interno por administradores e recepcionistas de um único estabelecimento. Apoiar cadastros, reservas, check-in, check-out e consulta da ocupação passada, atual e prevista. Hóspedes não acessam o sistema nesta versão.

## 2. Especificação de Requisitos

### 2.1 Requisitos Funcionais (RF01, RF02, ...)

A lista detalhada RF01–RF20 está na seção 3 de [Concepção e Requisitos](requisitos.md). Inclui autenticação, funcionários, estrutura física, hóspedes, reservas, entradas/saídas, mapa temporal e correção de histórico. A seção 4 registra as regras de negócio e a seção 6 os cenários de aceite.

### 2.2 Requisitos Não Funcionais (RNF01, RNF02, ...)

RNF01–RNF11 estão na seção 5 de [Concepção e Requisitos](requisitos.md): segurança, banco relacional, transações, API REST em JSON, recuperação e validação, entre outros.

**Pendente:** requisito mensurável de desempenho, com tempo de resposta, volume de dados, carga concorrente e ambiente de medição acordados. Não há valores quantitativos aprovados.

H2 em memória é a configuração inicial de desenvolvimento. Migrações, persistência durável e backup permanecem requisitos para a evolução do produto e ainda não estão implementados.

## 3. Arquitetura e Modelagem de Dados

Backend Java 21 com Spring Boot 4.1.2, Web MVC, Security e Spring Data JPA. Frontend separado consumindo a API REST. H2 em memória no perfil `dev`.

Organização prevista: controller → service → repository → banco, com DTOs nos contratos REST. Hoje existem apenas inicialização, controller de healthcheck e configuração de segurança; negócio e persistência ainda serão desenvolvidos.

### 3.1 Diagrama de Classes UML

**Pendente.** Inserir o diagrama e um link para seu arquivo-fonte editável após modelar:

- Classes e atributos com tipos.
- Métodos principais e responsabilidades.
- Relacionamentos, cardinalidades e opcionalidade.
- Correspondência com entidades e associações implementadas.

O modelo deve atender estrutura física, hóspedes, reservas, ocupantes e bloqueios. Registrar a decisão sobre separar reserva e hospedagem. O mínimo de cinco entidades não substitui a cobertura dos conceitos necessários ao domínio.

### 3.2 Mapeamento de Entidades JPA e Dicionário de Dados

**Pendente: nenhuma entidade ou interface `JpaRepository` implementada nesta revisão.** São exigidas no mínimo cinco entidades de domínio persistidas e suas associações.

Preencher uma seção por entidade usando a estrutura abaixo:

| Entidade / tabela | Atributo / coluna | Tipo Java / SQL | Obrigatório | PK / FK / unicidade | Regra / descrição |
|---|---|---|---|---|---|
| A definir | A definir | A definir | A definir | A definir | A definir |

Para os relacionamentos:

| Origem → destino | Cardinalidade | Anotação JPA | Lado proprietário / FK | Cascade / exclusão |
|---|---|---|---|---|
| A definir | A definir | A definir | A definir | A definir |

Relacionar as interfaces `JpaRepository`, as entidades e os tipos dos identificadores. Validar persistência, associações e restrições no H2 e registrar os resultados. Essas tabelas são modelos de preenchimento, não evidências de implementação.

## Acompanhamento

Consulte o [TODO](todo.md) e o [contexto](context.md). Os requisitos descrevem o produto pretendido; código e validação demonstram o que foi implementado.
