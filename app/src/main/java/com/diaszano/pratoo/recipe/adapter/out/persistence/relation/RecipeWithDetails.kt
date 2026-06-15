package com.diaszano.pratoo.recipe.adapter.out.persistence.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeSectionEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeTagCrossRef
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.TagEntity

data class RecipeWithDetails(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        entity = RecipeSectionEntity::class,
        parentColumn = "id",
        entityColumn = "recipe_id"
    )
    val sections: List<RecipeSectionWithDetails>,
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
