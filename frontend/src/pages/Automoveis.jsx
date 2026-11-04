import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import CabecalhoPagina from '../components/CabecalhoPagina'
import Confirmacao from '../components/Confirmacao'
import Dialogo from '../components/Dialogo'
import FormAutomovel from '../components/FormAutomovel'
import Placa from '../components/Placa'
import { useAuth } from '../context/useAuth'
import { useToast } from '../context/useToast'
import { automoveis } from '../services/api'
import { ehAgente } from '../utils/formato'
import './Automoveis.css'

export default function Automoveis() {
  const { usuario } = useAuth()
  const avisar = useToast()
  const agente = ehAgente(usuario.perfil)
  const [frota, setFrota] = useState(null)
  const [erro, setErro] = useState('')
  const [editando, setEditando] = useState(null) // null | 'novo' | automóvel
  const [excluindo, setExcluindo] = useState(null)

  useEffect(() => {
    automoveis
      .listar()
      .then(setFrota)
      .catch((e) => setErro(e.message))
  }, [])

  async function salvar(dados) {
    if (editando === 'novo') {
      const novo = await automoveis.criar(dados)
      setFrota((lista) => [...lista, novo])
      avisar(`${novo.marca} ${novo.modelo} cadastrado`)
    } else {
      const atualizado = await automoveis.atualizar(editando.id, dados)
      setFrota((lista) => lista.map((a) => (a.id === atualizado.id ? atualizado : a)))
      avisar(`${atualizado.marca} ${atualizado.modelo} atualizado`)
    }
    setEditando(null)
  }

  async function excluir() {
    await automoveis.excluir(excluindo.id)
    setFrota((lista) => lista.filter((a) => a.id !== excluindo.id))
    avisar(`${excluindo.marca} ${excluindo.modelo} excluído`)
  }

  return (
    <>
      <CabecalhoPagina
        titulo="Automóveis"
        descricao={
          agente
            ? 'Frota que os clientes podem pedir. Placa e matrícula não podem se repetir.'
            : 'Escolha um carro para pedir. A disponibilidade nas datas é conferida na análise.'
        }
        acoes={
          agente && (
            <button type="button" className="botao botao--primario" onClick={() => setEditando('novo')}>
              Cadastrar automóvel
            </button>
          )
        }
      />

      {erro && <p className="aviso">{erro}</p>}
      {!frota && !erro && <p className="carregando">Carregando automóveis…</p>}

      {frota?.length === 0 && (
        <div className="vazio">
          <p className="vazio__titulo">Nenhum automóvel na frota</p>
          <p>
            {agente
              ? 'Cadastre o primeiro carro para os clientes poderem fazer pedidos.'
              : 'Ainda não há carros para pedir. Volte mais tarde.'}
          </p>
          {agente && (
            <button type="button" className="botao botao--primario" onClick={() => setEditando('novo')}>
              Cadastrar automóvel
            </button>
          )}
        </div>
      )}

      {frota?.length > 0 && (
        <ul className="frota">
          {frota.map((a) => (
            <li key={a.id} className="veiculo">
              <div className="veiculo__placa">
                <Placa texto={a.placa} tamanho="m" />
              </div>
              <div className="veiculo__info">
                <h2 className="veiculo__modelo">
                  {a.marca} {a.modelo}
                </h2>
                <p className="veiculo__detalhe">
                  <span className="dado">{a.ano}</span> · matrícula <span className="dado">{a.matricula}</span>
                </p>
                {/* Dono do carro é dado de outro cliente: só agentes veem */}
                {agente && a.proprietarioNome && <p className="veiculo__dono">Registrado em nome de {a.proprietarioNome}</p>}
              </div>
              <div className="veiculo__acoes">
                {agente ? (
                  <>
                    <button type="button" className="botao botao--secundario botao--compacto" onClick={() => setEditando(a)}>
                      Editar
                    </button>
                    <button type="button" className="botao botao--texto veiculo__excluir" onClick={() => setExcluindo(a)}>
                      Excluir
                    </button>
                  </>
                ) : (
                  <Link className="botao botao--primario botao--compacto" to={`/pedidos/novo?automovel=${a.id}`}>
                    Pedir este carro
                  </Link>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}

      <Dialogo
        aberto={editando !== null}
        aoFechar={() => setEditando(null)}
        titulo={editando === 'novo' ? 'Cadastrar automóvel' : 'Editar automóvel'}
        largo
      >
        <FormAutomovel
          key={editando === 'novo' ? 'novo' : editando?.id}
          inicial={editando === 'novo' ? null : editando}
          aoSalvar={salvar}
          aoCancelar={() => setEditando(null)}
        />
      </Dialogo>

      <Confirmacao
        aberto={excluindo !== null}
        aoFechar={() => setExcluindo(null)}
        titulo={`Excluir ${excluindo?.marca ?? ''} ${excluindo?.modelo ?? ''}?`}
        texto="O carro sai da frota. Carros que já têm pedidos não podem ser excluídos."
        rotuloConfirmar="Excluir automóvel"
        aoConfirmar={excluir}
      />
    </>
  )
}
