import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import CabecalhoPagina from '../../components/CabecalhoPagina'
import Campo from '../../components/Campo'
import Dialogo from '../../components/Dialogo'
import PedidoCard from '../../components/PedidoCard'
import Placa from '../../components/Placa'
import { useAuth } from '../../context/useAuth'
import { useToast } from '../../context/useToast'
import { contratos, pedidos } from '../../services/api'
import { data, protocolo, TIPOS_CONTRATO } from '../../utils/formato'
import './Agente.css'

const FILTROS = [
  { status: 'EM_ANALISE', rotulo: 'Em análise' },
  { status: 'APROVADO', rotulo: 'Aprovados' },
  { status: 'CONTRATADO', rotulo: 'Contratados' },
  { status: 'REPROVADO', rotulo: 'Reprovados' },
  { status: 'CANCELADO', rotulo: 'Cancelados' },
  { status: 'TODOS', rotulo: 'Todos' },
]

const VAZIO_POR_FILTRO = {
  EM_ANALISE: 'Nenhum pedido esperando análise agora.',
  APROVADO: 'Nenhum pedido aprovado esperando contrato.',
  CONTRATADO: 'Nenhum pedido virou contrato ainda.',
  REPROVADO: 'Nenhum pedido reprovado.',
  CANCELADO: 'Nenhum pedido cancelado.',
  TODOS: 'Nenhum pedido feito ainda.',
}

export default function PedidosAnalise() {
  const { usuario } = useAuth()
  const avisar = useToast()
  const [lista, setLista] = useState(null)
  const [erro, setErro] = useState('')
  const [filtro, setFiltro] = useState('EM_ANALISE')
  // Pedidos que acabaram de mudar continuam visíveis no filtro atual até trocar de aba (para ver o carimbo cair)
  const [recentes, setRecentes] = useState(() => new Set())
  const [animarId, setAnimarId] = useState(null)
  const [avaliandoId, setAvaliandoId] = useState(null)
  const [contratando, setContratando] = useState(null)

  useEffect(() => {
    pedidos
      .listar()
      .then(setLista)
      .catch((e) => setErro(e.message))
  }, [])

  function trocarFiltro(status) {
    setFiltro(status)
    setRecentes(new Set())
    setAvaliandoId(null)
  }

  function substituir(atualizado) {
    setLista((l) => l.map((p) => (p.id === atualizado.id ? atualizado : p)))
    setRecentes((r) => new Set(r).add(atualizado.id))
    setAnimarId(atualizado.id)
  }

  const contagem = (status) => (status === 'TODOS' ? lista.length : lista.filter((p) => p.status === status).length)
  const visiveis = lista?.filter((p) => filtro === 'TODOS' || p.status === filtro || recentes.has(p.id)) ?? []

  return (
    <>
      <CabecalhoPagina
        titulo="Pedidos"
        descricao="Analise a renda de cada cliente, dê o parecer e execute o contrato dos pedidos aprovados."
      />

      {erro && <p className="aviso">{erro}</p>}
      {!lista && !erro && <p className="carregando">Carregando pedidos…</p>}

      {lista && (
        <>
          <div className="filtros" role="group" aria-label="Filtrar pedidos por status">
            {FILTROS.map((f) => (
              <button
                key={f.status}
                type="button"
                className="filtro"
                aria-pressed={filtro === f.status}
                onClick={() => trocarFiltro(f.status)}
              >
                {f.rotulo}
                <span className="filtro__contagem">{contagem(f.status)}</span>
              </button>
            ))}
          </div>

          {visiveis.length === 0 ? (
            <div className="vazio">
              <p className="vazio__titulo">{VAZIO_POR_FILTRO[filtro]}</p>
              {filtro === 'EM_ANALISE' && <p>Pedidos novos dos clientes aparecem aqui.</p>}
            </div>
          ) : (
            <div className="lista-canhotos">
              {visiveis.map((p) => (
                <PedidoCard key={p.id} pedido={p} visao="agente" animar={p.id === animarId}>
                  {p.status === 'EM_ANALISE' &&
                    (avaliandoId === p.id ? (
                      <FormAvaliacao
                        pedido={p}
                        aoAvaliar={(atualizado) => {
                          substituir(atualizado)
                          setAvaliandoId(null)
                          avisar(`Pedido ${protocolo(p.id)} ${atualizado.status === 'APROVADO' ? 'aprovado' : 'reprovado'}`)
                        }}
                        aoFechar={() => setAvaliandoId(null)}
                      />
                    ) : (
                      <div className="acoes">
                        <button type="button" className="botao botao--primario botao--compacto" onClick={() => setAvaliandoId(p.id)}>
                          Avaliar pedido
                        </button>
                        <Link className="botao botao--secundario botao--compacto" to={`/pedidos/${p.id}/editar`}>
                          Modificar pedido
                        </Link>
                      </div>
                    ))}
                  {p.status === 'APROVADO' && (
                    <div className="canhoto__acoes">
                      <p className="canhoto__passo">Aprovado. O próximo passo é executar o contrato.</p>
                      <button type="button" className="botao botao--primario botao--compacto" onClick={() => setContratando(p)}>
                        Executar contrato
                      </button>
                    </div>
                  )}
                  {p.status === 'CONTRATADO' && (
                    <div className="canhoto__acoes">
                      <p className="canhoto__passo">Contrato executado.</p>
                      <Link to="/contratos" className="canhoto__link">
                        Ver contratos
                      </Link>
                    </div>
                  )}
                </PedidoCard>
              ))}
            </div>
          )}
        </>
      )}

      <Dialogo aberto={contratando !== null} aoFechar={() => setContratando(null)} titulo="Executar contrato" largo>
        {contratando && (
          <FormContrato
            pedido={contratando}
            perfil={usuario.perfil}
            aoExecutar={(contrato) => {
              substituir({ ...contratando, status: 'CONTRATADO', editavel: false, cancelavel: false })
              setContratando(null)
              avisar(`Contrato ${protocolo(contrato.id)} executado`)
            }}
            aoCancelar={() => setContratando(null)}
          />
        )}
      </Dialogo>
    </>
  )
}

/** US15: parecer + aprovar/reprovar, aberto dentro do próprio canhoto */
function FormAvaliacao({ pedido, aoAvaliar, aoFechar }) {
  const [parecer, setParecer] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function avaliar(aprovado) {
    if (!parecer.trim()) {
      setErro('Escreva o parecer antes de aprovar ou reprovar.')
      return
    }
    setEnviando(true)
    setErro('')
    try {
      aoAvaliar(await pedidos.avaliar(pedido.id, aprovado, parecer.trim()))
    } catch (e) {
      setErro(e.message)
      setEnviando(false)
    }
  }

  return (
    <div className="avaliacao">
      <Campo id={`parecer-${pedido.id}`} rotulo="Parecer da análise financeira" erro={erro}>
        <textarea
          value={parecer}
          onChange={(e) => setParecer(e.target.value)}
          maxLength={500}
          placeholder="Ex.: renda compatível com o período pedido."
          autoFocus
        />
      </Campo>
      <div className="acoes">
        <button type="button" className="botao botao--aprovar botao--compacto" onClick={() => avaliar(true)} disabled={enviando}>
          Aprovar pedido
        </button>
        <button type="button" className="botao botao--reprovar botao--compacto" onClick={() => avaliar(false)} disabled={enviando}>
          Reprovar pedido
        </button>
        <button type="button" className="botao botao--texto" onClick={aoFechar}>
          Fechar
        </button>
      </div>
    </div>
  )
}

/** US16: tipo de contrato (quem fica com o carro) e valor total */
function FormContrato({ pedido, perfil, aoExecutar, aoCancelar }) {
  const [tipo, setTipo] = useState('PROPRIEDADE_CLIENTE')
  const [valor, setValor] = useState('')
  const [erros, setErros] = useState({})
  const [erroGeral, setErroGeral] = useState('')
  const [enviando, setEnviando] = useState(false)

  const opcoes = [
    { tipo: 'PROPRIEDADE_CLIENTE', texto: `O automóvel fica registrado em nome de ${pedido.clienteNome}.` },
    {
      tipo: 'PROPRIEDADE_EMPRESA',
      texto: 'O automóvel fica registrado em nome da sua empresa.',
      bloqueio: perfil !== 'EMPRESA' && 'Só uma empresa executa este tipo.',
    },
    {
      tipo: 'PROPRIEDADE_BANCO',
      texto: 'O automóvel fica registrado em nome do banco.',
      bloqueio: perfil !== 'BANCO' && 'Só um banco executa este tipo.',
    },
  ]

  async function enviar(e) {
    e.preventDefault()
    setEnviando(true)
    setErros({})
    setErroGeral('')
    try {
      aoExecutar(await contratos.executar({ pedidoId: pedido.id, tipo, valorTotal: valor === '' ? null : Number(valor) }))
    } catch (err) {
      setErros(err.campos ?? {})
      setErroGeral(err.message)
      setEnviando(false)
    }
  }

  return (
    <form className="form-contrato" onSubmit={enviar} noValidate>
      <div className="form-contrato__pedido">
        <Placa texto={pedido.automovel.placa} tamanho="p" />
        <p>
          <strong>
            {pedido.automovel.marca} {pedido.automovel.modelo}
          </strong>{' '}
          para {pedido.clienteNome}
          <br />
          <span className="dado">
            {protocolo(pedido.id)} · {data(pedido.dataInicio)} → {data(pedido.dataFim)}
          </span>
        </p>
      </div>

      {erroGeral && (
        <p className="aviso" role="alert">
          {erroGeral}
        </p>
      )}

      <fieldset className="tipos-contrato" aria-labelledby="titulo-tipo">
        <p id="titulo-tipo" className="rotulo">
          Quem fica com o carro
        </p>
        {opcoes.map((o) => (
          <label key={o.tipo} className={`tipo-contrato ${o.bloqueio ? 'tipo-contrato--bloqueado' : ''}`}>
            <input
              type="radio"
              name="tipo"
              value={o.tipo}
              checked={tipo === o.tipo}
              onChange={() => setTipo(o.tipo)}
              disabled={Boolean(o.bloqueio)}
            />
            <span>
              <span className="tipo-contrato__titulo">{TIPOS_CONTRATO[o.tipo]}</span>
              <span className="tipo-contrato__texto">{o.bloqueio || o.texto}</span>
            </span>
          </label>
        ))}
      </fieldset>

      <Campo id="valor-total" rotulo="Valor total do contrato (R$)" erro={erros.valorTotal}>
        <input type="number" min="0.01" step="0.01" inputMode="decimal" value={valor} onChange={(e) => setValor(e.target.value)} />
      </Campo>

      <div className="dialogo__rodape">
        <button type="button" className="botao botao--secundario" onClick={aoCancelar}>
          Voltar
        </button>
        <button type="submit" className="botao botao--primario" disabled={enviando}>
          {enviando ? 'Executando…' : 'Executar contrato'}
        </button>
      </div>
    </form>
  )
}
