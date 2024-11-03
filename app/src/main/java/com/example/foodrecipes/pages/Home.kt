package com.example.foodrecipes.pages

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.foodrecipes.modules.firebase.AuthState
import com.example.foodrecipes.modules.firebase.AuthViewModel
import com.example.foodrecipes.R
import com.example.foodrecipes.modules.firebase.Recipe
import com.example.foodrecipes.modules.firebase.RecipeViewModel
import com.example.foodrecipes.modules.firebase.UserViewModel
import com.example.foodrecipes.ui.theme.blue
import com.example.foodrecipes.ui.theme.darkBlue
import com.example.foodrecipes.ui.theme.whiteBlue
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(modifier: Modifier,
         navController: NavController,
         authViewModel: ViewModel,
         userViewModel: ViewModel,
         recipeViewModel: ViewModel
) {

    val listState = rememberLazyListState()

    val thisUser by (userViewModel as UserViewModel).user.observeAsState()
    val recipes by (recipeViewModel as RecipeViewModel).recipes.observeAsState(emptyList())
    val db = Firebase.firestore

    var context = LocalContext.current

    val authState = (authViewModel as AuthViewModel).authState.observeAsState()

    var search by remember { mutableStateOf("") }
    var categoriesLoading by remember { mutableStateOf(false) }
    var recipesLoading by remember { mutableStateOf(false) }
    var moreLoading by remember { mutableStateOf(false) }
    val categories = remember { mutableStateListOf<String>() }
    val selected = remember { mutableStateListOf<String>() }
    var selectedState by remember { mutableStateOf(selected.toList()) }

    var original = recipes


    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login") { popUpTo(0) }
            is AuthState.Error -> Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    val navigation = rememberNavController()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(Unit) {
        categoriesLoading = true
        db.collection("categories").get().addOnSuccessListener { docs ->
            categoriesLoading = false
            for (doc in docs) {
                if (!categories.contains(doc.data["title"].toString())) {
                    categories.add(doc.data["title"].toString())
                }
            }
        }
    }

    LaunchedEffect(selectedState) {
        recipesLoading = true
        if (selectedState.isNotEmpty()) {
            (recipeViewModel as RecipeViewModel).fetchRecipes(
                selectedState.toMutableList(),
                onSuccess = {
                    original = recipes
                    recipesLoading = false
                },
                onFailure = {
                    original = recipes
                    recipesLoading = false
                }
            )
        } else {
            (recipeViewModel as RecipeViewModel).fetchRecipes (
                onSuccess = {
                    recipesLoading = false
                    original = recipes
                },
                onFailure = {
                    original = recipes
                    recipesLoading = false
                }
            )
        }
//        Log.w("nadir", "bloc ${recipes}")
    }
    selectedState = selected.toList()
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex
        }
            .collectLatest { index ->
                if (index >= (recipes.size / 2) - 5) {
                    moreLoading = true
                    if (selectedState.isNotEmpty()) {
                        (recipeViewModel as RecipeViewModel).fetchRecipes(
                            categories = selectedState.toMutableList(),
                            loadMore = true,
                            onSuccess = {
                                moreLoading = false
                            },
                            onFailure = {
                                moreLoading = false
                            }
                        )
                    } else {
                        (recipeViewModel as RecipeViewModel).fetchRecipes(
                            loadMore = true,
                            onSuccess = {
                                moreLoading = false
                            },
                            onFailure = {
                                moreLoading = false
                            }
                        )
                    }
                }
            }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(blue),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(R.drawable.user),
                        contentDescription = null,
                        modifier = Modifier
                            .width(100.dp)
                            .padding(bottom = 20.dp))
                    Button(
                        onClick = {
                            authViewModel.logout()
                            (recipeViewModel as RecipeViewModel).deleteRecipes()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(40)
                    ) {
                        Icon(painter = painterResource(R.drawable.logout),
                            contentDescription = null,
                            modifier = Modifier
                                .width(15.dp)
                                .padding(end = 2.dp))
                        Text(text = "log out",
                            color = Color.White)
                    }
//                    Button(
//                        onClick = {
//                        },
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color.DarkGray,
//                            contentColor = Color.White
//                        ),
//                        shape = RoundedCornerShape(40)
//                    ) {
//                        Text(text = "install",
//                            color = Color.White)
//                    }
                }
                Divider()
                NavigationDrawerItem(
                    label = {
                        Text(text = "Downloads")
                    },
                    icon = { Icons.Default.Home },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        navController.navigate("downloads")
                    }
                )
            }
        }) {
        Scaffold(
            topBar  = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Hi, ${thisUser?.first_name?.capitalize()?:"Guest"}",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = whiteBlue,
                    ),
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("favorites")
                        }) {
                            Icon(painter = painterResource(R.drawable.star),
                                contentDescription = null,
                                tint = darkBlue,
                                modifier = Modifier.height(25.dp))
                        }
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(painter = painterResource(R.drawable.more),
                                contentDescription = null,
                                tint = darkBlue,
                                modifier = Modifier.height(24.dp))
                        }
                    }
                )
            }
        ) {paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(whiteBlue)
                    .padding(paddingValues)
            ) {
                item {
                    Box(
                       modifier = Modifier
                           .fillMaxWidth()
                           .padding(bottom = 25.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TextField(
                            value = search,
                            onValueChange = {
                                search = it
                                if (search.isEmpty()) {
                                    recipesLoading = true
                                    if (selectedState.isNotEmpty()) {
                                        (recipeViewModel as RecipeViewModel).fetchRecipes(
                                            selectedState.toMutableList(),
                                            onSuccess = {
                                                recipesLoading = false
                                                original = recipes
                                            },
                                            onFailure = {
                                                original = recipes
                                                recipesLoading = false
                                            }
                                        )
                                    } else {
                                        (recipeViewModel as RecipeViewModel).fetchRecipes (
                                            onSuccess = {
                                                original = recipes
                                                recipesLoading = false
                                            },
                                            onFailure = {
                                                original = recipes
                                                recipesLoading = false
                                            }
                                        )
                                    }
                                } else {
                                    var temporaryList = mutableListOf<Recipe>()
                                    original.forEach { Recipe ->
                                        var title = Recipe.title.lowercase()
                                        if (search.length <= title.length && search.lowercase() == title.slice(IntRange(0, search.length - 1))) {
                                            temporaryList.add(Recipe)
                                        }
                                    }
                                    (recipeViewModel as RecipeViewModel).setRecipes(temporaryList)
                                }
                            },
                            placeholder = { Text("Search", color = Color.Gray) },
                            modifier = Modifier.width(300.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(25),
                            singleLine = true,
                            trailingIcon = {
                                if (search.isEmpty()) {
                                    Icon(painter = painterResource(R.drawable.search),
                                        contentDescription = null,
                                        tint = darkBlue,
                                        modifier = Modifier.width(25.dp))
                                } else {
                                    IconButton(onClick = {
                                        search = ""
                                        recipesLoading = true
                                        if (selectedState.isNotEmpty()) {
                                            (recipeViewModel as RecipeViewModel).fetchRecipes(
                                                selectedState.toMutableList(),
                                                onSuccess = {
                                                    recipesLoading = false
                                                    original = recipes
                                                },
                                                onFailure = {
                                                    original = recipes
                                                    recipesLoading = false
                                                }
                                            )
                                        } else {
                                            (recipeViewModel as RecipeViewModel).fetchRecipes (
                                                onSuccess = {
                                                    original = recipes
                                                    recipesLoading = false
                                                },
                                                onFailure = {
                                                    original = recipes
                                                    recipesLoading = false
                                                }
                                            )
                                        }
                                    }) {
                                        Icon(painter = painterResource(R.drawable.x),
                                            contentDescription = null,
                                            tint = darkBlue,
                                            modifier = Modifier.width(20.dp))
                                    }
                                }

                            },
                        )
                    }
                }
                item {
                    Text(
                        text = "Categories",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(start = 15.dp, bottom = 10.dp)
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBlue)
                            .padding(start = 35.dp, end = 15.dp, bottom = 25.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (categoriesLoading) {
                            item {
                                Box (
                                    modifier = Modifier
                                        .height(30.dp)
                                        .shimmerEffect()
                                        .width(70.dp)
                                        .padding(top = 10.dp)
                                ) {

                                }
                            }
                            item {
                                Box (
                                    modifier = Modifier
                                        .height(30.dp)
                                        .shimmerEffect()
                                        .width(70.dp)
                                        .padding(top = 10.dp)
                                ) {

                                }
                            }
                            item {
                                Box (
                                    modifier = Modifier
                                        .height(30.dp)
                                        .shimmerEffect()
                                        .width(70.dp)
                                        .padding(top = 10.dp)
                                ) {

                                }
                            }
                        }
                        for ((index, cat) in selected.withIndex()) {
                            item {
                                FilterChip(
                                    onClick = {
                                        if (selected.contains(cat)) {
                                            selected.removeAt(selected.indexOf(cat))
                                        } else {
                                            selected.add(cat)
                                        }
                                    },
                                    label = {
                                        Text(cat)
                                    },
                                    selected = selected.contains(cat),
                                    leadingIcon = if (selected.contains(cat)) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.Done,
                                                contentDescription = "Done icon",
                                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                )
                            }
                        }
                        for ((index, cat) in categories.withIndex()) {
                            if (!selected.contains(cat)) {
                                item {
                                    FilterChip(
                                        onClick = {
                                            if (selected.contains(cat)) {
                                                selected.removeAt(selected.indexOf(cat))
                                            } else {
                                                selected.add(cat)
                                            }
                                        },
                                        label = {
                                            Text(cat)
                                        },
                                        selected = selected.contains(cat),
                                        leadingIcon = if (selected.contains(cat)) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Filled.Done,
                                                    contentDescription = "Done icon",
                                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                                )
                                            }
                                        } else {
                                            null
                                        },
                                    )
                                }
                            }
                        }

                    }
                }
                item {
                    Text(
                        text = "Recipes",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(start = 15.dp, bottom = 10.dp)
                    )
                }
//                    Column(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(whiteBlue)
//                            .padding(start = 15.dp, end = 15.dp),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
                if (recipesLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp, start = 15.dp, end = 15.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .shimmerEffect()
                                    .fillMaxWidth(.48f)
                                    .height(250.dp)
                                    .padding(10.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .shimmerEffect()
                                    .fillMaxWidth(.94f)
                                    .height(250.dp)
                                    .padding(10.dp),
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp, start = 15.dp, end = 15.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .shimmerEffect()
                                    .fillMaxWidth(.48f)
                                    .height(250.dp)
                                    .padding(10.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .shimmerEffect()
                                    .fillMaxWidth(.94f)
                                    .height(250.dp)
                                    .padding(10.dp),
                            )
                        }
                    }
                }
                if (recipes != null) {
                            for (i in recipes!!.indices step 2) {
                                val first = recipes!![i]
                                val second = if (i + 1 < recipes!!.size) recipes!![i + 1] else null  // Handle odd list size
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp, start = 15.dp, end = 15.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(Color.White)
                                                .clickable {
                                                    navController.navigate("recipe/${first.id}")
                                                }
                                                .fillMaxWidth(.48f)
                                                .height(250.dp)
                                                .padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            AsyncImage(
                                                model = first.imgUrl,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(.7f)
                                                    .padding(bottom = 5.dp)
                                            )
                                            Text(text = first.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = first.duration,
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )

                                        }
                                        if (second != null) {
                                            Column(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(Color.White)
                                                    .clickable {
                                                        navController.navigate("recipe/${second.id}")
                                                    }
                                                    .fillMaxWidth(.94f)
                                                    .height(250.dp)
                                                    .padding(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                AsyncImage(
                                                    model = second.imgUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .fillMaxHeight(.7f)
                                                        .padding(bottom = 5.dp)
                                                )
                                                Text(text = second.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = second.duration,
                                                    color = Color.Gray,
                                                    fontSize = 12.sp
                                                )

                                            }
                                        }
                                    }
                                }
                            }
                }
                if (moreLoading) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
//                    }
            }
        }
    }


    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
}

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    val transition = rememberInfiniteTransition()
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        )
    )

    clip(RoundedCornerShape(10.dp))
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFB8B5B5),
                    Color(0xFF8F8B8B),
                    Color(0xFFB8B5B5),
                ),
                start = Offset(startOffsetX, 0f),
                end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
            )
        )
        .onGloballyPositioned {
            size = it.size
        }
}