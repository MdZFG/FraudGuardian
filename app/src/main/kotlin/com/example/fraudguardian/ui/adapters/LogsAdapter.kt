package com.example.fraudguardian.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fraudguardian.R
import com.example.fraudguardian.entities.CallLog
import com.example.fraudguardian.entities.OtpLog
import java.text.DateFormat
import java.util.*

class LogsAdapter(private val items: List<Any>) : RecyclerView.Adapter<LogsAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        if (item is OtpLog) {
            holder.title.text = item.code ?: item.message
            holder.subtitle.text = "${item.sender} • ${DateFormat.getDateTimeInstance().format(Date(item.receivedAt))}"
        } else if (item is CallLog) {
            holder.title.text = item.number
            holder.subtitle.text = "${item.name ?: ""} • ${DateFormat.getDateTimeInstance().format(Date(item.timestamp))}"
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.log_title)
        val subtitle: TextView = view.findViewById(R.id.log_sub)
    }
}
