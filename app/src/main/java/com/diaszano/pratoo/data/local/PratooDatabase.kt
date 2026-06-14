package com.diaszano.pratoo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diaszano.pratoo.data.local.dao.IngredientDao
import com.diaszano.pratoo.data.local.dao.MeasurementUnitDao
import com.diaszano.pratoo.data.local.dao.RecipeDao
import com.diaszano.pratoo.data.local.dao.StepDao
import com.diaszano.pratoo.data.local.dao.TagDao
import com.diaszano.pratoo.data.local.entity.IngredientEntity
import com.diaszano.pratoo.data.local.entity.MeasurementUnit
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
        RecipeTagCrossRef::class,
        MeasurementUnit::class
    ],
    version = 3,
    exportSchema = true
)
abstract class PratooDatabase : RoomDatabase() {
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
