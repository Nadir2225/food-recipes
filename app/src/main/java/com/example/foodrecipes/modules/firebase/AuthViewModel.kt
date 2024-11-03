package com.example.foodrecipes.modules.firebase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore

class AuthViewModel: ViewModel() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private var db = Firebase.firestore

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuth()
    }

    fun checkAuth() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated

        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("email and password should not be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "something went wrong")
                }
            }
    }

    fun signup(fname: String, lname: String, email: String, password: String, cpassword: String) {
        if (fname.isEmpty() || lname.isEmpty() || email.isEmpty() || password.isEmpty() || cpassword.isEmpty()) {
            _authState.value = AuthState.Error("all the fields should not be empty")
            return
        } else if (password != cpassword) {
            _authState.value =
                AuthState.Error("confirm password field should be indentical to the password field")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    var doc: DocumentReference? = auth.currentUser?.let {
                        db.collection("users").document(
                            it.uid)
                    }
                    var usr = hashMapOf<String, Any>(
                        "firstName" to fname,
                        "lastName" to lname,
                        "favorites" to listOf<String>()
                    )
                    doc?.set(usr)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _authState.value = AuthState.Authenticated
                        } else {
                            _authState.value =
                                AuthState.Error(task.exception?.message ?: "something went wrong")
                        }
                    }
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "something went wrong")
                }
            }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Authenticated: AuthState()
    object Unauthenticated: AuthState()
    object Loading: AuthState()
    data class Error(var message: String): AuthState()
}