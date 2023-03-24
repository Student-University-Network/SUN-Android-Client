package com.sun.sunclient.ui.shared

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sun.sunclient.R
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    context: Context,
    label: String = "",
    value: String = "",
    onValueChanged: (Date?) -> Unit,
    readOnly: Boolean = false
) {
    var selectedDate by remember { mutableStateOf(value) }
    val mCalendar = Calendar.getInstance()
    val mDatePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            selectedDate = "$day/${month + 1}/$year"
            onValueChanged(SimpleDateFormat("yyyy/MM/dd").parse("$year/${month + 1}/$day"))
        },
        mCalendar.get(Calendar.YEAR),
        mCalendar.get(Calendar.MONTH),
        mCalendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
        )
        TextField(
            value = selectedDate,
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calender),
                    contentDescription = "Pick date",
                    modifier = Modifier.clickable {
                        if (!readOnly)mDatePickerDialog.show()
                    }
                )
            },
            readOnly = true
        )
    }
}