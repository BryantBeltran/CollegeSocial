package com.example.newcollegeplanner;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton; // Import for MaterialButton

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextResetEmail;
    private MaterialButton buttonResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editTextResetEmail = findViewById(R.id.editTextResetEmail);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);

        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptResetPassword();
            }
        });
    }

    private void attemptResetPassword() {
        String email = editTextResetEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Mock Password Reset Logic ---
        // In a real app, you would make an API call here to your backend
        // to send a password reset link to the provided email.
        // For now, we'll just simulate success.

        Toast.makeText(this, "Password reset link sent to " + email + ".", Toast.LENGTH_LONG).show();
        finish(); // Close this activity and go back to LoginActivity
    }
}