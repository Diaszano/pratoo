# Database Schema — Pratoo

> Room (SQLite) schema documentation for the Pratoo app.
> Local database, no backend — all data lives on the device.

---

## Overview

| Property | Value |
|---|---|
| File | `pratoo.db` (SQLite via Room) |
| Current version | `1` |
| Schema exported | `app/schemas/` (KSP `room.schemaLocation`) |
| Migrations | None (baseline schema for v0.1.0) |
| Destructive fallback | Disabled (`false`) |
| Auto-seed | `MeasurementCategoryEntity` + `MeasurementUnitEntity` (via `onCreate` / `onOpen` callback) |

> **Note:** Previous pre-release migrations (v1→v2→v3→v4) were removed intentionally
> before the first public release (v0.1.0). The current schema version 1 is the
> consolidated baseline. Future schema changes after v0.1.0 must use proper migrations
> and must not reset the database.

---

## ER Diagram

```
┌──────────────────────────────────────┐
│             recipes                  │
│──────────────────────────────────────│
│ id            INTEGER (PK, auto)     │
│ title         TEXT NOT NULL          │
│ notes         TEXT (default '')      │
│ image_uri     TEXT (nullable)        │
│ servings      INTEGER (default 1)    │
│ prep_time_min INTEGER (default 0)    │
│ cook_time_min INTEGER (default 0)    │
│ source_url    TEXT (nullable)        │
│ is_favorite   INTEGER (default 0)    │
│ created_at    INTEGER (epoch ms)     │
│ updated_at    INTEGER (epoch ms)     │
└──────────────┬───────────────────────┘
               │ 1
               │
       ┌───────┼───────────┐
       │                   │
       │ N                 │ N
┌──────▼──────┐   ┌───────▼──────────────────┐
│ ingredients │   │ recipe_tag_cross_ref      │
│─────────────│   │───────────────────────────│
│ id     (PK) │   │ recipe_id (PK, FK→recipes)│
│ recipe_id   │   │ tag_id    (PK, FK→tags)   │
│  FK→recipes │   └───────────┬───────────────┘
│ name        │               │ N
│ quantity    │               │
│ unit        │        ┌──────▼──────┐
│ position    │        │    tags     │
└─────────────┘        │─────────────│
                       │ id    (PK)  │
┌──────────────┐       │ name (UNIQUE)│
│    steps     │       └─────────────┘
│──────────────│
│ id     (PK)  │       ┌───────────────────────────┐
│ recipe_id    │       │ measurement_categories    │
│  FK→recipes  │       │───────────────────────────│
│ text         │       │ id          (PK)          │
│ step_order   │       │ code        TEXT UNIQUE   │
└──────────────┘       │ display_name TEXT         │
                       │ sort_order  INTEGER       │
                       └───────────┬───────────────┘
                                   │ 1
                                   │
                        ┌──────────▼──────────┐
                        │ measurement_units   │
                        │──────────────────────│
                        │ id          (PK)     │
                        │ abbreviation (UNIQUE)│
                        │ displayName          │
                        │ category_id (FK)     │
                        └──────────────────────┘
```

### Cardinality

| Relation | Type |
|---|---|
| `recipes` → `ingredients` | 1:N (CASCADE delete) |
| `recipes` → `steps` | 1:N (CASCADE delete) |
| `recipes` ↔ `tags` | N:M (via `recipe_tag_cross_ref`, CASCADE delete both sides) |
| `measurement_categories` → `measurement_units` | 1:N (RESTRICT delete) |

---

## Tables

### 1. `recipes`

| Column | Type | Constraints | Default |
|---|---|---|---|
| `id` | `INTEGER` | `PRIMARY KEY AUTOINCREMENT` | `0` |
| `title` | `TEXT` | `NOT NULL` | — |
| `notes` | `TEXT` | — | `""` |
| `image_uri` | `TEXT` | nullable | `null` |
| `servings` | `INTEGER` | — | `1` |
| `prep_time_minutes` | `INTEGER` | — | `0` |
| `cook_time_minutes` | `INTEGER` | — | `0` |
| `source_url` | `TEXT` | nullable | `null` |
| `is_favorite` | `INTEGER` | mapped as 0/1 | `false` |
| `created_at` | `INTEGER` | epoch millis | `System.currentTimeMillis()` |
| `updated_at` | `INTEGER` | epoch millis | `System.currentTimeMillis()` |

### 2. `ingredients`

| Column | Type | Constraints | Default |
|---|---|---|---|
| `id` | `INTEGER` | `PRIMARY KEY AUTOINCREMENT` | `0` |
| `recipe_id` | `INTEGER` | `FK → recipes.id ON DELETE CASCADE` | — |
| `name` | `TEXT` | `NOT NULL` | — |
| `quantity` | `TEXT` | — | `""` |
| `unit` | `TEXT` | — | `""` |
| `position` | `INTEGER` | — | `0` |

**Indices:** `recipe_id`.

**Notes:**
- `quantity` stores strings like `"1/2"`, `"2-3"`.
- `unit` stores abbreviation strings (e.g. `"g"`, `"xíc"`, `"cs"`) — not a FK to `measurement_units` (design choice for flexibility).
- `position` controls display order.

### 3. `steps`

| Column | Type | Constraints | Default |
|---|---|---|---|
| `id` | `INTEGER` | `PRIMARY KEY AUTOINCREMENT` | `0` |
| `recipe_id` | `INTEGER` | `FK → recipes.id ON DELETE CASCADE` | — |
| `text` | `TEXT` | `NOT NULL` | — |
| `step_order` | `INTEGER` | — | `0` |

**Indices:** `recipe_id`.

### 4. `tags`

| Column | Type | Constraints |
|---|---|---|
| `id` | `INTEGER` | `PRIMARY KEY AUTOINCREMENT` |
| `name` | `TEXT` | `UNIQUE` |

**Indices:** `name` (UNIQUE).

### 5. `recipe_tag_cross_ref`

| Column | Type | Constraints |
|---|---|---|
| `recipe_id` | `INTEGER` | `PK`, `FK → recipes.id ON DELETE CASCADE` |
| `tag_id` | `INTEGER` | `PK`, `FK → tags.id ON DELETE CASCADE` |

**Indices:** `tag_id` (beyond the composite PK).

### 6. `measurement_categories`

| Column | Type | Constraints | Default |
|---|---|---|---|
| `id` | `INTEGER` | `PRIMARY KEY AUTOINCREMENT` | `0` |
| `code` | `TEXT` | `NOT NULL`, `UNIQUE` | — |
| `display_name` | `TEXT` | `NOT NULL` | — |
| `sort_order` | `INTEGER` | — | `0` |

**Indices:** `code` (UNIQUE), `sort_order`.

**Notes:**
- Lookup table for measurement unit categories.
- Seeded automatically with 8 default categories.
- User does not edit categories.

### 7. `measurement_units`

| Column | Type | Constraints | Default |
|---|---|---|---|
| `id` | `INTEGER` | `PRIMARY KEY AUTOINCREMENT` | `0` |
| `abbreviation` | `TEXT` | `NOT NULL`, `UNIQUE` | — |
| `displayName` | `TEXT` | `NOT NULL` | — |
| `category_id` | `INTEGER` | `FK → measurement_categories.id ON DELETE RESTRICT` | — |

**Indices:** `abbreviation` (UNIQUE), `category_id`, `(category_id, displayName)`.

**Notes:**
- Reference/lookup table — not a FK from `ingredients.unit` (design choice).
- Seeded automatically with 37 default units.

---

## Seed Data

File: `di/DatabaseModule.kt`

Seeding occurs in two steps:
1. **Categories** are inserted first (if table is empty).
2. **Units** are inserted after, using the category IDs fetched by code.

37 units seeded automatically on first run:

| Category | Units |
|---|---|
| **weight** | kg, g, mg, lb, oz |
| **volume** | L, ml, fl oz |
| **kitchen** | xíc, 1/2 xíc, cs (sopa), ct (chá), cc (café), copo, copo amer. |
| **count** | un, dz (dúzia), par |
| **portion** | fatia, pedaço, porção, rodela, cubo |
| **ingredient_unit** | dente, folha, ramo, maço |
| **package** | lata, garrafa, pacote, sachê, caixa, vidro, tablete |
| **other** | pitada, fio, q.b. (quanto baste), a gosto |

---

## Architecture

```
app/src/main/java/com/diaszano/pratoo/
├── recipe/
│   ├── adapter/out/persistence/
│   │   ├── entity/       # Room entities
│   │   ├── relation/     # Room relations and projections
│   │   ├── dao/          # Room DAOs
│   │   ├── mapper/       # Entity ↔ Domain mappers
│   │   └── RoomRecipeRepository.kt
│   ├── database/
│   │   └── AppDatabase.kt
│   ├── domain/
│   │   ├── model/        # Pure domain models
│   │   ├── repository/   # Repository interfaces
│   │   └── validation/   # Recipe validation
│   └── application/usecase/  # Use cases
└── di/
    └── DatabaseModule.kt
```

**Design rules:**
- Room entities stay in the persistence adapter.
- Domain models stay pure (no Room annotations).
- ViewModels use use cases or domain repositories, not DAOs directly.
- `ingredients.unit` remains a String — not a FK to `measurement_units`.
