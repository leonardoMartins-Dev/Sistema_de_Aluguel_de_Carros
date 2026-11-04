import { STATUS } from '../utils/formato'
import './Carimbo.css'

/**
 * Status do pedido como carimbo de tinta (assinatura visual do sistema).
 * animar: o carimbo "cai" no papel — usado quando o status acabou de mudar.
 * giro: inclinação em graus; varia por pedido para não parecer clonado.
 */
export default function Carimbo({ status, tamanho = 'm', animar = false, giro = -5, atraso = 0, className = '' }) {
  return (
    <span
      className={`carimbo carimbo--${status.toLowerCase()} carimbo--${tamanho} ${animar ? 'carimbo--anima' : ''} ${className}`}
      style={{ '--giro': `${giro}deg`, '--atraso': `${atraso}ms` }}
    >
      {STATUS[status] ?? status}
    </span>
  )
}
