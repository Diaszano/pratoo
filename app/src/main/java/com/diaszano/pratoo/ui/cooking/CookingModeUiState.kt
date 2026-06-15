package com.diaszano.pratoo.ui.cooking

data class CookingModeUiState(
    val recipeId: Long = 0,
    val recipeTitle: String = "",
    val sections: List<CookingSectionUiState> = emptyList(),
    val completedIngredientIds: Set<String> = emptySet(),
    val completedStepIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val showExitDialog: Boolean = false,
    val isFinished: Boolean = false
)

data class CookingSectionUiState(
    val id: String,
    val name: String,
    val showHeader: Boolean,
    val ingredients: List<CookingItemUiState>,
    val steps: List<CookingItemUiState>
)

data class CookingItemUiState(
    val id: String,
    val name: String = "",
    val quantity: String = "",
    val unit: String = "",
    val order: Int = 0
) {
    val displayText: String
        get() = buildString {
            if (quantity.isNotBlank()) append("$quantity ")
            if (unit.isNotBlank()) append("$unit ")
            append(name)
        }
}
