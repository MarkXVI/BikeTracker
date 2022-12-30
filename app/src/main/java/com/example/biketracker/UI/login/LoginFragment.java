package com.example.biketracker.UI.login;

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

import com.example.biketracker.DB.Connect;
import com.example.biketracker.MainActivity;
import com.example.biketracker.R;
import com.example.biketracker.UI.register.RegisterFragment;

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
            EditText email = rootView.findViewById(R.id.editTextLoginEmail);
            EditText password = rootView.findViewById(R.id.editTextLoginPassword);
            Connect connect = new Connect();
            connect.initialize();
            connect.read(email.getText().toString(), password.getText().toString(), check -> {

                switch (check.get()) {
                    case 0:
                        Log.v("LoginFragment", "Success");
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        Log.e("LoginFragment", "Wrong email");
                        break;
                    case 2:
                        Log.e("LoginFragment", "Wrong password");
                        break;
                    default:
                        Log.e("LoginFragment", "Error");
                        break;
                }
            });
        });
        return rootView;
    }
}