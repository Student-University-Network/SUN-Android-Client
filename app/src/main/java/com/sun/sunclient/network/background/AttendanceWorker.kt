package com.sun.sunclient.network.background

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.work.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.sun.sunclient.config.Config
import com.sun.sunclient.data.AppDataStore
import com.sun.sunclient.network.AddCookieInterceptor
import com.sun.sunclient.network.SaveCookiesInterceptor
import com.sun.sunclient.network.schemas.Lecture
import com.sun.sunclient.network.schemas.MarkAttendanceInput
import com.sun.sunclient.network.schemas.MarkAttendanceResponse
import com.sun.sunclient.network.schemas.TakeAttendanceInput
import com.sun.sunclient.network.service.AttendanceApiService
import com.sun.sunclient.utils.Constants
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.Byte
import java.util.*
import java.util.concurrent.TimeUnit


class AttendanceWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val attendanceApiService: AttendanceApiService

    private val UUID = ParcelUuid.fromString("b161c53c-0715-11e6-b512-3e1d05defe78")
    private var isBroadcasting = false
    private var isReceiving = false
    private val dataCallback: DataCallback

    private val bleManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private val bleAdapter: BluetoothAdapter = bleManager.adapter
    private var bleAdvertiser: BluetoothLeAdvertiser? = null
    private var bleScanner: BluetoothLeScanner? = null

    private var advertiserData: AdvertiseData? = null

    private val timer: Timer = Timer()

    init {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .build()
        val dataStore = AppDataStore(applicationContext)
        val okHttpClient = OkHttpClient().newBuilder()
            .addInterceptor(AddCookieInterceptor(dataStore))
            .addInterceptor(SaveCookiesInterceptor(dataStore))
            .build()
        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(Config.API_BASE_URL)
            .build()
        attendanceApiService = retrofit.create(AttendanceApiService::class.java)
        dataCallback = object : DataCallback {
            override fun onDataUpdate(token: String) {
                Log.d(TAG, "onDataUpdate: Received token ${token}")
                runBlocking {
                    val response = try {
                        val response =
                            attendanceApiService.markAttendance(MarkAttendanceInput(token))
                        response
                    } catch (e: Exception) {
                        Log.e(TAG, "markAttendance: $e")
                        MarkAttendanceResponse("failed", null)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun doWork(): Result {

        val context = applicationContext
        val role = inputData.getString(Constants.DATA_ROLE_KEY) ?: Constants.STUDENT
        val courseId = inputData.getString(Constants.DATA_COURSE_ID_KEY) ?: ""
        val lectureId = inputData.getString(Constants.DATA_LECTURE_ID_KEY) ?: ""

        if (!bleAdapter.isEnabled) {
            bleAdapter.enable()
        }
        bleAdvertiser = bleAdapter.bluetoothLeAdvertiser
        bleScanner = bleAdapter.bluetoothLeScanner

        if (role == Constants.FACULTY) { // take attendance
            var token = ""
            runBlocking {
                token = try {
                    val response = attendanceApiService.takeAttendance(
                        TakeAttendanceInput(
                            lectureId,
                            courseId
                        )
                    )
                    response.data.token
                } catch (e: Exception) {
                    Log.e(TAG, "TakeAttendance: $e")
                    ""
                }
            }
            Log.d(TAG, "doWork: $token")
            takeAttendance(token)
            setTimeout()
        } else { // give attendance
            markAttendance()
            setTimeout()
        }

        return Result.success()
    }

    override fun onStopped() {
        Log.d(TAG, "onStopped: Called")
        stopAttendance()
        timer.cancel()
        super.onStopped()
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            val reason = when (errorCode) {
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "Too many advertisers"
                ADVERTISE_FAILED_ALREADY_STARTED -> "Already started"
                ADVERTISE_FAILED_INTERNAL_ERROR -> "Internal error"
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "Unsupported feature"
                ADVERTISE_FAILED_DATA_TOO_LARGE -> "Too large data"
                else -> "Unknown error"
            }
            Log.d(TAG, "Failed to start BLE, error: $errorCode")
            Log.d(TAG, "Reason: $reason")
            stopAttendance()
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Successfully started BLE")
        }
    }
    private val scanCallback = object : ScanCallback() {
        var isMarked = false
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val record = result.scanRecord?.serviceData
            record?.let {
                val token = it.get(UUID)?.let { it1 -> String(it1) } ?: return
                Log.d(TAG, "onScanResult: token : $token")
//                if (!isMarked) {
                    dataCallback.onDataUpdate(token ?: "")
//                }
//                isMarked = true
            }
            Log.d(TAG, "onScanResult: got token, marked attendance")
            stopAttendance()
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d(TAG, "onScanFailed: errorCode $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun takeAttendance(token: String) {
        if (isReceiving) stopAttendance()
        if (!isBroadcasting) {
            advertiserData = AdvertiseData.Builder()
                .addServiceData(UUID, token.toByteArray(Charsets.UTF_8))
                .build()
            val settings = buildAdvertiseSettings()
            bleAdvertiser?.startAdvertising(settings, advertiserData, advertiseCallback)
            isBroadcasting = true
            Log.d(TAG, "Started taking attendance")
        } else {
            Log.d(TAG, "Already started taking attendance")
        }
    }

    @SuppressLint("MissingPermission")
    fun markAttendance() {
        if (isBroadcasting) stopAttendance()
        if (!isReceiving) {
            val scanFilter = ScanFilter.Builder().build()
            val scanSettings =
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
            bleScanner?.startScan(listOf(scanFilter), scanSettings, scanCallback)
            isReceiving = true
            Log.d(TAG, "Started marking attendance")
        } else {
            Log.d(TAG, "Already started marking attendace")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopAttendance() {
        if (isBroadcasting) {
            bleAdvertiser?.stopAdvertising(advertiseCallback)
            Log.d(TAG, "Stopping broadcast")
        }
        if (isReceiving) {
            bleScanner?.stopScan(scanCallback)
            Log.d(TAG, "Stopping scanning")
        }
        isBroadcasting = false
        isReceiving = false
    }

    private fun setTimeout() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "run: stopping attendance.")
                stopAttendance()
            }
        }, 9 * 60 * 1000)
    }

    private fun buildAdvertiseSettings() = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        .setTimeout(0)
        .build()

    companion object {
        val TAG = "AttendanceWorker"

        fun scheduleAttendance(
            context: Context,
            courseId: String,
            lectureId: String,
            role: String,
            delay: Long
        ) {
            WorkManager.getInstance(context).cancelAllWorkByTag(Constants.ATTENDANCE_SCHEDULER_TAG)
            val data = Data.Builder()
                .putString(Constants.DATA_ROLE_KEY, role)
                .putString(Constants.DATA_LECTURE_ID_KEY, lectureId)
                .putString(Constants.DATA_COURSE_ID_KEY, courseId)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<AttendanceWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(Constants.ATTENDANCE_SCHEDULER_TAG)
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d(TAG, "scheduleAttendance: Scheduled attendance")
        }

        fun takeALectureAttendance(
            context: Context, lectureId: String, courseId: String, role: String
        ) {
            WorkManager.getInstance(context).cancelAllWorkByTag(Constants.ATTENDANCE_SCHEDULER_TAG)
            val data = Data.Builder()
                .putString(Constants.DATA_ROLE_KEY, role)
                .putString(Constants.DATA_LECTURE_ID_KEY, lectureId)
                .putString(Constants.DATA_COURSE_ID_KEY, courseId)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<AttendanceWorker>()
                .setInitialDelay(0, TimeUnit.MILLISECONDS)
                .addTag(Constants.ATTENDANCE_SCHEDULER_TAG)
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d(TAG, "takeALectureAttendance: Started taking attendance")
        }
    }
}

interface DataCallback {
    fun onDataUpdate(token: String)
}