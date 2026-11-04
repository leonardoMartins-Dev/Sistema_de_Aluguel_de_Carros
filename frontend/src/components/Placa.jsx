import './Placa.css'

/**
 * Placa no padrão Mercosul: faixa azul com "BRASIL" e os 7 caracteres.
 * É a identidade de cada automóvel e também a marca do sistema ("ALUGUEL").
 * tamanho: marca | p | m | g | hero
 */
export default function Placa({ texto, tamanho = 'm', vazia = false, className = '' }) {
  const caracteres = (texto || '').toUpperCase()

  return (
    <span
      className={`placa placa--${tamanho} ${vazia ? 'placa--vazia' : ''} ${className}`}
      role="img"
      aria-label={`Placa ${caracteres}`}
    >
      <span className="placa__faixa" aria-hidden="true">
        <svg className="placa__emblema" viewBox="0 0 20 20">
          <circle cx="10" cy="10" r="8.4" fill="none" stroke="currentColor" strokeWidth="1.4" />
          <circle cx="7" cy="8" r="1.3" fill="currentColor" />
          <circle cx="12.6" cy="6.6" r="1.1" fill="currentColor" />
          <circle cx="13.2" cy="12" r="1.3" fill="currentColor" />
          <circle cx="8.6" cy="13.6" r="1" fill="currentColor" />
        </svg>
        <span className="placa__pais">Brasil</span>
        <svg className="placa__bandeira" viewBox="0 0 28 20">
          <rect width="28" height="20" fill="#1f8a3b" />
          <path d="M14 2.6 25.4 10 14 17.4 2.6 10z" fill="#f2c500" />
          <circle cx="14" cy="10" r="4.2" fill="#13307a" />
        </svg>
      </span>
      <span className="placa__corpo" aria-hidden="true">
        <span className="placa__br">BR</span>
        <span className="placa__caracteres">{caracteres || 'ABC1D23'}</span>
      </span>
    </span>
  )
}
