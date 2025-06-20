package com.example.newcollegeplanner

import android.content.SharedPreferences
import android.content.Context
import android.app.TimePickerDialog
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime



class AddInfoActivity : AppCompatActivity() {

    companion object {
        private const val PREF_NAME = "LoginPrefs"
        private const val KEY_LOGGED_IN_USER_ID = "loggedInUserId"
    }


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
        val prefs: SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentUserId: String? = prefs.getString(KEY_LOGGED_IN_USER_ID, null)

        if (currentUserId == null) {
            Toast.makeText(this, "Error: User not logged in. Cannot add event.", Toast.LENGTH_LONG).show()
            Log.e("AddInfoActivity", "Attempted to add event without a logged-in user ID.")
            // Optionally, navigate back to login or handle this error gracefully
            finish()
            return
        }

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
    var isLocationValid by remember { mutableStateOf(true) }
    var isTimeValid by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                val formatter = SimpleDateFormat("hh:mm a", Locale.US)
                time = formatter.format(cal.time)
                isTimeValid = true
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
    }

    fun validateLocation(location: String): Boolean {
        val allowedCities = listOf("New York", "Los Angeles", "Chicago", "Rexburg")
        return location.isNotBlank() &&
                allowedCities.any { it.equals(location.trim(), ignoreCase = true) }
    }

    fun validateTime(time: String): Boolean {
        return try {
            val format = SimpleDateFormat("hh:mm a", Locale.US)
            format.isLenient = false
            format.parse(time)
            true
        } catch (e: Exception) {
            false
        }
    }

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

        // Time Picker Field
        OutlinedTextField(
            value = time,
            onValueChange = {}, // disable text editing
            label = { Text("Time") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { timePickerDialog.show() }, // opens the dialog
            readOnly = true, // blocks keyboard
            enabled = true, // keep it visually active
            isError = !isTimeValid,
            trailingIcon = {
                Icon(Icons.Default.AccessTime, contentDescription = "Select Time")
            }
        )


        if (!isTimeValid && time.isNotBlank()) {
            Text(
                text = "Invalid time.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedTextField(
            value = location,
            onValueChange = {
                location = it
                isLocationValid = true
            },
            label = { Text("Location") },
            isError = !isLocationValid,
            modifier = Modifier.fillMaxWidth()
        )

        if (!isLocationValid && location.isNotBlank()) {
            Text(
                text = "Invalid location. Only New York, Los Angeles, Chicago, or Rexburg allowed.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val isLocValidNow = validateLocation(location)
            val isTimeValidNow = validateTime(time)

            isLocationValid = isLocValidNow
            isTimeValid = isTimeValidNow

            if (isDateValid && isLocValidNow && isTimeValidNow) {
                onSubmit(name, date, time, location)
            }
        }) {
            Text("Submit")
        }
    }
}


