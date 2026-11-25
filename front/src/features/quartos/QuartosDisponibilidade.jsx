import { useState } from 'react'
import { useEstrutura } from '../../contexts/HotelContexts'
import { useCadastrarQuarto, useDisponibilidade, useQuartos } from './hooks'
import { formatarMoeda, validarBusca, validarQuarto } from './quartos'

const formatarData = data => data.split('-').reverse().join('/')
const QUARTO_VAZIO = { numero: '', capacidade: '', diaria: '' }

// TODO(#6): trocar useEstrutura().andares pelo hook de andares quando a #6 for integrada.
function useAndares() {
  const { dados, carregando, erro } = useEstrutura()
  return { dados: dados?.andares, carregando, erro }
}

function CadastroQuarto({ andares }) {
  const [andarId, setAndarId] = useState(String(andares[0]?.id ?? ''))
  const [quarto, setQuarto] = useState(QUARTO_VAZIO)
  const [aviso, setAviso] = useState(null)
  const cadastro = useCadastrarQuarto()
  const lista = useQuartos(andarId === '' ? undefined : Number(andarId))
  const alterar = campo => evento => setQuarto(atual => ({ ...atual, [campo]: evento.target.value }))

  function cadastrar(evento) {
    evento.preventDefault()
    const pedido = { andarId, ...quarto }
    const erro = validarQuarto(pedido)
    if (erro) return setAviso({ erro: true, texto: erro })
    setAviso(null)
    cadastro.mutate(pedido, {
      onSuccess: criado => {
        setQuarto(QUARTO_VAZIO)
        setAviso({ erro: false, texto: `Quarto ${criado.numero} cadastrado no ${criado.andar}º andar.` })
      },
      onError: e => setAviso({ erro: true, texto: e.message }),
    })
  }

  return <div className="mt-8">
    <h3 className="font-semibold">Cadastrar quarto</h3>
    <form className="campos" onSubmit={cadastrar} noValidate>
      <label>Andar<select value={andarId} onChange={e => setAndarId(e.target.value)}>
        {andares.map(a => <option key={a.id} value={a.id}>{a.numero}º andar</option>)}
      </select></label>
      <label>Identificação<input value={quarto.numero} onChange={alterar('numero')} maxLength={10} /></label>
      <label>Capacidade<input type="number" min="1" step="1" value={quarto.capacidade} onChange={alterar('capacidade')} /></label>
      <label>Diária padrão (R$)<input type="number" min="0" step="0.01" value={quarto.diaria} onChange={alterar('diaria')} /></label>
      <button type="submit" disabled={cadastro.isPending}>{cadastro.isPending ? 'Cadastrando…' : 'Cadastrar quarto'}</button>
    </form>
    {aviso && <p role={aviso.erro ? 'alert' : 'status'} className={aviso.erro ? 'text-red-800' : 'text-green-800'}>{aviso.texto}</p>}
    {lista.erro && <p role="alert">{lista.mensagemErro ?? 'Falha ao carregar os quartos.'}</p>}
    {lista.dados?.length === 0 && <p className="text-stone-600">Nenhum quarto cadastrado neste andar.</p>}
    {lista.dados?.length > 0 && <ul className="floor-list mt-3" aria-label="Quartos do andar">
      {lista.dados.map(q => <li key={q.id}>{q.numero} · {q.capacidade} {q.capacidade === 1 ? 'vaga' : 'vagas'} · {formatarMoeda(q.diaria)}</li>)}
    </ul>}
  </div>
}

/**
 * Busca de disponibilidade e cadastro de quartos (issue #7). Ao escolher um quarto, chama
 * `onEscolher({ quartoId, numero, andar, diaria, entrada, saida, pessoas })`, ponto de integração com a #8.
 */
export default function QuartosDisponibilidade({ onEscolher }) {
  const andares = useAndares()
  const [busca, setBusca] = useState({ entrada: '', saida: '', pessoas: '' })
  const [filtro, setFiltro] = useState(null)
  const [aviso, setAviso] = useState(null)
  const [escolhido, setEscolhido] = useState(null)
  const disponiveis = useDisponibilidade(filtro)
  const alterar = campo => evento => setBusca(atual => ({ ...atual, [campo]: evento.target.value }))

  function buscar(evento) {
    evento.preventDefault()
    const erro = validarBusca(busca)
    setAviso(erro)
    setEscolhido(null)
    setFiltro(erro ? null : { ...busca, pessoas: Number(busca.pessoas) })
  }

  function escolher(quarto) {
    const selecao = { quartoId: quarto.id, numero: quarto.numero, andar: quarto.andar, diaria: quarto.diaria, ...filtro }
    setEscolhido(selecao)
    onEscolher?.(selecao)
  }

  return <section id="quartos" className="feature">
    <h2 className="section-title">Quartos e disponibilidade</h2>
    <h3 className="font-semibold">Disponibilidade por período</h3>
    <form className="campos" onSubmit={buscar} noValidate role="search">
      <label>Entrada<input type="date" value={busca.entrada} onChange={alterar('entrada')} /></label>
      <label>Saída<input type="date" value={busca.saida} min={busca.entrada || undefined} onChange={alterar('saida')} /></label>
      <label>Pessoas<input type="number" min="1" step="1" value={busca.pessoas} onChange={alterar('pessoas')} /></label>
      <button type="submit">Buscar quartos</button>
    </form>
    {aviso && <p role="alert" className="text-red-800">{aviso}</p>}
    {disponiveis.erro && <p role="alert" className="text-red-800">{disponiveis.mensagemErro ?? 'Falha ao buscar os quartos.'}</p>}
    {disponiveis.carregando && <p role="status">Buscando quartos…</p>}
    {filtro && disponiveis.dados?.length === 0 && <p role="status">
      Nenhum quarto comporta {filtro.pessoas} {filtro.pessoas === 1 ? 'pessoa' : 'pessoas'} em todas as noites
      de {formatarData(filtro.entrada)} a {formatarData(filtro.saida)}. Tente outro período ou outra quantidade de pessoas.
    </p>}
    {filtro && disponiveis.dados?.length > 0 && <div className="overflow-x-auto"><table>
      <caption className="sr-only">Quartos disponíveis de {formatarData(filtro.entrada)} a {formatarData(filtro.saida)}</caption>
      <thead><tr>{['Quarto', 'Andar', 'Capacidade', 'Diária padrão', 'Ação'].map(t => <th key={t}>{t}</th>)}</tr></thead>
      <tbody>{disponiveis.dados.map(q => <tr key={q.id} aria-selected={escolhido?.quartoId === q.id}
        className={escolhido?.quartoId === q.id ? 'bg-stone-100' : undefined}>
        <th scope="row">{q.numero}</th><td>{q.andar}º andar</td>
        <td>{q.capacidade} {q.capacidade === 1 ? 'pessoa' : 'pessoas'}</td><td>{formatarMoeda(q.diaria)}</td>
        <td><button type="button" onClick={() => escolher(q)}>{escolhido?.quartoId === q.id ? 'Escolhido' : 'Escolher'}</button></td>
      </tr>)}</tbody>
    </table></div>}
    {escolhido && <p role="status" className="total">Quarto {escolhido.numero} escolhido para {escolhido.pessoas}
      {escolhido.pessoas === 1 ? ' pessoa' : ' pessoas'} de {formatarData(escolhido.entrada)} a {formatarData(escolhido.saida)}
      {' · '}diária padrão {formatarMoeda(escolhido.diaria)}</p>}

    {andares.erro && <p role="alert">Falha ao carregar os andares.</p>}
    {andares.dados && <CadastroQuarto andares={andares.dados} />}
  </section>
}
