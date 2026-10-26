# UML do Pernoite

Revisão de 25/10/2026, baseada nas classes de `src/main/java/br/com/hotelaria/hotelaria/model`.

- `pernoite-completo.puml`: modelo consolidado com nove entidades e quatro enums.
- `01-associacoes` a `06-correcoes`: vistas complementares usadas na seção 3.1 do documento, em PlantUML e PNG.
- A divisão em vistas é apenas visual: todas pertencem ao mesmo modelo e pacote Java.

## Conferência de consistência

- Reserva–Hospede usa `0..*` nas duas extremidades, corrigindo o `0..` incompleto do Mermaid enviado. A lista pode estar incompleta antes do check-in.
- Foram incluídos os campos de associação Java, como `Reserva.quarto`, `hospedes`, `criadaPor`, `correcoes` e as referências de autoria dos bloqueios e correções.
- Cada associação `@ManyToOne(optional = false)` aponta para exatamente uma entidade; o outro lado admite zero ou muitos registros.
- As duas coleções de correções são bidirecionais. As referências ao funcionário são unidirecionais; não foram inventadas coleções em `Funcionario`.
- Reserva e hospedagem são representadas por `Reserva`; não existe entidade `Hospedagem` no código.
- Não há associação 1:1 ou herança no modelo atual. Não se deve acrescentá-las apenas para exemplificar notação.
- Foram omitidos construtores, acessores, `equals/hashCode`, constantes e auxiliares privados. Os atributos de instância e métodos de negócio representados seguem os tipos Java.
- Relações não implicam que todas as regras já estejam implementadas: os serviços de disponibilidade, concorrência e unicidade de quarto no bloco continuam necessários.

## Renderização local

Com Java, Graphviz e o JAR do PlantUML:

```bash
java -Djava.awt.headless=true -jar plantuml.jar -charset UTF-8 -tpng 'entregas/uml/0*.puml'
java -Djava.awt.headless=true -jar plantuml.jar -charset UTF-8 -tsvg entregas/uml/pernoite-completo.puml
```

Sintaxe: https://plantuml.com/class-diagram
