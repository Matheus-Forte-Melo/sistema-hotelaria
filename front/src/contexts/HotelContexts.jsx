import { createContext, useContext, useEffect, useState } from 'react'

const EstruturaContext = createContext(null)
const ReservasContext = createContext(null)

function useLista(repository) {
  const [resultado, setResultado] = useState({ carregando: true })
  useEffect(() => {
    let ativo = true
    setResultado({ carregando: true })
    async function carregar() {
      try {
        const dados = await repository.listar()
        if (ativo) setResultado({ dados })
      } catch {
        if (ativo) setResultado({ erro: true })
      }
    }
    carregar()
    return () => { ativo = false }
  }, [repository])
  return resultado
}

export function HotelProvider({ repositories, children }) {
  const estrutura = useLista(repositories.estrutura)
  const reservas = useLista(repositories.reservas)
  return (
    <EstruturaContext.Provider value={estrutura}>
      <ReservasContext.Provider value={reservas}>{children}</ReservasContext.Provider>
    </EstruturaContext.Provider>
  )
}

export const useEstrutura = () => useContext(EstruturaContext)
export const useReservas = () => useContext(ReservasContext)
