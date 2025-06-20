package com.example.newcollegeplanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // Make sure TextView is imported if not already
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// These imports must be correct based on your package structure
import com.example.newcollegeplanner.ApiService;
import com.example.newcollegeplanner.User;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewForgotPassword; // NEW: Declare TextView for "Forgot Password?" link
    private TextView textViewSignUp;         // Also declare if you use "Sign Up Here" TextView

    private ApiService apiService; // Declare ApiService instance

    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_LOGGED_IN_USER_ID = "loggedInUserId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check login status first
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

        if (isLoggedIn) {
            navigateToMainActivity();
            return;
        }

        setContentView(R.layout.activity_login); // Set the layout for the login screen

        // Initialize UI elements from activity_login.xml
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword); // NEW: Find "Forgot Password?" TextView
        textViewSignUp = findViewById(R.id.textViewSignUp);                 // Find "Sign Up Here" TextView

        // Initialize Retrofit and ApiService for API calls
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://68117ac53ac96f7119a4aa8d.mockapi.io/") // Make sure this is your CORRECT MockAPI.io Base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Set OnClickListener for the Login Button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        // NEW: Set OnClickListener for "Forgot Password?" TextView
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent); // Start the ForgotPasswordActivity
            }
        });


    }

    // Method to handle the login attempt via API
    private void attemptLogin() {
        String enteredUsername = editTextUsername.getText().toString().trim();
        String enteredPassword = editTextPassword.getText().toString().trim();

        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();
                    boolean isAuthenticated = false;
                    String loggedInUserId = null; // NEW: To store the ID of the authenticated user

                    for (User user : users) {
                        if (user.getUsername().equals(enteredUsername) && user.getPassword().equals(enteredPassword)) {
                            isAuthenticated = true;
                            loggedInUserId = user.getId(); // NEW: Get the user's ID
                            break;
                        }
                    }

                    if (isAuthenticated) {
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(KEY_IS_LOGGED_IN, true);
                        editor.putString(KEY_LOGGED_IN_USER_ID, loggedInUserId); // NEW: Save the user's ID
                        editor.apply();
                        navigateToMainActivity();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Log.e("LoginActivity", "API Response Error: " + response.code() + " - " + response.message());
                    Toast.makeText(LoginActivity.this, "Login failed: Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e("LoginActivity", "API Call Failed: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Login failed: Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Helper method to navigate to MainActivity and finish LoginActivity
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}