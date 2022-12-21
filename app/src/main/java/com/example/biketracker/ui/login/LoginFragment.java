package com.example.biketracker.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.biketracker.Connect;
import com.example.biketracker.LoginActivity;
import com.example.biketracker.MainActivity;
import com.example.biketracker.R;

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;

public class LoginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        Button btnRegister = rootView.findViewById(R.id.buttonCreateAccount);

        btnRegister.setOnClickListener(view -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, RegisterFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("name")
                    .commit();
        });

        Button btnLogin = rootView.findViewById(R.id.buttonLogin);
        btnLogin.setOnClickListener(view -> {
            EditText email = rootView.findViewById(R.id.editTextEmailAddress);
            EditText password = rootView.findViewById(R.id.editTextPassword);
            Connect connect = new Connect();
            connect.initialize();

            AtomicInteger check = new AtomicInteger(0);
            try {
                check.set(connect.read(email.getText().toString(), password.getText().toString()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String LOG_TAG = LoginActivity.class.getSimpleName();

            switch (check.get()) {
                case 0:
                    Log.d(LOG_TAG, "wrong email");
                    //wrong email
                    break;
                case 1:
                    Log.d(LOG_TAG, "wrong password");
                    //wrong password;
                    break;
                case 2:
                    //success
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                default:
                    //error
            }
        });
        return rootView;
    }

    public void switchToMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }
}