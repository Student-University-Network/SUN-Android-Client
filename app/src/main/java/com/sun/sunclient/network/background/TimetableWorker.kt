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
import kotlin.collections.ArrayList

class TimetableWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext
        val courseName = inputData.getString(Constants.DATA_COURSE_NAME_KEY) ?: "Unknown"
        val professorName = inputData.getString(Constants.DATA_PROFESSOR_NAME_KEY) ?: "Unknown"
        val room = inputData.getString(Constants.DATA_ROOM_KEY) ?: "Unknown"
        val status = inputData.getString(Constants.DATA_STATUS_KEY) ?: "Unknown"
        val timeText = inputData.getString(Constants.DATA_TIME_TEXT_KEY) ?: "Unknown"
        val titleText = inputData.getString(Constants.DATA_TITLE_TEXT_KEY) ?: "Unknown"
        val startLectureDelay = inputData.getLong(Constants.DATA_START_DELAY_KEY, 0)
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
                Constants.NOTIFICATION_CHANNEL,
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
            Notification.Builder(context, Constants.NOTIFICATION_CHANNEL)
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
                context,
                courseName,
                professorName,
                room,
                timeText,
                status,
                startLectureDelay,
                "Current lecture:"
            )
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
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            Log.d(TAG, "scheduleCurrentDayTimetable: Cancel all ")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.deleteNotificationChannel(Constants.NOTIFICATION_CHANNEL)
            } else {
                notificationManager.cancelAll()
            }

            // calculate all delays
            val lecturesList = ArrayList<LectureSchedule>()
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

                    if (reminderDelay >= 0) {
                        // if its more than 10 mins before the lecture
                        // do nothing
                    } else if (startDelay >= 0) {
                        // if its less than 10 min before lecture but not ongoing lecture
                        reminderDelay = 0 // start reminder immediately
                    } else if (endDelay >= 0 ) {
                        // If lecture is already ongoing
                        reminderDelay = 0 // start reminder immediately
                        startDelay = 0 // start current lecture immediately
                    }

                    lecturesList.add(
                        LectureSchedule(
                            lecture.courseName,
                            lecture.professorName,
                            lecture.room,
                            lecture.status,
                            getLectureTimeString(lecture.startTime, lecture.endTime),
                            remindLectureDelay = reminderDelay,
                            startLectureDelay = startDelay,
                            endLectureDelay = endDelay
                        )
                    )
                }
            }
            // set new work request
            val constraints = Constraints.Builder().build()
            for (lecture in lecturesList) {
                if (lecture.remindLectureDelay >= 0) {
                    val data = Data.Builder()
                        .putString(Constants.DATA_COURSE_NAME_KEY, lecture.courseName)
                        .putString(Constants.DATA_PROFESSOR_NAME_KEY, lecture.professorName)
                        .putString(Constants.DATA_ROOM_KEY, lecture.room)
                        .putString(Constants.DATA_STATUS_KEY, lecture.status.toString())
                        .putString(Constants.DATA_TIME_TEXT_KEY, lecture.timeText)
                        .putString(Constants.DATA_TITLE_TEXT_KEY, "Upcoming lecture..")
                        .putLong(Constants.DATA_START_DELAY_KEY, lecture.startLectureDelay)
                        .build()
                    val workRequest = OneTimeWorkRequestBuilder<TimetableWorker>()
                        .setInitialDelay(lecture.remindLectureDelay, TimeUnit.MILLISECONDS)
                        .addTag(Constants.UPCOMING_LECTURE_TAG)
                        .setInputData(data)
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance(context).enqueue(workRequest)
                    Log.d(TAG, "scheduleCurrentDayTimetable: Scheduled: $lecture")
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
            startLectureDelay: Long,
            titleText: String
        ) {
            WorkManager.getInstance(context).cancelAllWorkByTag(Constants.CURRENT_LECTURE_TAG)
            val data = Data.Builder()
                .putString(Constants.DATA_COURSE_NAME_KEY, courseName)
                .putString(Constants.DATA_PROFESSOR_NAME_KEY, professorName)
                .putString(Constants.DATA_ROOM_KEY, room)
                .putString(Constants.DATA_STATUS_KEY, status)
                .putString(Constants.DATA_TIME_TEXT_KEY, timeText)
                .putString(Constants.DATA_TITLE_TEXT_KEY, titleText)
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

data class LectureSchedule(
    val courseName: String,
    val professorName: String,
    val room: String,
    val status: LectureStatus,
    val timeText: String,
    val remindLectureDelay: Long,
    val startLectureDelay: Long,
    val endLectureDelay: Long
)
