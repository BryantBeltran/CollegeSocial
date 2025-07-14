package com.example.newcollegeplanner

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(
    val events: MutableList<Event>,
    private val onDeleteEvent: ((Event, Int) -> Unit)? = null,
    private val enableSwipeHints: Boolean = false
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventText: TextView = itemView.findViewById(R.id.eventText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val layoutId = if (enableSwipeHints) R.layout.item_event_swipeable else R.layout.item_event
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val displayText = "Event: ${event.event_name}\n${event.date} at ${event.time}\nLocation: ${event.location}"
        holder.eventText.text = displayText
        Log.d("EventAdapter", "Binding position $position: Text -> $displayText")
    }


    override fun getItemCount() = events.size

    fun removeItem(position: Int) {
        if (position >= 0 && position < events.size) {
            events.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getItem(position: Int): Event? {
        return if (position >= 0 && position < events.size) events[position] else null
    }
}
