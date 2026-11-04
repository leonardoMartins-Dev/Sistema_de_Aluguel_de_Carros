// Rótulos dos enums do back-end
export const STATUS = {
  EM_ANALISE: 'Em análise',
  APROVADO: 'Aprovado',
  REPROVADO: 'Reprovado',
  CANCELADO: 'Cancelado',
  CONTRATADO: 'Contratado',
}

export const PERFIS = {
  CLIENTE: 'Cliente',
  EMPRESA: 'Empresa',
  BANCO: 'Banco',
}

export const TIPOS_CONTRATO = {
  PROPRIEDADE_CLIENTE: 'Carro fica com o cliente',
  PROPRIEDADE_EMPRESA: 'Carro fica com a empresa',
  PROPRIEDADE_BANCO: 'Carro fica com o banco',
}

export const MAX_RENDIMENTOS = 3

export function ehAgente(perfil) {
  return perfil === 'EMPRESA' || perfil === 'BANCO'
}

export function rotaInicial(perfil) {
  return perfil === 'CLIENTE' ? '/pedidos' : '/analise'
}

/** Inclinação estável do carimbo por pedido: entre -7° e -2° */
export function giroDoCarimbo(id) {
  return -2 - ((id * 37) % 6)
}

/** 12 → "Nº 000012" */
export function protocolo(id) {
  return `Nº ${String(id).padStart(6, '0')}`
}

/** "2026-11-03" ou "2026-11-03T10:00:00" → "03/11/2026" */
export function data(iso) {
  if (!iso) return ''
  const [ano, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${ano}`
}

export function moeda(valor) {
  return Number(valor ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

export function percentual(valor) {
  return `${Number(valor).toLocaleString('pt-BR', { maximumFractionDigits: 4 })}%`
}

export function plural(n, singular, pluralForma) {
  return `${n} ${n === 1 ? singular : pluralForma}`
}

/** Data de hoje no fuso local, no formato do input date */
export function hojeISO() {
  const d = new Date()
  const mes = String(d.getMonth() + 1).padStart(2, '0')
  const dia = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${mes}-${dia}`
}

export function somarDias(iso, dias) {
  const d = new Date(`${iso}T12:00:00`)
  d.setDate(d.getDate() + dias)
  return d.toISOString().slice(0, 10)
}

/** Número de diárias entre retirada e devolução */
export function diarias(inicio, fim) {
  if (!inicio || !fim) return 0
  const ms = new Date(`${fim}T12:00:00`) - new Date(`${inicio}T12:00:00`)
  return Math.round(ms / 86_400_000)
}

export function mascaraCPF(valor) {
  const d = valor.replace(/\D/g, '').slice(0, 11)
  return d
    .replace(/^(\d{3})(\d)/, '$1.$2')
    .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d)/, '.$1-$2')
}

export function mascaraCEP(valor) {
  const d = valor.replace(/\D/g, '').slice(0, 8)
  return d.length > 5 ? `${d.slice(0, 5)}-${d.slice(5)}` : d
}

/** "abc-1d23" → "ABC1D23" (mesma normalização do back-end) */
export function limparPlaca(valor) {
  return valor.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 7)
}

/** Parcela pela Tabela Price, igual ao ContratoCredito.calcularValorParcela() do back-end */
export function parcelaPrice(valor, taxaPercentual, parcelas) {
  const pv = Number(valor)
  const n = Number(parcelas)
  const i = Number(taxaPercentual) / 100
  if (!(pv > 0) || !(n >= 1) || !(i >= 0)) return null
  if (i === 0) return pv / n
  const fator = (1 + i) ** n
  return (pv * i * fator) / (fator - 1)
}
