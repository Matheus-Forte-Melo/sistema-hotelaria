# Commits, pull requests e testes

## Mensagem de commit

Use `tipo/<mensagem>` como título do commit. Depois da barra, escreva uma frase curta que descreva a mudança.

| Tipo | Quando usar | Exemplo |
|---|---|---|
| feat | Acrescentar uma funcionalidade | `feat/lista quartos disponíveis por período` |
| fix | Corrigir um comportamento | `fix/libera o quarto na data de saída` |
| chore | Manutenção, configuração ou documentação | `chore/documenta o fluxo de reservas` |

## Escopo do PR

Um PR pode entregar o backend, o frontend ou o par completo. Indique o escopo e use uma linha `Closes #N` por issue cujos critérios de aceite foram atendidos. Para mencionar uma issue relacionada que continua aberta, escreva `Relacionada a #N`. Exemplo para o mapa temporal:

| Entrega | Referências na descrição |
|---|---|
| Somente backend | `Closes #5` e `Relacionada a #10` |
| Somente frontend | `Closes #10` e `Relacionada a #5` |
| Backend e frontend | `Closes #5` e `Closes #10`, em linhas separadas |

Preencha as três seções do [modelo de PR](../.github/pull_request_template.md). Em **O QUE FOI FEITO**, descreva o comportamento entregue e identifique as issues. Em **POR QUE**, explique a regra, o problema ou o cenário que motivou a mudança. Em **TESTES**, registre os cenários, os comandos executados e os resultados. Informe qualquer verificação pendente.

## Testes unitários e TDD

Inclua testes unitários para comportamentos novos ou corrigidos em cada lado alterado. No backend, cubra as regras do domínio ou do serviço. No frontend, cubra comportamentos observáveis dos componentes ou contextos. Quando a garantia depender do banco, como em duas reservas simultâneas para o mesmo quarto, complemente com um teste de integração.

Agentes devem preferir TDD. Escolha um critério de aceite, escreva um teste que falhe pelo motivo esperado, implemente o comportamento, confirme que passou e refatore com os testes verdes. Repita para o próximo critério. Para alterações exclusivamente documentais, registre em TESTES a conferência feita, como a validação de links.

## Trabalhar enquanto a API está em desenvolvimento

**Frontend.** Recomendamos TanStack Query para dados assíncronos. A home já usa esse caminho: componente → `useReservas()` ou `useEstrutura()` → consulta TanStack → método do repositório injetado em `HotelProvider`. Com os [repositórios de demonstração](../front/src/data/mockRepositories.js), basta rodar `npm ci` e `npm run dev` em `front/` para trabalhar nas telas. O healthcheck continuará informando se o Spring está disponível.

1. Combine o formato mínimo de entrada e saída com o responsável pela issue do backend. Para uma tela de reservas, use o mesmo `quartoId` presente na lista de quartos e datas no formato `AAAA-MM-DD`.
2. Se sua tela precisa apenas da lista, use o hook existente. Ele já recebe os dados simulados. Se precisar de outra operação, acrescente um método assíncrono no repositório da funcionalidade e exponha-o por um hook.
3. Se até o hook de um colega estiver pendente e isso bloquear a tela, use um hook temporário com a mesma assinatura. Por exemplo, para renderizar um seletor de andares enquanto a consulta é combinada:

   ```js
   // Dentro da feature que está sendo desenvolvida
   function useAndaresMock() {
     return { dados: [{ id: 1, numero: 1 }], carregando: false, erro: false }
   }
   ```

   Registre a substituição pendente na issue de frontend e vincule a issue do backend. Assim que o hook compartilhado estiver disponível, troque a importação e valide a tela com o repositório integrado.
4. Quando a API estiver pronta, implemente o mesmo método com HTTP. Por exemplo, no ponto de inicialização, `{ ...mockRepositories, reservas: reservasHttp }` mantém a estrutura simulada e lê as reservas via API. Preserve o contrato de `reservas.listar()`. No navegador, valide também erro e lista vazia e ajuste o rótulo de dados de demonstração.

Para novas consultas, use `useQuery` e inclua os filtros relevantes na chave, por exemplo `['mapa', andarId, data]`. Para salvar, editar ou cancelar, prefira `useMutation`. Após o sucesso, invalide as consultas afetadas, como reservas, disponibilidade e mapa. Para campos do formulário e seleção visual, use estado local do React. Rode `npm test` e `npm run build` em `front/`.

**Backend.** Recomendamos testar regras com entidades reais construídas no teste. Para consultas, vínculos, conflitos e transações, grave registros de teste em H2 e use os repositórios JPA reais. O [teste de persistência](../back/src/test/java/br/com/hotelaria/hotelaria/repository/PersistenciaJpaTest.java) serve de exemplo. Isso permite trabalhar sem o frontend ou os serviços de outra issue e verificar o comportamento do banco. Execute `mvn test` em `back/`.
