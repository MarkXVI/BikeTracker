package com.example.biketracker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.biketracker.DB.Connect;

import io.realm.Realm;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Connect connect = new Connect();
        Realm.init(this);

    }
}