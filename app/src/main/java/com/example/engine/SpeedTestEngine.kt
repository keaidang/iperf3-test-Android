package com.example.engine

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.regex.Pattern

data class SpeedTestState(
    val isRunning: Boolean = false,
    val currentBandwidthMbps: Float = 0f,
    val minBandwidthMbps: Float = 0f,
    val maxBandwidthMbps: Float = 0f,
    val avgBandwidthMbps: Float = 0f,
    val bandwidthHistory: List<Float> = emptyList(), // For real-time chart
    val error: String? = null
)

class SpeedTestEngine(private val context: Context) {
    private val _state = MutableStateFlow(SpeedTestState())
    val state = _state.asStateFlow()

    private var activeJob: Job? = null
    private var process: Process? = null

    private fun getExecutablePath(): String {
        return File(context.applicationInfo.nativeLibraryDir, "libiperf3.so").absolutePath
    }

    private fun parseBandwidth(line: String): Float? {
        // e.g. [  5]   0.00-1.00   sec  11.4 MBytes  95.7 Mbits/sec
        // Or [SUM]   0.00-1.00   sec  22.8 MBytes  191 Mbits/sec
        val pattern = Pattern.compile("(?i)([0-9.]+)\\s+([KMGT]bits?/sec)")
        val matcher = pattern.matcher(line)
        if (matcher.find()) {
            val value = matcher.group(1)?.toFloatOrNull() ?: return null
            val unit = matcher.group(2)?.uppercase() ?: return null
            return when {
                unit.startsWith("G") -> value * 1000f
                unit.startsWith("M") -> value
                unit.startsWith("K") -> value / 1000f
                else -> value / 1000000f
            }
        }
        return null
    }

    fun startServer(port: Int) {
        val job = Job()
        activeJob = job
        _state.update { SpeedTestState(isRunning = true) }

        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO + job) {
            try {
                val execPath = getExecutablePath()
                val pb = ProcessBuilder(execPath, "-s", "-p", port.toString(), "--forceflush")
                pb.redirectErrorStream(true)
                process = pb.start()
                
                val history = mutableListOf<Float>()
                
                val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                while (isActive) {
                    val line = reader.readLine() ?: break
                    if (line.contains("error", ignoreCase = true)) {
                        _state.update { it.copy(error = line) }
                    }
                    if (line.contains("sec") && line.contains("bits/sec")) {
                        val mbps = parseBandwidth(line)
                        if (mbps != null) {
                            history.add(mbps)
                            if (history.size > 60) history.removeAt(0)
                            val max = history.maxOrNull() ?: 0f
                            val min = history.minOrNull() ?: 0f
                            val avg = if (history.isNotEmpty()) history.average().toFloat() else 0f
                            
                            _state.update { it.copy(
                                currentBandwidthMbps = mbps,
                                minBandwidthMbps = min,
                                maxBandwidthMbps = max,
                                avgBandwidthMbps = avg,
                                bandwidthHistory = history.toList()
                            ) }
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            } finally {
                process?.destroy()
                _state.update { it.copy(isRunning = false) }
            }
        }
    }

    fun startClient(host: String, port: Int, isUpload: Boolean, threadCount: Int, onComplete: (SpeedTestState) -> Unit) {
        val job = Job()
        activeJob = job
        _state.update { SpeedTestState(isRunning = true) }

        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO + job) {
            try {
                val execPath = getExecutablePath()
                
                val args = mutableListOf(execPath, "-c", host, "-p", port.toString(), "-P", threadCount.toString(), "--forceflush")
                if (!isUpload) {
                    args.add("-R")
                }
                
                val pb = ProcessBuilder(args)
                pb.redirectErrorStream(true)
                process = pb.start()
                
                val history = mutableListOf<Float>()
                
                val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                while (isActive) {
                    val line = reader.readLine() ?: break
                    if (line.contains("error", ignoreCase = true)) {
                        _state.update { it.copy(error = line) }
                    }
                    // Avoid catching sender/receiver summary lines directly, but grab interval sums if parallel
                    if (line.contains("sec") && line.contains("bits/sec") && (!line.contains("sender") && !line.contains("receiver"))) {
                        // If P > 1, we want the [SUM] lines, else the specific thread line
                        if (threadCount > 1 && !line.contains("[SUM]")) continue

                        val mbps = parseBandwidth(line)
                        if (mbps != null) {
                            history.add(mbps)
                            if (history.size > 60) history.removeAt(0)
                            val max = history.maxOrNull() ?: 0f
                            val min = history.minOrNull() ?: 0f
                            val avg = if (history.isNotEmpty()) history.average().toFloat() else 0f
                            
                            _state.update { it.copy(
                                currentBandwidthMbps = mbps,
                                minBandwidthMbps = min,
                                maxBandwidthMbps = max,
                                avgBandwidthMbps = avg,
                                bandwidthHistory = history.toList()
                            ) }
                        }
                    }
                }
                
                // wait for exit
                process?.waitFor()
                
                val finalState = _state.value
                withContext(Dispatchers.Main) {
                    onComplete(finalState)
                }
                
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            } finally {
                process?.destroy()
                _state.update { it.copy(isRunning = false) }
            }
        }
    }

    fun stop() {
        process?.destroy()
        activeJob?.cancel()
        _state.update { it.copy(isRunning = false) }
    }
}
