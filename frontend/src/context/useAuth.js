import { useContext } from 'react'
import { AuthContext } from './contextos'

export function useAuth() {
  return useContext(AuthContext)
}
