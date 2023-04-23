package com.sun.sunclient.network

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sun.sunclient.network.background.TimetableWorker
import com.sun.sunclient.network.repository.AuthRepository
import com.sun.sunclient.network.repository.TimetableRepository
import com.sun.sunclient.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppNotificationService : FirebaseMessagingService() {
    private val TAG = "AppNotificationService"

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var timetableRepository: TimetableRepository

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        val updateStatus = MutableLiveData<String>()
    }

    init {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            Log.d(TAG, "Token is $token")
        })
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}, data: ${remoteMessage.data}")
        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            scope.launch {
                authRepository.refresh()
                timetableRepository.refreshCache()
                updateStatus.postValue(Constants.FETCH_TIMETABLE)
                TimetableWorker.scheduleCurrentDayTimetable(
                    applicationContext,
                    timetableRepository.timetableData
                )
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }
}