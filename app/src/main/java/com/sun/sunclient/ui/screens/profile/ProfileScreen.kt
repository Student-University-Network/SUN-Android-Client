package com.sun.sunclient.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sun.sunclient.MainViewModel
import com.sun.sunclient.R
import com.sun.sunclient.ui.shared.DatePickerField
import com.sun.sunclient.ui.shared.DropDownField
import com.sun.sunclient.ui.shared.InputField
import java.text.SimpleDateFormat

@Composable
fun ProfileScreen(mainViewModel: MainViewModel) {
    val context = LocalContext.current

    val profileViewModel: ProfileViewModel = hiltViewModel()
    val state = profileViewModel.profileState

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = "User Profile",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(100.dp)
                )
            }
            item {
                Text(
                    text = "${profileViewModel.profileState.firstName} ${profileViewModel.profileState.lastName}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier
                    .height(20.dp)
                    .padding(horizontal = 6.dp))
            }
            // Personal Details
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 50.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(vertical = 10.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                "Personal Details",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            AnimatedVisibility(
                                visible = !state.isEditingPersonalDetails,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Button(onClick = {
                                    profileViewModel.onEvent(ProfileEvent.EditPersonalDetails)
                                }) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_pencil),
                                            contentDescription = "Edit",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text("Edit")
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(28.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            InputField(
                                label = "First Name", value = state.firstName,
                                enabled = state.isEditingPersonalDetails,
                                onValueChange = {
                                    profileViewModel.onEvent(
                                        ProfileEvent.OnPersonalDetailsChange(
                                            state.copy(firstName = it)
                                        )
                                    )
                                },
                            )
                            InputField(
                                label = "Middle Name", value = state.middleName ?: "",
                                enabled = state.isEditingPersonalDetails,
                                onValueChange = {
                                    profileViewModel.onEvent(
                                        ProfileEvent.OnPersonalDetailsChange(
                                            state.copy(middleName = it)
                                        )
                                    )
                                },
                            )
                            InputField(
                                label = "Last Name", value = state.lastName,
                                enabled = state.isEditingPersonalDetails,
                                onValueChange = {
                                    profileViewModel.onEvent(
                                        ProfileEvent.OnPersonalDetailsChange(
                                            state.copy(lastName = it)
                                        )
                                    )
                                },
                            )
                            DropDownField(
                                value = state.gender ?: "None",
                                options = profileViewModel.genderOptions,
                                label = "Gender",
                                onValueChange = {
                                    profileViewModel.onEvent(
                                        ProfileEvent.OnPersonalDetailsChange(
                                            state.copy(gender = if (it == "None") null else it)
                                        )
                                    )
                                },
                                readOnly = !state.isEditingPersonalDetails
                            )
                            DatePickerField(
                                context = context,
                                label = "Date of Birth",
                                value = if (state.dateOfBirth != null)
                                    SimpleDateFormat("yyyy/MM/dd").format(state.dateOfBirth)
                                else "",
                                onValueChanged = {
                                    profileViewModel.onEvent(
                                        ProfileEvent.OnPersonalDetailsChange(
                                            state.copy(dateOfBirth = it)
                                        )
                                    )
                                },
                                readOnly = !state.isEditingPersonalDetails,
                            )
                        }
                        AnimatedVisibility(
                            visible = state.isEditingPersonalDetails,
                            enter = slideInVertically(initialOffsetY = { fullHeight -> -fullHeight / 2 }),
                            exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight / 2 })
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(onClick = {
                                    profileViewModel.onEvent(ProfileEvent.UpdatePersonalDetails)
                                }) {
                                    Text("Update")
                                }
                                Button(onClick = {
                                    profileViewModel.onEvent(ProfileEvent.ClosePersonalDetailsUpdate)
                                }) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
            }
            // Account details
            item {
                Card(
                    modifier = Modifier.padding(bottom = 28.dp).fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(vertical = 10.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                "Account Details",
                                fontSize = 22.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(28.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Change Password of your account: ", fontSize = 16.sp,)

                                OutlinedButton(onClick = { profileViewModel.onEvent(ProfileEvent.ShowChangePassword) }) {
                                    Text("Change Password")
                                }
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Logout from your account: ", fontSize = 16.sp,)
                                OutlinedButton(onClick = { mainViewModel.logout() }) {
                                    Text("Logout")
                                }
                            }
                        }
                    }
                }
            }
        }
        AnimatedVisibility(visible = state.isChangePasswordVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
            exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight })
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .shadow(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(6.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 18.dp, horizontal = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(28.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 14.dp)
                        ) {
                            Text("Change Password", fontSize = 20.sp)
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                modifier = Modifier
                                    .padding()
                                    .clickable {
                                        profileViewModel.onEvent(ProfileEvent.CloseChangePassword)
                                    })
                        }
                        InputField(
                            label = "Current Password",
                            value = state.currentPassword,
                            type = "password",
                            onValueChange = {
                                profileViewModel.onEvent(
                                    ProfileEvent.OnPersonalDetailsChange(
                                        state.copy(currentPassword = it)
                                    )
                                )
                            }
                        )
                        InputField(
                            label = "New Password",
                            value = state.newPassword,
                            onValueChange = {
                                profileViewModel.onEvent(
                                    ProfileEvent.OnPersonalDetailsChange(
                                        state.copy(newPassword = it)
                                    )
                                )
                            }
                        )
                        InputField(
                            label = "Confirm Password",
                            value = state.confirmPassword,
                            onValueChange = {
                                profileViewModel.onEvent(
                                    ProfileEvent.OnPersonalDetailsChange(
                                        state.copy(confirmPassword = it)
                                    )
                                )
                            }
                        )
                        Box(modifier = Modifier.padding(vertical = 8.dp)) {
                            Button(onClick = { profileViewModel.onEvent(ProfileEvent.ChangePassword) }) {
                                Text("Change Password")
                            }
                        }
                    }
                }
            }
        }
    }
}