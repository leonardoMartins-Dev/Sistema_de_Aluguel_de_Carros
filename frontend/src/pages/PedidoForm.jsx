import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router'
import CabecalhoPagina from '../components/CabecalhoPagina'
import Campo from '../components/Campo'
import Carimbo from '../components/Carimbo'
import Placa from '../components/Placa'
import { useAuth } from '../context/useAuth'
import { useToast } from '../context/useToast'
import { automoveis, pedidos } from '../services/api'
import { data, diarias, ehAgente, hojeISO, plural, protocolo, somarDias } from '../utils/formato'
import './PedidoForm.css'

/**
 * /pedidos/novo        → cliente introduz um pedido (US08)
 * /pedidos/:id/editar  → cliente (só os seus) ou agente modificam um pedido em análise (US10, US14)
 */
export default function PedidoForm() {
  const { id } = useParams()
  const [params] = useSearchParams()
  const { usuario } = useAuth()
  const avisar = useToast()
  const navigate = useNavigate()
  const editando = Boolean(id)
  const voltarPara = ehAgente(usuario.perfil) ? '/analise' : '/pedidos'

  const [frota, setFrota] = useState(null)
  const [pedido, setPedido] = useState(null)
  const [automovelId, setAutomovelId] = useState(params.get('automovel') ?? '')
  const [inicio, setInicio] = useState('')
  const [fim, setFim] = useState('')
  const [erros, setErros] = useState({})
  const [erroGeral, setErroGeral] = useState('')
  const [erroCarga, setErroCarga] = useState('')
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    Promise.all([automoveis.listar(), editando ? pedidos.buscar(id) : null])
      .then(([lista, existente]) => {
        setFrota(lista)
        if (existente) {
          setPedido(existente)
          setAutomovelId(String(existente.automovel.id))
          setInicio(existente.dataInicio)
          setFim(existente.dataFim)
        }
      })
      .catch((e) => setErroCarga(e.message))
  }, [editando, id])

  const hoje = hojeISO()
  const escolhido = frota?.find((a) => String(a.id) === String(automovelId))
  const n = diarias(inicio, fim)

  async function enviar(e) {
    e.preventDefault()
    if (!automovelId) {
      setErroGeral('Escolha um automóvel.')
      return
    }
    setEnviando(true)
    setErros({})
    setErroGeral('')
    const corpo = { automovelId: Number(automovelId), dataInicio: inicio || null, dataFim: fim || null }
    try {
      if (editando) {
        await pedidos.modificar(id, corpo)
        avisar(`Pedido ${protocolo(id)} atualizado`)
        navigate(voltarPara)
      } else {
        const criado = await pedidos.criar(corpo)
        avisar(`Pedido ${protocolo(criado.id)} enviado para análise`)
        navigate('/pedidos', { state: { novo: criado.id } })
      }
    } catch (err) {
      setErros(err.campos ?? {})
      setErroGeral(err.message)
      setEnviando(false)
    }
  }

  if (erroCarga) return <p className="aviso">{erroCarga}</p>
  if (!frota) return <p className="carregando">Carregando…</p>

  if (pedido && !pedido.editavel) {
    return (
      <div className="vazio">
        <Carimbo status={pedido.status} />
        <p className="vazio__titulo">O pedido {protocolo(pedido.id)} não pode mais ser modificado</p>
        <p>Só pedidos em análise podem ser modificados.</p>
        <Link className="botao botao--secundario" to={voltarPara}>
          Voltar aos pedidos
        </Link>
      </div>
    )
  }

  return (
    <>
      <CabecalhoPagina
        titulo={editando ? `Modificar pedido ${protocolo(id)}` : 'Novo pedido'}
        descricao={
          editando
            ? `Pedido de ${pedido?.clienteNome}. Só dá para modificar enquanto estiver em análise.`
            : 'Escolha o carro e as datas. O pedido vai para análise de uma empresa ou banco.'
        }
      />

      <form className="pedido-form" onSubmit={enviar} noValidate>
        <div className="pedido-form__campos">
          {erroGeral && (
            <p className="aviso" role="alert">
              {erroGeral}
            </p>
          )}

          <fieldset className="pedido-form__bloco" aria-labelledby="titulo-carro">
            <h2 id="titulo-carro" className="secao-form__titulo">
              Automóvel
            </h2>
            {frota.length === 0 ? (
              <p className="pedido-form__nota">Nenhum automóvel cadastrado ainda.</p>
            ) : (
              <div className="opcoes-carro">
                {frota.map((a) => (
                  <label key={a.id} className="opcao-carro">
                    <input
                      type="radio"
                      name="automovel"
                      value={a.id}
                      checked={String(a.id) === String(automovelId)}
                      onChange={(e) => setAutomovelId(e.target.value)}
                      className="opcao-carro__radio"
                    />
                    <Placa texto={a.placa} tamanho="p" />
                    <span className="opcao-carro__modelo">
                      {a.marca} {a.modelo}
                    </span>
                    <span className="opcao-carro__ano dado">{a.ano}</span>
                  </label>
                ))}
              </div>
            )}
            {erros.automovelId && <p className="campo__erro">{erros.automovelId}</p>}
          </fieldset>

          <fieldset className="pedido-form__bloco" aria-labelledby="titulo-periodo">
            <h2 id="titulo-periodo" className="secao-form__titulo">
              Período
            </h2>
            <div className="grade">
              <Campo id="inicio" rotulo="Retirada" erro={erros.dataInicio} className="col-3 manter-meio">
                <input type="date" value={inicio} min={hoje} onChange={(e) => setInicio(e.target.value)} />
              </Campo>
              <Campo id="fim" rotulo="Devolução" erro={erros.dataFim} className="col-3 manter-meio">
                <input type="date" value={fim} min={inicio ? somarDias(inicio, 1) : somarDias(hoje, 1)} onChange={(e) => setFim(e.target.value)} />
              </Campo>
            </div>
          </fieldset>
        </div>

        <aside className="resumo" aria-label="Resumo do pedido">
          <p className="rotulo">Resumo</p>
          <div className="resumo__placa">
            <Placa texto={escolhido?.placa} vazia={!escolhido} tamanho="m" />
          </div>
          <p className="resumo__modelo">{escolhido ? `${escolhido.marca} ${escolhido.modelo}` : 'Escolha um automóvel'}</p>
          <dl className="resumo__linhas">
            <div>
              <dt>Retirada</dt>
              <dd className="dado">{inicio ? data(inicio) : '—'}</dd>
            </div>
            <div>
              <dt>Devolução</dt>
              <dd className="dado">{fim ? data(fim) : '—'}</dd>
            </div>
            <div>
              <dt>Duração</dt>
              <dd>{n > 0 ? plural(n, 'diária', 'diárias') : '—'}</dd>
            </div>
          </dl>
          <button type="submit" className="botao botao--primario resumo__enviar" disabled={enviando}>
            {enviando ? 'Enviando…' : editando ? 'Salvar alterações' : 'Enviar pedido'}
          </button>
          <Link to={voltarPara} className="resumo__voltar">
            Voltar sem salvar
          </Link>
        </aside>
      </form>
    </>
  )
}
