import { useState } from 'react'
import { useEstrutura, useSalvarReserva } from '../../../contexts/HotelContexts'
import {
  calcularPrevisao, formatarMoeda, formularioDaReserva, formularioVazio, opcoesDeQuarto, paraPedido, validarFormulario,
} from './logicaFormulario'
import { useQuartosDisponiveisTemporario } from './useQuartosDisponiveisTemporario'

function Campo({ nome, rotulo, erro, children }) {
  return <label className="campo-reserva">
    {rotulo}
    {children}
    {erro && <span id={`${nome}-erro`} className="erro-campo">{erro}</span>}
  </label>
}

/**
 * Formulário único para criar (sem `reserva`) e editar (com `reserva`) uma reserva.
 * Se a gravação for recusada, os campos continuam preenchidos e o motivo é exibido.
 */
export default function FormularioReserva({ reserva = null, onSalva }) {
  const editando = reserva != null
  const [valores, setValores] = useState(() => editando ? formularioDaReserva(reserva) : formularioVazio())
  const [tentouEnviar, setTentouEnviar] = useState(false)
  const [aviso, setAviso] = useState(null)
  const estrutura = useEstrutura()
  const { salvar, salvando, erro: erroApi, reiniciar } = useSalvarReserva()
  const disponibilidade = useQuartosDisponiveisTemporario({
    entrada: valores.entrada, saida: valores.saida, pessoas: valores.pessoas, ignorarReservaId: reserva?.id ?? null,
  })

  const quartos = estrutura.dados?.quartos ?? []
  const andares = estrutura.dados?.andares ?? []
  const quartoEscolhido = quartos.find(quarto => String(quarto.id) === valores.quartoId) ?? null
  const erros = validarFormulario(valores, quartoEscolhido)
  const errosVisiveis = tentouEnviar ? erros : {}
  const previsao = calcularPrevisao(valores)
  const opcoes = opcoesDeQuarto(disponibilidade.dados, quartos, valores.quartoId)
  const periodoInformado = valores.entrada && valores.saida && valores.saida > valores.entrada

  function alterar(campo, valor) {
    setValores(atuais => {
      const novos = { ...atuais, [campo]: valor }
      if (campo === 'quartoId' && atuais.diaria === '') {
        const quarto = quartos.find(q => String(q.id) === valor)
        if (quarto) novos.diaria = String(quarto.diaria)
      }
      return novos
    })
    setAviso(null)
    if (erroApi) reiniciar()
  }

  function enviar(evento) {
    evento.preventDefault()
    setTentouEnviar(true)
    if (Object.keys(erros).length > 0) return
    salvar({ id: reserva?.id, pedido: paraPedido(valores) }, {
      onSuccess: salva => {
        setTentouEnviar(false)
        setAviso(editando ? 'Alterações salvas.' : `Reserva de ${salva.hospede} criada no quarto ${quartoEscolhido?.numero}.`)
        if (!editando) setValores(formularioVazio())
        onSalva?.(salva)
      },
    })
  }

  const ligarErro = nome => errosVisiveis[nome]
    ? { 'aria-invalid': true, 'aria-describedby': `${nome}-erro` }
    : {}
  const numeroDoAndar = quarto => andares.find(andar => andar.id === quarto.andarId)?.numero

  return <form className="form-reserva" onSubmit={enviar} noValidate>
    <div className="form-grade">
      <Campo nome="hospede" rotulo="Nome do hóspede" erro={errosVisiveis.hospede}>
        <input value={valores.hospede} onChange={e => alterar('hospede', e.target.value)} maxLength={120}
          autoComplete="off" {...ligarErro('hospede')} />
      </Campo>
      <Campo nome="pessoas" rotulo="Pessoas" erro={errosVisiveis.pessoas}>
        <input type="number" min="1" step="1" value={valores.pessoas}
          onChange={e => alterar('pessoas', e.target.value)} {...ligarErro('pessoas')} />
      </Campo>
      <Campo nome="entrada" rotulo="Entrada" erro={errosVisiveis.entrada}>
        <input type="date" value={valores.entrada} onChange={e => alterar('entrada', e.target.value)}
          {...ligarErro('entrada')} />
      </Campo>
      <Campo nome="saida" rotulo="Saída" erro={errosVisiveis.saida}>
        <input type="date" value={valores.saida} min={valores.entrada || undefined}
          onChange={e => alterar('saida', e.target.value)} {...ligarErro('saida')} />
      </Campo>
      <Campo nome="quartoId" rotulo="Quarto" erro={errosVisiveis.quartoId}>
        <select value={valores.quartoId} onChange={e => alterar('quartoId', e.target.value)}
          disabled={!periodoInformado && !valores.quartoId} {...ligarErro('quartoId')}>
          <option value="">
            {!periodoInformado ? 'Informe o período primeiro'
              : opcoes.length === 0 ? 'Nenhum quarto disponível' : 'Escolha um quarto'}
          </option>
          {opcoes.map(({ quarto, disponivel }) => <option key={quarto.id} value={quarto.id}>
            {quarto.numero} · {numeroDoAndar(quarto)}º andar · até {quarto.capacidade} pessoa(s) · {formatarMoeda(quarto.diaria)}
            {disponivel ? '' : ' — indisponível no período'}
          </option>)}
        </select>
      </Campo>
      <Campo nome="diaria" rotulo="Diária acordada (R$)" erro={errosVisiveis.diaria}>
        <input type="number" min="0" step="0.01" value={valores.diaria}
          onChange={e => alterar('diaria', e.target.value)} {...ligarErro('diaria')} />
      </Campo>
    </div>

    {periodoInformado && !disponibilidade.carregando && disponibilidade.dados.length === 0 &&
      <p className="dica">Nenhum quarto comporta {valores.pessoas} pessoa(s) em todas as noites desse período.</p>}
    {quartoEscolhido && !opcoes.find(opcao => opcao.quarto.id === quartoEscolhido.id)?.disponivel && periodoInformado &&
      <p className="dica">O quarto {quartoEscolhido.numero} aparece ocupado nesse período. Ao salvar, o sistema confirmará.</p>}

    <div className="form-rodape">
      <p className="total" aria-live="polite">
        Total previsto
        {previsao
          ? <strong>{formatarMoeda(previsao.total)}</strong>
          : <strong>R$ —</strong>}
        {previsao && <span className="detalhe-total">
          {previsao.noites} diária{previsao.noites > 1 ? 's' : ''} de {formatarMoeda(Number(valores.diaria))}
        </span>}
      </p>
      <button type="submit" className="primario" disabled={salvando}>
        {salvando ? 'Salvando…' : editando ? 'Salvar alterações' : 'Criar reserva'}
      </button>
    </div>

    {erroApi && <p role="alert" className="alerta">Não foi possível salvar: {erroApi}</p>}
    {aviso && <p role="status" className="sucesso">{aviso}</p>}
  </form>
}
