package com.diaszano.pratoo.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diaszano.pratoo.recipe.adapter.out.persistence.RoomRecipeRepository
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.IngredientDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.MeasurementUnitDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.RecipeDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.StepDao
import com.diaszano.pratoo.recipe.adapter.out.persistence.dao.TagDao
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
                    .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
                    .addCallback(
                            object : RoomDatabase.Callback() {
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val dao =
                                                Room.databaseBuilder(
                                                                context,
                                                                AppDatabase::class.java,
                                                                "pratoo.db"
                                                        )
                                                        .build()
                                                        .measurementUnitDao()
                                        if (dao.count() == 0) {
                                            dao.insertAll(defaultMeasurementUnits())
                                        }
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
                                        val dao = database.measurementUnitDao()
                                        if (dao.count() == 0) {
                                            dao.insertAll(defaultMeasurementUnits())
                                        }
                                    }
                                }
                            }
                    )
                    .fallbackToDestructiveMigration(false)
                    .build()

    private fun defaultMeasurementUnits() =
            listOf(
                    MeasurementUnitEntity(abbreviation = "kg", displayName = "Quilograma", category = "weight"),
                    MeasurementUnitEntity(abbreviation = "g", displayName = "Grama", category = "weight"),
                    MeasurementUnitEntity(abbreviation = "mg", displayName = "Miligrama", category = "weight"),
                    MeasurementUnitEntity(abbreviation = "lb", displayName = "Libra", category = "weight"),
                    MeasurementUnitEntity(abbreviation = "oz", displayName = "Onça", category = "weight"),
                    MeasurementUnitEntity(abbreviation = "L", displayName = "Litro", category = "volume"),
                    MeasurementUnitEntity(abbreviation = "ml", displayName = "Mililitro", category = "volume"),
                    MeasurementUnitEntity(abbreviation = "fl oz", displayName = "Onça fluida", category = "volume"),
                    MeasurementUnitEntity(abbreviation = "xíc", displayName = "Xícara", category = "kitchen"),
                    MeasurementUnitEntity(abbreviation = "1/2 xíc", displayName = "Meia xícara", category = "kitchen"),
                    MeasurementUnitEntity(abbreviation = "cs", displayName = "Colher de sopa", category = "kitchen"),
                    MeasurementUnitEntity(abbreviation = "ct", displayName = "Colher de chá", category = "kitchen"),
                    MeasurementUnitEntity(abbreviation = "cc", displayName = "Colher de café", category = "kitchen"),
                    MeasurementUnitEntity(abbreviation = "copo", displayName = "Copo", category = "kitchen"),
                    MeasurementUnitEntity(abbreviation = "copo amer.", displayName = "Copo americano", category = "kitchen"),
                    MeasurementUnitEntity(abbreviation = "un", displayName = "Unidade", category = "count"),
                    MeasurementUnitEntity(abbreviation = "dz", displayName = "Dúzia", category = "count"),
                    MeasurementUnitEntity(abbreviation = "par", displayName = "Par", category = "count"),
                    MeasurementUnitEntity(abbreviation = "fatia", displayName = "Fatia", category = "portion"),
                    MeasurementUnitEntity(abbreviation = "pedaço", displayName = "Pedaço", category = "portion"),
                    MeasurementUnitEntity(abbreviation = "porção", displayName = "Porção", category = "portion"),
                    MeasurementUnitEntity(abbreviation = "rodela", displayName = "Rodela", category = "portion"),
                    MeasurementUnitEntity(abbreviation = "cubo", displayName = "Cubo", category = "portion"),
                    MeasurementUnitEntity(abbreviation = "dente", displayName = "Dente", category = "ingredient_unit"),
                    MeasurementUnitEntity(abbreviation = "folha", displayName = "Folha", category = "ingredient_unit"),
                    MeasurementUnitEntity(abbreviation = "ramo", displayName = "Ramo", category = "ingredient_unit"),
                    MeasurementUnitEntity(abbreviation = "maço", displayName = "Maço", category = "ingredient_unit"),
                    MeasurementUnitEntity(abbreviation = "lata", displayName = "Lata", category = "package"),
                    MeasurementUnitEntity(abbreviation = "garrafa", displayName = "Garrafa", category = "package"),
                    MeasurementUnitEntity(abbreviation = "pacote", displayName = "Pacote", category = "package"),
                    MeasurementUnitEntity(abbreviation = "sachê", displayName = "Sachê", category = "package"),
                    MeasurementUnitEntity(abbreviation = "caixa", displayName = "Caixa", category = "package"),
                    MeasurementUnitEntity(abbreviation = "vidro", displayName = "Vidro", category = "package"),
                    MeasurementUnitEntity(abbreviation = "tablete", displayName = "Tablete", category = "package"),
                    MeasurementUnitEntity(abbreviation = "pitada", displayName = "Pitada", category = "other"),
                    MeasurementUnitEntity(abbreviation = "fio", displayName = "Fio", category = "other"),
                    MeasurementUnitEntity(abbreviation = "q.b.", displayName = "Quanto baste", category = "other"),
                    MeasurementUnitEntity(abbreviation = "a gosto", displayName = "A gosto", category = "other")
            )

    @Provides fun provideRecipeDao(database: AppDatabase): RecipeDao = database.recipeDao()
    @Provides fun provideIngredientDao(database: AppDatabase): IngredientDao = database.ingredientDao()
    @Provides fun provideStepDao(database: AppDatabase): StepDao = database.stepDao()
    @Provides fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()
    @Provides fun provideMeasurementUnitDao(database: AppDatabase): MeasurementUnitDao = database.measurementUnitDao()

    @Provides @Singleton
    fun provideRecipeRepository(impl: RoomRecipeRepository): RecipeRepository = impl

    @Provides @Singleton
    fun provideBackupCodec(impl: JsonRecipeBackupCodec): RecipeBackupCodec = impl
}
