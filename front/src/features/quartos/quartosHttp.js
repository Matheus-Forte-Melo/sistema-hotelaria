// Cadastro, listagem e disponibilidade de quartos pela API do backend (issue #2).
// Mantém os campos do repositório de demonstração: `numero` é a identificação e `diaria` a diária padrão.
function daApi(quarto) {
  return {
    id: quarto.id, andarId: quarto.andarId, andar: quarto.andarNumero, numero: quarto.identificacao,
    capacidade: quarto.capacidade, diaria: Number(quarto.valorDiariaPadrao),
  }
}

/** Recebe o fetch para permitir testes sem rede. As recusas viram erros com a mensagem devolvida pela API. */
export function criarQuartosHttp(fetchApi = globalThis.fetch) {
  async function pedir(url, opcoes) {
    const resposta = await fetchApi(url, opcoes)
    const corpo = await resposta.json().catch(() => null)
    if (!resposta.ok) throw new Error(corpo?.mensagem ?? `Falha na API (${resposta.status})`)
    return corpo
  }
  return {
    async listar(andarId) {
      const filtro = andarId == null ? '' : `?andarId=${encodeURIComponent(andarId)}`
      return (await pedir(`/api/quartos${filtro}`)).map(daApi)
    },
    async cadastrar({ andarId, numero, capacidade, diaria }) {
      return daApi(await pedir('/api/quartos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          andarId: Number(andarId), identificacao: String(numero).trim(),
          capacidade: Number(capacidade), valorDiariaPadrao: Number(diaria),
        }),
      }))
    },
    async buscarDisponiveis({ entrada, saida, pessoas }) {
      const filtro = new URLSearchParams({ entrada, saida, pessoas: String(pessoas) })
      return (await pedir(`/api/quartos/disponiveis?${filtro}`)).map(daApi)
    },
  }
}
