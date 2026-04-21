package com.example.fraudguardian.ui

import android.view.Menu
import android.view.MenuItem
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import com.example.fraudguardian.workers.SosWorker
import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.fraudguardian.R
import com.example.fraudguardian.databinding.ActivityMainBinding
import com.example.fraudguardian.services.ForegroundListenerService
import com.example.fraudguardian.ui.SettingsActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.PermissionRequest

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val prefs by lazy { getSharedPreferences("fraud_guardian_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set custom toolbar (VERY IMPORTANT)
        setSupportActionBar(binding.toolbarMain)

        checkAndRequestPermissions()
        requestDialerRole()
        handleBatteryOptimization()

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, HomeFragment())
            .commit()

        // ====== SOS BUTTON FUNCTION ======
        val sosButton = findViewById<Button>(R.id.btnSOS)
        sosButton.setOnClickListener {
            val data = workDataOf(
                "message" to "SOS: I might be in trouble. Please check on me."
            )

            val work = OneTimeWorkRequestBuilder<SosWorker>()
                .setInputData(data)
                .build()

            WorkManager.getInstance(this).enqueue(work)
            Toast.makeText(this, "SOS alert sent", Toast.LENGTH_SHORT).show()
        }
    }

    // =====================================================================
    //  MENU (⋮) – SETTINGS ICON FIX
    // =====================================================================

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // =====================================================================
    // PERMISSIONS / ROLE / BATTERY STUFF
    // =====================================================================

    private fun checkAndRequestPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.VIBRATE,
            Manifest.permission.READ_CALL_LOG
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
        }

        val missingPermissions = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            showPermissionDialog(requiredPermissions)
        }
    }

    private fun showPermissionDialog(permissions: List<String>) {
        val dialogListener = DialogOnAnyDeniedMultiplePermissionsListener.Builder
            .withContext(this)
            .withTitle("Permissions required")
            .withMessage("We need SMS, Phone, and Contacts permissions to detect fraud and protect you.")
            .withButtonText(android.R.string.ok)
            .build()

        val listener = object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report?.areAllPermissionsGranted() == true) {
                    Toast.makeText(this@MainActivity, "Permissions granted", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }
        }

        Dexter.withContext(this)
            .withPermissions(permissions)
            .withListener(CompositeMultiplePermissionsListener(dialogListener, listener))
            .check()
    }

    private fun requestDialerRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_DIALER) == true &&
                !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
            ) {
                startActivityForResult(
                    roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER),
                    200
                )
            }
        }
    }

    private fun handleBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(PowerManager::class.java)
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    fun startMonitoring() {
        startService(Intent(this, ForegroundListenerService::class.java))
        prefs.edit().putBoolean("monitoring_enabled", true).apply()
    }

    fun stopMonitoring() {
        stopService(Intent(this, ForegroundListenerService::class.java))
        prefs.edit().putBoolean("monitoring_enabled", false).apply()
    }

    fun isMonitoringEnabled(): Boolean = prefs.getBoolean("monitoring_enabled", false)
}
