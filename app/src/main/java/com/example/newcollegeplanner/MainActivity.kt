package com.example.newcollegeplanner

import android.content.Intent
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

class MainActivity : AppCompatActivity() {
    private lateinit var refreshLauncher: ActivityResultLauncher<Intent>
    private lateinit var api: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }

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
}
