package com.example.fraudguardian.dao

import androidx.room.*
import com.example.fraudguardian.entities.*

@Dao
interface AppDao {
    @Insert suspend fun insertOtp(otp: OtpLog): Long
    @Query("SELECT * FROM otp_logs ORDER BY receivedAt DESC") suspend fun getOtps(): List<OtpLog>

    @Insert suspend fun insertCall(call: CallLog): Long
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC") suspend fun getCalls(): List<CallLog>

    @Insert suspend fun insertTrusted(contact: TrustedContact): Long
    @Query("SELECT * FROM trusted_contacts ORDER BY name ASC") suspend fun getTrusted(): List<TrustedContact>
    @Delete suspend fun deleteTrusted(contact: TrustedContact)
}
