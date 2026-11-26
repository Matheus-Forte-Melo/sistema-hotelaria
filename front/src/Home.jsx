import { useEstrutura, useReservas } from './contexts/HotelContexts'
import HealthCheck from './HealthCheck'
import SecaoFormularioReserva from './features/reservas/formulario/SecaoFormularioReserva'

const formatarData = data => data.split('-').reverse().join('/')

const navegacao = [
  ['inicio', 'Visão geral'], ['mapa', 'Mapa temporal'], ['reservas', 'Consultar reservas'],
  ['formulario', 'Criar e editar reserva'], ['quartos', 'Quartos e disponibilidade'], ['andares', 'Andares'],
]

function Campos({ itens, acao }) {
  return <fieldset disabled className="campos">
    {itens.map(([nome, tipo]) => <label key={nome}>{nome}<input type={tipo} /></label>)}
    <button type="button">{acao}</button>
  </fieldset>
}

function Painel() {
  const estrutura = useEstrutura()
  const reservas = useReservas()
  if (estrutura.erro || reservas.erro) return <p role="alert">Falha ao carregar o painel.</p>
  if (estrutura.carregando || reservas.carregando) return <p role="status">Carregando o painel…</p>
  const { andares, quartos } = estrutura.dados
  const vigentes = reservas.dados.filter(reserva => reserva.situacao === 'Vigente')
  return <>
    <dl className="grid grid-cols-1 gap-3 sm:grid-cols-3">
      {[[andares.length, 'Andares'], [quartos.length, 'Quartos'], [vigentes.length, 'Reservas vigentes']].map(([quantidade, titulo]) =>
        <div key={titulo} className="metric"><dt>{titulo}</dt><dd>{quantidade}</dd></div>)}
    </dl>
    <section id="mapa" className="mapa">
      <h2 className="section-title">Mapa temporal</h2>
      <fieldset disabled className="mapa-bar">
        <label>Andar <select><option>1º andar</option></select></label>
        <label>Data <input type="date" defaultValue="2026-11-24" /></label>
        <button aria-label="Dia anterior">←</button><button>Hoje</button><button aria-label="Próximo dia">→</button>
      </fieldset>
      <div className="mapa-body">
        <div><p className="map-summary">1 de 2 quartos reservados</p><div className="room-grid">
          <div className="room reserved"><strong>101</strong><span>Reservado</span></div>
          <div className="room"><strong>102</strong><span>Livre</span></div>
        </div></div>
        <aside className="room-detail"><h3>Detalhes do quarto 101</h3><strong>Marina Costa</strong><p>24 a 26 de novembro · 2 pessoas</p></aside>
      </div>
    </section>
    <section id="reservas" className="feature"><h2 className="section-title">Consultar reservas</h2>
      <Campos itens={[[ 'Nome do hóspede', 'search' ]]} acao="Buscar" />
      <div className="overflow-x-auto"><table>
        <thead><tr>{['Hóspede', 'Quarto', 'Entrada', 'Saída', 'Situação', 'Ações'].map(t => <th key={t}>{t}</th>)}</tr></thead>
        <tbody>{reservas.dados.map(r => <tr key={r.id}>
          <th scope="row">{r.hospede}</th><td>{quartos.find(q => q.id === r.quartoId)?.numero ?? '—'}</td>
          <td>{formatarData(r.entrada)}</td><td>{formatarData(r.saida)}</td><td><span className={r.situacao === 'Vigente' ? 'active' : 'inactive'}>{r.situacao}</span></td>
          <td><button type="button" disabled>Detalhes</button></td>
        </tr>)}</tbody>
      </table></div>
      <details className="preview-details"><summary>Prévia dos detalhes da reserva</summary>
        <p>Marina Costa · Quarto 101 · 2 pessoas</p><p>2 diárias de R$ 180 · Total previsto de R$ 360</p>
        <fieldset disabled className="flex flex-wrap gap-2 mt-3"><button>Editar</button><button>Cancelar reserva</button></fieldset>
      </details>
    </section>
    <section id="formulario" className="feature"><h2 className="section-title">Criar e editar reserva</h2>
      <SecaoFormularioReserva />
    </section>
    <section id="quartos" className="feature"><h2 className="section-title">Quartos e disponibilidade</h2>
      <h3>Disponibilidade por período</h3>
      <Campos itens={[[ 'Entrada', 'date' ], ['Saída', 'date'], ['Pessoas', 'number']]} acao="Buscar quartos" />
      <details className="preview-details"><summary>Cadastro de quarto</summary>
        <Campos itens={[[ 'Andar', 'number' ], ['Identificação', 'text'], ['Capacidade', 'number'], ['Diária padrão (R$)', 'number']]} acao="Cadastrar quarto" />
      </details>
    </section>
    <section id="andares" className="feature"><h2 className="section-title">Andares</h2>
      <Campos itens={[[ 'Número do andar', 'number' ]]} acao="Cadastrar andar" />
      <ul className="floor-list">{andares.map(andar => <li key={andar.id}>{andar.numero}º andar</li>)}</ul>
    </section>
  </>
}

export default function Home() {
  return <><header><div className="shell flex items-center justify-between"><b className="brand">Pernoite</b><span>Recepção</span></div></header>
    <div className="shell dashboard">
      <nav aria-label="Seções da recepção"><div className="nav-list">{navegacao.map(([id, titulo]) =>
        <a key={id} href={'#' + id}>{titulo}</a>)}</div></nav>
      <main id="inicio"><h1>Visão geral</h1><p className="mb-7 text-stone-600">Prévia das telas de recepção com dados de demonstração.</p>
        <Painel /><HealthCheck />
      </main>
    </div></>
}
