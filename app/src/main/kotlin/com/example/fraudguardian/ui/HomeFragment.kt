package com.example.fraudguardian.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fraudguardian.databinding.FragmentHomeBinding
import com.example.fraudguardian.db.AppDatabase
import com.example.fraudguardian.ui.adapters.LogsAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val main = activity as? MainActivity
        //  Restore previous monitoring state
        val wasMonitoring = main?.isMonitoringEnabled() ?: false
        binding.switchMonitor.isChecked = wasMonitoring

        //Toggle listener
        binding.switchMonitor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) main?.startMonitoring() else main?.stopMonitoring()
        }

        /*  binding.switchMonitor.setOnCheckedChangeListener { _, isChecked ->
              (activity as? MainActivity)?.let {
                  if (isChecked) it.startMonitoring() else it.stopMonitoring()
              }
          }*/

        binding.recyclerLogs.layoutManager = LinearLayoutManager(requireContext())
        loadLogs()

        binding.buttonTrusted.setOnClickListener {
            startActivity(Intent(requireContext(), TrustedContactsActivity::class.java))
        }
    }

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadLogs()
        }
    }



    override fun onResume() {
        super.onResume()
        ContextCompat.registerReceiver(
            requireContext(),
            updateReceiver,
            IntentFilter("com.example.fraudguardian.UPDATE_LOGS"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(updateReceiver)
    }

    private fun loadLogs() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext()).appDao()
            val otps = db.getOtps()
            val calls = db.getCalls()
            val merged = mutableListOf<Any>()
            merged.addAll(otps)
            merged.addAll(calls)
            binding.recyclerLogs.adapter = LogsAdapter(merged)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
