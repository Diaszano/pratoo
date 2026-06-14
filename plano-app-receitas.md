# Plano de Desenvolvimento — App de Receitas (Android)

> App pessoal e local para salvar receitas.
> Stack principal: **Kotlin + Jetpack Compose**.

## Visão geral

Como o app é **só pra uso pessoal e local**, dá pra cortar bastante complexidade que normalmente come tempo: nada de autenticação, conta de usuário, backend, sincronização em nuvem obrigatória, telemetria ou exigências da Play Store. O projeto pode ser enxuto e direto.

O único ponto a **não** ignorar, mesmo sendo pessoal, é o **backup dos dados** — uma troca de celular ou limpeza do app não pode significar perder todas as receitas. Por isso o backup entra já no MVP, numa versão simples.

---

## Escopo

### MVP (v1) — o que faz o app já valer a pena

- CRUD de receita: criar, ver, editar, apagar.
- Cada receita tem: título, foto (opcional), ingredientes, modo de preparo (passos), porções, tempo de preparo/cozimento, tags e anotações.
- Lista de todas as receitas com busca por título/ingrediente.
- Filtro por tag.
- Backup simples: exportar/importar um JSON (é o único dado e não há nuvem segurando ele).

### Depois (v2+) — em ordem de "quão legal vs quão trabalhoso"

- Favoritos.
- Escalar porções (recalcula as quantidades) — fácil e muito útil.
- Lista de compras a partir de receitas selecionadas.
- Modo cozinha (tela sempre ligada, passo a passo).
- Importar receita de um site (parsing do JSON-LD `schema.org/Recipe`) — o mais trabalhoso.

---

## Stack técnica

| Área | Escolha | Observação |
|------|---------|------------|
| Linguagem / UI | Kotlin + Jetpack Compose (Material 3) | — |
| Arquitetura | MVVM com fluxo unidirecional | `ViewModel` expõe estado via `StateFlow`; UI observa e dispara eventos (parecido com React + state hoisting) |
| Persistência | Room (wrapper de SQLite) | DAOs retornam `Flow`, então a lista se atualiza sozinha quando o banco muda |
| Injeção de dependência | Hilt | Alternativa mais leve: Koin, ou DI manual num app pequeno |
| Navegação | Navigation Compose com rotas type-safe (`@Serializable`) | — |
| Imagens | Coil | Foto fica no armazenamento interno do app; no banco guarda-se só o caminho/URI |
| Settings | DataStore (Preferences) | Substitui o antigo SharedPreferences |
| Tooling | Android Studio (última estável), Gradle com Kotlin DSL (`build.gradle.kts`), version catalog (`libs.versions.toml`) | — |

> Os números de versão das libs mudam rápido — pegar as últimas estáveis na documentação oficial de cada uma na hora de adicionar.

---

## Modelo de dados

Quatro entidades:

- **Recipe** — `id`, título, anotações, uri da imagem, porções, tempos, fonte, `createdAt`/`updatedAt`, `isFavorite`.
- **Ingredient** — `id`, `recipeId` (FK), nome, quantidade, unidade, posição.
- **Step** — `id`, `recipeId` (FK), texto, ordem.
- **Tag** — `id`, nome — com tabela de junção **RecipeTagCrossRef** (`recipeId`, `tagId`) para o relacionamento many-to-many.

Separar ingredientes e passos em tabelas próprias (em vez de jogar tudo num campo texto) entrega busca por ingrediente e o recurso de escalar porções "de graça" depois. No Room isso se monta com `@Relation`/`@Embedded` e foreign keys com `onDelete = CASCADE`.

---

## Telas

- **Lista (home):** grid ou lista com thumbnail e título, barra de busca, chips de filtro por tag e um FAB para adicionar.
- **Detalhe:** foto, metadados, ingredientes, passos e ações de editar/apagar.
- **Adicionar/Editar:** formulário com título, seletor de foto, linhas dinâmicas de ingrediente e de passo, tags, tempos e porções.
- **Settings (opcional):** tema, unidades e backup/restore.

Grafo de navegação: `Lista → Detalhe → Editar` e `Lista → Adicionar`.

---

## Estrutura do projeto

```
com.seuapp.recipes/
├── data/
│   ├── local/        (Room: entities, daos, database)
│   └── repository/
├── ui/
│   ├── recipelist/
│   ├── recipedetail/
│   ├── recipeedit/
│   ├── settings/
│   └── theme/
└── di/               (módulos Hilt)
```

Para um app pessoal pequeno, dá pra deixar o repository falando direto com o Room, sem uma camada de domínio separada. Se for preciso depois, é fácil inserir um `domain/` no meio.

---

## Roadmap em fases

Organizado para ter um app rodando cedo e ir incrementando:

| Fase | Foco | Entregável |
|------|------|------------|
| 0 | Setup | Projeto Compose novo, version catalog, dependências, tema Material 3, scaffold de navegação |
| 1 | Camada de dados | Entities, DAOs, database, repository; testar o CRUD com dados semente |
| 2 | Leitura (lista + detalhe) | Mostrar receitas e abrir o detalhe; conectar ViewModel + StateFlow + Flow do Room |
| 3 | Escrita (add/editar) | Formulário, listas dinâmicas, seletor de foto (Photo Picker), salvar no banco |
| 4 | Busca e filtro | Busca por título/ingrediente e chips de tag |
| 5 | Polimento | Estados vazios, confirmação de exclusão, tema escuro, ícone do app, backup/export |
| 6 | Stretch | Escalar porções, lista de compras, modo cozinha, importar de URL |

**Um app usável já está pronto no fim da fase 3.**

---

## Pontos específicos de Android (vindo de web/backend)

- **Ciclo de vida:** o `ViewModel` sobrevive à rotação de tela — observar o estado com `collectAsStateWithLifecycle()` para evitar vazamentos.
- **Fotos:** usar o **Photo Picker** moderno — não exige permissão de armazenamento.
- **Sem servidor = migrations locais:** ao mudar o schema, é preciso escrever uma migration no Room (ou apagar/reinstalar enquanto está em desenvolvimento). Não esquecer disso quando o modelo evoluir.
- **Compose ≈ React:** UI declarativa, renderizada a partir do estado, com state hoisting equivalente ao "lifting state up". A curva é suave para quem já fez frontend.
