# Back-end — Sistema de Aluguel de Carros

API REST do subsistema **Gestão de Pedidos e Contratos** (LAB02 · DIAW · PUC Minas).
Clientes fazem pedidos de aluguel; empresas e bancos (agentes) avaliam, executam o contrato e concedem crédito.

Modelagem completa (premissas, regras, histórias de usuário e diagramas): [`docs/MODELAGEM.md`](../docs/MODELAGEM.md).
O front-end que consome esta API está em [`frontend/`](../frontend/README.md).

## Stack

| | |
|---|---|
| Linguagem | Java 25 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security) |
| Autenticação | Spring Security + JWT (jjwt 0.12), senhas com BCrypt |
| Banco | PostgreSQL no Supabase (tabelas criadas pelo Hibernate, `ddl-auto=update`) |
| Outros | Lombok, JUnit 5 + AssertJ |

## Como rodar

1. Copie `.env.example` para `.env` e preencha:

   | Variável | O que é |
   |---|---|
   | `DB_URL`, `DB_USER`, `DB_PASSWORD` | conexão do Supabase (use o *Session pooler*) |
   | `JWT_SECRET` | texto aleatório com **pelo menos 32 caracteres** (menos que isso a aplicação não sobe) |
   | `FRONTEND_URL` | origem liberada no CORS, padrão `http://localhost:5173` (aceita várias separadas por vírgula) |
   | `SEED_SENHA_AGENTES` | senha dos agentes criados automaticamente, padrão `123456` |

2. Na pasta `backend/`:

   ```bash
   ./mvnw spring-boot:run
   ```

   Ou rode `AluguelcarrosApplication` pelo VS Code. O `.env` é lido tanto de `backend/` quanto da raiz do projeto.

3. A API sobe em `http://localhost:8080`.

### Dados iniciais

Na primeira execução, o `DataSeeder` cria os agentes (premissa P1: agentes não se cadastram pela tela) e 4 automóveis de exemplo. Ele só insere o que ainda não existe, então pode rodar sempre.

| Login | Senha | Perfil |
|---|---|---|
| `empresa` | `123456` | EMPRESA |
| `banco` | `123456` | BANCO |

Clientes se cadastram por `POST /api/auth/register` (ou pela tela de cadastro do front).

## Testes

```bash
./mvnw test -Dtest='ClienteTest,PedidoAluguelTest,ContratoTest'
```

São 17 testes das regras de negócio dos models (rendimentos, ciclo do pedido, proprietário do contrato, parcela pela Tabela Price). Eles não usam banco.

> O `AluguelcarrosApplicationTests` sobe o contexto inteiro e por isso **conecta no banco do `.env`**. Rodar `./mvnw test` sem filtro inclui esse teste.

## Arquitetura (MVC)

| MVC | Pacote | O que tem |
|---|---|---|
| **Model** | `model/` | entidades JPA e as regras que dependem só do próprio objeto |
| | `repository/` | interfaces Spring Data (consultas) |
| | `service/` | regras que envolvem várias entidades, permissão de dono e transações |
| **Controller** | `controller/` | endpoints REST: recebem DTO com `@Valid`, chamam o service e devolvem DTO |
| **View** | front-end React | consome a API |

```
src/main/java/br/pucminas/aluguelcarros/
├── config/       SecurityConfig (rotas por perfil, CORS, BCrypt), DataSeeder
├── security/     JwtService, JwtAuthenticationFilter, UsuarioDetailsService,
│                 UsuarioAutenticado (usuário logado), SecurityErrorHandler (401/403 em JSON)
├── model/        Usuario (abstrata, herança JOINED), Cliente, Endereco, Rendimento,
│                 Agente (abstrata), Empresa, Banco, Automovel, PedidoAluguel,
│                 Contrato, ContratoCredito, enums/
├── repository/   Usuario, Cliente, Agente, Banco, Automovel, PedidoAluguel, Contrato
├── dto/          auth/, cliente/, automovel/, pedido/, contrato/  (records)
├── service/      AuthService, ClienteService, AutomovelService,
│                 PedidoAluguelService, ContratoService
├── controller/   AuthController, ClienteController, AutomovelController,
│                 PedidoAluguelController, ContratoController
└── exception/    BusinessException (422), ResourceNotFoundException (404),
                  GlobalExceptionHandler, ErroResponse
```

A herança `JOINED` cria as tabelas `usuario`, `cliente`, `agente`, `empresa` e `banco`. Ao todo são 10 tabelas, junto com `rendimento`, `automovel`, `pedido_aluguel`, `contrato` e `contrato_credito`.

## Onde cada regra de negócio está

| Regra | Onde |
|---|---|
| RN01 – só cadastro e login são públicos | `SecurityConfig` |
| RN02 – login único, senha em BCrypt e nunca devolvida | `Usuario`, `AuthService`, DTOs de resposta sem senha |
| RN03 – perfis CLIENTE, EMPRESA e BANCO | `SecurityConfig` (rotas por perfil), `UsuarioAutenticado` |
| RN05 – até 3 rendimentos com valor > 0 | `Cliente.adicionarRendimento` / `substituirRendimentos`, `@Size(max = 3)` no DTO |
| RN06 – CPF válido; CPF e RG únicos | `@CPF` no DTO, `AuthService`, `ClienteService` |
| RN07 / RN09 – cliente só vê e altera o que é dele | `ClienteController` usa o id do token; `PedidoAluguelService.buscarVisivel` devolve 404 para pedido alheio |
| RN08 – pedido nasce EM_ANALISE | construtor de `PedidoAluguel` |
| RN10 / RN14 – só modifica EM_ANALISE | `PedidoAluguel.modificar` |
| RN11 / RN12 – cancela só EM_ANALISE ou APROVADO | `PedidoAluguel.cancelar` |
| RN13 – início não no passado, fim depois do início | `PedidoAluguel.validarPeriodo` |
| RN15 – parecer positivo → APROVADO, negativo → REPROVADO | `PedidoAluguel.aprovar` / `reprovar` |
| RN16 – só APROVADO vira contrato | `PedidoAluguel.marcarComoContratado` |
| RN17 – um contrato por pedido | `ContratoService` + coluna `pedido_id` única |
| RN18 – tipo do contrato define o dono do carro | `Contrato.definirProprietario` |
| RN19 – crédito só concedido por banco | rota só para BANCO; `ContratoCredito.banco` é do tipo `Banco` |
| RN20 – placa e matrícula únicas | `AutomovelService` + colunas únicas |
| RN21 – carro sem períodos sobrepostos aprovados/contratados | `PedidoAluguelRepository.existsConflitoDePeriodo` |
| RN22 – não exclui conta com pedido CONTRATADO | `ClienteService.excluir` |

## Autenticação

`POST /api/auth/login` devolve `{ token, tipo, id, login, nome, perfil }`.
Nas demais rotas, envie `Authorization: Bearer <token>`. O token vale 24 horas (`jwt.expiration-ms`).

O filtro JWT carrega o usuário do banco a cada requisição. Por isso, uma conta excluída perde o acesso na hora, mesmo com um token ainda válido.

## Endpoints

| Método | Rota | Perfil | Observação |
|---|---|---|---|
| POST | `/api/auth/register` | público | cadastro de cliente → 201 |
| POST | `/api/auth/login` | público | |
| GET | `/api/clientes/me` | CLIENTE | |
| PUT | `/api/clientes/me` | CLIENTE | não muda o login; `senha` vazia mantém a atual |
| DELETE | `/api/clientes/me` | CLIENTE | apaga também os pedidos sem contrato; 422 se tiver pedido CONTRATADO |
| GET | `/api/automoveis` | todos | |
| GET | `/api/automoveis/{id}` | todos | |
| POST | `/api/automoveis` | EMPRESA, BANCO | 201 |
| PUT | `/api/automoveis/{id}` | EMPRESA, BANCO | |
| DELETE | `/api/automoveis/{id}` | EMPRESA, BANCO | 422 se o carro tiver pedidos |
| POST | `/api/pedidos` | CLIENTE | nasce EM_ANALISE → 201 |
| GET | `/api/pedidos/meus` | CLIENTE | |
| GET | `/api/pedidos?status=EM_ANALISE` | EMPRESA, BANCO | sem `status`, lista todos |
| GET | `/api/pedidos/{id}` | todos | cliente só vê os próprios (404 nos outros) |
| PUT | `/api/pedidos/{id}` | todos | só EM_ANALISE; cliente só os próprios |
| PATCH | `/api/pedidos/{id}/cancelar` | CLIENTE | só EM_ANALISE ou APROVADO |
| PATCH | `/api/pedidos/{id}/avaliar` | EMPRESA, BANCO | corpo `{ aprovado, parecer }` |
| GET | `/api/contratos` | EMPRESA, BANCO | |
| GET | `/api/contratos/{id}` | EMPRESA, BANCO | |
| POST | `/api/contratos` | EMPRESA, BANCO | corpo `{ pedidoId, tipo, valorTotal }`; pedido vira CONTRATADO → 201 |
| POST | `/api/contratos/{id}/credito` | BANCO | corpo `{ valor, taxaJurosMensal, numeroParcelas }` → 201 |

### O que as respostas trazem além do óbvio

- **Pedido:** `clienteProfissao` e `clienteRendaTotal`, base da análise financeira do agente. Também `editavel` e `cancelavel`, para o front saber quais botões mostrar.
- **Contrato:** `clienteNome`, o `automovel` e o `credito` (com `valorParcela` já calculada).
- **Automóvel:** `proprietarioId` e `proprietarioNome`, preenchidos depois que um contrato é executado.

### Tipo de contrato (RN18)

| `tipo` | Quem fica com o carro | Quem pode executar |
|---|---|---|
| `PROPRIEDADE_CLIENTE` | o cliente do pedido | empresa ou banco |
| `PROPRIEDADE_EMPRESA` | a empresa que executou | só empresa |
| `PROPRIEDADE_BANCO` | o banco que executou | só banco |

Se um banco conceder crédito num contrato `PROPRIEDADE_BANCO`, o banco do crédito passa a ser o proprietário.

A taxa de juros é em percentual ao mês (`1.5` = 1,5% a.m.). A parcela segue a Tabela Price: R$ 10.000 a 1,5% em 12x dá R$ 916,80.

## Erros

Todos os erros seguem o mesmo formato:

```json
{
  "timestamp": "2026-11-02T10:00:51",
  "status": 422,
  "erro": "Unprocessable Content",
  "mensagem": "O automóvel já está reservado em parte desse período.",
  "caminho": "/api/pedidos/2/avaliar",
  "campos": null
}
```

| Status | Quando |
|---|---|
| 400 | validação (`campos` traz a mensagem de cada campo, ex. `"endereco.cep"`, `"rendimentos[0].valor"`) ou JSON mal formado |
| 401 | sem token, token inválido ou expirado, ou login/senha errados |
| 403 | perfil sem permissão para a rota |
| 404 | recurso não existe (ou não pertence ao cliente logado) |
| 409 | violação de restrição no banco |
| 422 | regra de negócio (status do pedido, duplicidade, período ocupado…) |
