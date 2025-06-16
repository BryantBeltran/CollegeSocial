package com.example.newcollegeplanner // Make sure this matches your package name

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
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {
    private lateinit var refreshLauncher: ActivityResultLauncher<Intent>
    private lateinit var api: ApiService

    // Define constants for SharedPreferences file name and key
    companion object {
        private const val PREF_NAME = "LoginPrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://68117ac53ac96f7119a4aa8d.mockapi.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ApiService::class.java)

        // Fetch and load events into the RecyclerView
        loadEvents(recyclerView)

        // Register launcher to refresh after returning from AddInfoActivity
        refreshLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadEvents(recyclerView)
            }
        }

        // Button to launch AddInfoActivity
        findViewById<Button>(R.id.supabutton).setOnClickListener {
            val intent = Intent(this, AddInfoActivity::class.java)
            refreshLauncher.launch(intent)
        }

        // --- Logout Button Listener --
        findViewById<Button>(R.id.buttonLogout).setOnClickListener {
            Log.d("MainActivity", "Logout button clicked!") // Log to confirm click listener fires
            logoutUser() // CALLS THE FUNCTION BELOW
        }
    }

    // This is a private function within MainActivity, so it can be called by things inside MainActivity
    private fun loadEvents(recyclerView: RecyclerView) {
        api.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    val events = response.body() ?: emptyList()
                    recyclerView.adapter = EventAdapter(events)
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                Log.e("MainActivity", "Error loading events: ${t.message}")
            }
        })
    }


    private fun logoutUser() {
        // Clear login status from SharedPreferences
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false) // Set to false
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

} //