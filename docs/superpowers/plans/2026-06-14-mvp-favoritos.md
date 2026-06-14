# MVP + Favoritos Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fechar o MVP do app de receitas e adicionar favoritos com UX consistente, mantendo backup local e o fluxo principal estável.

**Architecture:** A implementação fica em cima da arquitetura atual MVVM + Room + Hilt, sem criar novas camadas. O foco é completar o que já existe no modelo de dados (`isFavorite`, `sourceUrl`, tags, settings) e melhorar a UX das telas principais com mudanças pequenas e testáveis.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Navigation Compose, Room, Hilt, DataStore, Coil.

---

### Task 1: Expor favoritos no fluxo de dados e nas telas principais

**Files:**
- Modify: `app/src/main/java/com/diaszano/pratoo/data/repository/RecipeRepository.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/data/repository/RecipeRepositoryImpl.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/app/src/main/java/com/diaszano/pratoo/data/local/dao/RecipeDao.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipelist/RecipeListScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipedetail/RecipeDetailScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipedetail/RecipeDetailViewModel.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipeedit/RecipeEditScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipeedit/RecipeEditViewModel.kt`

- [x] Adicionar ao DAO um update simples para `is_favorite` por `id`, sem alterar o restante da receita.
- [x] Expor essa operação no `RecipeRepository` e implementar no `RecipeRepositoryImpl`.
- [x] Incluir ação de favorito na lista e no detalhe (ícone de estrela no card/listagem, ícone no topo do detalhe)
- [x] Atualizar `RecipeEditUiState` para carregar e salvar `isFavorite`.
- [x] Adicionar o controle de favorito na tela de edição (FilterChip "Favorita").
- [x] Rodar `./gradlew :app:assembleDebug`.

### Task 2: Completar `sourceUrl` no formulário e no detalhe

**Files:**
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipeedit/RecipeEditScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipeedit/RecipeEditViewModel.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipedetail/RecipeDetailScreen.kt`

- [x] Adicionar campo de texto para `sourceUrl` na tela de edição ("URL da fonte").
- [x] Adicionar handler no `RecipeEditViewModel` para atualizar `sourceUrl` no estado.
- [x] Exibir a fonte no detalhe apenas quando houver valor (texto simples).
- [x] Rodar `./gradlew :app:assembleDebug`.

### Task 3: Permitir criar tag nova pela tela de edição

**Files:**
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipeedit/RecipeEditScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipeedit/RecipeEditViewModel.kt`

- [x] Adicionar na tela de edição: campo para nova tag + botão de adicionar.
- [x] Conectar o campo ao `newTagName` já existente no estado.
- [x] Após criar a tag, selecionar automaticamente a nova tag no estado atual.
- [x] Evitar criação de tags vazias ou só com espaço (guard no ViewModel).
- [x] Rodar `./gradlew :app:assembleDebug`.

### Task 4: Melhorar feedback e UX do fluxo principal

**Files:**
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipelist/RecipeListScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipeedit/RecipeEditScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipedetail/RecipeDetailScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-pt/strings.xml`

- [x] Levantar strings hardcoded mais visíveis das telas principais.
- [x] Mover essas strings para recursos (strings.xml en + pt).
- [x] Garantir que erro de título obrigatório continue claro e consistente.
- [x] Revisar rótulos, descrições e `contentDescription` dos ícones de ação.
- [x] Rodar `./gradlew :app:assembleDebug`.

### Task 5: Expandir settings com tema e preparar preferência de unidades

**Files:**
- Create: `app/src/main/java/com/diaszano/pratoo/data/settings/AppPreferences.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/settings/SettingsViewModel.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/MainActivity.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/theme/Color.kt`

- [x] Criar `AppPreferences` com DataStore para tema (Sistema/Claro/Escuro/Moonlight) e unidade (placeholder).
- [x] Fazer `MainActivity` observar a preferência de tema via `collectAsStateWithLifecycle`.
- [x] Expor controles na tela de settings (FilterChips para cada tema).
- [x] Manter backup/import atual intacto.
- [x] Adicionar tema Moonlight (paleta azul-escura inspirada em Tokyo Night).
- [x] Rodar `./gradlew :app:assembleDebug`.

### Task 6: Revisar robustez do backup e imagem local

**Files:**
- Modify: `app/src/main/java/com/diaszano/pratoo/data/repository/BackupManager.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/data/repository/BackupDto.kt`

- [x] Adicionar teste cobrindo export/import com receitas que têm e não têm imagem/URL/tags.
- [x] Garantir versionamento do JSON de backup continue explícito (`BACKUP_VERSION` constante).
- [x] Adicionar validação de versão no import (rejeitar versão incompatível).
- [x] Rodar `./gradlew :app:assembleDebug`.

### Task 7: Cobertura de testes para fluxos principais

**Files:**
- Create: `app/src/test/java/com/diaszano/pratoo/data/repository/FakeRecipeRepository.kt`
- Create: `app/src/test/java/com/diaszano/pratoo/data/repository/RecipeRepositoryTest.kt`
- Create: `app/src/test/java/com/diaszano/pratoo/data/repository/BackupManagerTest.kt`

- [x] Cobrir ao menos: salvar receita com campos mínimos, editar receita existente, alternar favorito, criar tag nova, excluir receita.
- [x] Rodar `./gradlew test` (25 testes passando).

### Task 8: Limpeza técnica e validação final

**Files:**
- Modify: `app/src/main/java/com/diaszano/pratoo/di/DatabaseModule.kt`
- Modify: `app/build.gradle.kts`

- [x] Ajustar o warning do `fallbackToDestructiveMigration()` → `fallbackToDestructiveMigration(false)`.
- [x] Rodar `./gradlew :app:assembleDebug`.
- [x] Rodar `./gradlew test`.
