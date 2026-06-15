package com.diaszano.pratoo.data.repository

import com.diaszano.pratoo.recipe.adapter.out.backup.JsonRecipeBackupCodec
import com.diaszano.pratoo.recipe.domain.model.Ingredient
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.model.RecipeSection
import com.diaszano.pratoo.recipe.domain.model.RecipeStep
import com.diaszano.pratoo.recipe.domain.model.Tag
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BackupManagerTest {

    private lateinit var repository: FakeRecipeRepository
    private lateinit var codec: JsonRecipeBackupCodec

    @Before
    fun setup() {
        repository = FakeRecipeRepository()
        codec = JsonRecipeBackupCodec()
    }

    @Test
    fun `backup version constant is correct`() {
        assertEquals(2, JsonRecipeBackupCodec.BACKUP_VERSION)
    }

    @Test
    fun `save recipe with favorite persists favorite state`() = runTest {
        val id = repository.saveRecipe(
            Recipe(title = "Fav", isFavorite = true)
        )
        val saved = repository.getRecipe(id)
        assertNotNull(saved)
        assertTrue(saved!!.isFavorite)
    }

    @Test
    fun `save recipe with tags persists tag associations`() = runTest {
        val id = repository.saveRecipe(
            Recipe(title = "Tagged", tags = listOf(Tag(name = "Doce"), Tag(name = "Rapida")))
        )
        val saved = repository.getRecipe(id)
        assertNotNull(saved)
        assertEquals(2, saved!!.tags.size)
    }

    @Test
    fun `getAllRecipes returns complete recipes`() = runTest {
        repository.saveRecipe(
            Recipe(
                title = "R1",
                sections = listOf(
                    RecipeSection(
                        ingredients = listOf(Ingredient(name = "Farinha", quantity = "200g")),
                        steps = listOf(RecipeStep(text = "Misturar"))
                    )
                ),
                tags = listOf(Tag(name = "Sobremesa"))
            )
        )

        val all = repository.getAllRecipes()
        assertEquals(1, all.size)
        assertEquals("R1", all[0].title)
        assertEquals(1, all[0].sections.size)
        assertEquals(1, all[0].sections[0].ingredients.size)
        assertEquals(1, all[0].sections[0].steps.size)
        assertEquals(1, all[0].tags.size)
    }

    @Test
    fun `codec encode and decode preserves recipes`() = runTest {
        val recipes = listOf(
            Recipe(
                title = "Bolo",
                isFavorite = true,
                sourceUrl = "https://example.com",
                sections = listOf(
                    RecipeSection(
                        name = "",
                        ingredients = listOf(Ingredient(name = "Acucar", quantity = "100g")),
                        steps = listOf(RecipeStep(text = "Pre aquecer forno"))
                    )
                ),
                tags = listOf(Tag(name = "Doce"))
            ),
            Recipe(
                title = "Salada",
                sections = listOf(
                    RecipeSection(
                        ingredients = listOf(Ingredient(name = "Tomate", quantity = "2")),
                        steps = listOf(RecipeStep(text = "Lavar e picar"))
                    )
                )
            )
        )

        val json = codec.encode(recipes)
        val decoded = codec.decode(json)

        assertEquals(2, decoded.size)

        val bolo = decoded.find { it.title == "Bolo" }
        assertNotNull(bolo)
        assertTrue(bolo!!.isFavorite)
        assertEquals("https://example.com", bolo.sourceUrl)
        assertEquals(1, bolo.sections.size)
        assertEquals("Acucar", bolo.sections[0].ingredients[0].name)

        val salada = decoded.find { it.title == "Salada" }
        assertNotNull(salada)
        assertEquals(1, salada!!.sections.size)
        assertEquals("Tomate", salada.sections[0].ingredients[0].name)
    }

    @Test
    fun `codec validates imported data`() = runTest {
        val recipes = listOf(
            Recipe(
                title = "  Bolo  ",
                servings = -1,
                prepTimeMinutes = -5,
                sections = listOf(
                    RecipeSection(
                        ingredients = listOf(
                            Ingredient(name = "Acucar", quantity = "100g"),
                            Ingredient(name = "", quantity = "")
                        ),
                        steps = listOf(
                            RecipeStep(text = "Passo 1"),
                            RecipeStep(text = "")
                        )
                    )
                ),
                tags = listOf(Tag(name = "  Doce  "), Tag(name = "doce"))
            )
        )

        val json = codec.encode(recipes)
        val decoded = codec.decode(json)

        assertEquals(1, decoded.size)
        val recipe = decoded[0]
        assertEquals("Bolo", recipe.title)
        assertEquals(1, recipe.servings)
        assertEquals(0, recipe.prepTimeMinutes)
        assertEquals(1, recipe.sections.size)
        assertEquals(1, recipe.sections[0].ingredients.size)
        assertEquals(1, recipe.sections[0].steps.size)
        assertEquals(1, recipe.tags.size)
        assertEquals("Doce", recipe.tags[0].name)
    }

    @Test
    fun `delete all recipes clears backup`() = runTest {
        repository.saveRecipe(Recipe(title = "R1"))
        repository.deleteAllRecipes()
        val all = repository.getAllRecipes()
        assertEquals(0, all.size)
    }
}
