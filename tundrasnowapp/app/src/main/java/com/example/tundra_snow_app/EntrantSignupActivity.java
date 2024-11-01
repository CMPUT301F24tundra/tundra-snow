package com.example.tundra_snow_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class EntrantSignupActivity extends AppCompatActivity{
    private Button createAccountButton;
    private Button backButton;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText dateOfBirthEditText;
    private EditText phoneNumberEditText;

    private ToggleButton notificationToggleButton;

    // adding toggle for notifications and gelocation
    // if gelolication toggled on, add a field to input location

    // profile pic setup idk how



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_signup_activity);

        Intent intent = getIntent();

        HashMap<String, String> userData = new HashMap<>();


        backButton = findViewById(R.id.backButton);
        firstNameEditText = findViewById(R.id.editTextFirstName);
        lastNameEditText = findViewById(R.id.editTextLastName);
        emailEditText = findViewById(R.id.editTextEmail);
        dateOfBirthEditText = findViewById(R.id.editTextDateOfBirth);
        phoneNumberEditText = findViewById(R.id.editTextPhoneNumber);
        passwordEditText = findViewById(R.id.editTextPassword);
        notificationToggleButton = findViewById(R.id.toggleButtonNotification);

    }
}
