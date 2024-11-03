package com.example.foodrecipes.modules.firebase

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.foodrecipes.modules.room.RecipeDao
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.RoomDatabase
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.firebase.firestore.DocumentSnapshot
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class RecipeViewModel(
    private val dao: RecipeDao
): ViewModel() {
    val db = Firebase.firestore

    private var lastVisibleRecipe: DocumentSnapshot? = null

    private val _recipes = MutableLiveData<MutableList<Recipe>>()
    val recipes: LiveData<MutableList<Recipe>> get() = _recipes

    private val _downloads = MutableLiveData<List<com.example.foodrecipes.modules.room.Recipe>>()
    val downloads: LiveData<List<com.example.foodrecipes.modules.room.Recipe>> get() = _downloads

    private val _download = MutableLiveData<com.example.foodrecipes.modules.room.Recipe>()
    val download: LiveData<com.example.foodrecipes.modules.room.Recipe> get() = _download

    private val _favRecipes = MutableLiveData<MutableList<Recipe>>()
    val favRecipes: LiveData<MutableList<Recipe>> get() = _favRecipes

    private val _recipe = MutableLiveData<Recipe?>()
    val recipe: LiveData<Recipe?> get() = _recipe

    init {
        _recipes.value = mutableListOf()
        fetchRecipes (
            onSuccess = { },
            onFailure = { }
        )
    }

    fun setRecipes(list: MutableList<Recipe>) {
        _recipes.value = list
    }

    @OptIn(UnstableApi::class)
    fun fetchRecipes(
        categories: MutableList<String> = mutableListOf(),
        loadMore: Boolean = false,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        if (!loadMore) {
            var query = if (categories.isNotEmpty()) {
                db.collection("recipes").whereIn("category", categories).orderBy("title").limit(10L)
            } else {
                db.collection("recipes").orderBy("title").limit(10L)
            }
            query.get()
                .addOnSuccessListener { docs ->
                    onSuccess()
                    val newRecipes = mutableListOf<Recipe>()
                    for (doc in docs) {
                        val recipe = Recipe(
                            id = doc.id,
                            title = doc.data["title"].toString(),
                            duration = doc.data["duration"].toString(),
                            imgUrl = doc.data["imgUrl"].toString()
                        )
                        if (!newRecipes.contains(recipe)) {
                            newRecipes.add(recipe)
                        }
                    }
                    _recipes.value = newRecipes
                }
                .addOnFailureListener {
                    onFailure()
                }
        } else {
            var lastVisibleRecipeTask = _recipes.value?.lastOrNull()?.let { doc ->
                db.collection("recipes").document(doc.id).get()
            }
            lastVisibleRecipeTask?.addOnCompleteListener { task ->
                lastVisibleRecipe = task.result
                val query = if (categories.isNotEmpty()) {
                    db.collection("recipes").whereIn("category", categories).orderBy("title").startAfter(lastVisibleRecipe?.getString("title")).limit(10L)
                } else {
                    db.collection("recipes").orderBy("title").startAfter(lastVisibleRecipe?.getString("title")).limit(10L)
                }
                query.get()
                    .addOnSuccessListener { docs ->
                        onSuccess()
                        val updatedRecipes = _recipes.value.orEmpty().toMutableList()
                        val newRecipes = mutableListOf<Recipe>()
                        for (doc in docs) {
                            val recipe = Recipe(
                                id = doc.id,
                                title = doc.data["title"].toString(),
                                duration = doc.data["duration"].toString(),
                                imgUrl = doc.data["imgUrl"].toString()
                            )
                            if (!newRecipes.contains(recipe) && !_recipes.value?.contains(recipe)!!) {
                                newRecipes.add(recipe)
                            }
                        }

                        updatedRecipes.addAll(newRecipes)
                        _recipes.value = updatedRecipes
                    }
                    .addOnFailureListener {
                        onFailure()
                    }
            }
        }
    }

    fun fetchFavRecipes(favorites: List<String>, onComplete: () -> Unit) {
        _favRecipes.value = mutableListOf<Recipe>()
        db.collection("recipes").get()
            .addOnSuccessListener { docs ->
                onComplete()
                val newRecipes = mutableListOf<Recipe>()
                for (doc in docs) {
                    if (favorites.contains(doc.id)) {
                        val recipe = Recipe(
                            id = doc.id,
                            title = doc.data["title"].toString(),
                            duration = doc.data["duration"].toString(),
                            imgUrl = doc.data["imgUrl"].toString()
                        )
                        if (!newRecipes.contains(recipe)) {
                            newRecipes.add(recipe)
                        }
                    }
                }
                _favRecipes.value = newRecipes
            }
            .addOnFailureListener {
                onComplete()
            }
    }

    @OptIn(UnstableApi::class)
    fun fetchRecipe(id: String) {
        _recipe.value = Recipe(loading = true)
        db.collection("recipes")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                val recipe = Recipe(
                    id = id,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    duration = doc.getString("duration") ?: "",
                    imgUrl = doc.getString("imgUrl") ?: "",
                    videoUrl = doc.getString("videoUrl") ?: "",
                    ingredients = (doc.get("ingredients") as? List<String>)?:emptyList(),
                    instructions = (doc.get("instructions") as? List<String>)?: emptyList()
                )
                _recipe.value = recipe
            }
            .addOnFailureListener { exception ->
                Log.e("RecipeViewModel", "Error fetching recipe: ", exception)
                _recipe.value = null // Clear the recipe on failure or handle it accordingly
            }
    }

    private suspend fun getBitmap(context: Context, imageUrl: String): Bitmap? {
        val loading = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()
        val result = (loading.execute(request) as SuccessResult).drawable
        return (result as BitmapDrawable).bitmap
    }

    @OptIn(UnstableApi::class)
    fun downloadRecipe(context: Context, recipeId: String, onComplete: () -> Unit) {
        val storage = Firebase.storage
        db.collection("recipes")
            .document(recipeId)
            .get()
            .addOnSuccessListener { doc ->
                val imgUrl = doc.getString("imgUrl").orEmpty()

                // Download media files
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val imgPath = getBitmap(context, imgUrl)
                        val recipe = com.example.foodrecipes.modules.room.Recipe(
                            id = doc.id,
                            title = doc.getString("title").orEmpty(),
                            description = doc.getString("description").orEmpty(),
                            duration = doc.getString("duration").orEmpty(),
                            imgUrl = imgPath,
                            videoUrl = doc.getString("videoUrl").orEmpty(),
                            ingredients = (doc.get("ingredients") as? List<String>)?:emptyList(),
                            instructions = (doc.get("instructions") as? List<String>)?: emptyList()
                        )
                        // Save to Room database
                        dao.upsertRecipe(recipe)
                        _downloads.postValue(dao.getRecipes())
                        onComplete()
                    } catch (e: Exception) {
                        // Handle exceptions
                        Log.w("nadir", "$e")
                        onComplete()
                    }
                }
            }
    }

    fun getDownloads() {
        CoroutineScope(Dispatchers.IO).launch {
            _downloads.postValue(dao.getRecipes())
        }
    }

    fun getDownload(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _download.postValue(dao.getRecipe(id))
        }
    }

    fun deleteRecipe(recipeId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(recipeId)
            _downloads.postValue(dao.getRecipes())
        }
    }

    fun deleteRecipes() {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete()
            _downloads.postValue(dao.getRecipes())
        }
    }


//    @OptIn(UnstableApi::class)
//    fun uploadData() {
//        val recipes = listOf()
//
//        var i = 1
//        for (recipe in recipes) {
//            db.collection("recipes")
//                .add(recipe) // Use .add() for auto-generated IDs
//                .addOnSuccessListener { documentReference ->
//                    Log.w("nadir", "Recipe no ${i++} added")
//                }
//                .addOnFailureListener { e ->
//                    Log.w("nadir", "Error adding recipe", e)
//                }
//        }
//    }

}

data class Recipe(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var duration: String = "",
    var imgUrl: String = "",
    var videoUrl: String = "",
    var ingredients: List<String> = listOf<String>(),
    var instructions: List<String> = listOf<String>(),
    var loading: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Recipe) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}