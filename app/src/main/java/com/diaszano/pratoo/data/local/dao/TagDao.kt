package com.diaszano.pratoo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diaszano.pratoo.data.local.entity.RecipeTagCrossRef
import com.diaszano.pratoo.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name")
    suspend fun getAll(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: RecipeTagCrossRef)

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("DELETE FROM recipe_tag_cross_ref WHERE recipe_id = :recipeId")
    suspend fun deleteCrossRefsByRecipeId(recipeId: Long)

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): TagEntity?
}
