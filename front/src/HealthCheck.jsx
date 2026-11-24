import { useEffect, useState } from 'react'

export default function HealthCheck() {
  const [tentativa, setTentativa] = useState(0)
  const [estado, setEstado] = useState('Verificando')
  useEffect(() => {
    const controller = new AbortController()
    let ativo = true
    const timeout = setTimeout(() => controller.abort(), 5000)
    async function verificar() {
      try {
        const resposta = await fetch('/api/health', { signal: controller.signal, cache: 'no-store' })
        if (!resposta.ok || (await resposta.json()).status !== 'UP') throw new Error()
        if (ativo) setEstado('Conectada')
      } catch {
        if (ativo) setEstado('Indisponível')
      } finally {
        clearTimeout(timeout)
      }
    }
    verificar()
    return () => { ativo = false; clearTimeout(timeout); controller.abort() }
  }, [tentativa])

  return <section className="connection" aria-label="Conexão com o backend">
    <div><h2>Conexão com a API</h2>
      <p role="status"><span className={'dot ' + (estado === 'Conectada' ? 'online' : '')} />{estado}</p>
    </div>
    <button disabled={estado === 'Verificando'} onClick={() => {
      setEstado('Verificando')
      setTentativa(valor => valor + 1)
    }}>Verificar conexão</button>
  </section>
}
