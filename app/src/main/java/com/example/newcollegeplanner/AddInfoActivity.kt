package com.example.newcollegeplanner

// Standard Android Imports
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast

// NEW IMPORTS FOR DATE/TIME PICKERS CALLBACKS
import android.widget.DatePicker // Make sure this is here for DatePickerDialog's callback
import android.widget.TimePicker // Make sure this is here for TimePickerDialog's callback

// Jetpack Compose Imports
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.* // For Material Design 3 Composables
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color // For setting specific colors if needed

// Retrofit and Data Model Imports
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Java Date/Time Utilities
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.* // For Calendar, Date, Locale


class AddInfoActivity : AppCompatActivity() {

    companion object {
        private const val PREF_NAME = "LoginPrefs"
        private const val KEY_LOGGED_IN_USER_ID = "loggedInUserId"
    }

    // State for loading indicator (managed at Activity level for persistence)
    private var isLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val context = LocalContext.current
                AddInfoScreen(
                    onSubmit = { name, date, time, location ->
                        // Show loading indicator
                        isLoading = true
                        sendToApi(name, date, time, location)
                    },
                    context = context,
                    isLoading = isLoading, // Pass current loading state
                    onBackClick = { finish() } // Pass back navigation callback
                )
            }
        }
    }

    private fun sendToApi(event_name: String, date: String, time: String, location: String) {
        val prefs: SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentUserId: String? = prefs.getString(KEY_LOGGED_IN_USER_ID, null)

        if (currentUserId == null) {
            Toast.makeText(this, "Error: User not logged in. Cannot add event.", Toast.LENGTH_LONG).show()
            Log.e("AddInfoActivity", "Attempted to add event without a logged-in user ID. User ID was null.")
            isLoading = false // Hide loading if error
            finish()
            return
        }

        Log.d("AddInfoActivity", "Sending event with creatorId: $currentUserId")

        val event = Event(event_name, date, time, location, creatorId = currentUserId)
        val call = RetrofitInstance.api.addEvent(event)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                isLoading = false // Hide loading on response
                if (response.isSuccessful) {
                    Toast.makeText(this@AddInfoActivity, "Event added!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Log.e("AddInfoActivity", "API Response Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@AddInfoActivity, "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                isLoading = false // Hide loading on failure
                Log.e("AddInfoActivity", "Error: ${t.message}")
                Toast.makeText(this@AddInfoActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInfoScreen(
    onSubmit: (String, String, String, String) -> Unit,
    context: Context,
    isLoading: Boolean, // Receive loading state from Activity
    onBackClick: () -> Unit // Receive back navigation callback from Activity
) {
    // State variables for input fields
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // State variables for validation feedback
    var isNameValid by remember { mutableStateOf(true) }
    var isDateValid by remember { mutableStateOf(true) }
    var isTimeValid by remember { mutableStateOf(true) }
    var isLocationValid by remember { mutableStateOf(true) }


    // --- Date Picker Dialog setup ---
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDayOfMonth)
                }
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                date = dateFormat.format(selectedCalendar.time)
                isDateValid = true
                Log.d("DatePicker", "Date selected: $date")
            }, year, month, day
        )
    }
    datePickerDialog.setOnDismissListener { Log.d("DatePicker", "DatePickerDialog dismissed") }
    datePickerDialog.setOnCancelListener { Log.d("DatePicker", "DatePickerDialog cancelled") }


    // --- Time Picker Dialog setup ---
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
                time = timeFormat.format(selectedCalendar.time)
                isTimeValid = true
                Log.d("TimePicker", "Time selected: $time")
            }, hour, minute, false // 12-hour format
        )
    }
    timePickerDialog.setOnDismissListener { Log.d("TimePicker", "TimePickerDialog dismissed") }
    timePickerDialog.setOnCancelListener { Log.d("TimePicker", "TimePickerDialog cancelled") }


    // --- Validation functions (moved inside Composable for easier access to state) ---
    fun validateName(name: String): Boolean = name.isNotBlank()

    fun validateLocation(loc: String): Boolean {
        val allowedCities = listOf("New York", "Los Angeles", "Chicago", "Rexburg")
        return loc.isNotBlank() && allowedCities.any { it.equals(loc.trim(), ignoreCase = true) }
    }


    // --- UI Layout for the Add Event Screen using Scaffold ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Event", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { // Back button action
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary) // Toolbar color
            )
        },
        containerColor = Color.White // Set background color of the content area
    ) { paddingValues -> // PaddingValues provided by Scaffold to avoid system overlaps
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 16.dp, vertical = 24.dp), // Additional inner padding
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Event Name Input Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    isNameValid = validateName(it)
                },
                label = { Text("Event Name") },
                isError = !isNameValid,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
            if (!isNameValid && name.isNotBlank()) {
                Text(
                    text = "Event Name cannot be empty.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }


            // Date Input Field (Read-only, clickable to trigger DatePickerDialog)
            OutlinedTextField(
                value = date,
                onValueChange = { /* Value is set by DatePickerDialog, so this is read-only */ },
                label = { Text("Date (MM/DD/YYYY)") },
                readOnly = true,
                isError = !isDateValid,
                trailingIcon = { // Calendar icon
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { // Makes the whole TextField clickable
                        Log.d(
                            "DatePicker",
                            "Date field clicked! Attempting to show DatePickerDialog."
                        )
                        datePickerDialog.show() // Show the date picker dialog
                    }
            )
            if (!isDateValid && date.isNotBlank()) {
                Text(
                    text = "Please select a date.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }


            // Time Input Field (Read-only, clickable to trigger TimePickerDialog)
            OutlinedTextField(
                value = time,
                onValueChange = { /* Value is set by TimePickerDialog, so this is read-only */ },
                label = { Text("Time (HH:MM AM/PM)") },
                readOnly = true,
                isError = !isTimeValid,
                trailingIcon = { // Clock icon
                    Icon(Icons.Default.AccessTime, contentDescription = "Select Time")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { // Makes the whole TextField clickable
                        Log.d(
                            "TimePicker",
                            "Time field clicked! Attempting to show TimePickerDialog."
                        )
                        timePickerDialog.show() // Show the time picker dialog
                    }
            )
            if (!isTimeValid && time.isNotBlank()) {
                Text(
                    text = "Please select a time.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }


            // Location Input Field
            OutlinedTextField(
                value = location,
                onValueChange = {
                    location = it
                    isLocationValid = validateLocation(it) // Validate on change for location
                },
                label = { Text("Location") },
                isError = !isLocationValid,
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            )
            if (!isLocationValid && location.isNotBlank()) {
                Text(
                    text = "Invalid location. Only New York, Los Angeles, Chicago, or Rexburg allowed.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    // Re-validate all fields on click to ensure final feedback before submission
                    val isNameValidNow = validateName(name)
                    val isDateSelectedNow = date.isNotBlank()
                    val isTimeSelectedNow = time.isNotBlank()
                    val isLocationValidNow = validateLocation(location)

                    // Update state variables for visual error feedback
                    isNameValid = isNameValidNow
                    isDateValid = isDateSelectedNow
                    isTimeValid = isTimeSelectedNow
                    isLocationValid = isLocationValidNow

                    // Submit only if all fields are valid
                    if (isNameValidNow && isDateSelectedNow && isTimeSelectedNow && isLocationValidNow) {
                        onSubmit(name, date, time, location)
                    } else {
                        Toast.makeText(
                            context,
                            "Please correct the errors and fill all fields.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading // Disable button while loading
            ) {
                Text("Submit Event")
            }

            // --- Loading Indicator ---
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
            // --- END NEW ---
        }
    }
}