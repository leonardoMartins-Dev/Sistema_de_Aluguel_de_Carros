
import { useEffect, useState } from 'react'
import './StatusBackend.css'

const API_URL = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '')

export default function StatusBackend() {
  const [status, setStatus] = useState('checking')

  useEffect(() => {
    let ativo = true
    let proximaVerificacao
    let timeout

    async function verificar() {
      const controller = new AbortController()

      setStatus('checking')

      timeout = setTimeout(() => {
        controller.abort()
      }, 90000)

      try {
        const resposta = await fetch(`${API_URL}/api/health`, {
          method: 'GET',
          signal: controller.signal,
          cache: 'no-store'
        })

        if (!resposta.ok) {
          throw new Error('Backend indisponível')
        }

        if (ativo) setStatus('online')
      } catch {
        if (ativo) setStatus('offline')
      } finally {
        clearTimeout(timeout)

        if (ativo) {
          proximaVerificacao = setTimeout(verificar, 30000)
        }
      }
    }

    verificar()

    return () => {
      ativo = false
      clearTimeout(timeout)
      clearTimeout(proximaVerificacao)
    }
  }, [])

  const informacoes = {
    checking: {
      texto: 'Verificando Backend(Render)...',
      classe: 'checking'
    },
    online: {
      texto: 'Backend(Render) online',
      classe: 'online'
    },
    offline: {
      texto: 'Backend(Render) offline',
      classe: 'offline'
    }
  }

  const atual = informacoes[status]

  return (
    <div
      className={`status-backend status-backend--${atual.classe}`}
      role="status"
      aria-live="polite"
    >
      <span className="status-backend__indicador" />
      <span>{atual.texto}</span>
    </div>
  )
}
