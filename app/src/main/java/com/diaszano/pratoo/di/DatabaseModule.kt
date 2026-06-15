package com.diaszano.pratoo.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diaszano.pratoo.data.local.PratooDatabase
import com.diaszano.pratoo.data.local.dao.IngredientDao
import com.diaszano.pratoo.data.local.dao.MeasurementUnitDao
import com.diaszano.pratoo.data.local.dao.RecipeDao
import com.diaszano.pratoo.data.local.dao.StepDao
import com.diaszano.pratoo.data.local.dao.TagDao
import com.diaszano.pratoo.data.local.entity.MeasurementUnit
import com.diaszano.pratoo.data.repository.RecipeRepository
import com.diaszano.pratoo.data.repository.RecipeRepositoryImpl
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
    fun provideDatabase(@ApplicationContext context: Context): PratooDatabase =
            Room.databaseBuilder(context, PratooDatabase::class.java, "pratoo.db")
                    .addMigrations(PratooDatabase.MIGRATION_1_2, PratooDatabase.MIGRATION_2_3)
                    .addCallback(
                            object : RoomDatabase.Callback() {
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val dao =
                                                Room.databaseBuilder(
                                                                context,
                                                                PratooDatabase::class.java,
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
                                                                PratooDatabase::class.java,
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
                    // Peso
                    MeasurementUnit(
                            abbreviation = "kg",
                            displayName = "Quilograma",
                            category = "weight"
                    ),
                    MeasurementUnit(abbreviation = "g", displayName = "Grama", category = "weight"),
                    MeasurementUnit(
                            abbreviation = "mg",
                            displayName = "Miligrama",
                            category = "weight"
                    ),
                    MeasurementUnit(
                            abbreviation = "lb",
                            displayName = "Libra",
                            category = "weight"
                    ),
                    MeasurementUnit(abbreviation = "oz", displayName = "Onça", category = "weight"),

                    // Volume
                    MeasurementUnit(abbreviation = "L", displayName = "Litro", category = "volume"),
                    MeasurementUnit(
                            abbreviation = "ml",
                            displayName = "Mililitro",
                            category = "volume"
                    ),
                    MeasurementUnit(
                            abbreviation = "fl oz",
                            displayName = "Onça fluida",
                            category = "volume"
                    ),

                    // Medidas culinárias
                    MeasurementUnit(
                            abbreviation = "xíc",
                            displayName = "Xícara",
                            category = "kitchen"
                    ),
                    MeasurementUnit(
                            abbreviation = "1/2 xíc",
                            displayName = "Meia xícara",
                            category = "kitchen"
                    ),
                    MeasurementUnit(
                            abbreviation = "cs",
                            displayName = "Colher de sopa",
                            category = "kitchen"
                    ),
                    MeasurementUnit(
                            abbreviation = "ct",
                            displayName = "Colher de chá",
                            category = "kitchen"
                    ),
                    MeasurementUnit(
                            abbreviation = "cc",
                            displayName = "Colher de café",
                            category = "kitchen"
                    ),
                    MeasurementUnit(
                            abbreviation = "copo",
                            displayName = "Copo",
                            category = "kitchen"
                    ),
                    MeasurementUnit(
                            abbreviation = "copo amer.",
                            displayName = "Copo americano",
                            category = "kitchen"
                    ),

                    // Quantidade / contagem
                    MeasurementUnit(
                            abbreviation = "un",
                            displayName = "Unidade",
                            category = "count"
                    ),
                    MeasurementUnit(abbreviation = "dz", displayName = "Dúzia", category = "count"),
                    MeasurementUnit(abbreviation = "par", displayName = "Par", category = "count"),

                    // Cortes e formatos
                    MeasurementUnit(
                            abbreviation = "fatia",
                            displayName = "Fatia",
                            category = "portion"
                    ),
                    MeasurementUnit(
                            abbreviation = "pedaço",
                            displayName = "Pedaço",
                            category = "portion"
                    ),
                    MeasurementUnit(
                            abbreviation = "porção",
                            displayName = "Porção",
                            category = "portion"
                    ),
                    MeasurementUnit(
                            abbreviation = "rodela",
                            displayName = "Rodela",
                            category = "portion"
                    ),
                    MeasurementUnit(
                            abbreviation = "cubo",
                            displayName = "Cubo",
                            category = "portion"
                    ),

                    // Ingredientes específicos comuns
                    MeasurementUnit(
                            abbreviation = "dente",
                            displayName = "Dente",
                            category = "ingredient_unit"
                    ),
                    MeasurementUnit(
                            abbreviation = "folha",
                            displayName = "Folha",
                            category = "ingredient_unit"
                    ),
                    MeasurementUnit(
                            abbreviation = "ramo",
                            displayName = "Ramo",
                            category = "ingredient_unit"
                    ),
                    MeasurementUnit(
                            abbreviation = "maço",
                            displayName = "Maço",
                            category = "ingredient_unit"
                    ),

                    // Embalagens
                    MeasurementUnit(
                            abbreviation = "lata",
                            displayName = "Lata",
                            category = "package"
                    ),
                    MeasurementUnit(
                            abbreviation = "garrafa",
                            displayName = "Garrafa",
                            category = "package"
                    ),
                    MeasurementUnit(
                            abbreviation = "pacote",
                            displayName = "Pacote",
                            category = "package"
                    ),
                    MeasurementUnit(
                            abbreviation = "sachê",
                            displayName = "Sachê",
                            category = "package"
                    ),
                    MeasurementUnit(
                            abbreviation = "caixa",
                            displayName = "Caixa",
                            category = "package"
                    ),
                    MeasurementUnit(
                            abbreviation = "vidro",
                            displayName = "Vidro",
                            category = "package"
                    ),
                    MeasurementUnit(
                            abbreviation = "tablete",
                            displayName = "Tablete",
                            category = "package"
                    ),

                    // Medidas livres
                    MeasurementUnit(
                            abbreviation = "pitada",
                            displayName = "Pitada",
                            category = "other"
                    ),
                    MeasurementUnit(abbreviation = "fio", displayName = "Fio", category = "other"),
                    MeasurementUnit(
                            abbreviation = "q.b.",
                            displayName = "Quanto baste",
                            category = "other"
                    ),
                    MeasurementUnit(
                            abbreviation = "a gosto",
                            displayName = "A gosto",
                            category = "other"
                    )
            )

    @Provides fun provideRecipeDao(database: PratooDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideIngredientDao(database: PratooDatabase): IngredientDao = database.ingredientDao()

    @Provides fun provideStepDao(database: PratooDatabase): StepDao = database.stepDao()

    @Provides fun provideTagDao(database: PratooDatabase): TagDao = database.tagDao()

    @Provides
    fun provideMeasurementUnitDao(database: PratooDatabase): MeasurementUnitDao =
            database.measurementUnitDao()

    @Provides
    @Singleton
    fun provideRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository = impl
}
