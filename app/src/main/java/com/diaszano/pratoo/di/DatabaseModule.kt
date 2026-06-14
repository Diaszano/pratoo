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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PratooDatabase =
        Room.databaseBuilder(context, PratooDatabase::class.java, "pratoo.db")
            .addMigrations(PratooDatabase.MIGRATION_1_2, PratooDatabase.MIGRATION_2_3)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = Room.databaseBuilder(
                            context, PratooDatabase::class.java, "pratoo.db"
                        ).build().measurementUnitDao()
                        if (dao.count() == 0) {
                            dao.insertAll(defaultMeasurementUnits())
                        }
                    }
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        val database = Room.databaseBuilder(
                            context, PratooDatabase::class.java, "pratoo.db"
                        ).build()
                        val dao = database.measurementUnitDao()
                        if (dao.count() == 0) {
                            dao.insertAll(defaultMeasurementUnits())
                        }
                    }
                }
            })
            .fallbackToDestructiveMigration(false)
            .build()

    private fun defaultMeasurementUnits() = listOf(
        MeasurementUnit(abbreviation = "kg", displayName = "Quilograma", category = "weight"),
        MeasurementUnit(abbreviation = "g", displayName = "Grama", category = "weight"),
        MeasurementUnit(abbreviation = "mg", displayName = "Miligrama", category = "weight"),
        MeasurementUnit(abbreviation = "lb", displayName = "Libra", category = "weight"),
        MeasurementUnit(abbreviation = "oz", displayName = "Onça", category = "weight"),
        MeasurementUnit(abbreviation = "L", displayName = "Litro", category = "volume"),
        MeasurementUnit(abbreviation = "ml", displayName = "Mililitro", category = "volume"),
        MeasurementUnit(abbreviation = "xic", displayName = "Xícara", category = "volume"),
        MeasurementUnit(abbreviation = "cs", displayName = "Colher de sopa", category = "volume"),
        MeasurementUnit(abbreviation = "ct", displayName = "Colher de chá", category = "volume"),
        MeasurementUnit(abbreviation = "cc", displayName = "Colher de café", category = "volume"),
        MeasurementUnit(abbreviation = "fl oz", displayName = "Onça fluida", category = "volume"),
        MeasurementUnit(abbreviation = "un", displayName = "Unidade", category = "count"),
        MeasurementUnit(abbreviation = "dente", displayName = "Dente", category = "count"),
        MeasurementUnit(abbreviation = "fatia", displayName = "Fatia", category = "count"),
        MeasurementUnit(abbreviation = "pedaco", displayName = "Pedaço", category = "count"),
        MeasurementUnit(abbreviation = "folha", displayName = "Folha", category = "count"),
        MeasurementUnit(abbreviation = "lata", displayName = "Lata", category = "count"),
        MeasurementUnit(abbreviation = "garrafa", displayName = "Garrafa", category = "count"),
        MeasurementUnit(abbreviation = "maço", displayName = "Maço", category = "count"),
        MeasurementUnit(abbreviation = "molho", displayName = "Molho", category = "count"),
        MeasurementUnit(abbreviation = "pitada", displayName = "Pitada", category = "other"),
        MeasurementUnit(abbreviation = "a gosto", displayName = "A gosto", category = "other"),
    )

    @Provides
    fun provideRecipeDao(database: PratooDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideIngredientDao(database: PratooDatabase): IngredientDao = database.ingredientDao()

    @Provides
    fun provideStepDao(database: PratooDatabase): StepDao = database.stepDao()

    @Provides
    fun provideTagDao(database: PratooDatabase): TagDao = database.tagDao()

    @Provides
    fun provideMeasurementUnitDao(database: PratooDatabase): MeasurementUnitDao = database.measurementUnitDao()

    @Provides
    @Singleton
    fun provideRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository = impl
}
