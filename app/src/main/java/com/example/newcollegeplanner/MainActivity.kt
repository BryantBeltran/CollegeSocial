package com.example.newcollegeplanner // Make sure this matches your package name exactly

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import androidx.appcompat.widget.Toolbar
// No need for androidx.core.app.ActivityCompat unless explicitly used elsewhere
// import androidx.core.app.ActivityCompat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.widget.ImageView // Make sure ImageView is imported for your icon


class MainActivity : AppCompatActivity() {
    private lateinit var refreshLauncher: ActivityResultLauncher<Intent>
    private lateinit var api: ApiService

    // --- Correct Class Member Declarations (accessible throughout the class) ---
    private lateinit var recyclerView: RecyclerView
    private lateinit var supabutton: Button // Your "Add New Event" button
    private lateinit var iconAllEvents: ImageView // Your "All Events" icon in the toolbar
    private lateinit var buttonLogout: Button // Your "Logout" button in the toolbar
    // --- End Class Member Declarations ---

    // Define constants for SharedPreferences file name and keys
    companion object {
        private const val PREF_NAME = "LoginPrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_LOGGED_IN_USER_ID = "loggedInUserId" // Key for user ID stored
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Toolbar and set it as ActionBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Hide default title

        // --- CORRECT INITIALIZATION OF CLASS MEMBERS (assign to lateinit vars) ---
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        supabutton = findViewById(R.id.supabutton)
        iconAllEvents = findViewById(R.id.iconAllEvents)
        buttonLogout = findViewById(R.id.buttonLogout)
        // --- END CORRECT INITIALIZATION ---

        // Initialize Retrofit for API calls
        val retrofit = Retrofit.Builder()
            // Make sure this is your CORRECT MockAPI.io Base URL (e.g., https://YOUR_UNIQUE_ID.mockapi.io/)
            .baseUrl("https://68117ac53ac96f7119a4aa8d.mockapi.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ApiService::class.java)

        // Fetch and load events into the RecyclerView
        loadEvents() // Calling loadEvents WITHOUT the recyclerView parameter now

        // Register launcher to refresh after returning from AddInfoActivity
        refreshLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadEvents() // Call loadEvents without the parameter here too
            }
        }

        // Set OnClickListener for the "Add New Event" button
        supabutton.setOnClickListener { // Uses the class member 'supabutton'
            val intent = Intent(this, AddInfoActivity::class.java)
            refreshLauncher.launch(intent)
        }

        // Set OnClickListener for the "Logout" button
        buttonLogout.setOnClickListener { // Uses the class member 'buttonLogout'
            Log.d("MainActivity", "Logout button clicked!")
            logoutUser()
        }

        // Set OnClickListener for the "All Events" icon in the toolbar
        iconAllEvents.setOnClickListener { // Uses the class member 'iconAllEvents'
            Log.d("MainActivity", "All Events icon clicked!")
            val intent = Intent(this, AllEventsActivity::class.java)
            startActivity(intent)
        }
    } // <-- onCreate() method ends here

    // --- loadEvents() method definition (no recyclerView parameter) ---
    private fun loadEvents() {
        val prefs: SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentUserId: String? = prefs.getString(KEY_LOGGED_IN_USER_ID, null)

        if (currentUserId == null) {
            Log.w("MainActivity", "No logged-in user ID found. Displaying no events.")
            recyclerView.adapter = EventAdapter(emptyList()) // Uses the class member 'recyclerView'
            return
        }

        api.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    val allEvents = response.body() ?: emptyList()
                    val now = Calendar.getInstance().time
                    Log.d("MainActivity", "Current time (now): $now")

                    // First, filter by creatorId, then by upcoming date/time, then sort
                    val filteredEvents = allEvents
                        .filter { event ->
                            event.creatorId == currentUserId // Filter by current user
                        }
                        .filter { event ->
                            // Filter by upcoming date/time
                            val eventDateTime = parseEventDateTime(event.date, event.time)
                            val isUpcoming = eventDateTime != null && eventDateTime.after(now)
                            Log.d("MainActivity", "Event: ${event.event_name}, Date/Time: ${event.date} ${event.time}, Parsed: $eventDateTime, Creator: ${event.creatorId}, Is Upcoming: $isUpcoming")
                            isUpcoming
                        }
                        .sortedBy { event ->
                            // Sort by date/time
                            parseEventDateTime(event.date, event.time)
                        }

                    Log.d("MainActivity", "Total events from API: ${allEvents.size}")
                    Log.d("MainActivity", "Events by current user: ${filteredEvents.filter { it.creatorId == currentUserId }.size}") // This log might be redundant after first filter, but for clarity.
                    Log.d("MainActivity", "Upcoming user events to display: ${filteredEvents.size}")
                    filteredEvents.forEach { event ->
                        Log.d("MainActivity", "Displaying (User Specific, Upcoming): ${event.event_name} - ${event.date} ${event.time} (Creator: ${event.creatorId})")
                    }

                    recyclerView.adapter = EventAdapter(filteredEvents) // Uses the class member 'recyclerView'

                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                Log.e("MainActivity", "Error loading events: ${t.message}")
            }
        })
    }

    // --- Helper function to parse date and time strings into a Date object ---
    // It is a direct member of the MainActivity class.
    private fun parseEventDateTime(dateString: String, timeString: String): Date? {
        val dateTimeString = "$dateString $timeString"
        // Ensure this format matches your MockAPI.io data (e.g., "MM/DD/YYYY hh:mm a" for 12-hour AM/PM)
        val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US)

        return try {
            dateFormat.parse(dateTimeString)
        } catch (e: ParseException) {
            Log.e("MainActivity", "Error parsing date/time: $dateTimeString - ${e.message}")
            null
        }
    }
    // --- End of parseEventDateTime function ---

    private fun logoutUser() {
        // Clear login status from SharedPreferences
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false) // Set to false
            putString(KEY_LOGGED_IN_USER_ID, null) // Clear the stored user ID
            apply() // Apply changes asynchronously
        }

        // Navigate back to LoginActivity and clear the activity stack
        val intent = Intent(this, LoginActivity::class.java)
        // These flags ensure that when you log out, all previous activities
        // are removed from the back stack, so the user can't go back to a logged-in state.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finish MainActivity, destroying it from memory
    }

} // --- End of MainActivity class ---