package com.diaszano.pratoo.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diaszano.pratoo.recipe.adapter.out.persistence.RoomRecipeRepository
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.IngredientDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.MeasurementCategoryDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.MeasurementUnitDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.RecipeDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.RecipeSectionDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.StepDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.TagDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.MeasurementCategoryEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.MeasurementUnitEntity
import com.diaszano.pratoo.recipe.database.AppDatabase
import com.diaszano.pratoo.recipe.domain.repository.RecipeBackupCodec
import com.diaszano.pratoo.recipe.domain.repository.RecipeRepository
import com.diaszano.pratoo.recipe.adapter.out.backup.JsonRecipeBackupCodec
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "pratoo.db")
                    .addCallback(
                            object : RoomDatabase.Callback() {
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val database =
                                                Room.databaseBuilder(
                                                                context,
                                                                AppDatabase::class.java,
                                                                "pratoo.db"
                                                        )
                                                        .build()
                                        seedDatabase(database)
                                    }
                                }

                                override fun onOpen(db: SupportSQLiteDatabase) {
                                    super.onOpen(db)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val database =
                                                Room.databaseBuilder(
                                                                context,
                                                                AppDatabase::class.java,
                                                                "pratoo.db"
                                                        )
                                                        .build()
                                        seedDatabase(database)
                                    }
                                }
                            }
                    )
                    .fallbackToDestructiveMigration(false)
                    .build()

    private suspend fun seedDatabase(database: AppDatabase) {
        val categoryDao = database.measurementCategoryDao()
        val unitDao = database.measurementUnitDao()

        // Seed categories if empty
        if (categoryDao.count() == 0) {
            categoryDao.insertAll(defaultCategories())
        }

        // Seed units if empty
        if (unitDao.count() == 0) {
            val categories = categoryDao.getAll().associateBy { it.code }
            unitDao.insertAll(defaultMeasurementUnits(categories))
        }
    }

    private fun defaultCategories() = listOf(
        MeasurementCategoryEntity(code = "weight", displayName = "Weight", sortOrder = 10),
        MeasurementCategoryEntity(code = "volume", displayName = "Volume", sortOrder = 20),
        MeasurementCategoryEntity(code = "kitchen", displayName = "Kitchen measurements", sortOrder = 30),
        MeasurementCategoryEntity(code = "count", displayName = "Quantity", sortOrder = 40),
        MeasurementCategoryEntity(code = "portion", displayName = "Portions and cuts", sortOrder = 50),
        MeasurementCategoryEntity(code = "ingredient_unit", displayName = "Ingredient units", sortOrder = 60),
        MeasurementCategoryEntity(code = "package", displayName = "Packages", sortOrder = 70),
        MeasurementCategoryEntity(code = "other", displayName = "Other", sortOrder = 80)
    )

    private fun defaultMeasurementUnits(
        categories: Map<String, MeasurementCategoryEntity>
    ) = listOf(
        MeasurementUnitEntity(abbreviation = "kg", displayName = "Quilograma", categoryId = categories.getValue("weight").id),
        MeasurementUnitEntity(abbreviation = "g", displayName = "Grama", categoryId = categories.getValue("weight").id),
        MeasurementUnitEntity(abbreviation = "mg", displayName = "Miligrama", categoryId = categories.getValue("weight").id),
        MeasurementUnitEntity(abbreviation = "lb", displayName = "Libra", categoryId = categories.getValue("weight").id),
        MeasurementUnitEntity(abbreviation = "oz", displayName = "Onça", categoryId = categories.getValue("weight").id),
        MeasurementUnitEntity(abbreviation = "L", displayName = "Litro", categoryId = categories.getValue("volume").id),
        MeasurementUnitEntity(abbreviation = "ml", displayName = "Mililitro", categoryId = categories.getValue("volume").id),
        MeasurementUnitEntity(abbreviation = "fl oz", displayName = "Onça fluida", categoryId = categories.getValue("volume").id),
        MeasurementUnitEntity(abbreviation = "xíc", displayName = "Xícara", categoryId = categories.getValue("kitchen").id),
        MeasurementUnitEntity(abbreviation = "1/2 xíc", displayName = "Meia xícara", categoryId = categories.getValue("kitchen").id),
        MeasurementUnitEntity(abbreviation = "cs", displayName = "Colher de sopa", categoryId = categories.getValue("kitchen").id),
        MeasurementUnitEntity(abbreviation = "ct", displayName = "Colher de chá", categoryId = categories.getValue("kitchen").id),
        MeasurementUnitEntity(abbreviation = "cc", displayName = "Colher de café", categoryId = categories.getValue("kitchen").id),
        MeasurementUnitEntity(abbreviation = "copo", displayName = "Copo", categoryId = categories.getValue("kitchen").id),
        MeasurementUnitEntity(abbreviation = "copo amer.", displayName = "Copo americano", categoryId = categories.getValue("kitchen").id),
        MeasurementUnitEntity(abbreviation = "un", displayName = "Unidade", categoryId = categories.getValue("count").id),
        MeasurementUnitEntity(abbreviation = "dz", displayName = "Dúzia", categoryId = categories.getValue("count").id),
        MeasurementUnitEntity(abbreviation = "par", displayName = "Par", categoryId = categories.getValue("count").id),
        MeasurementUnitEntity(abbreviation = "fatia", displayName = "Fatia", categoryId = categories.getValue("portion").id),
        MeasurementUnitEntity(abbreviation = "pedaço", displayName = "Pedaço", categoryId = categories.getValue("portion").id),
        MeasurementUnitEntity(abbreviation = "porção", displayName = "Porção", categoryId = categories.getValue("portion").id),
        MeasurementUnitEntity(abbreviation = "rodela", displayName = "Rodela", categoryId = categories.getValue("portion").id),
        MeasurementUnitEntity(abbreviation = "cubo", displayName = "Cubo", categoryId = categories.getValue("portion").id),
        MeasurementUnitEntity(abbreviation = "dente", displayName = "Dente", categoryId = categories.getValue("ingredient_unit").id),
        MeasurementUnitEntity(abbreviation = "folha", displayName = "Folha", categoryId = categories.getValue("ingredient_unit").id),
        MeasurementUnitEntity(abbreviation = "ramo", displayName = "Ramo", categoryId = categories.getValue("ingredient_unit").id),
        MeasurementUnitEntity(abbreviation = "maço", displayName = "Maço", categoryId = categories.getValue("ingredient_unit").id),
        MeasurementUnitEntity(abbreviation = "lata", displayName = "Lata", categoryId = categories.getValue("package").id),
        MeasurementUnitEntity(abbreviation = "garrafa", displayName = "Garrafa", categoryId = categories.getValue("package").id),
        MeasurementUnitEntity(abbreviation = "pacote", displayName = "Pacote", categoryId = categories.getValue("package").id),
        MeasurementUnitEntity(abbreviation = "sachê", displayName = "Sachê", categoryId = categories.getValue("package").id),
        MeasurementUnitEntity(abbreviation = "caixa", displayName = "Caixa", categoryId = categories.getValue("package").id),
        MeasurementUnitEntity(abbreviation = "vidro", displayName = "Vidro", categoryId = categories.getValue("package").id),
        MeasurementUnitEntity(abbreviation = "tablete", displayName = "Tablete", categoryId = categories.getValue("package").id),
        MeasurementUnitEntity(abbreviation = "pitada", displayName = "Pitada", categoryId = categories.getValue("other").id),
        MeasurementUnitEntity(abbreviation = "fio", displayName = "Fio", categoryId = categories.getValue("other").id),
        MeasurementUnitEntity(abbreviation = "q.b.", displayName = "Quanto baste", categoryId = categories.getValue("other").id),
        MeasurementUnitEntity(abbreviation = "a gosto", displayName = "A gosto", categoryId = categories.getValue("other").id)
    )

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
