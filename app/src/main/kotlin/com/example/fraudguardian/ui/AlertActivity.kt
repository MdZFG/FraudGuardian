package com.example.fraudguardian.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.fraudguardian.R
import com.example.fraudguardian.services.SosScheduler

class AlertActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)

        val message = intent.getStringExtra("message") ?: "Suspicious activity detected!"
        val from = intent.getStringExtra("from") ?: "Unknown"

        val text = findViewById<TextView>(R.id.alert_text)
        val buttonOk = findViewById<Button>(R.id.button_im_ok)
        val buttonCall = findViewById<Button>(R.id.button_call)

        text.text = message

        // Cancel SOS if user acknowledges alert
        buttonOk.setOnClickListener {
            SosScheduler.cancelAllSos(this)
            finish()
        }

        // Optional: allow user to call back the sender if it was a call alert
        buttonCall.setOnClickListener {
            val dial = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$from")
            }
            startActivity(dial)
        }
    }
}