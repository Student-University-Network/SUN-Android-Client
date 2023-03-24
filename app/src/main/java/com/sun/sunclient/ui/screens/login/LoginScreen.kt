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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sun.sunclient.R
import com.sun.sunclient.config.Config

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    setLoggedIn: () -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val loginViewModel: LoginViewModel = hiltViewModel()
    val state = loginViewModel.state

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(vertical = 40.dp, horizontal = 28.dp),
            text = Config.University.nickname,
            fontSize = 42.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp))
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
                    value = state.username,
                    onValueChange = { changedUsername ->
                        loginViewModel.onEvent(LoginEvent.OnUsernameChange(changedUsername))
                    },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Username") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                // password input
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.password,
                    onValueChange = { changedPassword ->
                        loginViewModel.onEvent(LoginEvent.OnPasswordChange(changedPassword))
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Username") },
                    trailingIcon = {
                        IconButton(onClick = { loginViewModel.onEvent(LoginEvent.TogglePasswordVisibility) }) {
                            val visibilityIcon =
                                if (state.isPasswordVisible) painterResource(id = R.drawable.ic_visibility_on)
                                else painterResource(id = R.drawable.ic_visibility_off)
                            val description =
                                if (state.isPasswordVisible) "Show password" else "Hide password"
                            Icon(painter = visibilityIcon, contentDescription = description)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    visualTransformation =
                    if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                )
                Column(horizontalAlignment = Alignment.End) {
                    // submit button
                    Button(
                        onClick = {
                            if (!state.isLoading) {
                                loginViewModel.onEvent(LoginEvent.Login)
                            }
                        },
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Text("SIGN IN")
                        }
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
}