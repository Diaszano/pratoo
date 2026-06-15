package com.diaszano.pratoo.recipe.adapter.out.persistence.mapper

import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.IngredientEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.MeasurementCategoryEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.MeasurementUnitEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.StepEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.TagEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.relation.MeasurementUnitWithCategory
import com.diaszano.pratoo.recipe.adapter.out.persistence.relation.MeasurementUnitWithCategoryProjection
import com.diaszano.pratoo.recipe.adapter.out.persistence.relation.RecipeListProjection
import com.diaszano.pratoo.recipe.adapter.out.persistence.relation.RecipeWithDetails
import com.diaszano.pratoo.recipe.domain.model.Ingredient
import com.diaszano.pratoo.recipe.domain.model.MeasurementCategory
import com.diaszano.pratoo.recipe.domain.model.MeasurementUnit
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.model.RecipeListItem
import com.diaszano.pratoo.recipe.domain.model.RecipeStep
import com.diaszano.pratoo.recipe.domain.model.Tag

/** Maps between Room persistence entities/projections and domain models. */
object RecipeMapper {

    // ── Room → Domain ──────────────────────────────────────────────

    fun RecipeWithDetails.toDomain() = Recipe(
        id = recipe.id,
        title = recipe.title,
        notes = recipe.notes,
        imageUri = recipe.imageUri,
        servings = recipe.servings,
        prepTimeMinutes = recipe.prepTimeMinutes,
        cookTimeMinutes = recipe.cookTimeMinutes,
        sourceUrl = recipe.sourceUrl,
        isFavorite = recipe.isFavorite,
        createdAt = recipe.createdAt,
        updatedAt = recipe.updatedAt,
        ingredients = ingredients.map { it.toDomain() },
        steps = steps.map { it.toDomain() },
        tags = tags.map { it.toDomain() }
    )

    fun RecipeListProjection.toDomain() = RecipeListItem(
        id = id,
        title = title,
        imageUri = imageUri,
        isFavorite = isFavorite,
        updatedAt = updatedAt
    )

    fun IngredientEntity.toDomain() = Ingredient(
        id = id,
        name = name,
        quantity = quantity,
        unit = unit,
        position = position
    )

    fun StepEntity.toDomain() = RecipeStep(
        id = id,
        text = text,
        order = order
    )

    fun TagEntity.toDomain() = Tag(
        id = id,
        name = name
    )

    fun MeasurementCategoryEntity.toDomain() = MeasurementCategory(
        id = id,
        code = code,
        displayName = displayName,
        sortOrder = sortOrder
    )

    fun MeasurementUnitWithCategoryProjection.toDomain() = MeasurementUnit(
        id = id,
        abbreviation = abbreviation,
        displayName = displayName,
        category = MeasurementCategory(
            id = categoryId,
            code = categoryCode,
            displayName = categoryDisplayName,
            sortOrder = categorySortOrder
        )
    )

    fun MeasurementUnitWithCategory.toDomain() = MeasurementUnit(
        id = unit.id,
        abbreviation = unit.abbreviation,
        displayName = unit.displayName,
        category = category.toDomain()
    )

    // ── Domain → Room ──────────────────────────────────────────────

    fun Recipe.toEntity() = RecipeEntity(
        id = id,
        title = title,
        notes = notes,
        imageUri = imageUri,
        servings = servings,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        sourceUrl = sourceUrl,
        isFavorite = isFavorite,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun Ingredient.toEntity(recipeId: Long) = IngredientEntity(
        id = id,
        recipeId = recipeId,
        name = name,
        quantity = quantity,
        unit = unit,
        position = position
    )

    fun RecipeStep.toEntity(recipeId: Long) = StepEntity(
        id = id,
        recipeId = recipeId,
        text = text,
        order = order
    )

    fun Tag.toEntity() = TagEntity(
        id = id,
        name = name
    )
}
