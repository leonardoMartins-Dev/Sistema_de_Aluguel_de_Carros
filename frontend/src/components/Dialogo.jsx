import { useEffect, useId, useRef } from 'react'
import './Dialogo.css'

/** <dialog> nativo: foco preso no diálogo, Esc fecha, clique fora fecha. */
export default function Dialogo({ aberto, aoFechar, titulo, children, largo = false }) {
  const ref = useRef(null)
  const idTitulo = useId()

  useEffect(() => {
    const dialogo = ref.current
    if (aberto && !dialogo.open) dialogo.showModal()
    if (!aberto && dialogo.open) dialogo.close()
  }, [aberto])

  return (
    <dialog
      ref={ref}
      className={`dialogo ${largo ? 'dialogo--largo' : ''}`}
      aria-labelledby={idTitulo}
      onClose={aoFechar}
      onClick={(e) => e.target === ref.current && aoFechar()}
    >
      <div className="dialogo__cabecalho">
        <h2 id={idTitulo} className="dialogo__titulo">
          {titulo}
        </h2>
        <button type="button" className="dialogo__fechar" onClick={aoFechar} aria-label="Fechar">
          ×
        </button>
      </div>
      <div className="dialogo__corpo">{aberto && children}</div>
    </dialog>
  )
}
