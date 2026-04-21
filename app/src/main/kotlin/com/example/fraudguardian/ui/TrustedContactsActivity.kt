package com.example.fraudguardian.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fraudguardian.databinding.ActivityTrustedBinding
import com.example.fraudguardian.db.AppDatabase
import com.example.fraudguardian.entities.TrustedContact
import kotlinx.coroutines.launch

class TrustedContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrustedBinding

    // launcher to pick a contact
    private val contactPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val contactUri: Uri? = result.data?.data
                contactUri?.let {
                    val cursor = contentResolver.query(
                        it,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        ),
                        null,
                        null,
                        null
                    )
                    cursor?.use { c ->
                        if (c.moveToFirst()) {
                            val name =
                                c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                            val number =
                                c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            binding.editName.setText(name)
                            binding.editNumber.setText(number)
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrustedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // pick contact button
        binding.buttonPick.setOnClickListener {
            val pickIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            contactPicker.launch(pickIntent)
        }

        binding.buttonAdd.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val num = binding.editNumber.text.toString().trim()
            if (num.isNotEmpty()) {
                lifecycleScope.launch {
                    AppDatabase.getInstance(this@TrustedContactsActivity).appDao()
                        .insertTrusted(TrustedContact(name = if (name.isEmpty()) null else name, number = num))
                    loadTrusted()
                }
                binding.editName.text?.clear()
                binding.editNumber.text?.clear()
            } else {
                Toast.makeText(this, "Please enter or pick a valid contact", Toast.LENGTH_SHORT).show()
            }
        }

        loadTrusted()
    }

    private fun loadTrusted() {
        lifecycleScope.launch {
            val list = AppDatabase.getInstance(this@TrustedContactsActivity).appDao().getTrusted()
            binding.textTrusted.text = ""
            list.forEach {
                binding.textTrusted.append("${it.name ?: "Contact"}: ${it.number}\n")
            }
        }
    }
}
