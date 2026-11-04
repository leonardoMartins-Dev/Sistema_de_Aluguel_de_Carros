import { useState } from 'react'
import Dialogo from './Dialogo'

/** Diálogo de confirmação para ações que não dá para desfazer. */
export default function Confirmacao({ aberto, aoFechar, titulo, texto, rotuloConfirmar, rotuloVoltar = 'Voltar', aoConfirmar }) {
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState('')

  function fechar() {
    setErro('')
    aoFechar()
  }

  async function confirmar() {
    setEnviando(true)
    setErro('')
    try {
      await aoConfirmar()
      aoFechar()
    } catch (e) {
      setErro(e.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Dialogo aberto={aberto} aoFechar={fechar} titulo={titulo}>
      <p>{texto}</p>
      {erro && <p className="aviso">{erro}</p>}
      <div className="dialogo__rodape">
        <button type="button" className="botao botao--secundario" onClick={fechar}>
          {rotuloVoltar}
        </button>
        <button type="button" className="botao botao--perigo-cheio" onClick={confirmar} disabled={enviando}>
          {enviando ? 'Aguarde…' : rotuloConfirmar}
        </button>
      </div>
    </Dialogo>
  )
}
