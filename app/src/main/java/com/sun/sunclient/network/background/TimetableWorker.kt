package com.sun.sunclient.network.background

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.sun.sunclient.R
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.work.*
import com.sun.sunclient.network.schemas.LectureStatus
import com.sun.sunclient.network.schemas.Timetable
import com.sun.sunclient.ui.screens.timetable.getLectureTimeString
import com.sun.sunclient.utils.Constants
import java.util.*
import java.util.concurrent.TimeUnit

class CancelAllWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val context = applicationContext
        Log.d("CancelAllWorker", "doWork: Cancel all ")
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(Constants.REMINDER_NOTIFICATION_CHANNEL)
        } else {
            notificationManager.cancelAll()
        }
        return Result.success()
    }
}

class TimetableWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext
        val courseName = inputData.getString(Constants.DATA_COURSE_NAME_KEY) ?: "Unknown"
        val professorName = inputData.getString(Constants.DATA_PROFESSOR_NAME_KEY) ?: "Unknown"
        val room = inputData.getString(Constants.DATA_ROOM_KEY) ?: "Unknown"
        val status = inputData.getString(Constants.DATA_STATUS_KEY) ?: "Unknown"
        val role = inputData.getString(Constants.DATA_ROLE_KEY) ?: Constants.STUDENT
        val timeText = inputData.getString(Constants.DATA_TIME_TEXT_KEY) ?: "Unknown"
        val titleText = inputData.getString(Constants.DATA_TITLE_TEXT_KEY) ?: "Lecture:"
        val courseId = inputData.getString(Constants.DATA_COURSE_ID_KEY) ?: ""
        val lectureId = inputData.getString(Constants.DATA_LECTURE_ID_KEY) ?: ""
        val startLectureDelay = inputData.getLong(Constants.DATA_START_DELAY_KEY, 0)
        val attendanceDelay = inputData.getLong(Constants.DATA_ATTEND_DELAY_KEY, 0)
        Log.d(
            TAG,
            "Notification: course:$courseName, professor:$professorName, room:$room, status:$status"
        )

        // build notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM).build()
            val notificationChannel = NotificationChannel(
                Constants.REMINDER_NOTIFICATION_CHANNEL,
                "Timetable notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "Here notifications related to timetable are posted"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                audioAttributes
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, Constants.REMINDER_NOTIFICATION_CHANNEL)
        } else {
            Notification.Builder(context)
        }

        val notificationSmallLayout =
            RemoteViews(context.packageName, R.layout.lecture_notification_small_layout)
        notificationSmallLayout.setTextViewText(R.id.course_name, courseName)
        notificationSmallLayout.setTextViewText(R.id.time_text, timeText)
        notificationSmallLayout.setTextViewText(R.id.room_text, room)
        notificationSmallLayout.setTextViewText(R.id.title_text, titleText)
        val notificationBigLayout =
            RemoteViews(context.packageName, R.layout.lecture_notification_big_layout)
        notificationBigLayout.setTextViewText(R.id.course_name, courseName)
        notificationBigLayout.setTextViewText(R.id.time_text, timeText)
        notificationBigLayout.setTextViewText(R.id.room_text, room)
        notificationBigLayout.setTextViewText(R.id.professor_text, professorName)
        notificationBigLayout.setTextViewText(R.id.title_text, titleText)

        if (status == LectureStatus.CANCELLED.toString()) {
            notificationSmallLayout.setTextViewText(R.id.title_text, "Lecture was cancelled")
            notificationBigLayout.setTextViewText(R.id.title_text, "Lecture was cancelled")
        }

        val notification = builder
            .setContentTitle(courseName)
            .setContentText("Upcoming Lecture !!")
            .setAutoCancel(false)
            .setSmallIcon(R.drawable.ic_calender)
            .setCustomContentView(notificationSmallLayout)
            .setCustomBigContentView(notificationBigLayout)
            .setOngoing(true)
            .build()

        notificationManager.notify(Constants.REMINDER_NOTIFICATION_CHANNEL_ID, notification);

        // temporary solution
        if (titleText == "Upcoming lecture..") {
            setCurrentLectureWork(
                context = context,
                courseName = courseName,
                professorName = professorName,
                room = room,
                timeText = timeText,
                status = status,
                startLectureDelay = startLectureDelay,
                role = role,
                titleText = "Current lecture:",
                lectureId = lectureId,
                courseId = courseId,
                attendanceDelay = attendanceDelay
            )
        } else { // if its current lecture
            AttendanceWorker.scheduleAttendance(context, courseId, lectureId, role, attendanceDelay)
        }
        return Result.success()
    }

    companion object {
        val TAG = "TimetableWorker"

        fun scheduleCurrentDayTimetable(context: Context, timetable: Timetable) {
            val todayDate = Calendar.getInstance()// select todays week day
            val weekDay = todayDate.get(Calendar.DAY_OF_WEEK) - 1 // week day in index of 0
            val todaysLectures = timetable.days.find { d -> d.weekDay == weekDay }

            // remove old work requests
            WorkManager.getInstance(context).cancelAllWorkByTag(Constants.UPCOMING_LECTURE_TAG)
            WorkManager.getInstance(context).cancelAllWorkByTag(Constants.CURRENT_LECTURE_TAG)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


            todaysLectures?.let { day ->
                var lastLectureDelay = todayDate.timeInMillis
                if (day.lectures.isEmpty()) return
                var lastLecture = day.lectures.first()
                val currentTimeMillis = System.currentTimeMillis()
                day.lectures.forEach {
                    val endTime = Calendar.getInstance().apply {
                        this.set(Calendar.HOUR_OF_DAY, it.endTime.hour)
                        this.set(Calendar.MINUTE, it.endTime.minute)
                        this.set(Calendar.SECOND, 0)
                    }
                    val endDelay = endTime.timeInMillis - currentTimeMillis
                    if (endDelay >= lastLectureDelay) {
                        lastLectureDelay = endDelay
                        lastLecture = it
                    }
                }
                Log.d(
                    DailySchedulerWorker.TAG,
                    "doWork: lastLecture $lastLecture, delay: $lastLectureDelay"
                )
                val workRequest = OneTimeWorkRequestBuilder<CancelAllWorker>()
                    .setInitialDelay(lastLectureDelay, TimeUnit.MILLISECONDS)
                    .addTag("CANCEL_ALL_WORK")
                    .build()
                WorkManager.getInstance(context).enqueue(workRequest)
            }

            Log.d(TAG, "scheduleCurrentDayTimetable: Cancel all ")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.deleteNotificationChannel(Constants.REMINDER_NOTIFICATION_CHANNEL)
            } else {
                notificationManager.cancelAll()
            }

            // calculate all delays
            todaysLectures?.let {
                for (lecture in todaysLectures.lectures) {
                    val currentTimeMillis = System.currentTimeMillis()
                    val scheduledTime = Calendar.getInstance().apply {
                        this.set(Calendar.HOUR_OF_DAY, lecture.startTime.hour)
                        this.set(Calendar.MINUTE, lecture.startTime.minute)
                        this.set(Calendar.SECOND, 0)
                    }
                    val endTime = Calendar.getInstance().apply {
                        this.set(Calendar.HOUR_OF_DAY, lecture.endTime.hour)
                        this.set(Calendar.MINUTE, lecture.endTime.minute)
                        this.set(Calendar.SECOND, 0)
                    }

                    var startDelay = scheduledTime.timeInMillis - currentTimeMillis
                    scheduledTime.add(Calendar.MINUTE, -10) // 10 minutes before
                    var reminderDelay = scheduledTime.timeInMillis - currentTimeMillis
                    val endDelay = endTime.timeInMillis - currentTimeMillis
                    endTime.add(Calendar.MINUTE, -10)
                    var attendanceDelay = endTime.timeInMillis - currentTimeMillis

                    if (reminderDelay >= 0) {
                        // if its more than 10 mins before the lecture
                        // do nothing
                    } else if (startDelay >= 0) {
                        // if its less than 10 min before lecture but not ongoing lecture
                        reminderDelay = 0 // start reminder immediately
                    } else if (attendanceDelay >= 0) {
                        // If lecture is already ongoing
                        reminderDelay = 0 // start reminder immediately
                        startDelay = 0 // start current lecture immediately
                    } else if (endDelay >= 0) {
                        // If lecture is already ongoing and its time for attendance
                        reminderDelay = 0 // start reminder immediately
                        startDelay = 0 // start current lecture immediately
                        attendanceDelay = 0 // start attendance immediately
                    }
                    val role = if (lecture.batchId != null) Constants.FACULTY else Constants.STUDENT

                    // set new work request
                    if (reminderDelay >= 0) {
                        val data = Data.Builder()
                            .putString(Constants.DATA_COURSE_NAME_KEY, lecture.courseName)
                            .putString(Constants.DATA_PROFESSOR_NAME_KEY, lecture.professorName)
                            .putString(Constants.DATA_ROOM_KEY, lecture.room)
                            .putString(Constants.DATA_STATUS_KEY, lecture.status.toString())
                            .putString(
                                Constants.DATA_TIME_TEXT_KEY,
                                getLectureTimeString(lecture.startTime, lecture.endTime)
                            )
                            .putString(Constants.DATA_TITLE_TEXT_KEY, "Upcoming lecture..")
                            .putString(Constants.DATA_ROLE_KEY, role)
                            .putString(Constants.DATA_LECTURE_ID_KEY, lecture.id)
                            .putString(Constants.DATA_COURSE_ID_KEY, lecture.courseId)
                            .putLong(Constants.DATA_START_DELAY_KEY, startDelay)
                            .putLong(Constants.DATA_ATTEND_DELAY_KEY, attendanceDelay)
                            .build()
                        val workRequest = OneTimeWorkRequestBuilder<TimetableWorker>()
                            .setInitialDelay(reminderDelay, TimeUnit.MILLISECONDS)
                            .addTag(Constants.UPCOMING_LECTURE_TAG)
                            .setInputData(data)
                            .build()
                        WorkManager.getInstance(context).enqueue(workRequest)
                        Log.d(TAG, "scheduleCurrentDayTimetable: Scheduled: $lecture")
                    }
                }
            }
        }

        fun setCurrentLectureWork(
            context: Context,
            courseName: String,
            professorName: String,
            room: String,
            timeText: String,
            status: String,
            role: String,
            startLectureDelay: Long,
            attendanceDelay: Long,
            titleText: String,
            lectureId: String,
            courseId: String
        ) {
            WorkManager.getInstance(context).cancelAllWorkByTag(Constants.CURRENT_LECTURE_TAG)
            val data = Data.Builder()
                .putString(Constants.DATA_COURSE_NAME_KEY, courseName)
                .putString(Constants.DATA_PROFESSOR_NAME_KEY, professorName)
                .putString(Constants.DATA_ROOM_KEY, room)
                .putString(Constants.DATA_STATUS_KEY, status)
                .putString(Constants.DATA_TIME_TEXT_KEY, timeText)
                .putString(Constants.DATA_TITLE_TEXT_KEY, titleText)
                .putString(Constants.DATA_ROLE_KEY, role)
                .putString(Constants.DATA_LECTURE_ID_KEY, lectureId)
                .putString(Constants.DATA_COURSE_ID_KEY, courseId)
                .putLong(Constants.DATA_ATTEND_DELAY_KEY, attendanceDelay)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<TimetableWorker>()
                .setInitialDelay(startLectureDelay, TimeUnit.MILLISECONDS)
                .addTag(Constants.CURRENT_LECTURE_TAG)
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d(TAG, "setCurrentLectureWork: Scheduled: $courseName time: $startLectureDelay")
        }
    }
}


