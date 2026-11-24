import { createRoot } from 'react-dom/client'
import { HotelProvider } from './contexts/HotelContexts'
import { mockRepositories } from './data/mockRepositories'
import Home from './Home'
import './styles.css'

createRoot(document.getElementById('root')).render(
  <HotelProvider repositories={mockRepositories}><Home /></HotelProvider>,
)
