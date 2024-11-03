package com.example.foodrecipes.pages

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.foodrecipes.R
import com.example.foodrecipes.modules.firebase.RecipeViewModel
import com.example.foodrecipes.modules.firebase.UserViewModel
import com.example.foodrecipes.ui.theme.whiteBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Downloads(
    modifier: Modifier,
    navController: NavController,
    recipeViewModel: ViewModel
) {
    val downloads by (recipeViewModel as RecipeViewModel).downloads.observeAsState()

    LaunchedEffect(Unit) {
        (recipeViewModel as RecipeViewModel).getDownloads()
    }

    var recipesLoading by remember { mutableStateOf(false) }
    var emptyornull by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Downloads", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = whiteBlue,
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("home") {
                            popUpTo(0)
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = null,
                            modifier = Modifier.height(25.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(whiteBlue)
                .padding(paddingValues)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(whiteBlue)
                        .padding(start = 15.dp, end = 15.dp),
                    verticalArrangement = if (emptyornull) Arrangement.Center else Arrangement.spacedBy(16.dp),
                    horizontalAlignment = if (emptyornull) Alignment.CenterHorizontally else Alignment.Start
                ) {
                    if (recipesLoading) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
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
                                .fillMaxWidth(),
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
                    } else {
                        if (!downloads.isNullOrEmpty()) {
                            for (i in downloads!!.indices step 2) {
                                val first = downloads!![i]
                                val second = if (i + 1 < downloads!!.size) downloads!![i + 1] else null  // Handle odd list size
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White)
                                            .clickable {
                                                navController.navigate("download/${first.id}")
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
                                                    navController.navigate("download/${second.id}")
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
                        } else {
                            emptyornull = true
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "there is no downloaded recipes yet")
                            }
                        }
                    }
                }
            }
        }
    }
}