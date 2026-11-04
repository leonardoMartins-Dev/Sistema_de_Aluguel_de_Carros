import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import CabecalhoPagina from '../../components/CabecalhoPagina'
import Campo from '../../components/Campo'
import Dialogo from '../../components/Dialogo'
import Placa from '../../components/Placa'
import { useAuth } from '../../context/useAuth'
import { useToast } from '../../context/useToast'
import { contratos } from '../../services/api'
import { data, moeda, parcelaPrice, percentual, protocolo, TIPOS_CONTRATO } from '../../utils/formato'
import './Agente.css'

export default function Contratos() {
  const { usuario } = useAuth()
  const avisar = useToast()
  const [lista, setLista] = useState(null)
  const [erro, setErro] = useState('')
  const [creditando, setCreditando] = useState(null)
  const banco = usuario.perfil === 'BANCO'

  useEffect(() => {
    contratos
      .listar()
      .then(setLista)
      .catch((e) => setErro(e.message))
  }, [])

  return (
    <>
      <CabecalhoPagina
        titulo="Contratos"
        descricao={
          banco
            ? 'Contratos executados. Como banco, você pode conceder crédito para financiar o aluguel.'
            : 'Contratos executados a partir de pedidos aprovados. Crédito só pode ser concedido por um banco.'
        }
      />

      {erro && <p className="aviso">{erro}</p>}
      {!lista && !erro && <p className="carregando">Carregando contratos…</p>}

      {lista?.length === 0 && (
        <div className="vazio">
          <p className="vazio__titulo">Nenhum contrato executado ainda</p>
          <p>Um contrato nasce quando um pedido aprovado é executado na tela de pedidos.</p>
          <Link className="botao botao--secundario" to="/analise">
            Ir para pedidos
          </Link>
        </div>
      )}

      {lista?.length > 0 && (
        <div className="contratos">
          {lista.map((c) => (
            <article key={c.id} className="contrato" aria-labelledby={`contrato-${c.id}`}>
              <header className="contrato__topo">
                <h2 id={`contrato-${c.id}`} className="contrato__numero">
                  <span className="rotulo">Contrato</span> <span className="dado">{protocolo(c.id)}</span>
                </h2>
                <span className="contrato__data">
                  assinado em <span className="dado">{data(c.dataAssinatura)}</span>
                </span>
              </header>

              <div className="contrato__carro">
                <Placa texto={c.automovel.placa} tamanho="p" />
                <span className="contrato__modelo">
                  {c.automovel.marca} {c.automovel.modelo}
                </span>
              </div>

              <dl className="contrato__dados">
                <div>
                  <dt className="rotulo">Cliente</dt>
                  <dd>{c.clienteNome}</dd>
                </div>
                <div>
                  <dt className="rotulo">Valor total</dt>
                  <dd className="dado">{moeda(c.valorTotal)}</dd>
                </div>
                <div>
                  <dt className="rotulo">Propriedade</dt>
                  <dd>{TIPOS_CONTRATO[c.tipo]}</dd>
                </div>
                <div>
                  <dt className="rotulo">Executado por</dt>
                  <dd>{c.agenteResponsavelNome}</dd>
                </div>
                <div>
                  <dt className="rotulo">Pedido</dt>
                  <dd className="dado">{protocolo(c.pedidoId)}</dd>
                </div>
              </dl>

              <div className={`contrato__credito ${c.credito ? 'contrato__credito--concedido' : ''}`}>
                {c.credito ? (
                  <>
                    <p className="rotulo">Crédito de {c.credito.bancoNome}</p>
                    <p className="contrato__parcelas">
                      {c.credito.numeroParcelas} × <span className="dado">{moeda(c.credito.valorParcela)}</span>
                    </p>
                    <p className="contrato__credito-detalhe">
                      <span className="dado">{moeda(c.credito.valor)}</span> a {percentual(c.credito.taxaJurosMensal)} ao mês ·
                      concedido em <span className="dado">{data(c.credito.dataConcessao)}</span>
                    </p>
                  </>
                ) : banco ? (
                  <>
                    <p className="contrato__credito-detalhe">Sem crédito associado.</p>
                    <button type="button" className="botao botao--primario botao--compacto" onClick={() => setCreditando(c)}>
                      Conceder crédito
                    </button>
                  </>
                ) : (
                  <p className="contrato__credito-detalhe">Sem crédito associado.</p>
                )}
              </div>
            </article>
          ))}
        </div>
      )}

      <Dialogo aberto={creditando !== null} aoFechar={() => setCreditando(null)} titulo="Conceder crédito">
        {creditando && (
          <FormCredito
            contrato={creditando}
            aoConceder={(atualizado) => {
              setLista((l) => l.map((c) => (c.id === atualizado.id ? atualizado : c)))
              setCreditando(null)
              avisar(`Crédito concedido no contrato ${protocolo(atualizado.id)}`)
            }}
            aoCancelar={() => setCreditando(null)}
          />
        )}
      </Dialogo>
    </>
  )
}

/** US17: o banco informa valor, juros e parcelas; a parcela aparece calculada antes de enviar */
function FormCredito({ contrato, aoConceder, aoCancelar }) {
  const [valor, setValor] = useState(String(contrato.valorTotal))
  const [taxa, setTaxa] = useState('')
  const [parcelas, setParcelas] = useState('12')
  const [erros, setErros] = useState({})
  const [erroGeral, setErroGeral] = useState('')
  const [enviando, setEnviando] = useState(false)

  const parcela = parcelaPrice(valor, taxa === '' ? NaN : taxa, parcelas)

  async function enviar(e) {
    e.preventDefault()
    setEnviando(true)
    setErros({})
    setErroGeral('')
    const numero = (v) => (v === '' ? null : Number(v))
    try {
      aoConceder(
        await contratos.concederCredito(contrato.id, {
          valor: numero(valor),
          taxaJurosMensal: numero(taxa),
          numeroParcelas: numero(parcelas),
        }),
      )
    } catch (err) {
      setErros(err.campos ?? {})
      setErroGeral(err.message)
      setEnviando(false)
    }
  }

  return (
    <form className="form-credito" onSubmit={enviar} noValidate>
      <p>
        Contrato <span className="dado">{protocolo(contrato.id)}</span> de {contrato.clienteNome}, valor total{' '}
        <span className="dado">{moeda(contrato.valorTotal)}</span>.
      </p>
      {erroGeral && (
        <p className="aviso" role="alert">
          {erroGeral}
        </p>
      )}
      <div className="grade">
        <Campo id="credito-valor" rotulo="Valor do crédito (R$)" erro={erros.valor}>
          <input type="number" min="0.01" step="0.01" inputMode="decimal" value={valor} onChange={(e) => setValor(e.target.value)} />
        </Campo>
        <Campo id="credito-taxa" rotulo="Juros ao mês (%)" erro={erros.taxaJurosMensal} className="col-3 manter-meio">
          <input type="number" min="0" step="0.01" inputMode="decimal" value={taxa} onChange={(e) => setTaxa(e.target.value)} placeholder="1,5" autoFocus />
        </Campo>
        <Campo id="credito-parcelas" rotulo="Parcelas" erro={erros.numeroParcelas} className="col-3 manter-meio">
          <input type="number" min="1" max="120" step="1" inputMode="numeric" value={parcelas} onChange={(e) => setParcelas(e.target.value)} />
        </Campo>
      </div>
      <p className="form-credito__previa" aria-live="polite">
        {parcela ? (
          <>
            <span className="rotulo">Parcela pela Tabela Price</span>
            <span className="form-credito__parcela">
              {parcelas} × <span className="dado">{moeda(parcela)}</span>
            </span>
          </>
        ) : (
          <span className="rotulo">Preencha valor, juros e parcelas para ver a parcela</span>
        )}
      </p>
      <div className="dialogo__rodape">
        <button type="button" className="botao botao--secundario" onClick={aoCancelar}>
          Voltar
        </button>
        <button type="submit" className="botao botao--primario" disabled={enviando}>
          {enviando ? 'Concedendo…' : 'Conceder crédito'}
        </button>
      </div>
    </form>
  )
}
