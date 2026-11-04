import { useEffect, useRef, useState } from 'react'
import { MAX_RENDIMENTOS, mascaraCEP, mascaraCPF, moeda } from '../utils/formato'
import Campo from './Campo'
import './FormCliente.css'

const ENDERECO_VAZIO = { cep: '', logradouro: '', numero: '', complemento: '', bairro: '', cidade: '', uf: '' }

function estadoInicial(inicial) {
  if (!inicial) {
    return { login: '', senha: '', email: '', nome: '', cpf: '', rg: '', profissao: '', endereco: ENDERECO_VAZIO }
  }
  return {
    senha: '',
    email: inicial.email,
    nome: inicial.nome,
    cpf: mascaraCPF(inicial.cpf),
    rg: inicial.rg,
    profissao: inicial.profissao,
    endereco: { ...ENDERECO_VAZIO, ...inicial.endereco, complemento: inicial.endereco?.complemento ?? '' },
  }
}

/**
 * Dados do cliente (US01 e US05).
 * modo="cadastro" pede login e senha; modo="edicao" não mostra login e a senha é opcional.
 * aoEnviar(corpo) deve lançar ApiError em caso de erro; os erros por campo aparecem embaixo de cada caixa.
 */
export default function FormCliente({ modo, inicial, aoEnviar, rotuloEnviar }) {
  const [dados, setDados] = useState(() => estadoInicial(inicial))
  const [rendimentos, setRendimentos] = useState(
    () => inicial?.rendimentos?.map((r) => ({ entidadeEmpregadora: r.entidadeEmpregadora, valor: String(r.valor) })) ?? [],
  )
  const [erros, setErros] = useState({})
  const [erroGeral, setErroGeral] = useState('')
  const [enviando, setEnviando] = useState(false)
  const formRef = useRef(null)
  const cadastro = modo === 'cadastro'

  // Depois de um erro, leva o foco para o primeiro campo inválido
  useEffect(() => {
    const primeiro = formRef.current?.querySelector('[aria-invalid="true"]')
    if (primeiro) primeiro.focus()
    else if (erroGeral) formRef.current?.querySelector('.aviso')?.scrollIntoView({ block: 'center' })
  }, [erros, erroGeral])

  const alterar = (campo, mascara) => (e) => {
    const valor = mascara ? mascara(e.target.value) : e.target.value
    setDados((d) => ({ ...d, [campo]: valor }))
  }

  const alterarEndereco = (campo, mascara) => (e) => {
    const valor = mascara ? mascara(e.target.value) : e.target.value
    setDados((d) => ({ ...d, endereco: { ...d.endereco, [campo]: valor } }))
  }

  const alterarRendimento = (indice, campo) => (e) => {
    const valor = e.target.value
    setRendimentos((lista) => lista.map((r, i) => (i === indice ? { ...r, [campo]: valor } : r)))
  }

  const rendaTotal = rendimentos.reduce((soma, r) => soma + (Number(r.valor) || 0), 0)

  async function enviar(e) {
    e.preventDefault()
    setEnviando(true)
    setErros({})
    setErroGeral('')

    const corpo = {
      ...dados,
      rendimentos: rendimentos.map((r) => ({
        entidadeEmpregadora: r.entidadeEmpregadora,
        valor: r.valor === '' ? null : Number(r.valor),
      })),
    }
    if (!cadastro) delete corpo.login

    try {
      await aoEnviar(corpo)
      if (!cadastro) setDados((d) => ({ ...d, senha: '' }))
    } catch (err) {
      setErros(err.campos ?? {})
      setErroGeral(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form ref={formRef} className="form-cliente" onSubmit={enviar} noValidate>
      {erroGeral && (
        <p className="aviso" role="alert">
          {erroGeral}
        </p>
      )}

      <fieldset className="secao-form" aria-labelledby="sec-acesso">
        <div>
          <h2 id="sec-acesso" className="secao-form__titulo">Acesso</h2>
          <p className="secao-form__texto">
            {cadastro ? 'Você vai entrar no sistema com este login e senha.' : `Seu login é "${inicial.login}" e não pode ser alterado.`}
          </p>
        </div>
        <div className="grade">
          {cadastro && (
            <Campo id="login" rotulo="Login" erro={erros.login} className="col-3">
              <input value={dados.login} onChange={alterar('login')} autoComplete="username" />
            </Campo>
          )}
          <Campo
            id="senha"
            rotulo={cadastro ? 'Senha' : 'Nova senha'}
            erro={erros.senha}
            dica={cadastro ? 'Pelo menos 6 caracteres.' : 'Deixe em branco para manter a senha atual.'}
            className={cadastro ? 'col-3' : ''}
          >
            <input type="password" value={dados.senha} onChange={alterar('senha')} autoComplete="new-password" />
          </Campo>
          <Campo id="email" rotulo="E-mail" erro={erros.email}>
            <input type="email" value={dados.email} onChange={alterar('email')} autoComplete="email" />
          </Campo>
        </div>
      </fieldset>

      <fieldset className="secao-form" aria-labelledby="sec-identificacao">
        <div>
          <h2 id="sec-identificacao" className="secao-form__titulo">Identificação</h2>
          <p className="secao-form__texto">CPF e RG não podem estar em outra conta.</p>
        </div>
        <div className="grade">
          <Campo id="nome" rotulo="Nome completo" erro={erros.nome}>
            <input value={dados.nome} onChange={alterar('nome')} autoComplete="name" />
          </Campo>
          <Campo id="cpf" rotulo="CPF" erro={erros.cpf} className="col-3">
            <input value={dados.cpf} onChange={alterar('cpf', mascaraCPF)} inputMode="numeric" placeholder="000.000.000-00" />
          </Campo>
          <Campo id="rg" rotulo="RG" erro={erros.rg} className="col-3">
            <input value={dados.rg} onChange={alterar('rg')} />
          </Campo>
          <Campo id="profissao" rotulo="Profissão" erro={erros.profissao}>
            <input value={dados.profissao} onChange={alterar('profissao')} autoComplete="organization-title" />
          </Campo>
        </div>
      </fieldset>

      <fieldset className="secao-form" aria-labelledby="sec-endereco">
        <div>
          <h2 id="sec-endereco" className="secao-form__titulo">Endereço</h2>
        </div>
        <div className="grade">
          <Campo id="cep" rotulo="CEP" erro={erros['endereco.cep']} className="col-2">
            <input value={dados.endereco.cep} onChange={alterarEndereco('cep', mascaraCEP)} inputMode="numeric" autoComplete="postal-code" placeholder="00000-000" />
          </Campo>
          <Campo id="logradouro" rotulo="Logradouro" erro={erros['endereco.logradouro']} className="col-4">
            <input value={dados.endereco.logradouro} onChange={alterarEndereco('logradouro')} autoComplete="address-line1" />
          </Campo>
          <Campo id="numero" rotulo="Número" erro={erros['endereco.numero']} className="col-2 manter">
            <input value={dados.endereco.numero} onChange={alterarEndereco('numero')} />
          </Campo>
          <Campo id="complemento" rotulo="Complemento" erro={erros['endereco.complemento']} className="col-4 manter">
            <input value={dados.endereco.complemento} onChange={alterarEndereco('complemento')} autoComplete="address-line2" placeholder="Opcional" />
          </Campo>
          <Campo id="bairro" rotulo="Bairro" erro={erros['endereco.bairro']} className="col-2">
            <input value={dados.endereco.bairro} onChange={alterarEndereco('bairro')} />
          </Campo>
          <Campo id="cidade" rotulo="Cidade" erro={erros['endereco.cidade']} className="col-3 manter">
            <input value={dados.endereco.cidade} onChange={alterarEndereco('cidade')} autoComplete="address-level2" />
          </Campo>
          <Campo id="uf" rotulo="UF" erro={erros['endereco.uf']} className="col-1 manter">
            <input
              value={dados.endereco.uf}
              onChange={alterarEndereco('uf', (v) => v.replace(/[^a-z]/gi, '').slice(0, 2).toUpperCase())}
              autoComplete="address-level1"
              placeholder="MG"
            />
          </Campo>
        </div>
      </fieldset>

      <fieldset className="secao-form" aria-labelledby="sec-rendimentos">
        <div>
          <h2 id="sec-rendimentos" className="secao-form__titulo">Rendimentos</h2>
          <p className="secao-form__texto">
            Até {MAX_RENDIMENTOS} fontes de renda. É com elas que empresas e bancos analisam seus pedidos.
          </p>
        </div>
        <div className="rendimentos">
          {erros.rendimentos && <p className="campo__erro">{erros.rendimentos}</p>}
          {rendimentos.length > 0 && (
            <ol className="rendimentos__lista">
              {rendimentos.map((r, i) => (
                <li key={i} className="rendimentos__item">
                  <div className="grade">
                    <Campo id={`rend-${i}-entidade`} rotulo="Empregador ou fonte" erro={erros[`rendimentos[${i}].entidadeEmpregadora`]} className="col-4">
                      <input value={r.entidadeEmpregadora} onChange={alterarRendimento(i, 'entidadeEmpregadora')} />
                    </Campo>
                    <Campo id={`rend-${i}-valor`} rotulo="Valor mensal (R$)" erro={erros[`rendimentos[${i}].valor`]} className="col-2">
                      <input type="number" min="0.01" step="0.01" inputMode="decimal" value={r.valor} onChange={alterarRendimento(i, 'valor')} />
                    </Campo>
                  </div>
                  <button
                    type="button"
                    className="botao botao--texto rendimentos__remover"
                    onClick={() => setRendimentos((lista) => lista.filter((_, j) => j !== i))}
                  >
                    Remover<span className="visualmente-oculto"> rendimento {i + 1}</span>
                  </button>
                </li>
              ))}
            </ol>
          )}
          <div className="rendimentos__rodape">
            <button
              type="button"
              className="botao botao--secundario botao--compacto"
              onClick={() => setRendimentos((lista) => [...lista, { entidadeEmpregadora: '', valor: '' }])}
              disabled={rendimentos.length >= MAX_RENDIMENTOS}
            >
              Adicionar rendimento
            </button>
            <span className="rendimentos__contagem">
              {rendimentos.length} de {MAX_RENDIMENTOS}
              {rendimentos.length > 0 && (
                <>
                  {' · '}renda total <span className="dado">{moeda(rendaTotal)}</span>
                </>
              )}
            </span>
          </div>
        </div>
      </fieldset>

      <div className="form-cliente__envio">
        <button type="submit" className="botao botao--primario" disabled={enviando}>
          {enviando ? 'Salvando…' : rotuloEnviar}
        </button>
      </div>
    </form>
  )
}
