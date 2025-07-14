package com.example.newcollegeplanner;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CalendarEventsActivity extends AppCompatActivity {

    private ApiService apiService;
    private RecyclerView recyclerView;
    private CalendarView calendarView;
    private TextView textSelectedDate;
    private TextView textNoEvents;
    private List<Event> allEvents = new ArrayList<>();
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
    private SimpleDateFormat eventDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_events);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarCalendar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize views
        calendarView = findViewById(R.id.calendarView);
        textSelectedDate = findViewById(R.id.textSelectedDate);
        textNoEvents = findViewById(R.id.textNoEvents);
        recyclerView = findViewById(R.id.recyclerViewCalendarEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://68117ac53ac96f7119a4aa8d.mockapi.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Set up calendar date change listener
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // Create selected date
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth);
                Date selectedDate = selectedCalendar.getTime();
                
                // Update selected date text
                textSelectedDate.setText("Events for " + displayDateFormat.format(selectedDate));
                
                // Filter and display events for selected date
                filterEventsByDate(selectedDate);
            }
        });

        // Load all events from API
        loadAllEvents();
        
        // Show today's events initially
        Calendar today = Calendar.getInstance();
        textSelectedDate.setText("Events for " + displayDateFormat.format(today.getTime()));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadAllEvents() {
        apiService.getEvents().enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allEvents = response.body();
                    Log.d("CalendarEventsActivity", "Total events loaded: " + allEvents.size());
                    
                    // Show today's events initially
                    Calendar today = Calendar.getInstance();
                    filterEventsByDate(today.getTime());
                    
                    // Log all event dates for debugging
                    for (Event event : allEvents) {
                        Log.d("CalendarEventsActivity", "Event: " + event.getEvent_name() + " on " + event.getDate() + " at " + event.getTime());
                    }
                    
                } else {
                    Log.e("CalendarEventsActivity", "API Response Error: " + response.code() + " - " + response.message());
                    Toast.makeText(CalendarEventsActivity.this, "Failed to load events: Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Log.e("CalendarEventsActivity", "API Call Failed: " + t.getMessage());
                Toast.makeText(CalendarEventsActivity.this, "Failed to load events: Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterEventsByDate(Date selectedDate) {
        List<Event> eventsForDate = new ArrayList<>();
        String selectedDateString = eventDateFormat.format(selectedDate);
        
        Log.d("CalendarEventsActivity", "Filtering events for date: " + selectedDateString);
        
        for (Event event : allEvents) {
            if (event.getDate().equals(selectedDateString)) {
                eventsForDate.add(event);
                Log.d("CalendarEventsActivity", "Found event for selected date: " + event.getEvent_name());
            }
        }
        
        Log.d("CalendarEventsActivity", "Events found for " + selectedDateString + ": " + eventsForDate.size());
        
        // Sort events by time for the selected date
        eventsForDate.sort((e1, e2) -> {
            Date time1 = parseEventTime(e1.getTime());
            Date time2 = parseEventTime(e2.getTime());
            if (time1 == null || time2 == null) return 0;
            return time1.compareTo(time2);
        });
        
        // Update RecyclerView with events for selected date
        recyclerView.setAdapter(new CalendarEventAdapter(eventsForDate));
        
        // Show/hide empty state
        if (eventsForDate.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            textNoEvents.setVisibility(View.VISIBLE);
            textSelectedDate.setText(textSelectedDate.getText() + " (No events)");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textNoEvents.setVisibility(View.GONE);
            textSelectedDate.setText(textSelectedDate.getText() + " (" + eventsForDate.size() + " event" + (eventsForDate.size() == 1 ? "" : "s") + ")");
        }
    }

    @Nullable
    private Date parseEventTime(String timeString) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        try {
            return timeFormat.parse(timeString);
        } catch (ParseException e) {
            Log.e("CalendarEventsActivity", "Error parsing time: " + timeString + " - " + e.getMessage());
            return null;
        }
    }

    @Nullable
    private Date parseEventDateTime(String dateString, String timeString) {
        String dateTimeString = dateString + " " + timeString;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US);

        try {
            return dateFormat.parse(dateTimeString);
        } catch (ParseException e) {
            Log.e("CalendarEventsActivity", "Error parsing date/time: " + dateTimeString + " - " + e.getMessage());
            return null;
        }
    }
}
