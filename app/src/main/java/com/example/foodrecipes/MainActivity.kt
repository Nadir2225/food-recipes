package com.example.foodrecipes

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.foodrecipes.modules.firebase.AuthState
import com.example.foodrecipes.modules.firebase.AuthViewModel
import com.example.foodrecipes.modules.firebase.RecipeViewModel
import com.example.foodrecipes.modules.firebase.UserViewModel
import com.example.foodrecipes.modules.room.RoomDatabase
import com.example.foodrecipes.pages.Download
import com.example.foodrecipes.pages.Downloads
import com.example.foodrecipes.pages.Favorites
import com.example.foodrecipes.pages.Home
import com.example.foodrecipes.pages.Login
import com.example.foodrecipes.pages.Recipe
import com.example.foodrecipes.pages.Signup
import com.example.foodrecipes.ui.theme.FoodRecipesTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            RoomDatabase::class.java,
            "foodRecipes.db"
        ).build()
    }
    private val recipeViewModel by viewModels<RecipeViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RecipeViewModel(db.recipeDao) as T
                }
            }
        }
    )

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {

        var isAuthChecked by mutableStateOf(false)

        var splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition { !isAuthChecked }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()
        val userViewModel: UserViewModel by viewModels()

        var start: String

        authViewModel.authState.observe(this@MainActivity) { authState ->
            isAuthChecked = true
            when (authState) {
                AuthState.Authenticated -> start = "home"
                AuthState.Unauthenticated -> start = "login"
                else -> start = "login"
            }
            setContent {
                FoodRecipesTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        MyNavigation(
                            modifier = Modifier.padding(innerPadding),
                            authViewModel = authViewModel,
                            userViewModel = userViewModel,
                            recipeViewModel = recipeViewModel,
                            start = start
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyNavigation(
    modifier: Modifier,
    authViewModel: ViewModel,
    userViewModel: ViewModel,
    recipeViewModel: ViewModel,
    start: String
) {
    val navController = rememberNavController()

//    val authState = (authViewModel as AuthViewModel).authState.observeAsState()

    val validStart = if (start.isNotEmpty()) start else "login"

//    var start by remember { mutableStateOf("login") }
////
//    LaunchedEffect(authState.value) {
//        when (authState.value) {
//            is AuthState.Authenticated -> start = "home"
//            is AuthState.Unauthenticated -> start = "login"
//            else -> Unit
//        }
//    }

    NavHost(navController = navController, startDestination = validStart) {
        composable("login") {
            Login(modifier = modifier,
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable("signup") {
            Signup(modifier = modifier,
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable("home") {
            Home(modifier = modifier,
                navController = navController,
                authViewModel = authViewModel,
                userViewModel = userViewModel,
                recipeViewModel = recipeViewModel
            )
        }
        composable("favorites") {
            Favorites(modifier = modifier,
                navController = navController,
                userViewModel = userViewModel,
                recipeViewModel = recipeViewModel
            )
        }
        composable("recipe/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")
            Recipe(modifier = modifier,
                navController = navController,
                userViewModel = userViewModel,
                recipeViewModel = recipeViewModel,
                recipeId = (recipeId as String)
            )
        }
        composable("downloads") {
            Downloads(modifier = modifier,
                navController = navController,
                recipeViewModel = recipeViewModel
            )
        }
        composable("download/{id}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("id")
            Download(modifier = modifier,
                navController = navController,
                recipeViewModel = recipeViewModel,
                id = (recipeId as String)
            )
        }
    }
}