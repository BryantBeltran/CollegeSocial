package com.example.newcollegeplanner

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.Button
import android.util.Log
import android.content.Intent



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textView)
        findViewById<Button>(R.id.supabutton).setOnClickListener {
            Log.d("BUTTONS", "User tapped the button")
            val intent = Intent(this, AddInfoActivity::class.java)
            startActivity(intent)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://68117ac53ac96f7119a4aa8d.mockapi.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        api.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    val events = response.body()
                    textView.text = events?.joinToString("\n\n") {
                        "Event: ${it.event_name}\n${it.date} at ${it.time}\n Location: ${it.location}"
                    } ?: "No events found."
                } else {
                    textView.text = "Response failed: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                textView.text = "Error: ${t.message}"
            }
        })
    }
}
