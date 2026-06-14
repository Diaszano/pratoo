package com.diaszano.pratoo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diaszano.pratoo.data.local.dao.IngredientDao
import com.diaszano.pratoo.data.local.dao.RecipeDao
import com.diaszano.pratoo.data.local.dao.StepDao
import com.diaszano.pratoo.data.local.dao.TagDao
import com.diaszano.pratoo.data.local.entity.IngredientEntity
import com.diaszano.pratoo.data.local.entity.RecipeEntity
import com.diaszano.pratoo.data.local.entity.RecipeTagCrossRef
import com.diaszano.pratoo.data.local.entity.StepEntity
import com.diaszano.pratoo.data.local.entity.TagEntity

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        TagEntity::class,
        RecipeTagCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
abstract class PratooDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun stepDao(): StepDao
    abstract fun tagDao(): TagDao
}
