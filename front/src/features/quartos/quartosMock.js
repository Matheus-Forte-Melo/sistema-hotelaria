import { validarBusca } from './quartos.js'

/**
 * Repositório de demonstração dos quartos (issue #7). Parte dos quartos de `estrutura.listar()` e
 * consulta `reservas.listar()` a cada busca, então reservas criadas ou canceladas nos outros mocks
 * também mudam a disponibilidade. Mesmos campos e mensagens do repositório HTTP.
 */
export function criarQuartosMock({ estrutura, reservas }) {
  let dados = null

  async function carregar() {
    if (!dados) {
      const { andares, quartos } = await estrutura.listar()
      dados = { andares, quartos: quartos.map(q => ({ ...q })) }
    }
    return dados
  }

  const comAndar = (quarto, andares) => ({
    id: quarto.id, andarId: quarto.andarId, andar: andares.find(a => a.id === quarto.andarId)?.numero ?? null,
    numero: quarto.numero, capacidade: quarto.capacidade, diaria: quarto.diaria,
  })

  const ordenar = (a, b) => (a.andar ?? 0) - (b.andar ?? 0) || a.numero.localeCompare(b.numero, 'pt-BR', { numeric: true })

  return {
    async listar(andarId) {
      const { andares, quartos } = await carregar()
      return quartos.filter(q => andarId == null || q.andarId === Number(andarId))
        .map(q => comAndar(q, andares)).sort(ordenar)
    },

    async cadastrar({ andarId, numero, capacidade, diaria }) {
      const { andares, quartos } = await carregar()
      const andar = andares.find(a => a.id === Number(andarId))
      if (!andar) throw new Error(`Andar ${andarId} não encontrado`)
      const identificacao = String(numero).trim()
      if (quartos.some(q => q.numero.toLowerCase() === identificacao.toLowerCase())) {
        throw new Error(`Já existe um quarto com a identificação ${identificacao}`)
      }
      const quarto = {
        id: Math.max(0, ...quartos.map(q => q.id)) + 1, andarId: andar.id, numero: identificacao,
        capacidade: Number(capacidade), diaria: Number(diaria),
      }
      quartos.push(quarto)
      return comAndar(quarto, andares)
    },

    /** Quartos que comportam o grupo e não têm reserva vigente em nenhuma noite de [entrada, saída). */
    async buscarDisponiveis({ entrada, saida, pessoas }) {
      const erro = validarBusca({ entrada, saida, pessoas })
      if (erro) throw new Error(erro)
      const { andares, quartos } = await carregar()
      const ocupadas = (await reservas.listar())
        .filter(r => r.situacao !== 'Cancelada' && r.entrada < saida && r.saida > entrada)
        .map(r => r.quartoId)
      return quartos.filter(q => q.capacidade >= Number(pessoas) && !ocupadas.includes(q.id))
        .map(q => comAndar(q, andares)).sort(ordenar)
    },
  }
}
