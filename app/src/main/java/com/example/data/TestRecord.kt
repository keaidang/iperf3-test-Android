package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_history")
data class TestRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "CLIENT", "SERVER"
    val mode: String, // "UPLOAD", "DOWNLOAD", "N/A"
    val targetHost: String, // IP or "Listen"
    val port: Int,
    val threadCount: Int,
    val maxBandwidthMbps: Float,
    val averageBandwidthMbps: Float,
    val timestamp: Long = System.currentTimeMillis()
)
