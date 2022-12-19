package com.example.biketracker.ui.register;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.biketracker.R;
import com.example.biketracker.databinding.FragmentLoginBinding;
import com.example.biketracker.databinding.FragmentRegisterBinding;
import com.example.biketracker.ui.login.LoginFragment;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;

    public RegisterFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.LoginBtn.setOnClickListener(v -> NavHostFragment.findNavController(RegisterFragment.this)
                .navigate(R.id.action_nav_register_to_nav_login));
    }
}