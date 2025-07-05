package com.example.newcollegeplanner;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast; // Import for Toast

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import for Toolbar
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class AllEventsActivity extends AppCompatActivity {

    private ApiService apiService;
    private RecyclerView recyclerView; // Declare RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_events); // Set the layout

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarAllEvents);
        setSupportActionBar(toolbar);
        // Display a back arrow to return to MainActivity
        if (getSupportActionBar() != null) { // Null check for getSupportActionBar()
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
        }

        recyclerView = findViewById(R.id.recyclerViewAllEvents); // Initialize RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Retrofit (same base URL as MainActivity)
        Retrofit retrofit = new Retrofit.Builder()

                .baseUrl("https://68117ac53ac96f7119a4aa8d.mockapi.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        loadAllEvents(); // Call to load events
    }

    // Handle the back arrow click in the Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Finish this activity to go back to the previous one
        return true;
    }

    private void loadAllEvents() {
        apiService.getEvents().enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Event> allEvents = response.body();
                    List<Event> upcomingThisMonth = new ArrayList<>();
                    List<Event> otherUpcoming = new ArrayList<>();
                    Date now = Calendar.getInstance().getTime();

                    // Get current month and year for filtering
                    Calendar currentMonthCal = Calendar.getInstance();
                    int currentMonth = currentMonthCal.get(Calendar.MONTH);
                    int currentYear = currentMonthCal.get(Calendar.YEAR);

                    for (Event event : allEvents) {
                        Date eventDateTime = parseEventDateTime(event.getDate(), event.getTime());

                        if (eventDateTime != null && eventDateTime.after(now)) { // Is upcoming?
                            Calendar eventCal = Calendar.getInstance();
                            eventCal.setTime(eventDateTime);

                            if (eventCal.get(Calendar.MONTH) == currentMonth && eventCal.get(Calendar.YEAR) == currentYear) {
                                upcomingThisMonth.add(event); // Upcoming AND this month
                            } else {
                                otherUpcoming.add(event); // Upcoming but NOT this month
                            }
                        }
                    }

                    // Sort both lists chronologically
                    Comparator<Event> eventComparator = new Comparator<Event>() {
                        @Override
                        public int compare(Event e1, Event e2) {
                            Date d1 = parseEventDateTime(e1.getDate(), e1.getTime());
                            Date d2 = parseEventDateTime(e2.getDate(), e2.getTime());
                            if (d1 == null || d2 == null) return 0; // Handle unparsable dates
                            return d1.compareTo(d2);
                        }
                    };

                    Collections.sort(upcomingThisMonth, eventComparator);
                    Collections.sort(otherUpcoming, eventComparator);

                    // Combine them: This month's upcoming events first, then others
                    List<Event> combinedEvents = new ArrayList<>();
                    combinedEvents.addAll(upcomingThisMonth);
                    combinedEvents.addAll(otherUpcoming);

                    Log.d("AllEventsActivity", "Total events from API: " + allEvents.size());
                    Log.d("AllEventsActivity", "Upcoming this month: " + upcomingThisMonth.size());
                    Log.d("AllEventsActivity", "Other upcoming: " + otherUpcoming.size());
                    Log.d("AllEventsActivity", "Combined events to display: " + combinedEvents.size());

                    recyclerView.setAdapter(new EventAdapter(combinedEvents)); // Set the adapter

                } else {
                    Log.e("AllEventsActivity", "API Response Error: " + response.code() + " - " + response.message());
                    Toast.makeText(AllEventsActivity.this, "Failed to load events: Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Log.e("AllEventsActivity", "API Call Failed: " + t.getMessage());
                Toast.makeText(AllEventsActivity.this, "Failed to load events: Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper function to parse date and time strings into a Date object
    @Nullable
    private Date parseEventDateTime(String dateString, String timeString) {
        String dateTimeString = dateString + " " + timeString;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US);

        try {
            return dateFormat.parse(dateTimeString);
        } catch (ParseException e) {
            Log.e("AllEventsActivity", "Error parsing date/time: " + dateTimeString + " - " + e.getMessage());
            return null;
        }
    }
}