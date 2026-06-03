package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TestRecord
import com.example.engine.SpeedTestEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SpeedTestViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val recordDao = database.testRecordDao()
    
    val engine = SpeedTestEngine(application.applicationContext)
    val engineState = engine.state

    private val prefs = application.getSharedPreferences("SpeedTestPrefs", Context.MODE_PRIVATE)

    var host: String
        get() = prefs.getString("host", "192.168.1.100") ?: "192.168.1.100"
        set(value) = prefs.edit().putString("host", value).apply()

    var port: String
        get() = prefs.getString("port", "5201") ?: "5201"
        set(value) = prefs.edit().putString("port", value).apply()

    var isUpload: Boolean
        get() = prefs.getBoolean("isUpload", true)
        set(value) = prefs.edit().putBoolean("isUpload", value).apply()

    var threadCount: Float
        get() = prefs.getFloat("threadCount", 1f)
        set(value) = prefs.edit().putFloat("threadCount", value).apply()

    var isClientMode: Boolean
        get() = prefs.getBoolean("isClientMode", true)
        set(value) = prefs.edit().putBoolean("isClientMode", value).apply()

    var isUdp: Boolean
        get() = prefs.getBoolean("isUdp", false)
        set(value) = prefs.edit().putBoolean("isUdp", value).apply()

    var duration: Float
        get() = prefs.getFloat("duration", 10f)
        set(value) = prefs.edit().putFloat("duration", value).apply()

    val testHistory: StateFlow<List<TestRecord>> = recordDao.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun startServer(port: Int) {
        engine.startServer(port)
    }

    fun startClient(host: String, port: Int, isUpload: Boolean, threadCount: Int, duration: Int, isUdp: Boolean) {
        engine.startClient(host, port, isUpload, threadCount, duration, isUdp) { finalState ->
            saveRecord(
                TestRecord(
                    type = "CLIENT",
                    mode = if (isUpload) "UPLOAD" else "DOWNLOAD",
                    targetHost = host,
                    port = port,
                    threadCount = threadCount,
                    maxBandwidthMbps = finalState.maxBandwidthMbps,
                    averageBandwidthMbps = finalState.avgBandwidthMbps,
                    minBandwidthMbps = finalState.minBandwidthMbps,
                    bandwidthHistoryString = finalState.bandwidthHistory.joinToString(","),
                    protocol = if (isUdp) "UDP" else "TCP",
                    duration = duration
                )
            )
        }
    }

    fun stopTest() {
        engine.stop()
        // If it was server, save the server record
        val current = engineState.value
        if (current.avgBandwidthMbps > 0) {
           saveRecord(
               TestRecord(
                   type = "SERVER",
                   mode = "LISTEN",
                   targetHost = "Local",
                   port = port.toIntOrNull() ?: 5201,
                   threadCount = 0,
                   maxBandwidthMbps = current.maxBandwidthMbps,
                   averageBandwidthMbps = current.avgBandwidthMbps,
                   minBandwidthMbps = current.minBandwidthMbps,
                   bandwidthHistoryString = current.bandwidthHistory.joinToString(","),
                   protocol = "TCP/UDP",
                   duration = current.bandwidthHistory.size
               )
           ) 
        }
    }

    private fun saveRecord(record: TestRecord) {
        viewModelScope.launch {
            recordDao.insertRecord(record)
        }
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            recordDao.clearHistory()
        }
    }
}
