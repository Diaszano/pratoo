package com.diaszano.pratoo.data.repository

import com.diaszano.pratoo.recipe.domain.model.Ingredient
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.model.RecipeStep
import com.diaszano.pratoo.recipe.domain.model.Tag
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
    ) = Recipe(
        id = id,
        title = title,
        isFavorite = isFavorite
    )

    @Test
    fun `save new recipe generates id`() = runTest {
        val recipe = createRecipe(title = "Bolo de Chocolate")
        val id = repository.saveRecipe(recipe)

        assertTrue(id > 0)
        val saved = repository.getRecipe(id)
        assertNotNull(saved)
        assertEquals("Bolo de Chocolate", saved!!.title)
    }

    @Test
    fun `save recipe with ingredients and steps`() = runTest {
        val recipe = Recipe(
            title = "Recipe",
            ingredients = listOf(
                Ingredient(name = "Flour", quantity = "200g"),
                Ingredient(name = "Sugar", quantity = "100g")
            ),
            steps = listOf(
                RecipeStep(text = "Mix ingredients"),
                RecipeStep(text = "Bake for 30 min")
            )
        )
        val id = repository.saveRecipe(recipe)

        val saved = repository.getRecipe(id)
        assertNotNull(saved)
        assertEquals(2, saved!!.ingredients.size)
        assertEquals("Flour", saved.ingredients[0].name)
        assertEquals(2, saved.steps.size)
        assertEquals("Mix ingredients", saved.steps[0].text)
    }

    @Test
    fun `edit existing recipe updates fields`() = runTest {
        val id = repository.saveRecipe(createRecipe(title = "Original"))

        val updated = createRecipe(id = id, title = "Updated Recipe")
        repository.saveRecipe(updated)

        val saved = repository.getRecipe(id)
        assertEquals("Updated Recipe", saved!!.title)
    }

    @Test
    fun `toggle favorite on recipe`() = runTest {
        val id = repository.saveRecipe(createRecipe(isFavorite = false))

        repository.toggleFavorite(id)
        var recipe = repository.getRecipe(id)
        assertTrue(recipe!!.isFavorite)

        repository.toggleFavorite(id)
        recipe = repository.getRecipe(id)
        assertFalse(recipe!!.isFavorite)
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
        val id = repository.saveRecipe(createRecipe(title = "To Delete"))
        assertNotNull(repository.getRecipe(id))

        repository.deleteRecipe(id)
        assertEquals(null, repository.getRecipe(id))
    }

    @Test
    fun `search by title filters recipes`() = runTest {
        repository.saveRecipe(createRecipe(title = "Bolo de Chocolate"))
        repository.saveRecipe(createRecipe(title = "Salada Caesar"))
        repository.saveRecipe(createRecipe(title = "Bolo de Cenoura"))

        val results = repository.searchRecipes(query = "Bolo", tagId = null).first()
        assertEquals(2, results.size)
        assertTrue(results.all { it.title.contains("Bolo") })
    }

    @Test
    fun `observeAllRecipes returns all saved recipes`() = runTest {
        repository.saveRecipe(createRecipe(title = "Recipe 1"))
        repository.saveRecipe(createRecipe(title = "Recipe 2"))

        val all = repository.observeAllRecipes().first()
        assertEquals(2, all.size)
    }

    @Test
    fun `getAllRecipes returns all for backup`() = runTest {
        repository.saveRecipe(createRecipe(title = "R1"))
        repository.saveRecipe(createRecipe(title = "R2"))

        val all = repository.getAllRecipes()
        assertEquals(2, all.size)
    }

    @Test
    fun `save recipe with tags creates cross refs`() = runTest {
        val tag1 = Tag(name = "Sobremesa")
        val tag2 = Tag(name = "Rápida")
        val recipe = createRecipe().copy(tags = listOf(tag1, tag2))
        val id = repository.saveRecipe(recipe)

        val saved = repository.getRecipe(id)
        assertNotNull(saved)
        assertEquals(2, saved!!.tags.size)
    }
}
