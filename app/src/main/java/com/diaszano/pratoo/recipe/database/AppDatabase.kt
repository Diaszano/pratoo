package com.diaszano.pratoo.recipe.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.IngredientDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.MeasurementUnitDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.RecipeDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.StepDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.TagDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.IngredientEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.MeasurementUnitEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeTagCrossRef
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.StepEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.TagEntity

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        TagEntity::class,
        RecipeTagCrossRef::class,
        MeasurementUnitEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun stepDao(): StepDao
    abstract fun tagDao(): TagDao
    abstract fun measurementUnitDao(): MeasurementUnitDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `measurement_units` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `abbreviation` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `category` TEXT NOT NULL
                    )"""
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM measurement_units")
            }
        }
    }
}
