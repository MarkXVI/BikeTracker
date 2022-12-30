package com.example.biketracker.UI.register;

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
import com.example.biketracker.R;
import com.example.biketracker.UI.login.LoginFragment;

public class RegisterFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);
        Button btnRegister = rootView.findViewById(R.id.buttonRegister);

        btnRegister.setOnClickListener(view -> {
            EditText name = rootView.findViewById(R.id.editTextRegisterName);
            EditText email = rootView.findViewById(R.id.editTextRegisterEmail);
            EditText password = rootView.findViewById(R.id.editTextRegisterPassword);

            Connect connect = new Connect();
            connect.initialize();

            connect.read(email.getText().toString(), password.getText().toString(), check1 -> {
                if (check1.get() != 1) Log.e("RegisterFragment", "Email Already Exists");
                else {
                    connect.create(name.getText().toString(), email.getText().toString(), password.getText().toString(), check2 -> {
                        if (check2.get() == 0) Log.e("RegisterFragment", "Error");
                        else {
                            Log.v("RegisterFragment", "Success");
                            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                            fragmentManager.beginTransaction()
                                    .replace(R.id.fragmentContainerView, LoginFragment.class, null)
                                    .setReorderingAllowed(true)
                                    .addToBackStack("name")
                                    .commit();
                        }
                    });
                }
            });
        });
        return rootView;
    }
}