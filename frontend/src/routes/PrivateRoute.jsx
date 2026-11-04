import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '../context/useAuth'
import { rotaInicial } from '../utils/formato'

/**
 * Sem sessão → login (guardando para onde a pessoa queria ir).
 * Com sessão mas perfil errado → página inicial do próprio perfil.
 */
export default function PrivateRoute({ perfis }) {
  const { usuario } = useAuth()
  const location = useLocation()

  if (!usuario) {
    return <Navigate to="/login" replace state={{ de: location.pathname + location.search }} />
  }
  if (perfis && !perfis.includes(usuario.perfil)) {
    return <Navigate to={rotaInicial(usuario.perfil)} replace />
  }
  return <Outlet />
}
