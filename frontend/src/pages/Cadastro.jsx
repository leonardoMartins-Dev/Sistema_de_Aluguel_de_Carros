import { Link, useNavigate } from 'react-router'
import CabecalhoPagina from '../components/CabecalhoPagina'
import FormCliente from '../components/FormCliente'
import Placa from '../components/Placa'
import { useAuth } from '../context/useAuth'
import { useToast } from '../context/useToast'
import { auth } from '../services/api'
import './Acesso.css'

export default function Cadastro() {
  const { entrar } = useAuth()
  const avisar = useToast()
  const navigate = useNavigate()

  async function cadastrar(corpo) {
    await auth.cadastrar(corpo)
    await entrar(corpo.login, corpo.senha)
    avisar('Conta criada. Escolha um carro para fazer seu primeiro pedido.')
    navigate('/automoveis', { replace: true })
  }

  return (
    <>
      <header className="publica__topo">
        <Link to="/login" aria-label="Aluguel de Carros, voltar para o login">
          <Placa texto="ALUGUEL" tamanho="marca" />
        </Link>
        <span>
          Já tem conta? <Link to="/login">Entrar</Link>
        </span>
      </header>
      <main className="pagina">
        <CabecalhoPagina
          titulo="Criar conta de cliente"
          descricao="Com a conta você pede automóveis e acompanha a análise de cada pedido. Todos os campos são obrigatórios, menos complemento e rendimentos."
        />
        <FormCliente modo="cadastro" aoEnviar={cadastrar} rotuloEnviar="Criar conta" />
      </main>
    </>
  )
}
