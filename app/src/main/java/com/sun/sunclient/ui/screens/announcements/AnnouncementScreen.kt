package com.sun.sunclient.ui.screens.announcements

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sun.sunclient.MainViewModel
import com.sun.sunclient.R
import com.sun.sunclient.ui.screens.profile.ProfileEvent
import com.sun.sunclient.ui.shared.DatePickerField
import com.sun.sunclient.ui.shared.DropDownField
import com.sun.sunclient.ui.shared.InputField
import com.sun.sunclient.utils.Constants

@Composable
fun AnnouncementScreen(mainViewModel: MainViewModel) {

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var programId by remember { mutableStateOf<String?>(null) }
    var programName by remember { mutableStateOf<String?>(null) }
    var isGlobal by remember { mutableStateOf<String>("FOR_ALL") }

    val role = mainViewModel.userData.role
    val announcementViewModel: AnnouncementViewModel = hiltViewModel()
    val announcementList = announcementViewModel.announcementsList
    val programList = announcementViewModel.programList

    LaunchedEffect(key1 = isGlobal) {
        if (isGlobal == "FOR_PROGRAM") {
            announcementViewModel.getProgramList()
        }
    }

    fun reset() {
        title = ""
        content = ""
        programId = null
        programName = null
        isGlobal = "FOR_ALL"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (role == Constants.FACULTY) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(28.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            InputField(
                                label = "Title", value = title,
                                onValueChange = {
                                    title = it
                                }
                            )
                            InputField(
                                label = "Content", value = content,
                                onValueChange = {
                                    content = it
                                },
                            )
                            DropDownField(
                                value = isGlobal,
                                options = listOf("FOR_ALL", "FOR_PROGRAM"),
                                label = "Announce To: ",
                                onValueChange = {
                                    isGlobal = it
                                },
                            )
                            if (isGlobal == "FOR_PROGRAM") {
                                DropDownField(
                                    value = "",
                                    options = programList.map { p -> p.programName },
                                    label = "Announce To: ",
                                    onValueChange = {
                                        val program =
                                            programList.find { p -> p.programName == it }
                                        program?.let { p ->
                                            programId = p.programId
                                            programName = it
                                        }
                                    },
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(onClick = {
                                announcementViewModel.postAnnouncement(
                                    title,
                                    content,
                                    programId
                                ) {
                                    reset()
                                }
                            }) {
                                Text("Post")
                            }
                            OutlinedButton(onClick = {
                                reset()
                            }) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }
        }
        item {
            Text(
                "Announcements",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Left
            )
        }
        item { Divider(modifier = Modifier.padding(8.dp)) }
        if (announcementList.isEmpty()) {
            item {
                Text(
                    "No announcements present",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        itemsIndexed(announcementList) { index, it ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(18.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        it.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .padding(start = 8.dp)
                            .padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_profile),
                            contentDescription = "User",
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurface)
                        )
                        Text(it.announcer)
                    }
                    Divider(modifier = Modifier.padding(4.dp))
                    Text(it.content)
                }
            }
        }
    }
}
