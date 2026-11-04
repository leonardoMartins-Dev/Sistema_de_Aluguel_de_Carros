import { data, diarias, giroDoCarimbo, moeda, plural, protocolo } from '../utils/formato'
import Carimbo from './Carimbo'
import Placa from './Placa'
import './PedidoCard.css'

/**
 * Pedido como um canhoto de protocolo: número, carro, período e o carimbo do status.
 * visao="agente" mostra também os dados do cliente usados na análise financeira.
 * children: ações, que ficam abaixo da linha picotada.
 */
export default function PedidoCard({ pedido, visao = 'cliente', animar = false, children }) {
  const { automovel } = pedido
  const n = diarias(pedido.dataInicio, pedido.dataFim)
  const idTitulo = `pedido-${pedido.id}`

  return (
    <article className="canhoto" aria-labelledby={idTitulo}>
      <div className="canhoto__corpo">
        <header className="canhoto__topo">
          <h3 id={idTitulo} className="canhoto__protocolo">
            <span className="rotulo">Pedido</span> <span className="dado">{protocolo(pedido.id)}</span>
          </h3>
          <span className="canhoto__criado">
            feito em <span className="dado">{data(pedido.dataCriacao)}</span>
          </span>
        </header>

        <div className="canhoto__carro">
          <Placa texto={automovel.placa} tamanho="p" />
          <p>
            <span className="canhoto__modelo">
              {automovel.marca} {automovel.modelo}
            </span>{' '}
            <span className="canhoto__ano dado">{automovel.ano}</span>
          </p>
        </div>

        <dl className="canhoto__periodo">
          <div>
            <dt className="rotulo">Retirada</dt>
            <dd className="dado">{data(pedido.dataInicio)}</dd>
          </div>
          <div>
            <dt className="rotulo">Devolução</dt>
            <dd className="dado">{data(pedido.dataFim)}</dd>
          </div>
          <div>
            <dt className="rotulo">Duração</dt>
            <dd>{plural(n, 'diária', 'diárias')}</dd>
          </div>
        </dl>

        {visao === 'agente' && (
          <dl className="canhoto__cliente">
            <div>
              <dt className="rotulo">Cliente</dt>
              <dd>
                <strong>{pedido.clienteNome}</strong>
                {pedido.clienteProfissao && <span className="canhoto__profissao"> · {pedido.clienteProfissao}</span>}
              </dd>
            </div>
            <div>
              <dt className="rotulo">Renda declarada</dt>
              <dd className="dado">{moeda(pedido.clienteRendaTotal)}</dd>
            </div>
          </dl>
        )}

        {pedido.parecer && (
          <figure className="canhoto__parecer">
            <figcaption className="rotulo">Parecer de {pedido.agenteAvaliadorNome}</figcaption>
            <blockquote>{pedido.parecer}</blockquote>
          </figure>
        )}
      </div>

      <div className="canhoto__carimbo">
        {/* key no status: quando ele muda, o carimbo é montado de novo e a animação roda */}
        <Carimbo key={pedido.status} status={pedido.status} animar={animar} giro={giroDoCarimbo(pedido.id)} />
      </div>

      {children && <footer className="canhoto__rodape">{children}</footer>}
    </article>
  )
}
