import { useCallback, useRef, useState } from 'react'
import { ToastContext } from './contextos'
import './Toast.css'

export function ToastProvider({ children }) {
  const [avisos, setAvisos] = useState([])
  const proximoId = useRef(0)

  const avisar = useCallback((texto, tipo = 'ok') => {
    const id = ++proximoId.current
    setAvisos((atuais) => [...atuais, { id, texto, tipo }])
    setTimeout(() => setAvisos((atuais) => atuais.filter((a) => a.id !== id)), 4500)
  }, [])

  return (
    <ToastContext.Provider value={avisar}>
      {children}
      <div className="toasts" role="status" aria-live="polite">
        {avisos.map((a) => (
          <p key={a.id} className={`toast toast--${a.tipo}`}>
            {a.texto}
          </p>
        ))}
      </div>
    </ToastContext.Provider>
  )
}
