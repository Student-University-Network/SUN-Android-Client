package com.sun.sunclient.ui.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import java.util.*

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DropDownField(
    value: String,
    label: String = "",
    options: List<String>,
    onValueChange: (value: String) -> Unit,
    readOnly: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var stateValue by remember { mutableStateOf(value) }

    val icon = if (expanded)
        Icons.Filled.Close
    else
        Icons.Filled.ArrowDropDown

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
        )
        Box {
            TextField(
                value = stateValue,
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { layoutCoordinates ->
                        textFieldSize = layoutCoordinates.size.toSize()
                    },
                trailingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Toggle",
                        modifier = Modifier.clickable {
                            if (!readOnly) expanded = !expanded
                        })
                },
                readOnly = true
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
            ) {
                options.forEachIndexed { index, _gender ->
                    DropdownMenuItem(
                        text = { Text(_gender) },
                        onClick = {
                            stateValue = options[index]
                            onValueChange(options[index])
                            expanded = false
                        })
                }
            }
        }
    }
}