import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createRoot } from 'react-dom/client'
import { HotelProvider } from './contexts/HotelContexts'
import { mockRepositories } from './data/mockRepositories'
import { criarQuartosMock } from './features/quartos/quartosMock'
import Home from './Home'
import './styles.css'

const queryClient = new QueryClient()
// Quartos de demonstração: partem da estrutura e consultam as reservas dos mocks.
// Com a API liberada, trocar por criarQuartosHttp().
const repositories = { ...mockRepositories, quartos: criarQuartosMock(mockRepositories) }

createRoot(document.getElementById('root')).render(
  <QueryClientProvider client={queryClient}>
    <HotelProvider repositories={repositories}><Home /></HotelProvider>
  </QueryClientProvider>,
)
