import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import Campo from '../components/Campo'
import Carimbo from '../components/Carimbo'
import Placa from '../components/Placa'
import { useAuth } from '../context/useAuth'
import { rotaInicial } from '../utils/formato'
import './Acesso.css'

export default function Login() {
  const { entrar } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [login, setLogin] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function enviar(e) {
    e.preventDefault()
    if (!login.trim() || !senha) {
      setErro('Informe o login e a senha.')
      return
    }
    setErro('')
    setEnviando(true)
    try {
      const usuario = await entrar(login.trim(), senha)
      navigate(location.state?.de ?? rotaInicial(usuario.perfil), { replace: true })
    } catch (err) {
      setErro(err.message)
      setEnviando(false)
    }
  }

  return (
    <div className="acesso">
      <section className="acesso__apresentacao" aria-labelledby="titulo-acesso">
        <Placa texto="ALUGUEL" tamanho="hero" />
        <h1 id="titulo-acesso" className="acesso__titulo">
          Peça um carro e acompanhe a análise até o contrato.
        </h1>
        <p className="acesso__texto">
          Clientes fazem o pedido. Empresas e bancos avaliam a renda, aprovam e executam o contrato.
        </p>
        <figure className="acesso__fluxo">
          <figcaption className="rotulo">O caminho de um pedido</figcaption>
          <ol>
            <li>
              <Carimbo status="EM_ANALISE" animar atraso={250} giro={-4} />
            </li>
            <li>
              <Carimbo status="APROVADO" animar atraso={650} giro={-7} />
            </li>
            <li>
              <Carimbo status="CONTRATADO" animar atraso={1050} giro={-3} />
            </li>
          </ol>
        </figure>
      </section>

      <section className="acesso__painel">
        <form className="acesso__form" onSubmit={enviar} noValidate>
          <h2 className="acesso__form-titulo">Entrar</h2>
          {location.state?.aviso && <p className="aviso aviso--info">{location.state.aviso}</p>}
          {erro && (
            <p className="aviso" role="alert">
              {erro}
            </p>
          )}
          <Campo id="login" rotulo="Login">
            <input value={login} onChange={(e) => setLogin(e.target.value)} autoComplete="username" autoFocus />
          </Campo>
          <Campo id="senha" rotulo="Senha">
            <input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} autoComplete="current-password" />
          </Campo>
          <button type="submit" className="botao botao--primario acesso__enviar" disabled={enviando}>
            {enviando ? 'Entrando…' : 'Entrar'}
          </button>
          <p className="acesso__alternativa">
            Ainda não tem conta? <Link to="/cadastro">Criar conta de cliente</Link>
          </p>
          <p className="acesso__nota">Empresas e bancos entram com o login criado pela administração do sistema.</p>
        </form>
      </section>
    </div>
  )
}
