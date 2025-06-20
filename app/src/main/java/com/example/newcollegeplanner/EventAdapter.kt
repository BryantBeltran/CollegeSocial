package com.example.newcollegeplanner

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(private val events: List<Event>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventText: TextView = itemView.findViewById(R.id.eventText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val displayText = "Event: ${event.event_name}\n${event.date} at ${event.time}\nLocation: ${event.location}"
        holder.eventText.text = displayText
        Log.d("EventAdapter", "Binding position $position: Text -> $displayText")
    }


    override fun getItemCount() = events.size
}
