import { useReducer, useState } from 'react'
import { useBuscaReservas, useCancelarReserva, useReserva } from '../../../contexts/HotelContexts'
import { fluxoCancelamento, formatarMoeda, rotuloPeriodo } from './consulta'

function Situacao({ valor }) {
  return <span className={valor === 'Vigente' ? 'active' : 'inactive'}>{valor}</span>
}

function Detalhes({ id, onEditar }) {
  const reserva = useReserva(id)
  const cancelamento = useCancelarReserva()
  const [fluxo, despachar] = useReducer(fluxoCancelamento, undefined, () => fluxoCancelamento(undefined, { tipo: 'desistir' }))

  if (reserva.erro) return <p role="alert">{reserva.mensagemErro ?? 'Falha ao carregar a reserva.'}</p>
  if (reserva.carregando || !reserva.dados) return <p role="status">Carregando a reserva…</p>
  const r = reserva.dados
  const confirmando = fluxo.etapa === 'confirmando' || fluxo.etapa === 'enviando' || fluxo.etapa === 'erro'

  function confirmar() {
    despachar({ tipo: 'confirmar' })
    cancelamento.mutate({ reservaId: r.id }, {
      onSuccess: () => despachar({ tipo: 'concluir' }),
      onError: erro => despachar({ tipo: 'falhar', mensagem: erro.message }),
    })
  }

  return <aside className="preview-details" aria-label={`Detalhes da reserva de ${r.hospede}`}>
    <h3 className="font-semibold">Reserva de {r.hospede}</h3>
    <dl className="mt-2 grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-sm">
      <dt>Quarto</dt><dd>{r.quarto}{r.andar != null && ` · ${r.andar}º andar`}</dd>
      <dt>Período</dt><dd>{rotuloPeriodo(r.entrada, r.saida)}</dd>
      <dt>Pessoas</dt><dd>{r.pessoas}</dd>
      <dt>Diária acordada</dt><dd>{formatarMoeda(r.diaria)}</dd>
      <dt>Total previsto</dt><dd>{r.diarias} {r.diarias === 1 ? 'diária' : 'diárias'} · {formatarMoeda(r.total)}</dd>
      <dt>Situação</dt><dd><Situacao valor={r.situacao} /></dd>
    </dl>
    {r.situacao === 'Vigente' && !confirmando && <div className="mt-3 flex flex-wrap gap-2">
      <button type="button" onClick={() => onEditar?.(r.id)}>Editar</button>
      <button type="button" onClick={() => despachar({ tipo: 'pedir', reservaId: r.id })}>Cancelar reserva</button>
    </div>}
    {r.situacao === 'Vigente' && confirmando && <div role="alertdialog" aria-label="Confirmar cancelamento"
      className="mt-3 rounded border border-amber-300 bg-amber-50 p-3 text-sm">
      <p>Cancelar a reserva de <strong>{r.hospede}</strong> no quarto {r.quarto}, de {rotuloPeriodo(r.entrada, r.saida)}?
        O período fica livre para outra reserva e o registro continua disponível para consulta.</p>
      {fluxo.erro && <p role="alert" className="mt-2 text-red-800">{fluxo.erro}</p>}
      <div className="mt-3 flex flex-wrap gap-2">
        <button type="button" disabled={fluxo.etapa === 'enviando'} onClick={confirmar}>
          {fluxo.etapa === 'enviando' ? 'Cancelando…' : 'Confirmar cancelamento'}</button>
        <button type="button" disabled={fluxo.etapa === 'enviando'} onClick={() => despachar({ tipo: 'desistir' })}>Desistir</button>
      </div>
    </div>}
  </aside>
}

export default function ConsultaReservas({ onEditar }) {
  const [nome, setNome] = useState('')
  const [termo, setTermo] = useState('')
  const [selecionada, setSelecionada] = useState(null)
  const busca = useBuscaReservas(termo)

  function buscar(evento) {
    evento.preventDefault()
    setTermo(nome)
    setSelecionada(null)
  }

  return <section id="reservas" className="feature">
    <h2 className="section-title">Consultar reservas</h2>
    <form className="campos" onSubmit={buscar} role="search">
      <label>Nome do hóspede<input type="search" value={nome} onChange={e => setNome(e.target.value)} /></label>
      <button type="submit" disabled={!nome.trim()}>Buscar</button>
    </form>
    {busca.erro && <p role="alert">{busca.mensagemErro ?? 'Falha ao buscar as reservas.'}</p>}
    {busca.carregando && <p role="status">Buscando reservas…</p>}
    {busca.dados?.length === 0 && <p role="status">Nenhuma reserva encontrada para “{termo.trim()}”.</p>}
    {busca.dados?.length > 0 && <div className="overflow-x-auto"><table>
      <thead><tr>{['Hóspede', 'Quarto', 'Período', 'Situação', 'Ações'].map(t => <th key={t}>{t}</th>)}</tr></thead>
      <tbody>{busca.dados.map(r => <tr key={r.id} aria-selected={r.id === selecionada}
        className={r.id === selecionada ? 'bg-stone-100' : undefined}>
        <th scope="row">{r.hospede}</th>
        <td>{r.quarto}{r.andar != null && <span className="text-stone-500"> · {r.andar}º andar</span>}</td>
        <td>{rotuloPeriodo(r.entrada, r.saida)}</td>
        <td><Situacao valor={r.situacao} /></td>
        <td><button type="button" onClick={() => setSelecionada(r.id)}>Detalhes</button></td>
      </tr>)}</tbody>
    </table></div>}
    {selecionada != null && <Detalhes key={selecionada} id={selecionada} onEditar={onEditar} />}
  </section>
}
