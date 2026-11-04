import { useEffect, useState } from 'react'
import CabecalhoPagina from '../../components/CabecalhoPagina'
import Confirmacao from '../../components/Confirmacao'
import FormCliente from '../../components/FormCliente'
import { useAuth } from '../../context/useAuth'
import { useToast } from '../../context/useToast'
import { clientes } from '../../services/api'
import './MeusDados.css'

export default function MeusDados() {
  const { atualizarNome, sair } = useAuth()
  const avisar = useToast()
  const [cliente, setCliente] = useState(null)
  const [erro, setErro] = useState('')
  const [excluindo, setExcluindo] = useState(false)

  useEffect(() => {
    clientes
      .meusDados()
      .then(setCliente)
      .catch((e) => setErro(e.message))
  }, [])

  async function salvar(corpo) {
    const atualizado = await clientes.atualizar(corpo)
    atualizarNome(atualizado.nome)
    avisar('Alterações salvas')
  }

  async function excluirConta() {
    await clientes.excluirConta()
    sair('Sua conta foi excluída.')
  }

  return (
    <>
      <CabecalhoPagina titulo="Meus dados" descricao="Esses dados aparecem para a empresa ou o banco que analisa seus pedidos." />

      {erro && <p className="aviso">{erro}</p>}
      {!cliente && !erro && <p className="carregando">Carregando seus dados…</p>}

      {cliente && (
        <>
          <FormCliente modo="edicao" inicial={cliente} aoEnviar={salvar} rotuloEnviar="Salvar alterações" />

          <section className="excluir-conta" aria-labelledby="titulo-excluir">
            <div>
              <h2 id="titulo-excluir" className="excluir-conta__titulo">
                Excluir conta
              </h2>
              <p>
                Apaga seus dados e os pedidos que não viraram contrato. Não dá para desfazer. Se algum pedido já tem contrato
                executado, a conta não pode ser excluída.
              </p>
            </div>
            <button type="button" className="botao botao--perigo" onClick={() => setExcluindo(true)}>
              Excluir minha conta
            </button>
          </section>
        </>
      )}

      <Confirmacao
        aberto={excluindo}
        aoFechar={() => setExcluindo(false)}
        titulo="Excluir sua conta?"
        texto="Seus dados e pedidos sem contrato serão apagados e você sairá do sistema."
        rotuloVoltar="Manter conta"
        rotuloConfirmar="Excluir minha conta"
        aoConfirmar={excluirConta}
      />
    </>
  )
}
