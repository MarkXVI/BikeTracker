package com.example.biketracker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.realm.Realm;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Realm.init(this);
    }
}