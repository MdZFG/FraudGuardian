package com.example.fraudguardian.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trusted_contacts")
data class TrustedContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String?,
    val number: String
)
