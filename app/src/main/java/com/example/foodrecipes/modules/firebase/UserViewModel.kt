package com.example.foodrecipes.modules.firebase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel: ViewModel() {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser?.uid
    val db = Firebase.firestore

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    init {
        fetchUser()
    }

    private fun fetchUser() {
        if (currentUser != null) {
            db.collection("users").document(currentUser).get().addOnSuccessListener { doc ->
                _user.value = User(
                    id = currentUser,
                    first_name = doc.getString("firstName").toString(),
                    last_name = doc.getString("lastName").toString(),
                    favorites = doc.get("favorites") as List<String>,
                )
            }.addOnFailureListener {
                _user.value = null
            }
        } else {
            _user.value = null
        }
    }

    fun setFavorites(userId: String, favorites: List<String>): Boolean {
        var success = false
        db.collection("users").document(userId).update("favorites", favorites).addOnSuccessListener {
            success = true
        }
        return success
    }

    fun setFavorites(userId: String, recipeId: String, update: String): Boolean {
        var success = false
        viewModelScope.launch {
            try {
                when (update) {
                    "add" -> {
                        db.collection("users").document(userId)
                            .update("favorites", FieldValue.arrayUnion(recipeId)).await()
                    }
                    "delete" -> {
                        db.collection("users").document(userId)
                            .update("favorites", FieldValue.arrayRemove(recipeId)).await()
                    }
                }
                fetchUser() // Refresh user data after update
                success = true
            } catch (e: Exception) {
                // Handle errors here
                e.printStackTrace()
            }
        }
        return success
    }

}

class User(
    var id:String,
    var first_name: String,
    var last_name: String,
    var favorites: List<String>,
)