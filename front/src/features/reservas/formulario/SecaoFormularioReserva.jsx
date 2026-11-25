import { useState } from 'react'
import { useEstrutura, useReservas } from '../../../contexts/HotelContexts'
import FormularioReserva from './FormularioReserva'

const formatarData = data => data.split('-').reverse().join('/')

/**
 * Seção "Criar e editar reserva". Até a consulta da issue #9 abrir a edição, a reserva a editar
 * é escolhida aqui mesmo; depois basta renderizar <FormularioReserva reserva={...} />.
 */
export default function SecaoFormularioReserva() {
  const [reservaId, setReservaId] = useState('')
  const reservas = useReservas()
  const estrutura = useEstrutura()

  if (reservas.erro || estrutura.erro) return <p role="alert">Falha ao carregar os dados do formulário.</p>
  if (reservas.carregando || estrutura.carregando) return <p role="status">Carregando o formulário…</p>

  const vigentes = reservas.dados.filter(reserva => reserva.situacao === 'Vigente')
  const reserva = vigentes.find(r => String(r.id) === reservaId) ?? null
  const numeroDoQuarto = id => estrutura.dados.quartos.find(quarto => quarto.id === id)?.numero ?? '—'

  return <>
    <label className="modo-formulario">
      Modo
      <select value={reserva ? reservaId : ''} onChange={e => setReservaId(e.target.value)}>
        <option value="">Nova reserva</option>
        {vigentes.map(r => <option key={r.id} value={r.id}>
          Editar: {r.hospede} · quarto {numeroDoQuarto(r.quartoId)} · {formatarData(r.entrada)} a {formatarData(r.saida)}
        </option>)}
      </select>
    </label>
    <FormularioReserva key={reserva?.id ?? 'nova'} reserva={reserva} />
  </>
}
