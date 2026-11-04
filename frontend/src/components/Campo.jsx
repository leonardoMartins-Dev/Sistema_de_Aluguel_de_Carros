import { cloneElement } from 'react'
import './Campo.css'

/**
 * Campo de formulário em "caixa", como em formulário de órgão público:
 * rótulo dentro da caixa, valor embaixo, erro logo abaixo da caixa.
 * O filho é o <input>/<select>/<textarea>; id, aria-* e classe são injetados aqui.
 */
export default function Campo({ id, rotulo, erro, dica, className = '', children }) {
  const idDescricao = erro ? `${id}-erro` : dica ? `${id}-dica` : undefined

  return (
    <div className={`campo ${erro ? 'campo--erro' : ''} ${className}`}>
      <div className="campo__caixa">
        <label className="campo__rotulo" htmlFor={id}>
          {rotulo}
        </label>
        {cloneElement(children, {
          id,
          className: `campo__entrada ${children.props.className ?? ''}`,
          'aria-invalid': erro ? true : undefined,
          'aria-describedby': idDescricao,
        })}
      </div>
      {erro ? (
        <p className="campo__erro" id={`${id}-erro`}>
          {erro}
        </p>
      ) : (
        dica && (
          <p className="campo__dica" id={`${id}-dica`}>
            {dica}
          </p>
        )
      )}
    </div>
  )
}
