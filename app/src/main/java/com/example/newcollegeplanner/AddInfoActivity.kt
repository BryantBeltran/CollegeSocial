package com.example.newcollegeplanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import com.example.newcollegeplanner.RetrofitInstance;

class AddInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AddInfoScreen(onSubmit = { name, date, time, location ->
                    sendToApi(name, date, time, location)
                })
            }
        }

    }

    private fun sendToApi(event_name: String, date: String, time: String, location: String) {
        val event = Event(event_name, date, time, location)
        val call = RetrofitInstance.api.addEvent(event)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddInfoActivity, "Event added!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@AddInfoActivity, "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AddInfoActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

@Composable
fun AddInfoScreen(onSubmit: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isDateValid by remember { mutableStateOf(true) }

    fun validateDate(dateString: String): Boolean {
        return try {
            val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            format.isLenient = false
            format.parse(dateString)
            true
        } catch (e: Exception) {
            false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Event Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = date,
            onValueChange = {
                date = it
                isDateValid = validateDate(it)
            },
            label = { Text("Date (MM/DD/YYYY)") },
            isError = !isDateValid,
            modifier = Modifier.fillMaxWidth()
        )

        if (!isDateValid && date.isNotBlank()) {
            Text(
                text = "Invalid date format.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (isDateValid) {
                onSubmit(name, date, time, location)
            }
        }) {
            Text("Submit")
        }
    }
}