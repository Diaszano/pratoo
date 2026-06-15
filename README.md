# Pratoo

App Android de receitas pessoais — salve, organize e compartilhe suas receitas favoritas.

## Funcionalidades

- **Criar e editar receitas** com foto, ingredientes, modo de preparo, anotações e URL da fonte
- **Favoritos** — marque suas receitas preferidas com estrela
- **Busca e filtros** — busque por título ou ingrediente, filtre por tags
- **Tags** — crie e gerencie tags para organizar suas receitas
- **Unidades de medida** — 23 unidades pré-cadastradas (g, kg, xícara, colher de sopa, etc.)
- **Reordenação de passos** — mova os passos do modo de preparo para cima e para baixo
- **Compartilhar** — envie receitas formatadas direto para WhatsApp ou outros apps
- **Backup** — exporte e importe todas as receitas em JSON
- **4 temas** — Sistema, Claro, Escuro e Moonlight (paleta azul-escura inspirada em Tokyo Night)

## Stack

- Kotlin
- Jetpack Compose (Material 3)
- Room (banco de dados local)
- Hilt (dependency injection)
- Navigation Compose (navegação tipada)
- DataStore (preferências)
- Coil (carregamento de imagens)

## Como rodar

1. Clone o repositório
2. Abra no Android Studio
3. Sincronize o Gradle
4. Execute no emulador ou dispositivo (minSdk 29 / Android 10+)

```bash
./gradlew :app:assembleDebug
```

## Qualidade de codigo

Formatter e lint de estilo Kotlin usam `ktlint`, seguindo o estilo oficial do Kotlin. Para checagens Android, continue usando o `lint` do AGP.

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck
./gradlew lint
```

## Estrutura do projeto

```
app/src/main/java/com/diaszano/pratoo/
├── data/
│   ├── local/
│   │   ├── dao/          # DAOs do Room
│   │   ├── entity/       # Entidades do banco
│   │   └── relation/     # Relações e projeções
│   ├── repository/       # Repository pattern
│   └── settings/         # Preferências do app
├── di/                   # Módulos Hilt
├── ui/
│   ├── recipeedit/       # Tela de criação/edição
│   ├── recipedetail/     # Tela de detalhe
│   ├── recipelist/       # Tela inicial (grid)
│   ├── settings/         # Configurações
│   ├── shared/           # Componentes reutilizáveis
│   └── theme/            # Temas e cores
└── navigation/           # Rotas tipadas
```

## Licença

MIT
