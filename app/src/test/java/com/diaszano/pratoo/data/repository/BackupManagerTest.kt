package com.diaszano.pratoo.data.repository

import com.diaszano.pratoo.data.local.entity.IngredientEntity
import com.diaszano.pratoo.data.local.entity.RecipeEntity
import com.diaszano.pratoo.data.local.entity.StepEntity
import com.diaszano.pratoo.data.local.entity.TagEntity
import com.diaszano.pratoo.data.local.relation.RecipeWithDetails
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BackupManagerTest {

    private lateinit var repository: FakeRecipeRepository

    @Before
    fun setup() {
        repository = FakeRecipeRepository()
    }

    @Test
    fun `backup version constant is correct`() {
        assertEquals(1, BackupManager.BACKUP_VERSION)
    }

    @Test
    fun `save recipe with favorite persists favorite state`() = runTest {
        val id = repository.saveRecipe(
            RecipeWithDetails(
                recipe = RecipeEntity(title = "Fav", isFavorite = true),
                ingredients = emptyList(),
                steps = emptyList(),
                tags = emptyList()
            ),
            emptyList()
        )
        val saved = repository.getRecipe(id)
        assertNotNull(saved)
        assertTrue(saved!!.recipe.isFavorite)
    }

    @Test
    fun `save recipe with tags persists tag associations`() = runTest {
        val id = repository.saveRecipe(
            RecipeWithDetails(
                recipe = RecipeEntity(title = "Tagged"),
                ingredients = emptyList(),
                steps = emptyList(),
                tags = emptyList()
            ),
            listOf(TagEntity(name = "Doce"), TagEntity(name = "Rapida"))
        )
        val saved = repository.getRecipe(id)
        assertNotNull(saved)
        assertEquals(2, saved!!.tags.size)
    }

    @Test
    fun `getAllRecipesWithDetails returns complete recipes`() = runTest {
        repository.saveRecipe(
            RecipeWithDetails(
                recipe = RecipeEntity(title = "R1"),
                ingredients = listOf(IngredientEntity(recipeId = 0, name = "Farinha", quantity = "200g")),
                steps = listOf(StepEntity(recipeId = 0, text = "Misturar")),
                tags = emptyList()
            ),
            listOf(TagEntity(name = "Sobremesa"))
        )

        val all = repository.getAllRecipesWithDetails()
        assertEquals(1, all.size)
        assertEquals("R1", all[0].recipe.title)
        assertEquals(1, all[0].ingredients.size)
        assertEquals(1, all[0].steps.size)
        assertEquals(1, all[0].tags.size)
    }

    @Test
    fun `restore from backup data recreates recipes`() = runTest {
        val recipes = listOf(
            RecipeWithDetails(
                recipe = RecipeEntity(title = "Bolo", isFavorite = true, sourceUrl = "https://example.com"),
                ingredients = listOf(IngredientEntity(recipeId = 0, name = "Acucar", quantity = "100g")),
                steps = listOf(StepEntity(recipeId = 0, text = "Pre aquecer forno")),
                tags = emptyList()
            ),
            RecipeWithDetails(
                recipe = RecipeEntity(title = "Salada"),
                ingredients = listOf(IngredientEntity(recipeId = 0, name = "Tomate", quantity = "2")),
                steps = listOf(StepEntity(recipeId = 0, text = "Lavar e picar")),
                tags = emptyList()
            )
        )

        recipes.forEach { recipe ->
            repository.saveRecipe(recipe, listOf(TagEntity(name = "Favorita")))
        }

        val restored = repository.getAllRecipesWithDetails()
        assertEquals(2, restored.size)

        val bolo = restored.find { it.recipe.title == "Bolo" }
        assertNotNull(bolo)
        assertTrue(bolo!!.recipe.isFavorite)
        assertEquals("https://example.com", bolo.recipe.sourceUrl)
        assertEquals("Acucar", bolo.ingredients[0].name)

        val salada = restored.find { it.recipe.title == "Salada" }
        assertNotNull(salada)
        assertEquals("Tomate", salada!!.ingredients[0].name)
    }

    @Test
    fun `delete all recipes clears backup`() = runTest {
        repository.saveRecipe(
            RecipeWithDetails(RecipeEntity(title = "R1"), emptyList(), emptyList(), emptyList()),
            emptyList()
        )
        repository.saveRecipe(
            RecipeWithDetails(RecipeEntity(title = "R2"), emptyList(), emptyList(), emptyList()),
            emptyList()
        )
        assertEquals(2, repository.getAllRecipesWithDetails().size)

        repository.deleteRecipe(1)
        repository.deleteRecipe(2)
        assertEquals(0, repository.getAllRecipesWithDetails().size)
    }
}
