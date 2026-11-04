# Front-end — Sistema de Aluguel de Carros

Interface web do subsistema **Construção Dinâmica das Páginas Web** (a *View* do MVC), feita em React + Vite.
Consome a API REST do [`backend/`](../backend/README.md): clientes pedem carros e acompanham a análise; empresas e bancos avaliam, executam contratos e concedem crédito.

## Como rodar

Pré-requisitos: Node 22 LTS ou superior (testado com o 24) e o back-end rodando em `http://localhost:8080`.

```bash
cd frontend
npm install
npm run dev
```

Abra `http://localhost:5173`.

Se a API estiver em outro endereço, crie um `.env` a partir do `.env.example`:

```
VITE_API_URL=http://localhost:8080
```

> O back-end só aceita requisições da origem configurada em `FRONTEND_URL` (padrão `http://localhost:5173`). Se mudar a porta do front, ajuste lá também.

### Contas para testar

| Login | Senha | Perfil | Como surge |
|---|---|---|---|
| `empresa` | `123456` | Empresa | criada pelo `DataSeeder` do back-end |
| `banco` | `123456` | Banco | criada pelo `DataSeeder` do back-end |
| — | — | Cliente | crie pela tela **Criar conta de cliente** |

## Scripts

| Comando | O que faz |
|---|---|
| `npm run dev` | servidor de desenvolvimento com recarga automática |
| `npm run build` | gera a versão de produção em `dist/` |
| `npm run preview` | serve o `dist/` localmente |
| `npm run lint` | roda o oxlint |

## Telas

| Rota | Perfil | O que faz | História |
|---|---|---|---|
| `/login` | público | entrar | US02 |
| `/cadastro` | público | criar conta de cliente, já com rendimentos; entra direto depois | US01 |
| `/pedidos` | Cliente | meus pedidos com o status, o parecer e as ações de modificar e cancelar | US09, US10, US11 |
| `/pedidos/novo` | Cliente | escolher o carro e as datas, com resumo de diárias | US08 |
| `/automoveis` | todos | frota; cliente pede o carro, agente cadastra, edita e exclui | US07, US12 |
| `/meus-dados` | Cliente | editar dados e rendimentos, trocar senha, excluir conta | US04, US05, US06 |
| `/analise` | Empresa, Banco | pedidos filtrados por status; avaliar com parecer e executar contrato | US13, US15, US16 |
| `/pedidos/:id/editar` | todos | modificar pedido em análise (cliente só os próprios) | US10, US14 |
| `/contratos` | Empresa, Banco | contratos executados; o banco concede crédito com prévia da parcela | US17 |

"Sair" fica no topo (US03). Rotas protegidas mandam para o login quem não está logado, e mandam para a página inicial do próprio perfil quem não tem permissão.

## Estrutura

```
src/
├── main.jsx, App.jsx          providers (rotas, sessão, avisos)
├── index.css                  tokens de cor e tipografia, botões, avisos, layout de página
├── services/api.js            fetch com base URL, token JWT e erros da API (ApiError)
├── context/
│   ├── AuthProvider.jsx       sessão: entrar, sair, nome no topo; sai sozinho quando a API devolve 401
│   ├── ToastProvider.jsx      avisos curtos depois de cada ação
│   └── useAuth.js, useToast.js, contextos.js
├── routes/
│   ├── AppRoutes.jsx          mapa de rotas
│   └── PrivateRoute.jsx       bloqueia sem login e por perfil
├── components/
│   ├── Placa.jsx              placa Mercosul (identidade de cada carro e marca do sistema)
│   ├── Carimbo.jsx            status do pedido como carimbo de tinta
│   ├── PedidoCard.jsx         pedido em forma de canhoto de protocolo
│   ├── Campo.jsx              campo de formulário com rótulo, dica e erro
│   ├── FormCliente.jsx        dados do cliente (cadastro e edição)
│   ├── FormAutomovel.jsx      cadastro de carro com a placa desenhada ao vivo
│   ├── Dialogo.jsx, Confirmacao.jsx
│   ├── Navbar.jsx, Layout.jsx, CabecalhoPagina.jsx
├── pages/
│   ├── Login.jsx, Cadastro.jsx, Automoveis.jsx, PedidoForm.jsx
│   ├── cliente/   MeusPedidos.jsx, MeusDados.jsx
│   └── agente/    PedidosAnalise.jsx, Contratos.jsx
└── utils/formato.js           datas, moeda, protocolo, máscaras de CPF e CEP, cálculo da parcela
```

## Como o front conversa com a API

- **Sessão:** o login guarda `{ token, usuario }` no `localStorage`. Ao abrir o site, a sessão é descartada se o token já expirou.
- **Requisições:** cada uma vai com `Authorization: Bearer <token>`. Se a API devolver 401 com sessão aberta (token expirado, adulterado ou conta excluída), o usuário é levado ao login com o aviso "Sua sessão expirou".
- **Erros:** chegam no formato `ErroResponse` do back-end. A `mensagem` aparece no topo do formulário, e os erros por campo (`campos`) aparecem embaixo de cada caixa, com o foco indo para o primeiro campo inválido. A validação fica no back-end; o front não duplica as regras.
- **Permissões:** o front esconde o que o perfil não pode fazer (por exemplo, "Conceder crédito" só aparece para banco), mas quem garante é a API.

## Identidade visual

A interface parte de dois objetos do mundo do aluguel de carros:

- **Placa Mercosul:** cada automóvel aparece como a própria placa, e a marca do sistema é uma placa com "ALUGUEL", que tem exatamente 7 letras.
- **Carimbo:** o status do pedido é um carimbo de tinta, nas cores clássicas (âmbar em análise, verde aprovado, vermelho reprovado, violeta contratado, cinza cancelado). Quando o status muda, o carimbo "cai" no papel.

O resto fica discreto, com cara de formulário de órgão público:

- campos em caixas com o rótulo dentro;
- números de protocolo em fonte monoespaçada;
- pedidos como canhotos com linha picotada.

Fontes (Google Fonts):

- **Barlow Condensed:** placas, carimbos e títulos. A família foi desenhada a partir de placas e sinalização.
- **Atkinson Hyperlegible Next:** texto. Diferencia 0/O e 1/l/I, o que importa em CPF, RG e placa.
- **Atkinson Hyperlegible Mono:** datas, valores e protocolos.

Acessibilidade:

- foco visível em todos os controles;
- diálogos com `<dialog>` nativo, que fecha no Esc e prende o foco;
- animações desligadas para quem usa "reduzir movimento";
- layout testado até 390 px de largura.
