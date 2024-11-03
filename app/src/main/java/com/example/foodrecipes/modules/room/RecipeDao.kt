package com.example.foodrecipes.modules.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RecipeDao {
    @Upsert
    suspend fun upsertRecipe(recipe: Recipe)

    @Query("DELETE FROM Recipe")
    suspend fun delete()

    @Query("DELETE FROM Recipe where id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * from Recipe")
    fun getRecipes(): List<Recipe>

    @Query("SELECT * from Recipe where id = :id")
    fun getRecipe(id: String): Recipe
}