package com.diaszano.pratoo.recipe.adapter.out.persistence.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.diaszano.pratoo.recipe.adapter.out.persistence.entity.RecipeEntity
import com.diaszano.pratoo.recipe.adapter.out.persistence.relation.RecipeListProjection
import com.diaszano.pratoo.recipe.adapter.out.persistence.relation.RecipeWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Transaction
    @Query("SELECT id, title, image_uri AS imageUri, is_favorite AS isFavorite, updated_at AS updatedAt FROM recipes ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<RecipeListProjection>>

    @Transaction
    @Query(
        """
        SELECT DISTINCT r.id, r.title, r.image_uri AS imageUri, r.is_favorite AS isFavorite, r.updated_at AS updatedAt 
        FROM recipes r
        LEFT JOIN ingredients i ON i.recipe_id = r.id
        LEFT JOIN recipe_tag_cross_ref rt ON rt.recipe_id = r.id
        WHERE (:query IS NULL OR r.title LIKE '%' || :query || '%' OR i.name LIKE '%' || :query || '%')
        AND (:tagId IS NULL OR rt.tag_id = :tagId)
        ORDER BY r.updated_at DESC
        """
    )
    fun search(query: String?, tagId: Long?): Flow<List<RecipeListProjection>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeById(id: Long): Flow<RecipeWithDetails?>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: Long): RecipeWithDetails?

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY updated_at DESC")
    suspend fun getAllWithDetails(): List<RecipeWithDetails>

    @Transaction
    @Query("SELECT id, title, image_uri AS imageUri, is_favorite AS isFavorite, updated_at AS updatedAt FROM recipes WHERE is_favorite = 1 ORDER BY updated_at DESC")
    fun observeFavoriteRecipes(): Flow<List<RecipeListProjection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity): Long

    @Update
    suspend fun update(recipe: RecipeEntity)

    @Delete
    suspend fun delete(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recipes")
    suspend fun deleteAll()

    @Query("UPDATE recipes SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)
}
