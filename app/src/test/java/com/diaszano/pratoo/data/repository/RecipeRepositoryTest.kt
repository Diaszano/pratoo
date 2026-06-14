package com.diaszano.pratoo.data.repository

import com.diaszano.pratoo.data.local.entity.IngredientEntity
import com.diaszano.pratoo.data.local.entity.RecipeEntity
import com.diaszano.pratoo.data.local.entity.StepEntity
import com.diaszano.pratoo.data.local.entity.TagEntity
import com.diaszano.pratoo.data.local.relation.RecipeWithDetails
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecipeRepositoryTest {

    private lateinit var repository: FakeRecipeRepository

    @Before
    fun setup() {
        repository = FakeRecipeRepository()
    }

    private fun createRecipe(
        id: Long = 0,
        title: String = "Test Recipe",
        isFavorite: Boolean = false
    ) = RecipeWithDetails(
        recipe = RecipeEntity(
            id = id,
            title = title,
            isFavorite = isFavorite
        ),
        ingredients = emptyList(),
        steps = emptyList(),
        tags = emptyList()
    )

    @Test
    fun `save new recipe generates id`() = runTest {
        val recipe = createRecipe(title = "Bolo de Chocolate")
        val id = repository.saveRecipe(recipe, emptyList())

        assertTrue(id > 0)
        val saved = repository.getRecipe(id)
        assertNotNull(saved)
        assertEquals("Bolo de Chocolate", saved!!.recipe.title)
    }

    @Test
    fun `save recipe with ingredients and steps`() = runTest {
        val recipe = RecipeWithDetails(
            recipe = RecipeEntity(title = "Recipe"),
            ingredients = listOf(
                IngredientEntity(recipeId = 0, name = "Flour", quantity = "200g"),
                IngredientEntity(recipeId = 0, name = "Sugar", quantity = "100g")
            ),
            steps = listOf(
                StepEntity(recipeId = 0, text = "Mix ingredients"),
                StepEntity(recipeId = 0, text = "Bake for 30 min")
            ),
            tags = emptyList()
        )
        val id = repository.saveRecipe(recipe, emptyList())

        val saved = repository.getRecipe(id)
        assertNotNull(saved)
        assertEquals(2, saved!!.ingredients.size)
        assertEquals("Flour", saved.ingredients[0].name)
        assertEquals(2, saved.steps.size)
        assertEquals("Mix ingredients", saved.steps[0].text)
    }

    @Test
    fun `edit existing recipe updates fields`() = runTest {
        val id = repository.saveRecipe(createRecipe(title = "Original"), emptyList())

        val updated = createRecipe(id = id, title = "Updated Recipe")
        repository.saveRecipe(updated, emptyList())

        val saved = repository.getRecipe(id)
        assertEquals("Updated Recipe", saved!!.recipe.title)
    }

    @Test
    fun `toggle favorite on recipe`() = runTest {
        val id = repository.saveRecipe(createRecipe(isFavorite = false), emptyList())

        repository.toggleFavorite(id)
        var recipe = repository.getRecipe(id)
        assertTrue(recipe!!.recipe.isFavorite)

        repository.toggleFavorite(id)
        recipe = repository.getRecipe(id)
        assertFalse(recipe!!.recipe.isFavorite)
    }

    @Test
    fun `toggle favorite on nonexistent recipe does not crash`() = runTest {
        repository.toggleFavorite(999L)
    }

    @Test
    fun `create new tag returns unique id`() = runTest {
        val id1 = repository.createTag("Sobremesa")
        val id2 = repository.createTag("Rápida")

        assertTrue(id1 > 0)
        assertTrue(id2 > 0)
        assertTrue(id1 != id2)

        val allTags = repository.observeAllTags().first()
        assertEquals(2, allTags.size)
    }

    @Test
    fun `create duplicate tag returns existing id`() = runTest {
        val id1 = repository.createTag("Sobremesa")
        val id2 = repository.createTag("sobremesa")

        assertEquals(id1, id2)
        val allTags = repository.observeAllTags().first()
        assertEquals(1, allTags.size)
    }

    @Test
    fun `create blank tag does not add`() = runTest {
        val id1 = repository.createTag("  ")
        val id2 = repository.createTag("")

        assertEquals(0L, id1)
        assertEquals(0L, id2)
        val allTags = repository.observeAllTags().first()
        assertEquals(0, allTags.size)
    }

    @Test
    fun `delete recipe removes it`() = runTest {
        val id = repository.saveRecipe(createRecipe(title = "To Delete"), emptyList())
        assertNotNull(repository.getRecipe(id))

        repository.deleteRecipe(id)
        assertEquals(null, repository.getRecipe(id))
    }

    @Test
    fun `search by title filters recipes`() = runTest {
        repository.saveRecipe(createRecipe(title = "Bolo de Chocolate"), emptyList())
        repository.saveRecipe(createRecipe(title = "Salada Caesar"), emptyList())
        repository.saveRecipe(createRecipe(title = "Bolo de Cenoura"), emptyList())

        val results = repository.searchRecipes(query = "Bolo", tagId = null).first()
        assertEquals(2, results.size)
        assertTrue(results.all { it.title.contains("Bolo") })
    }

    @Test
    fun `observeAllRecipes returns all saved recipes`() = runTest {
        repository.saveRecipe(createRecipe(title = "Recipe 1"), emptyList())
        repository.saveRecipe(createRecipe(title = "Recipe 2"), emptyList())

        val all = repository.observeAllRecipes().first()
        assertEquals(2, all.size)
    }

    @Test
    fun `getAllRecipesWithDetails returns all for backup`() = runTest {
        repository.saveRecipe(createRecipe(title = "R1"), emptyList())
        repository.saveRecipe(createRecipe(title = "R2"), emptyList())

        val all = repository.getAllRecipesWithDetails()
        assertEquals(2, all.size)
    }

    @Test
    fun `save recipe with tags creates cross refs`() = runTest {
        val tag1 = TagEntity(name = "Sobremesa")
        val tag2 = TagEntity(name = "Rápida")
        val id = repository.saveRecipe(createRecipe(), listOf(tag1, tag2))

        val saved = repository.getRecipe(id)
        assertNotNull(saved)
        assertEquals(2, saved!!.tags.size)
    }
}
