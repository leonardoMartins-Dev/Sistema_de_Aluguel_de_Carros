import { Link, NavLink } from 'react-router'
import { useAuth } from '../context/useAuth'
import { PERFIS, rotaInicial } from '../utils/formato'
import Placa from './Placa'
import './Navbar.css'

const LINKS = {
  CLIENTE: [
    { para: '/pedidos', texto: 'Meus pedidos' },
    { para: '/automoveis', texto: 'Automóveis' },
    { para: '/meus-dados', texto: 'Meus dados' },
  ],
  AGENTE: [
    { para: '/analise', texto: 'Pedidos' },
    { para: '/contratos', texto: 'Contratos' },
    { para: '/automoveis', texto: 'Automóveis' },
  ],
}

export default function Navbar() {
  const { usuario, sair } = useAuth()
  const links = usuario.perfil === 'CLIENTE' ? LINKS.CLIENTE : LINKS.AGENTE

  return (
    <header className="topo">
      <div className="topo__interno">
        <Link to={rotaInicial(usuario.perfil)} className="topo__marca" aria-label="Aluguel de Carros, página inicial">
          <Placa texto="ALUGUEL" tamanho="marca" />
        </Link>

        <nav className="topo__nav" aria-label="Principal">
          {links.map((l) => (
            <NavLink key={l.para} to={l.para} className="topo__link">
              {l.texto}
            </NavLink>
          ))}
        </nav>

        <div className="topo__usuario">
          <span className="topo__nome">{usuario.nome}</span>
          <span className={`topo__perfil topo__perfil--${usuario.perfil.toLowerCase()}`}>{PERFIS[usuario.perfil]}</span>
          <button type="button" className="botao botao--texto topo__sair" onClick={() => sair()}>
            Sair
          </button>
        </div>
      </div>
    </header>
  )
}
