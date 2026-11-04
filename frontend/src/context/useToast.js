import { useContext } from 'react'
import { ToastContext } from './contextos'

/** const avisar = useToast(); avisar('Pedido enviado para análise') */
export function useToast() {
  return useContext(ToastContext)
}
