# Pratoo — App de Receitas Pessoais

## Visão geral

O Pratoo é um app Android de receitas pessoais, local-first, sem autenticação nem backend. Tudo fica salvo no dispositivo. O usuário pode criar, editar, buscar e organizar receitas, favoritar as preferidas, e exportar/importar um backup em JSON.

**Stack:** Kotlin, Jetpack Compose (Material 3), Room, Hilt, Navigation Compose, DataStore, Coil.

---

## Telas e funcionalidades

### 1. Tela inicial (Lista de receitas)

- Grid adaptativo (mínimo 160dp) com cards de receita
- Cada card mostra: foto (ou placeholder), título, ícone de estrela se favorita
- Barra de busca por título ou nome de ingrediente (debounce 300ms)
- Chips de filtro por tag (exclusivos — toque para selecionar, toque de novo para desmarcar)
- Estado vazio com mensagem contextual ("Nenhuma receita ainda" ou "Nenhum resultado encontrado")
- FAB (+) para criar receita
- Ícone de configurações no topo

### 2. Tela de detalhe da receita

- Foto em destaque (16:9)
- Barra de ações: estrela (favoritar/desfavoritar), editar, excluir
- FAB flutuante para editar
- Metadados: porções, tempo de preparo, tempo de cozimento
- Tags como chips
- Seção de ingredientes (quantidade + unidade + nome)
- Seção de modo de preparo (passos numerados)
- Seção de anotações
- URL da fonte (quando preenchida)
- Diálogo de confirmação para exclusão

### 3. Tela de edição/criação

- Foto com seletor de galeria (Photo Picker, API 29+)
- Campo de título (obrigatório, com validação)
- Metadados: porções, tempo de preparo, tempo de cozimento
- Toggle de favorito (FilterChip com estrela)
- **Ingredientes:** cada linha tem nome, quantidade e unidade (dropdown com unidades de medida predefinidas)
- **Modo de preparo:** passos com texto e numeração
- **Tags:** chips selecionáveis + campo para criar nova tag (auto-seleciona após criação)
- Campo de anotações (multiline)
- Campo de URL da fonte
- Botão "Salvar receita" fixo no rodapé

### 4. Tela de configurações

- **Backup:** Exportar receitas para JSON / Importar de arquivo JSON
- **Aparência:** Seleção de tema (Sistema, Claro, Escuro, Moonlight)
- **Preferências:** Sistema de unidades (Métrico, Imperial)

---

## Unidades de medida

O app possui uma tabela de unidades no banco de dados com 14 unidades padrão de culinária:

| Categoria | Unidades |
|-----------|----------|
| Peso | g, kg, mg, oz, lb |
| Volume | L, ml, cup, tbsp, tsp, fl oz |
| Contagem | unit, pinch |
| Outro | to taste |

Na tela de edição, a unidade é selecionada via dropdown (menu expansivo) que mostra a abreviação e o nome completo.

---

## Temas

4 opções de tema disponíveis:

| Tema | Descrição |
|------|-----------|
| Sistema | Segue a configuração do Android |
| Claro | Paleta quente marrom (primary #825500) |
| Escuro | Paleta escura com dourado (primary #FEBF28) |
| Moonlight | Paleta azul-escura inspirada em Tokyo Night (primary #7AA2F7, bg #1A1B26) |

---

## Modelo de dados

### Entidades principais

- **RecipeEntity:** id, title, notes, imageUri, servings, prepTimeMinutes, cookTimeMinutes, sourceUrl, isFavorite, createdAt, updatedAt
- **IngredientEntity:** id, recipeId (FK), name, quantity, unit, position
- **StepEntity:** id, recipeId (FK), text, order
- **TagEntity:** id, name
- **RecipeTagCrossRef:** recipeId, tagId (tabela de junção many-to-many)
- **MeasurementUnit:** id, abbreviation, displayName, category

### Backup

- Exportação/importação em JSON via kotlinx.serialization
- Formato versionado (campo `version` no JSON)
- Inclui: receitas, ingredientes, passos, tags, favoritos, URL da fonte
- Limitação: URIs de imagem não são portáveis entre dispositivos

---

## Fluxos principais

1. **Criar receita:** Foto → título → metadados → favorito → ingredientes (com unidade) → passos → tags → anotações → fonte → salvar
2. **Editar receita:** Mesmo formulário pré-preenchido com dados existentes
3. **Buscar/filtrar:** Busca por título ou ingrediente + filtro por tag
4. **Favoritar:** Estrela na lista, no detalhe ou no formulário de edição
5. **Backup:** Exportar → salva JSON no armazenamento do dispositivo / Importar → seleciona arquivo JSON
6. **Mudar tema:** Configurações → Aparência → selecionar tema

---

## Notas para o designer

- O app usa Material 3 com 4 paletas de cores (light, dark, moonlight, system)
- A identidade visual atual é neutra — não tem logo ou ícone de launcher personalizado além do vetor básico
- Os cards da lista usam proporção 4:3 para a imagem
- O formulário de edição é longo — pode precisar de melhor organização visual
- O dropdown de unidades na edição de ingredente pode ser otimizado para UX mobile
- Os chips de tags podem_OVERFLOW quando há muitas tags
- A tela de configurações é simples e pode ser expandida com mais seções
- Não há onboarding ou tutorial para novos usuários
- Não há animações de transição entre telas
- Não há skeleton loading ou shimmer nos cards
- O estado vazio da lista usa um ícone de busca off — poderia ter ilustração personalizada
