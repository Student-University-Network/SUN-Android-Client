package com.sun.sunclient.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.sunclient.application.MainViewModel
import com.sun.sunclient.R
import com.sun.sunclient.config.University
import com.sun.sunclient.ui.theme.SUNTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    mainViewModel: MainViewModel = viewModel()
) {
    val focusManager = LocalFocusManager.current
    val usernameState = rememberSaveable { mutableStateOf("") }
    val passwordState = rememberSaveable { mutableStateOf("") }
    val passwordVisible = rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(vertical = 40.dp, horizontal = 28.dp),
            text = University.nickname,
            fontSize = 42.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp))
                .padding(vertical = 28.dp, horizontal = 28.dp)
        ) {
            // heading
            Column {
                Text(
                    "Welcome",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic
                )
                Text(
                    text = "Sign in to continue",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light
                )
            }
            // username input
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = usernameState.value,
                onValueChange = {
                    usernameState.value = it
                },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            // password input
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = passwordState.value,
                onValueChange = {
                    passwordState.value = it
                },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Username") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                        val visibilityIcon =
                            if (passwordVisible.value) painterResource(id = R.drawable.ic_visibility_on)
                            else painterResource(id = R.drawable.ic_visibility_off)
                        val description =
                            if (passwordVisible.value) "Show password" else "Hide password"
                        Icon(painter = visibilityIcon, contentDescription = description)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                visualTransformation =
                if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation()
            )
            Column(horizontalAlignment = Alignment.End) {
                // submit button
                Button(
                    onClick = {
                        // TODO: login on click
                        mainViewModel.logIn()
                    },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("SIGN IN")
                }
                // forgot password
                TextButton(
                    modifier = Modifier
                        .padding(top = 14.dp),
                    onClick = { },
                ) {
                    Text(
                        "Forgot password?",
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Preview(device = "spec:width=411dp,height=720dp,dpi=320", showBackground = true)
@Composable
fun Preview() {
    SUNTheme {
        LoginScreen()
    }
}