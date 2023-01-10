package com.example.biketracker.UI.checkpoint;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.biketracker.DB.DAOs.UserDAO;
import com.example.biketracker.DB.SaveSharedPreference;
import com.example.biketracker.R;
import com.example.biketracker.UI.map.MapFragment;

import java.util.ArrayList;

public class CreateCheckpointFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_checkpoints, container, false);

        UserDAO userDAO = new UserDAO();
        userDAO.initialize();

        Button btnCreateGroup = rootView.findViewById(R.id.buttonCreateCheckpoint);
        btnCreateGroup.setOnClickListener(view -> {

            String email = SaveSharedPreference.getEmail(getContext());

            EditText checkpointName = rootView.findViewById(R.id.editTextCreateCheckpointName);
            if (checkpointName.length() == 0) {
                Log.e("CREATE CHECKPOINT", "Checkpoint name can't be empty");
                return;
            }
            EditText latitude = rootView.findViewById(R.id.editTextCreateLatitude);
            if (latitude.length() == 0) {
                Log.e("CREATE CHECKPOINT", "latitude can't be empty");
                return;
            }
            EditText longitude = rootView.findViewById(R.id.editTextCreateLongitude);
            if (longitude.length() == 0) {
                Log.e("CREATE CHECKPOINT", "longitude can't be empty");
                return;
            }

            userDAO.addCheckPoint(checkpointName.getText().toString(), latitude.getText().toString(), longitude.getText().toString(), email, check -> {
                if (check.get() == 0) {
                    Log.e("CREATE CHECKPOINT", "Could not create the checkpoint");
                    return;
                }
                Log.v("CREATE CHECKPOINT", "Successfully created a checkpoint");
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_content_main, MapFragment.class, null)
                        .setReorderingAllowed(true)
                        .addToBackStack("name")
                        .commit();

            });
        });
        return rootView;
    }
}

