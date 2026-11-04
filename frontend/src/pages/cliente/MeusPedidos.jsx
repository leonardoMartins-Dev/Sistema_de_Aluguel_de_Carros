import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router'
import CabecalhoPagina from '../../components/CabecalhoPagina'
import Confirmacao from '../../components/Confirmacao'
import PedidoCard from '../../components/PedidoCard'
import { useToast } from '../../context/useToast'
import { pedidos } from '../../services/api'
import { protocolo } from '../../utils/formato'

// O que acontece a seguir com o pedido, do ponto de vista do cliente
const PROXIMO_PASSO = {
  EM_ANALISE: 'Aguardando a análise de uma empresa ou banco.',
  APROVADO: 'Aprovado. Falta o agente executar o contrato.',
  CONTRATADO: 'Contrato executado. Retire o carro na data combinada.',
}

export default function MeusPedidos() {
  const avisar = useToast()
  const location = useLocation()
  const [lista, setLista] = useState(null)
  const [erro, setErro] = useState('')
  const [cancelando, setCancelando] = useState(null)
  // Pedido recém-criado (vindo do formulário) ou recém-cancelado: o carimbo dele "cai" na tela
  const [animarId, setAnimarId] = useState(location.state?.novo ?? null)

  useEffect(() => {
    pedidos
      .meus()
      .then(setLista)
      .catch((e) => setErro(e.message))
  }, [])

  async function cancelar() {
    const atualizado = await pedidos.cancelar(cancelando.id)
    setLista((l) => l.map((p) => (p.id === atualizado.id ? atualizado : p)))
    setAnimarId(atualizado.id)
    avisar(`Pedido ${protocolo(atualizado.id)} cancelado`)
  }

  return (
    <>
      <CabecalhoPagina
        titulo="Meus pedidos"
        descricao="Acompanhe cada pedido da análise até o contrato."
        acoes={
          <Link className="botao botao--primario" to="/pedidos/novo">
            Novo pedido
          </Link>
        }
      />

      {erro && <p className="aviso">{erro}</p>}
      {!lista && !erro && <p className="carregando">Carregando pedidos…</p>}

      {lista?.length === 0 && (
        <div className="vazio">
          <p className="vazio__titulo">Você ainda não fez nenhum pedido</p>
          <p>Escolha um carro na frota e informe as datas de retirada e devolução.</p>
          <Link className="botao botao--primario" to="/automoveis">
            Ver automóveis
          </Link>
        </div>
      )}

      {lista?.length > 0 && (
        <div className="lista-canhotos">
          {lista.map((p) => {
            const passo = PROXIMO_PASSO[p.status]
            const temAcoes = p.editavel || p.cancelavel
            return (
              <PedidoCard key={p.id} pedido={p} animar={p.id === animarId}>
                {(passo || temAcoes) && (
                  <div className="canhoto__acoes">
                    {passo && <p className="canhoto__passo">{passo}</p>}
                    {temAcoes && (
                      <div className="acoes">
                        {p.editavel && (
                          <Link className="botao botao--secundario botao--compacto" to={`/pedidos/${p.id}/editar`}>
                            Modificar pedido
                          </Link>
                        )}
                        {p.cancelavel && (
                          <button type="button" className="botao botao--perigo botao--compacto" onClick={() => setCancelando(p)}>
                            Cancelar pedido
                          </button>
                        )}
                      </div>
                    )}
                  </div>
                )}
              </PedidoCard>
            )
          })}
        </div>
      )}

      <Confirmacao
        aberto={cancelando !== null}
        aoFechar={() => setCancelando(null)}
        titulo={cancelando ? `Cancelar o pedido ${protocolo(cancelando.id)}?` : ''}
        texto="O pedido sai da análise e não pode ser reaberto. Para alugar de novo, faça outro pedido."
        rotuloVoltar="Manter pedido"
        rotuloConfirmar="Cancelar pedido"
        aoConfirmar={cancelar}
      />
    </>
  )
}
