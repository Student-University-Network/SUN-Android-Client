package com.sun.sunclient.ui.screens.attendance

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.sunclient.network.repository.AttendanceRepository
import com.sun.sunclient.network.schemas.FacultyReport
import com.sun.sunclient.network.schemas.StudentReport
import com.sun.sunclient.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(private val attendanceRepository: AttendanceRepository) :
    ViewModel() {

    val TAG = "AttendanceViewModel"

    var studentAttendanceReport by mutableStateOf<StudentReport?>(null)
        private set
    var facultyAttendanceReport by mutableStateOf<FacultyReport?>(null)
        private set

    init {
        viewModelScope.launch {
            syncData()
        }
    }

    fun getReport(role: String, courseId: String, batchId: String) {
        viewModelScope.launch {
            attendanceRepository.getAttendanceReport(role, batchId, courseId)
            syncData()
        }
    }

    private fun syncData() {
        studentAttendanceReport = attendanceRepository.studentReport
        facultyAttendanceReport = attendanceRepository.facultyReport
    }
}