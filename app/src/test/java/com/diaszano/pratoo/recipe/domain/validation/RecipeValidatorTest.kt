package com.diaszano.pratoo.recipe.domain.validation

import com.diaszano.pratoo.recipe.domain.model.Ingredient
import com.diaszano.pratoo.recipe.domain.model.Recipe
import com.diaszano.pratoo.recipe.domain.model.RecipeSection
import com.diaszano.pratoo.recipe.domain.model.RecipeStep
import com.diaszano.pratoo.recipe.domain.model.Tag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeValidatorTest {
    @Test
    fun `valid recipe has no validation errors`() {
        val recipe =
            Recipe(
                title = "Bolo",
                servings = 8,
                sourceUrl = "https://example.com/bolo",
                sections =
                    listOf(
                        RecipeSection(
                            ingredients = listOf(Ingredient(name = "Farinha")),
                            steps = listOf(RecipeStep(text = "Misture tudo")),
                        ),
                    ),
            )

        assertTrue(RecipeValidator.validate(recipe).isEmpty())
    }

    @Test
    fun `blank title invalid servings and empty content are reported`() {
        val recipe =
            Recipe(
                title = " ",
                servings = 0,
                sections = listOf(RecipeSection()),
            )

        val errors = RecipeValidator.validate(recipe)

        assertTrue(errors.contains(RecipeValidationError.EmptyTitle))
        assertTrue(errors.contains(RecipeValidationError.InvalidServings))
        assertTrue(errors.contains(RecipeValidationError.EmptyContent))
    }

    @Test
    fun `invalid source url is reported`() {
        val recipe =
            Recipe(
                title = "Bolo",
                sourceUrl = "example.com/bolo",
                sections =
                    listOf(
                        RecipeSection(
                            ingredients = listOf(Ingredient(name = "Farinha")),
                        ),
                    ),
            )

        assertTrue(RecipeValidator.validate(recipe).contains(RecipeValidationError.InvalidSourceUrl))
    }

    @Test
    fun `multiple sections require names and content`() {
        val recipe =
            Recipe(
                title = "Bolo",
                sections =
                    listOf(
                        RecipeSection(
                            name = "Massa",
                            ingredients = listOf(Ingredient(name = "Farinha")),
                        ),
                        RecipeSection(name = ""),
                    ),
            )

        val errors = RecipeValidator.validate(recipe)

        assertTrue(errors.contains(RecipeValidationError.EmptySectionName(sectionIndex = 1)))
        assertTrue(errors.contains(RecipeValidationError.EmptySectionContent(sectionIndex = 1)))
    }

    @Test
    fun `ingredient with quantity but no name is reported`() {
        val recipe =
            Recipe(
                title = "Bolo",
                sections =
                    listOf(
                        RecipeSection(
                            ingredients = listOf(Ingredient(name = "", quantity = "2")),
                        ),
                    ),
            )

        assertTrue(
            RecipeValidator
                .validate(recipe)
                .contains(RecipeValidationError.EmptyIngredientName(sectionIndex = 0, ingredientIndex = 0)),
        )
    }

    @Test
    fun `tags are trimmed and deduplicated`() {
        val tags =
            RecipeValidator.normalizeTags(
                listOf(
                    Tag(name = " Doce "),
                    Tag(name = "doce"),
                    Tag(name = " "),
                    Tag(name = "Rápida"),
                ),
            )

        assertEquals(listOf("Doce", "Rápida"), tags.map { it.name })
    }

    @Test
    fun `blank placeholder ingredient is ignored during validation`() {
        val recipe =
            Recipe(
                title = "Bolo",
                sections =
                    listOf(
                        RecipeSection(
                            ingredients =
                                listOf(
                                    Ingredient(name = "Farinha"),
                                    Ingredient(name = "", quantity = "", unit = ""),
                                ),
                        ),
                    ),
            )

        val errors = RecipeValidator.validate(recipe)

        assertFalse(
            errors.contains(RecipeValidationError.EmptyIngredientName(sectionIndex = 0, ingredientIndex = 1)),
        )
    }
}
