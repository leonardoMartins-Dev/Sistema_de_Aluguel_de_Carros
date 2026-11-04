import { startTransition, useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router'
import { auth, definirAoSessaoExpirar, lerSessao, limparSessao, salvarSessao } from '../services/api'
import { AuthContext } from './contextos'

// Descarta a sessão guardada se o token já expirou (campo exp do JWT)
function sessaoValida(sessao) {
  if (!sessao?.token) return null
  try {
    const payload = sessao.token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const { exp } = JSON.parse(atob(payload))
    return exp * 1000 > Date.now() ? sessao : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [sessao, setSessao] = useState(() => sessaoValida(lerSessao()))
  const navigate = useNavigate()

  const sair = useCallback(
    (aviso) => {
      limparSessao()
      // Mesma transição da navegação do React Router: as duas mudanças entram juntas na tela.
      // Sem isso a página protegida renderiza sem usuário e manda para o login lembrando a página antiga.
      startTransition(() => {
        setSessao(null)
        navigate('/login', { replace: true, state: aviso ? { aviso } : undefined })
      })
    },
    [navigate],
  )

  useEffect(() => {
    definirAoSessaoExpirar(() => sair('Sua sessão expirou. Entre de novo para continuar.'))
  }, [sair])

  const entrar = useCallback(async (login, senha) => {
    const resposta = await auth.login(login, senha)
    const nova = {
      token: resposta.token,
      usuario: { id: resposta.id, login: resposta.login, nome: resposta.nome, perfil: resposta.perfil },
    }
    salvarSessao(nova)
    setSessao(nova)
    return nova.usuario
  }, [])

  // Depois de editar "Meus dados", o nome no topo acompanha
  const atualizarNome = useCallback((nome) => {
    setSessao((atual) => {
      if (!atual) return atual
      const nova = { ...atual, usuario: { ...atual.usuario, nome } }
      salvarSessao(nova)
      return nova
    })
  }, [])

  const valor = useMemo(
    () => ({ usuario: sessao?.usuario ?? null, entrar, sair, atualizarNome }),
    [sessao, entrar, sair, atualizarNome],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}
