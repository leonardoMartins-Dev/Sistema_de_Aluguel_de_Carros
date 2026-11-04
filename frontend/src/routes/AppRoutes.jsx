import { useState } from 'react'
import { Navigate, Route, Routes } from 'react-router'
import Layout from '../components/Layout'
import { useAuth } from '../context/useAuth'
import Automoveis from '../pages/Automoveis'
import Cadastro from '../pages/Cadastro'
import Login from '../pages/Login'
import PedidoForm from '../pages/PedidoForm'
import Contratos from '../pages/agente/Contratos'
import PedidosAnalise from '../pages/agente/PedidosAnalise'
import MeusDados from '../pages/cliente/MeusDados'
import MeusPedidos from '../pages/cliente/MeusPedidos'
import { rotaInicial } from '../utils/formato'
import PrivateRoute from './PrivateRoute'

const AGENTES = ['EMPRESA', 'BANCO']

// Login e cadastro só para quem não está logado.
// Vale para quem chegou já logado; quem acabou de entrar nesta página é levado pela própria página
// (senão este redirecionamento disputa com o navigate do Login/Cadastro).
function SoVisitante({ children }) {
  const { usuario } = useAuth()
  const [chegouLogado] = useState(() => usuario !== null)
  return chegouLogado ? <Navigate to={rotaInicial(usuario.perfil)} replace /> : children
}

function Inicio() {
  const { usuario } = useAuth()
  return <Navigate to={rotaInicial(usuario.perfil)} replace />
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<SoVisitante><Login /></SoVisitante>} />
      <Route path="/cadastro" element={<SoVisitante><Cadastro /></SoVisitante>} />

      <Route element={<PrivateRoute />}>
        <Route element={<Layout />}>
          <Route index element={<Inicio />} />
          <Route path="/automoveis" element={<Automoveis />} />
          {/* Cliente modifica os próprios pedidos; agentes modificam qualquer um em análise */}
          <Route path="/pedidos/:id/editar" element={<PedidoForm />} />

          <Route element={<PrivateRoute perfis={['CLIENTE']} />}>
            <Route path="/pedidos" element={<MeusPedidos />} />
            <Route path="/pedidos/novo" element={<PedidoForm />} />
            <Route path="/meus-dados" element={<MeusDados />} />
          </Route>

          <Route element={<PrivateRoute perfis={AGENTES} />}>
            <Route path="/analise" element={<PedidosAnalise />} />
            <Route path="/contratos" element={<Contratos />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
