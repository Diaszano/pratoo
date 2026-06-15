package com.diaszano.pratoo.recipe.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.IngredientDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.MeasurementCategoryDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.MeasurementUnitDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.RecipeDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.RecipeSectionDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.StepDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.TagDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.IngredientEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.MeasurementCategoryEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.MeasurementUnitEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeSectionEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeTagCrossRef
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.StepEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.TagEntity

/**
 * Room database for Pratoo.
 *
 * This is the baseline schema for v0.1.0. Do not reset migrations after
 * public release; add proper migrations instead.
 */
@Database(
    entities = [
        RecipeEntity::class,
        RecipeSectionEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        TagEntity::class,
        RecipeTagCrossRef::class,
        MeasurementCategoryEntity::class,
        MeasurementUnitEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    abstract fun recipeSectionDao(): RecipeSectionDao

    abstract fun ingredientDao(): IngredientDao

    abstract fun stepDao(): StepDao

    abstract fun tagDao(): TagDao

    abstract fun measurementUnitDao(): MeasurementUnitDao

    abstract fun measurementCategoryDao(): MeasurementCategoryDao
}
