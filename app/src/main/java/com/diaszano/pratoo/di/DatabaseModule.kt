package com.diaszano.pratoo.di

import android.content.Context
import androidx.room.Room
import com.diaszano.pratoo.data.local.PratooDatabase
import com.diaszano.pratoo.data.local.dao.IngredientDao
import com.diaszano.pratoo.data.local.dao.RecipeDao
import com.diaszano.pratoo.data.local.dao.StepDao
import com.diaszano.pratoo.data.local.dao.TagDao
import com.diaszano.pratoo.data.repository.RecipeRepository
import com.diaszano.pratoo.data.repository.RecipeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PratooDatabase =
        Room.databaseBuilder(context, PratooDatabase::class.java, "pratoo.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideRecipeDao(database: PratooDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideIngredientDao(database: PratooDatabase): IngredientDao = database.ingredientDao()

    @Provides
    fun provideStepDao(database: PratooDatabase): StepDao = database.stepDao()

    @Provides
    fun provideTagDao(database: PratooDatabase): TagDao = database.tagDao()

    @Provides
    @Singleton
    fun provideRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository = impl
}
