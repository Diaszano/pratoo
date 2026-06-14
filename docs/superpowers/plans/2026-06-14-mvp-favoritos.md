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

- [ ] Adicionar um teste de repository/DAO cobrindo alternância de favorito em uma receita existente.
- [ ] Rodar o teste novo isolado e confirmar que falha porque ainda não existe operação de toggle/update dedicada.
- [ ] Adicionar ao DAO um update simples para `is_favorite` por `id`, sem alterar o restante da receita.
- [ ] Expor essa operação no `RecipeRepository` e implementar no `RecipeRepositoryImpl`.
- [ ] Incluir ação de favorito na lista e no detalhe:
  - ícone de estrela no card/listagem
  - ícone no topo ou FAB secundário no detalhe
- [ ] Atualizar `RecipeEditUiState` para carregar e salvar `isFavorite`.
- [ ] Adicionar o controle de favorito na tela de edição.
- [ ] Rodar `./gradlew :app:assembleDebug`.
- [ ] Validar manualmente:
  - marcar/desmarcar favorito na lista
  - marcar/desmarcar favorito no detalhe
  - criar/editar receita favorita
  - confirmar atualização visual imediata

### Task 2: Completar `sourceUrl` no formulário e no detalhe

**Files:**
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipeedit/RecipeEditScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipeedit/RecipeEditViewModel.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipedetail/RecipeDetailScreen.kt`

- [ ] Adicionar teste de ViewModel cobrindo salvar receita com `sourceUrl` preenchida e vazia.
- [ ] Rodar o teste e confirmar falha ou cobertura ausente.
- [ ] Adicionar campo de texto para `sourceUrl` na tela de edição.
- [ ] Adicionar handler no `RecipeEditViewModel` para atualizar `sourceUrl` no estado.
- [ ] Exibir a fonte no detalhe apenas quando houver valor.
- [ ] Se fizer sentido ao UX atual, tornar o link clicável com `Intent.ACTION_VIEW`; caso contrário, exibir como texto nesta etapa.
- [ ] Rodar `./gradlew :app:assembleDebug`.
- [ ] Validar manualmente criação, edição e leitura de URL.

### Task 3: Permitir criar tag nova pela tela de edição

**Files:**
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipeedit/RecipeEditScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipeedit/RecipeEditViewModel.kt`

- [ ] Adicionar teste do `RecipeEditViewModel` cobrindo `onAddNewTag()` com nome válido e inválido.
- [ ] Rodar o teste e confirmar a falha esperada antes do ajuste de UI.
- [ ] Adicionar na tela de edição:
  - campo para nova tag
  - botão de adicionar
- [ ] Conectar o campo ao `newTagName` já existente no estado.
- [ ] Após criar a tag, selecionar automaticamente a nova tag no estado atual.
- [ ] Evitar criação de tags vazias ou só com espaço.
- [ ] Rodar `./gradlew :app:assembleDebug`.
- [ ] Validar manualmente: criar tag nova, reaproveitar tag existente e selecionar/desselecionar.

### Task 4: Melhorar feedback e UX do fluxo principal

**Files:**
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipelist/RecipeListScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipeedit/RecipeEditScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipeedit/RecipeEditViewModel.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/recipedetail/RecipeDetailScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-pt/strings.xml`

- [ ] Levantar strings hardcoded mais visíveis das telas principais.
- [ ] Mover essas strings para recursos.
- [ ] Adicionar feedback visual de carregamento/salvamento onde hoje o estado é silencioso.
- [ ] Garantir que erro de título obrigatório continue claro e consistente.
- [ ] Revisar rótulos, descrições e `contentDescription` dos ícones de ação.
- [ ] Rodar `./gradlew :app:assembleDebug`.
- [ ] Validar manualmente fluxo de criar, editar, apagar, exportar e importar.

### Task 5: Expandir settings com tema e preparar preferência de unidades

**Files:**
- Modify: `app/src/main/java/com/diaszano/pratoo/di/DataStoreModule.kt`
- Create: `app/src/main/java/com/diaszano/pratoo/data/settings/AppPreferences.kt`
- Create: `app/src/main/java/com/diaszano/pratoo/data/settings/AppSettingsRepository.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/settings/SettingsViewModel.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/MainActivity.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/theme/Theme.kt`

- [ ] Adicionar teste de repository/preferências cobrindo leitura/escrita do tema.
- [ ] Rodar o teste e confirmar falha inicial.
- [ ] Criar estrutura mínima de preferências com:
  - tema: sistema/claro/escuro
  - unidade preferida: métrico/imperial ou placeholder persistido, sem recalcular receitas ainda
- [ ] Fazer `MainActivity` observar a preferência de tema.
- [ ] Expor controles na tela de settings.
- [ ] Manter backup/import atual intacto.
- [ ] Rodar `./gradlew :app:assembleDebug`.
- [ ] Validar manualmente troca de tema e persistência após reiniciar o app.

### Task 6: Revisar robustez do backup e imagem local

**Files:**
- Modify: `app/src/main/java/com/diaszano/pratoo/data/repository/BackupManager.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/data/repository/BackupDto.kt`
- Modify: `app/src/main/java/com/diaszano/pratoo/ui/settings/SettingsViewModel.kt`

- [ ] Adicionar teste cobrindo export/import com receitas que têm e não têm imagem/URL/tags.
- [ ] Verificar se a estratégia atual de `imageUri` faz sentido após restore em outro dispositivo.
- [ ] Se a URI não for portátil, documentar a limitação no app e no código nesta etapa em vez de inventar sincronização de mídia.
- [ ] Garantir versionamento do JSON de backup continue explícito.
- [ ] Rodar `./gradlew :app:assembleDebug`.
- [ ] Validar export/import com arquivo real.

### Task 7: Cobertura de testes para fluxos principais

**Files:**
- Create: `app/src/test/java/com/diaszano/pratoo/ui/recipeedit/RecipeEditViewModelTest.kt`
- Create: `app/src/test/java/com/diaszano/pratoo/ui/recipedetail/RecipeDetailViewModelTest.kt`
- Create: `app/src/test/java/com/diaszano/pratoo/data/repository/RecipeRepositoryImplTest.kt`

- [ ] Cobrir ao menos:
  - salvar receita com campos mínimos
  - editar receita existente
  - alternar favorito
  - criar tag nova
  - excluir receita
- [ ] Rodar `./gradlew test`.
- [ ] Corrigir eventuais falhas reais encontradas pelos testes.

### Task 8: Limpeza técnica e validação final

**Files:**
- Modify: `app/src/main/java/com/diaszano/pratoo/di/DatabaseModule.kt`
- Modify: `app/build.gradle.kts`
- Modify: arquivos tocados nas tasks anteriores conforme necessário

- [ ] Revisar warnings deprecatos mais próximos do código tocado neste plano.
- [ ] Ajustar o warning do `fallbackToDestructiveMigration()` para a API nova do Room.
- [ ] Decidir se o warning de `FlowPreview` merece `@OptIn` explícito ou refactor pequeno.
- [ ] Rodar `./gradlew :app:assembleDebug`.
- [ ] Rodar `./gradlew test`.
- [ ] Fazer checagem manual final do fluxo:
  - criar receita
  - editar receita
  - favoritar/desfavoritar
  - buscar/filtrar por tag
  - exportar/importar backup
  - mudar tema
