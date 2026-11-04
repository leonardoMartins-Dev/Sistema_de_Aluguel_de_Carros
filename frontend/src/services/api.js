// Cliente HTTP da API Spring Boot: base URL, token JWT e erros no formato ErroResponse
const BASE_URL = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080').replace(/\/$/, '')
const CHAVE_SESSAO = 'aluguel.sessao'

export class ApiError extends Error {
  constructor(status, mensagem, campos) {
    super(mensagem)
    this.status = status
    // Erros de validação: { "cpf": "CPF inválido.", "rendimentos[0].valor": "..." }
    this.campos = campos ?? {}
  }
}

let aoSessaoExpirar = () => {}

/** O AuthContext registra aqui o que fazer quando a API responde 401 com sessão aberta. */
export function definirAoSessaoExpirar(callback) {
  aoSessaoExpirar = callback
}

export function lerSessao() {
  try {
    return JSON.parse(localStorage.getItem(CHAVE_SESSAO))
  } catch {
    return null
  }
}

export function salvarSessao(sessao) {
  try {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao))
  } catch {
    // Sem localStorage (aba anônima bloqueada): a sessão vale só até recarregar a página
  }
}

export function limparSessao() {
  try {
    localStorage.removeItem(CHAVE_SESSAO)
  } catch {
    // nada a limpar
  }
}

async function requisitar(metodo, caminho, corpo) {
  const sessao = lerSessao()
  const headers = {}
  if (corpo !== undefined) headers['Content-Type'] = 'application/json'
  if (sessao?.token) headers.Authorization = `Bearer ${sessao.token}`

  let resposta
  try {
    resposta = await fetch(BASE_URL + caminho, {
      method: metodo,
      headers,
      body: corpo !== undefined ? JSON.stringify(corpo) : undefined,
    })
  } catch {
    throw new ApiError(0, `Sem conexão com a API em ${BASE_URL}. Confira se o back-end está rodando.`)
  }

  if (resposta.status === 204) return null
  const dados = await resposta.json().catch(() => null)

  if (!resposta.ok) {
    if (resposta.status === 401 && sessao?.token) aoSessaoExpirar()
    throw new ApiError(resposta.status, dados?.mensagem ?? `Erro ${resposta.status} na API.`, dados?.campos)
  }
  return dados
}

export const auth = {
  login: (login, senha) => requisitar('POST', '/api/auth/login', { login, senha }),
  cadastrar: (cliente) => requisitar('POST', '/api/auth/register', cliente),
}

export const clientes = {
  meusDados: () => requisitar('GET', '/api/clientes/me'),
  atualizar: (dados) => requisitar('PUT', '/api/clientes/me', dados),
  excluirConta: () => requisitar('DELETE', '/api/clientes/me'),
}

export const automoveis = {
  listar: () => requisitar('GET', '/api/automoveis'),
  buscar: (id) => requisitar('GET', `/api/automoveis/${id}`),
  criar: (automovel) => requisitar('POST', '/api/automoveis', automovel),
  atualizar: (id, automovel) => requisitar('PUT', `/api/automoveis/${id}`, automovel),
  excluir: (id) => requisitar('DELETE', `/api/automoveis/${id}`),
}

export const pedidos = {
  criar: (pedido) => requisitar('POST', '/api/pedidos', pedido),
  meus: () => requisitar('GET', '/api/pedidos/meus'),
  listar: (status) => requisitar('GET', status ? `/api/pedidos?status=${status}` : '/api/pedidos'),
  buscar: (id) => requisitar('GET', `/api/pedidos/${id}`),
  modificar: (id, pedido) => requisitar('PUT', `/api/pedidos/${id}`, pedido),
  cancelar: (id) => requisitar('PATCH', `/api/pedidos/${id}/cancelar`),
  avaliar: (id, aprovado, parecer) => requisitar('PATCH', `/api/pedidos/${id}/avaliar`, { aprovado, parecer }),
}

export const contratos = {
  listar: () => requisitar('GET', '/api/contratos'),
  executar: (contrato) => requisitar('POST', '/api/contratos', contrato),
  concederCredito: (id, credito) => requisitar('POST', `/api/contratos/${id}/credito`, credito),
}
