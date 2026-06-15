package com.diaszano.pratoo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.diaszano.pratoo.ui.cooking.CookingModeScreen
import com.diaszano.pratoo.ui.recipedetail.RecipeDetailScreen
import com.diaszano.pratoo.ui.recipeedit.RecipeEditScreen
import com.diaszano.pratoo.ui.recipelist.RecipeListScreen
import com.diaszano.pratoo.backup.adapter.ui.BackupSettingsScreen
import com.diaszano.pratoo.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
data object RecipeListRoute

@Serializable
data class RecipeDetailRoute(
    val recipeId: Long,
)

@Serializable
data class RecipeEditRoute(
    val recipeId: Long? = null,
)

@Serializable
data class CookingModeRoute(
    val recipeId: Long,
)

@Serializable
data object SettingsRoute

@Serializable
data object BackupSettingsRoute

@Composable
fun PratooNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = RecipeListRoute,
        modifier = modifier,
    ) {
        composable<RecipeListRoute> {
            RecipeListScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate(RecipeDetailRoute(recipeId))
                },
                onAddRecipeClick = {
                    navController.navigate(RecipeEditRoute())
                },
                onSettingsClick = {
                    navController.navigate(SettingsRoute)
                },
            )
        }
        composable<RecipeDetailRoute> {
            RecipeDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditRecipe = { recipeId ->
                    navController.navigate(RecipeEditRoute(recipeId))
                },
                onStartCooking = { recipeId ->
                    navController.navigate(CookingModeRoute(recipeId))
                },
            )
        }
        composable<CookingModeRoute> {
            CookingModeScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<RecipeEditRoute> {
            RecipeEditScreen(
                onNavigateBack = { navController.popBackStack() },
                onRecipeSaved = { navController.popBackStack() },
            )
        }
        composable<SettingsRoute> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBackupSettings = {
                    navController.navigate(BackupSettingsRoute)
                },
            )
        }
        composable<BackupSettingsRoute> {
            BackupSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
