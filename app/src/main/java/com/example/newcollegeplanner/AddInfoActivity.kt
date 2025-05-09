package com.example.newcollegeplanner

import android.os.Bundle
import retrofit2.*
import android.widget.EditText
import android.widget.Button
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class AddInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_info)

        val nameEdit = findViewById<EditText>(R.id.editTextName)
        val dateEdit = findViewById<EditText>(R.id.editTextDate)
        val timeEdit = findViewById<EditText>(R.id.editTextTime)
        val locationEdit = findViewById<EditText>(R.id.editTextLocation)
        val submitButton = findViewById<Button>(R.id.submitButton)



        submitButton.setOnClickListener {
            Log.d("AddInfoActivity", "Button clicked")
            val event_name = nameEdit.text.toString()
            val date = dateEdit.text.toString()
            val time = timeEdit.text.toString()
            val location = locationEdit.text.toString()

            // Call a function to send this data to the API
            sendToApi(event_name, date, time, location)
        }
    }

    private fun sendToApi(event_name: String, date: String, time: String, location: String) {
        val event = Event(event_name, date, time, location)

        val call = RetrofitInstance.api.addEvent(event)
        call.enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddInfoActivity, "Event added!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@AddInfoActivity,
                        "Failed: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AddInfoActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }




}











