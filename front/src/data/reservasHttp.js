// Busca, detalhes e cancelamento pela API do backend (issue #4). Mantém os campos dos repositórios de demonstração.
const SITUACOES = { CONFIRMADA: 'Vigente', CANCELADA: 'Cancelada', EM_ANDAMENTO: 'Em andamento', FINALIZADA: 'Finalizada' }

function daApi(reserva) {
  return {
    id: reserva.id, hospede: reserva.nomeHospede, quartoId: reserva.quartoId, quarto: reserva.quartoIdentificacao,
    andar: reserva.andarNumero, entrada: reserva.dataEntrada, saida: reserva.dataSaida, pessoas: reserva.quantidadePessoas,
    diaria: Number(reserva.valorDiaria), diarias: reserva.quantidadeDiarias, total: Number(reserva.valorTotal),
    situacao: SITUACOES[reserva.situacao] ?? reserva.situacao,
  }
}

/** Recebe o fetch para permitir testes sem rede. As recusas viram erros com a mensagem devolvida pela API. */
export function criarReservasHttp(fetchApi = globalThis.fetch) {
  async function pedir(url, opcoes) {
    const resposta = await fetchApi(url, opcoes)
    const corpo = await resposta.json().catch(() => null)
    if (!resposta.ok) throw new Error(corpo?.mensagem ?? `Falha na API (${resposta.status})`)
    return corpo
  }
  return {
    async buscarPorNome(nome) {
      return (await pedir(`/api/reservas?nome=${encodeURIComponent(nome.trim())}`)).map(daApi)
    },
    async detalhar(id) {
      return daApi(await pedir(`/api/reservas/${id}`))
    },
    async cancelar(id, motivo) {
      return daApi(await pedir(`/api/reservas/${id}/cancelamento`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ motivo }),
      }))
    },
  }
}
