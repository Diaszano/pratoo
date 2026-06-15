package com.diaszano.pratoo.recipe.adapter.out.persistence.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.IngredientEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeTagCrossRef
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.StepEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.TagEntity

data class RecipeWithDetails(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id"
    )
    val ingredients: List<IngredientEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id"
    )
    val steps: List<StepEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RecipeTagCrossRef::class,
            parentColumn = "recipe_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<TagEntity>
)
