package com.example.foodrecipes.modules.room

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Recipe(
    @PrimaryKey(autoGenerate = false)
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var duration: String = "",
    var imgUrl: Bitmap? = null,
    var videoUrl: String = "",
    var ingredients: List<String> = listOf<String>(),
    var instructions: List<String> = listOf<String>(),
    var loading: Boolean = false
)