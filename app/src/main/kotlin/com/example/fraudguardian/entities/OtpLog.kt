package com.example.fraudguardian.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "otp_logs")
data class OtpLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val message: String,
    val code: String?,
    val receivedAt: Long
)
