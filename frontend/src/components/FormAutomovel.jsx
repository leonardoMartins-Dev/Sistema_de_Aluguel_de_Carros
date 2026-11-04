import { useState } from 'react'
import { limparPlaca } from '../utils/formato'
import Campo from './Campo'
import Placa from './Placa'

/** Cadastro/edição de automóvel (US12). A placa aparece desenhada enquanto é digitada. */
export default function FormAutomovel({ inicial, aoSalvar, aoCancelar }) {
  const [dados, setDados] = useState(() => ({
    placa: inicial?.placa ?? '',
    matricula: inicial?.matricula ?? '',
    marca: inicial?.marca ?? '',
    modelo: inicial?.modelo ?? '',
    ano: inicial?.ano ? String(inicial.ano) : '',
  }))
  const [erros, setErros] = useState({})
  const [erroGeral, setErroGeral] = useState('')
  const [enviando, setEnviando] = useState(false)

  const alterar = (campo, mascara) => (e) => {
    const valor = mascara ? mascara(e.target.value) : e.target.value
    setDados((d) => ({ ...d, [campo]: valor }))
  }

  async function enviar(e) {
    e.preventDefault()
    setEnviando(true)
    setErros({})
    setErroGeral('')
    try {
      await aoSalvar({ ...dados, ano: dados.ano === '' ? null : Number(dados.ano) })
    } catch (err) {
      setErros(err.campos ?? {})
      setErroGeral(err.message)
      setEnviando(false)
    }
  }

  return (
    <form onSubmit={enviar} noValidate className="form-automovel">
      <div className="form-automovel__previa" aria-hidden="true">
        <Placa texto={dados.placa} vazia={!dados.placa} tamanho="g" />
      </div>
      {erroGeral && (
        <p className="aviso" role="alert">
          {erroGeral}
        </p>
      )}
      <div className="grade">
        <Campo id="placa" rotulo="Placa" erro={erros.placa} dica="Padrão antigo ou Mercosul." className="col-3">
          <input value={dados.placa} onChange={alterar('placa', limparPlaca)} autoCapitalize="characters" placeholder="ABC1D23" autoFocus />
        </Campo>
        <Campo id="matricula" rotulo="Matrícula (Renavam)" erro={erros.matricula} className="col-3">
          <input value={dados.matricula} onChange={alterar('matricula')} inputMode="numeric" />
        </Campo>
        <Campo id="marca" rotulo="Marca" erro={erros.marca} className="col-2">
          <input value={dados.marca} onChange={alterar('marca')} placeholder="Fiat" />
        </Campo>
        <Campo id="modelo" rotulo="Modelo" erro={erros.modelo} className="col-2">
          <input value={dados.modelo} onChange={alterar('modelo')} placeholder="Argo" />
        </Campo>
        <Campo id="ano" rotulo="Ano" erro={erros.ano} className="col-2">
          <input type="number" value={dados.ano} onChange={alterar('ano')} inputMode="numeric" min="1900" max="2100" placeholder="2024" />
        </Campo>
      </div>
      <div className="dialogo__rodape">
        <button type="button" className="botao botao--secundario" onClick={aoCancelar}>
          Voltar
        </button>
        <button type="submit" className="botao botao--primario" disabled={enviando}>
          {enviando ? 'Salvando…' : inicial ? 'Salvar alterações' : 'Cadastrar automóvel'}
        </button>
      </div>
    </form>
  )
}
