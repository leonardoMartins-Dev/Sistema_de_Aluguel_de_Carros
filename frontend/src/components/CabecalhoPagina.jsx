export default function CabecalhoPagina({ titulo, descricao, acoes }) {
  return (
    <header className="cabecalho-pagina">
      <div>
        <h1 className="cabecalho-pagina__titulo">{titulo}</h1>
        {descricao && <p className="cabecalho-pagina__descricao">{descricao}</p>}
      </div>
      {acoes && <div className="acoes">{acoes}</div>}
    </header>
  )
}
