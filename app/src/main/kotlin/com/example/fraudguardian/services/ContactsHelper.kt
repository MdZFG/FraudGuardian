package com.example.fraudguardian.services

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract

object ContactsHelper {
    fun findContactName(context: Context, phoneNumber: String): String? {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER)
        val cursor: Cursor? = context.contentResolver.query(uri, projection, "${'$'}{ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?", arrayOf("%$phoneNumber%"), null)
        cursor?.use {
            if (it.moveToFirst()) {
                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                return name
            }
        }
        return null
    }
}
