# Pratoo

> App Android de receitas pessoais — salve, organize e compartilhe suas receitas favoritas.

![CI](https://github.com/Diaszano/pratoo/actions/workflows/build.yml/badge.svg)
![License](https://img.shields.io/github/license/Diaszano/pratoo)
![Kotlin](https://img.shields.io/badge/kotlin-2.2.10-purple)
![Android SDK](https://img.shields.io/badge/minSDK-29-brightgreen)

## Funcionalidades

- **Criar e editar receitas** com foto, ingredientes, modo de preparo, anotações e URL da fonte
- **Favoritos** — marque suas receitas preferidas com estrela
- **Busca e filtros** — busque por título ou ingrediente, filtre por tags
- **Tags** — crie, selecione e remova tags para organizar suas receitas
- **Unidades de medida** — 37 unidades pré-cadastradas (g, kg, xícara, colher de sopa, etc.)
- **Reordenação de passos** — mova os passos do modo de preparo para cima e para baixo
- **Modo preparo** — modo passo-a-passo com checklist de ingredientes
- **Compartilhar** — envie receitas formatadas direto para WhatsApp ou outros apps
- **Lixeira** — receitas excluídas podem ser restauradas ou removidas definitivamente; itens expiram após 30 dias
- **Backup manual** — exporte e importe todas as receitas em JSON
- **Backup Google Drive** — backup automático diário e backup ao criar novas receitas, usando o escopo `drive.appdata` 🔒
- **Restauração seletiva** — escolha qual backup do Drive restaurar
- **4 temas** — Sistema, Claro, Escuro e Moonlight (paleta azul-escura inspirada em Tokyo Night)

## Stack

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Kotlin 2.2 |
| UI | Jetpack Compose (Material 3) |
| Arquitetura | DDD / Hexagonal |
| Banco de dados | Room |
| DI | Hilt |
| Navegação | Navigation Compose (tipada) |
| Preferências | DataStore |
| Cloud backup | Google Drive API (`drive.appdata`) |
| Backup automático | WorkManager |
| Imagens | Coil |
| Serialização | kotlinx-serialization |
| HTTP | OkHttp |
| Formatação | ktlint |

## Como rodar

1. Clone o repositório
2. Abra no Android Studio
3. Sincronize o Gradle
4. Execute no emulador ou dispositivo (minSdk 29 / Android 10+)

```bash
./gradlew :app:assembleDebug
```

## Qualidade de código

```bash
# Formatar código Kotlin
./gradlew ktlintFormat

# Verificar estilo
./gradlew ktlintCheck

# Verificar lint Android
./gradlew :app:lintDebug

# Rodar testes
./gradlew test
```

O repositório usa **pre-commit hooks** para rodar essas verificações automaticamente antes de cada commit. Configure com:

```bash
pip install pre-commit
pre-commit install
```

## Estrutura do projeto

```
app/src/main/java/com/diaszano/pratoo/
├── backup/            # Backup Google Drive (domain/application/adapter/UI)
│   ├── domain/        # Modelos e portas
│   ├── application/   # Casos de uso
│   └── adapter/       # Cloud storage, worker, settings, UI
├── data/
│   └── settings/      # Preferências do app
├── di/                # Módulos Hilt
├── recipe/
│   ├── adapter/       # Persistência Room e codec de backup
│   ├── application/   # Casos de uso de receitas
│   ├── database/      # AppDatabase Room
│   └── domain/        # Modelos, portas e validação
├── ui/
│   ├── cooking/       # Modo preparo
│   ├── navigation/    # Rotas tipadas
│   ├── recipeedit/    # Tela de criação/edição
│   ├── recipedetail/  # Tela de detalhe
│   ├── recipelist/    # Tela inicial (grid)
│   ├── settings/      # Configurações
│   ├── shared/        # Componentes reutilizáveis
│   ├── trash/         # Lixeira de receitas
│   └── theme/         # Temas e cores
```

## Banco de dados

O app usa Room com migrations explícitas e exportação de schemas em `app/schemas/`.
Consulte [docs/database-schema.md](docs/database-schema.md) para a documentação do schema atual.

## Releases

As releases são geradas automaticamente via GitHub Actions. Basta criar uma tag semântica:

```bash
git tag v0.2.0
git push origin v0.2.0
```

O CI vai:
1. Buildar o APK e AAB de release
2. Assinar (se as secrets estiverem configuradas)
3. Criar uma GitHub Release com os artefatos

## Como contribuir

Veja o [CONTRIBUTING.md](CONTRIBUTING.md) para diretrizes detalhadas.

## Licença

MIT © [Diaszano](https://github.com/Diaszano)
