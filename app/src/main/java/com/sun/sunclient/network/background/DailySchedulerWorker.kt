package com.sun.sunclient.network.background

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.reflect.TypeToken
import com.sun.sunclient.data.dataStore
import com.sun.sunclient.network.schemas.Timetable
import com.sun.sunclient.utils.Constants
import com.sun.sunclient.utils.parseJson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.TimeUnit

class DailySchedulerWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        runBlocking {
            val dataStore = applicationContext.dataStore
            val dataStringFlow = dataStore.data.map { preferences ->
                preferences[stringPreferencesKey(Constants.TIMETABLE_KEY)] ?: ""
            }
            val dataString = dataStringFlow.first()
            if (dataString != "{}" && dataString != "") {
                val timetableData =
                    parseJson(dataString, TypeToken.get(Timetable::class.java)) ?: Timetable()
                TimetableWorker.scheduleCurrentDayTimetable(applicationContext, timetableData)
            } else {
                Log.d(TAG, "doWork: No timetable present")
            }
            Log.d(TAG, "doWork: finish setting scheduleCurrentDayTimetable")
        }
        addDailyScheduler(applicationContext)
        Log.d(TAG, "doWork: end")
        return Result.success()
    }

    companion object {
        val TAG = "DailySchedulerWorker"

        fun addDailyScheduler(context: Context, isStart: Boolean = false) {
            val todayDate = Calendar.getInstance().apply {
                this.set(Calendar.HOUR_OF_DAY, 1)
                this.set(Calendar.MINUTE, 0)
            }
            todayDate.add(Calendar.DAY_OF_YEAR, 1)
            var delay = todayDate.timeInMillis - System.currentTimeMillis()
            if (isStart) {
                delay = 0
            }
            if (delay < 0) delay = 0
            WorkManager.getInstance(context).cancelAllWorkByTag(Constants.DAILY_SCHEDULER_TAG)
            val workRequest = OneTimeWorkRequestBuilder<DailySchedulerWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(Constants.DAILY_SCHEDULER_TAG)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)

            runBlocking {
                context.dataStore.edit { preferences ->
                    preferences[stringPreferencesKey(Constants.IS_TIMETABLE_SCHEDULED)] = "SCHEDULED"
                }
            }
            Log.d(TAG, "addDailyScheduler: Enqueued daily scheduler")
        }
    }
}