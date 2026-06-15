# Contribuindo com o Pratoo

Obrigado pelo interesse em contribuir! Aqui estão as diretrizes para manter o projeto organizado e profissional.

## Fluxo de trabalho

1. Crie um fork ou branch a partir de `main`
2. Faça suas alterações em commits atômicos
3. Abra um Pull Request para `main`
4. Aguarde a revisão e CI passar

## Convenção de branches

| Prefixo | Uso |
|---------|-----|
| `feature/` | Nova funcionalidade |
| `fix/` | Correção de bug |
| `refactor/` | Refatoração sem mudança de comportamento |
| `docs/` | Documentação |
| `chore/` | Tarefas de manutenção (dependências, build, CI) |

## Estilo de commit

Usamos [commits semânticos](https://www.conventionalcommits.org/):

```
tipo(escopo): mensagem em inglês

Corpo opcional com mais detalhes.
```

**Tipos:** `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `style`, `perf`, `build`, `ci`

**Exemplos:**
- `feat(recipes): add search by ingredient`
- `fix(backup): handle null recipe count in restore dialog`
- `refactor(settings): extract theme selection to separate composable`

## Qualidade de código

Antes de abrir um PR, execute:

```bash
# Formatar código Kotlin
./gradlew ktlintFormat

# Verificar estilo e lint
./gradlew ktlintCheck :app:lintDebug

# Rodar testes
./gradlew test

# Build completo
./gradlew :app:assembleDebug
```

O CI roda os mesmos checks automaticamente. PRs só são mergeados se todos passarem.

## Convenções do projeto

- **Código, comentários e KDoc**: inglês
- **Strings de UI**: português (BR); chaves dos recursos em inglês
- **Arquitetura**: DDD/hexagonal (domain → application → adapter)
- **Injeção de dependência**: Hilt (`@Inject`, `@Singleton`, `@HiltViewModel`)
- **Testes unitários**: JUnit 4 + kotlinx-coroutines-test + Turbine

## Dúvidas?

Abra uma [Discussion](https://github.com/Diaszano/pratoo/discussions) ou issue.
