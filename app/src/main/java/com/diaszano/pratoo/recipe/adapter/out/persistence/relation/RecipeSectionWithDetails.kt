package com.diaszano.pratoo.recipe.adapter.out.persistence.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.IngredientEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeSectionEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.StepEntity

data class RecipeSectionWithDetails(
    @Embedded val section: RecipeSectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "section_id",
    )
    val ingredients: List<IngredientEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "section_id",
    )
    val steps: List<StepEntity>,
)
