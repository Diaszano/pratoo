package com.diaszano.pratoo.recipe.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.IngredientDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.MeasurementCategoryDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.MeasurementUnitDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.RecipeDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.StepDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.TagDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.IngredientEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.MeasurementCategoryEntity
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
        MeasurementCategoryEntity::class,
        MeasurementUnitEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun stepDao(): StepDao
    abstract fun tagDao(): TagDao
    abstract fun measurementUnitDao(): MeasurementUnitDao
    abstract fun measurementCategoryDao(): MeasurementCategoryDao

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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create measurement_categories
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS measurement_categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        code TEXT NOT NULL,
                        display_name TEXT NOT NULL,
                        sort_order INTEGER NOT NULL DEFAULT 0
                    )
                """)
                db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_measurement_categories_code
                    ON measurement_categories(code)
                """)
                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_measurement_categories_sort_order
                    ON measurement_categories(sort_order)
                """)

                // 2. Insert default categories
                db.execSQL("""
                    INSERT OR IGNORE INTO measurement_categories(code, display_name, sort_order)
                    VALUES
                    ('weight', 'Weight', 10),
                    ('volume', 'Volume', 20),
                    ('kitchen', 'Kitchen measurements', 30),
                    ('count', 'Quantity', 40),
                    ('portion', 'Portions and cuts', 50),
                    ('ingredient_unit', 'Ingredient units', 60),
                    ('package', 'Packages', 70),
                    ('other', 'Other', 80)
                """)

                // 3. Create measurement_units_new with category_id
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS measurement_units_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        abbreviation TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        category_id INTEGER NOT NULL,
                        FOREIGN KEY(category_id) REFERENCES measurement_categories(id) ON DELETE RESTRICT
                    )
                """)

                // 4. Copy old data, mapping category string to category_id
                db.execSQL("""
                    INSERT INTO measurement_units_new(id, abbreviation, displayName, category_id)
                    SELECT
                        mu.id,
                        TRIM(mu.abbreviation),
                        TRIM(mu.displayName),
                        COALESCE(
                            (SELECT mc.id FROM measurement_categories mc WHERE mc.code = TRIM(LOWER(mu.category)) LIMIT 1),
                            (SELECT mc.id FROM measurement_categories mc WHERE mc.code = 'other' LIMIT 1)
                        ) AS category_id
                    FROM measurement_units mu
                """)

                // 5. Remove duplicate abbreviations (keep lowest id)
                db.execSQL("""
                    DELETE FROM measurement_units_new
                    WHERE id NOT IN (
                        SELECT MIN(id)
                        FROM measurement_units_new
                        GROUP BY LOWER(TRIM(abbreviation))
                    )
                """)

                // 6. Drop old table and rename new one
                db.execSQL("DROP TABLE measurement_units")
                db.execSQL("ALTER TABLE measurement_units_new RENAME TO measurement_units")

                // 7. Create indexes on the new measurement_units table
                db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_measurement_units_abbreviation
                    ON measurement_units(abbreviation)
                """)
                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_measurement_units_category_id
                    ON measurement_units(category_id)
                """)
                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_measurement_units_category_id_displayName
                    ON measurement_units(category_id, displayName)
                """)
            }
        }
    }
}
