package com.example.fraudguardian.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val name: String?,
    val inContacts: Boolean = false,
    val timestamp: Long,
    val note: String?
)
