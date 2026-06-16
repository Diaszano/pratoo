package com.diaszano.pratoo.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diaszano.pratoo.recipe.adapter.out.backup.JsonRecipeBackupCodec
import com.diaszano.pratoo.recipe.adapter.out.persistence.RoomRecipeRepository
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.IngredientDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.MeasurementCategoryDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.MeasurementUnitDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.RecipeDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.RecipeSectionDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.StepDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.TagDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.MeasurementCategoryEntity
import com.diaszano.pratoo.recipe.database.AppDatabase
import com.diaszano.pratoo.recipe.domain.repository.RecipeBackupCodec
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    /**
     * Provides the singleton [AppDatabase] instance.
     *
     * Database callback seeds measurement categories and units on first creation.
     * Uses execSQL directly to avoid creating a second Room database instance.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room
            .databaseBuilder(context, AppDatabase::class.java, "pratoo.db")
            .addMigrations(MIGRATION_1_2)
            .addCallback(SeedCallback())
            .fallbackToDestructiveMigration(false)
            .build()

    private val MIGRATION_1_2 =
        object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN deleted_at INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recipes_deleted_at ON recipes(deleted_at)")
            }
        }

    /**
     * Database callback that seeds reference data (measurement categories and units)
     * on first creation or when the tables are empty.
     *
     * Uses execSQL for inserts to avoid opening a second Room connection.
     */
    private class SeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                seedCategories(db)
                seedUnits(db)
                updateCategoryDisplayNames(db)
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            // onOpen is called every time the database is opened.
            // Only seed if tables are empty (recovery after database deletion).
            CoroutineScope(Dispatchers.IO).launch {
                val categoryCount =
                    db
                        .query("SELECT COUNT(*) FROM measurement_categories")
                        .use {
                            it.moveToFirst()
                            it.getLong(0)
                        }
                if (categoryCount == 0L) {
                    seedCategories(db)
                    seedUnits(db)
                }
                updateCategoryDisplayNames(db)
            }
        }

        private fun seedCategories(db: SupportSQLiteDatabase) {
            val categories = defaultCategories()
            categories.forEach { category ->
                db.execSQL(
                    "INSERT OR IGNORE INTO measurement_categories (code, display_name, sort_order) VALUES (?, ?, ?)",
                    arrayOf<Any?>(category.code, category.displayName, category.sortOrder),
                )
            }
        }

        private fun seedUnits(db: SupportSQLiteDatabase) {
            val categories = defaultCategories().associateBy { it.code }
            val units = defaultMeasurementUnitsWithCode(categories.keys)
            units.forEach { (abbreviation, displayName, categoryCode) ->
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO measurement_units (abbreviation, displayName, category_id)
                    SELECT ?, ?, id FROM measurement_categories WHERE code = ?
                    """.trimIndent(),
                    arrayOf<Any?>(abbreviation, displayName, categoryCode),
                )
            }
        }

        private fun updateCategoryDisplayNames(db: SupportSQLiteDatabase) {
            defaultCategories().forEach { category ->
                db.execSQL(
                    "UPDATE measurement_categories SET display_name = ? WHERE code = ?",
                    arrayOf(category.displayName, category.code),
                )
            }
        }
    }

    internal fun defaultCategories(): List<MeasurementCategoryEntity> =
        listOf(
            MeasurementCategoryEntity(code = "weight", displayName = "Peso", sortOrder = 10),
            MeasurementCategoryEntity(code = "volume", displayName = "Volume", sortOrder = 20),
            MeasurementCategoryEntity(code = "kitchen", displayName = "Medidas culinárias", sortOrder = 30),
            MeasurementCategoryEntity(code = "count", displayName = "Quantidade", sortOrder = 40),
            MeasurementCategoryEntity(code = "portion", displayName = "Porções e cortes", sortOrder = 50),
            MeasurementCategoryEntity(code = "ingredient_unit", displayName = "Unidades de ingrediente", sortOrder = 60),
            MeasurementCategoryEntity(code = "package", displayName = "Embalagens", sortOrder = 70),
            MeasurementCategoryEntity(code = "other", displayName = "Outros", sortOrder = 80),
        )

    /**
     * Returns measurement units keyed by category code for SQL-based seeding.
     * Each entry is a triple of (abbreviation, displayName, categoryCode).
     */
    internal fun defaultMeasurementUnitsWithCode(categoryCodes: Set<String>): List<Triple<String, String, String>> {
        val all =
            listOf(
                Triple("kg", "Quilograma", "weight"),
                Triple("g", "Grama", "weight"),
                Triple("mg", "Miligrama", "weight"),
                Triple("lb", "Libra", "weight"),
                Triple("oz", "Onça", "weight"),
                Triple("L", "Litro", "volume"),
                Triple("ml", "Mililitro", "volume"),
                Triple("fl oz", "Onça fluida", "volume"),
                Triple("xíc", "Xícara", "kitchen"),
                Triple("1/2 xíc", "Meia xícara", "kitchen"),
                Triple("cs", "Colher de sopa", "kitchen"),
                Triple("ct", "Colher de chá", "kitchen"),
                Triple("cc", "Colher de café", "kitchen"),
                Triple("copo", "Copo", "kitchen"),
                Triple("copo amer.", "Copo americano", "kitchen"),
                Triple("un", "Unidade", "count"),
                Triple("dz", "Dúzia", "count"),
                Triple("par", "Par", "count"),
                Triple("fatia", "Fatia", "portion"),
                Triple("pedaço", "Pedaço", "portion"),
                Triple("porção", "Porção", "portion"),
                Triple("rodela", "Rodela", "portion"),
                Triple("cubo", "Cubo", "portion"),
                Triple("dente", "Dente", "ingredient_unit"),
                Triple("folha", "Folha", "ingredient_unit"),
                Triple("ramo", "Ramo", "ingredient_unit"),
                Triple("maço", "Maço", "ingredient_unit"),
                Triple("lata", "Lata", "package"),
                Triple("garrafa", "Garrafa", "package"),
                Triple("pacote", "Pacote", "package"),
                Triple("sachê", "Sachê", "package"),
                Triple("caixa", "Caixa", "package"),
                Triple("vidro", "Vidro", "package"),
                Triple("tablete", "Tablete", "package"),
                Triple("pitada", "Pitada", "other"),
                Triple("fio", "Fio", "other"),
                Triple("q.b.", "Quanto baste", "other"),
                Triple("a gosto", "A gosto", "other"),
            )
        return all.filter { it.third in categoryCodes }
    }

    @Provides fun provideRecipeDao(database: AppDatabase): RecipeDao = database.recipeDao()

    @Provides fun provideRecipeSectionDao(database: AppDatabase): RecipeSectionDao = database.recipeSectionDao()

    @Provides fun provideIngredientDao(database: AppDatabase): IngredientDao = database.ingredientDao()

    @Provides fun provideStepDao(database: AppDatabase): StepDao = database.stepDao()

    @Provides fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()

    @Provides fun provideMeasurementUnitDao(database: AppDatabase): MeasurementUnitDao = database.measurementUnitDao()

    @Provides fun provideMeasurementCategoryDao(database: AppDatabase): MeasurementCategoryDao = database.measurementCategoryDao()

    @Provides @Singleton
    fun provideRecipeRepository(impl: RoomRecipeRepository): RecipeRepository = impl

    @Provides @Singleton
    fun provideBackupCodec(impl: JsonRecipeBackupCodec): RecipeBackupCodec = impl
}
