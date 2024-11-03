package com.example.foodrecipes.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.foodrecipes.modules.firebase.AuthState
import com.example.foodrecipes.modules.firebase.AuthViewModel
import com.example.foodrecipes.R

@Composable
fun Signup(modifier: Modifier, navController: NavController, authViewModel: ViewModel) {
    var email by remember {
        mutableStateOf("")
    }
    var fName by remember {
        mutableStateOf("")
    }
    var lName by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var Cpassword by remember {
        mutableStateOf("")
    }
    var height = Size().height()

    var context = LocalContext.current

    val authState = (authViewModel as AuthViewModel).authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate("home") { popUpTo(0) }
            is AuthState.Error -> Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(R.drawable.background2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = (height * 0.15).dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(painter = painterResource(id = R.drawable.icon),
                contentDescription = null,
                modifier = Modifier
                    .width(100.dp)
                    .padding(bottom = 10.dp))
            OutlinedTextField(
                value = fName,
                onValueChange = {fName = it.trim()},
                label = { Text(text = "first name") }
            )
            OutlinedTextField(
                value = lName,
                onValueChange = {lName = it.trimStart()},
                label = { Text(text = "last name") }
            )
            OutlinedTextField(
                value = email,
                onValueChange = {email = it.trimStart()},
                label = { Text(text = "Email") }
            )
            OutlinedTextField(value = password,
                onValueChange = {password = it.trimStart()},
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text(text = "Password") }
            )
            OutlinedTextField(value = Cpassword,
                onValueChange = {Cpassword = it.trimStart()},
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text(text = "Confirm password") }
            )
            Button(modifier = Modifier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                ),
                enabled = authState.value != AuthState.Loading,
                onClick = {
                    authViewModel.signup(fName, lName, email, password, Cpassword)
                }) {
                Text("Sign up")
            }
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "you already have an account yet? ")
                Text(text = "Log in",
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
    }

}