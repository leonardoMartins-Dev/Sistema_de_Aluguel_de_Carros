import { createContext } from 'react'

// { usuario, entrar, sair, atualizarNome } — preenchido pelo AuthProvider
export const AuthContext = createContext(null)

// função avisar(texto, tipo) — preenchida pelo ToastProvider
export const ToastContext = createContext(() => {})
