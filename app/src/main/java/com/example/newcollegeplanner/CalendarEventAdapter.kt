package com.example.newcollegeplanner

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarEventAdapter(private val events: List<Event>) : RecyclerView.Adapter<CalendarEventAdapter.CalendarEventViewHolder>() {

    class CalendarEventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventText: TextView = itemView.findViewById(R.id.eventText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarEventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_event, parent, false)
        return CalendarEventViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarEventViewHolder, position: Int) {
        val event = events[position]
        val displayText = "${event.time} - ${event.event_name}\nLocation: ${event.location}"
        holder.eventText.text = displayText
        Log.d("CalendarEventAdapter", "Binding position $position: Text -> $displayText")
    }

    override fun getItemCount() = events.size
}
