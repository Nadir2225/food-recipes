package com.example.foodrecipes.pages


import VideoPlayerExo
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.foodrecipes.R
import com.example.foodrecipes.modules.firebase.RecipeViewModel
import com.example.foodrecipes.modules.firebase.UserViewModel
import com.example.foodrecipes.ui.theme.blue
import com.example.foodrecipes.ui.theme.darkBlue
import com.example.foodrecipes.ui.theme.whiteBlue
import kotlin.text.Typography.bullet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Recipe(modifier: Modifier,
           navController: NavController,
           userViewModel: ViewModel,
           recipeViewModel: ViewModel,
           recipeId: String
) {
    val user by (userViewModel as UserViewModel).user.observeAsState()
    val recipe by (recipeViewModel as RecipeViewModel).recipe.observeAsState()
    val downloads by (recipeViewModel as RecipeViewModel).downloads.observeAsState()

    LaunchedEffect(recipeId) {
        (recipeViewModel as RecipeViewModel).fetchRecipe(recipeId)
        recipeViewModel.getDownloads()
    }

    val context = LocalContext.current

    var favorite by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        if (user != null) {
            favorite = if (user!!.favorites.contains(recipeId)) true else false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Recipe", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = whiteBlue,
                ),
                navigationIcon = {
                    IconButton(onClick = { 
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = null,
                            modifier = Modifier.height(25.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (user != null) {
                            var action = if (user!!.favorites.contains(recipeId)) "delete" else "add"
                            (userViewModel as UserViewModel).setFavorites(
                                userId = user!!.id,
                                recipeId = recipeId,
                                update = action
                                )
                        }
                    }) {
                        Icon(painter = painterResource(if (favorite) R.drawable.filled_star else R.drawable.star),
                            contentDescription = null,
                            tint = darkBlue,
                            modifier = Modifier.height(25.dp))
                    }
                    IconButton(
                        enabled = !downloading,
                        onClick = {
                        if (downloads?.any { it.id == recipeId }?:false) {
                            showDialog = true
                        } else {
                            downloading = true
                            (recipeViewModel as RecipeViewModel).downloadRecipe(context = context, recipeId) {
                                downloading = false
                            }
                        }
                    }) {
                        if (downloads?.any { it.id == recipeId }?:false) {
                            Image(painter = painterResource(R.drawable.downloded),
                                contentDescription = null,
                                modifier = Modifier.height(25.dp))
                        } else {
                            Icon(painter = painterResource(R.drawable.download),
                                contentDescription = null,
                                tint = if (downloading) blue else darkBlue,
                                modifier = Modifier.height(25.dp))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
//        recipe?.loading?:false
        if (recipe?.loading?:false) {
            LazyColumn(
                modifier = Modifier
                    .background(whiteBlue)
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .shimmerEffect()
                    )
                }
                item {
                    Box(
                        modifier = Modifier
                            .width(300.dp)
                            .height(50.dp)
                            .padding(10.dp)
                            .padding(top = 10.dp)
                            .shimmerEffect()
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                if (recipe != null) {
                    VideoPlayerExo(
                        videoUrl = recipe!!.videoUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "something went wrong, the video couldn't load",
                            color = Color.White,
                        )
                    }
                }
                Text(
                    text = recipe?.title?:"",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(10.dp)
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                )
                Text(
                    text = recipe?.description?:"",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                ) {
                    Text(
                        text = "Duration :",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(start = 10.dp, end = 5.dp)
                    )
                    Text(
                        text = recipe?.duration?:"",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .padding(end = 10.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                ) {
                    Text(
                        text = "Ingredients :",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    val paragraphStyle = androidx.compose.ui.text.ParagraphStyle(textIndent = TextIndent(restLine = 12.sp))
                    Text(
                        buildAnnotatedString {
                            recipe?.ingredients
                                ?.forEach {
                                    withStyle(style = paragraphStyle) {
                                        append(bullet)
                                        append("\t")
                                        append(it)
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 10.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                ) {
                    Text(
                        text = "Instructions :",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    val paragraphStyle = androidx.compose.ui.text.ParagraphStyle(textIndent = TextIndent(restLine = 12.sp))
                    var i = 1
                    Text(
                        buildAnnotatedString {
                            recipe?.instructions?.forEach {
                                withStyle(style = paragraphStyle) {
                                    append("$i-")
                                    append("\t")
                                    append(it)
                                    append("\n")
                                }
                                i++
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 10.dp)
                    )
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Recipe") },
            text = { Text("Are you sure you want to delete this recipe?") },
            confirmButton = {
                Button(onClick = {
                    // Call your delete function here
                    (recipeViewModel as RecipeViewModel).deleteRecipe(recipeId)
                    showDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
