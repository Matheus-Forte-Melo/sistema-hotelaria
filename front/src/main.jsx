import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createRoot } from 'react-dom/client'
import { HotelProvider } from './contexts/HotelContexts'
import { mockRepositories } from './data/mockRepositories'
import Home from './Home'
import './styles.css'

const queryClient = new QueryClient()

createRoot(document.getElementById('root')).render(
  <QueryClientProvider client={queryClient}>
    <HotelProvider repositories={mockRepositories}><Home /></HotelProvider>
  </QueryClientProvider>,
)
