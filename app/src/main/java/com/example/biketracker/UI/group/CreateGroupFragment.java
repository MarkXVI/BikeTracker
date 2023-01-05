package com.example.biketracker.UI.group;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.biketracker.DB.Connect;
import com.example.biketracker.DB.Group;
import com.example.biketracker.DB.GroupDAO;
import com.example.biketracker.DB.SaveSharedPreference;
import com.example.biketracker.R;

import org.bson.types.ObjectId;

import java.util.ArrayList;

public class CreateGroupFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_group, container, false);
        Button btnCreateGroup = rootView.findViewById(R.id.buttonCreateGroup);
        btnCreateGroup.setOnClickListener(view -> {
            EditText name = rootView.findViewById(R.id.editTextCreateGroupName);
            if (name.length() == 0) {
                Log.e("CREATE GROUP", "Group name can't be empty");
                return;
            }
            String email = SaveSharedPreference.getEmail(getContext());

            ObjectId id = new ObjectId();
            Group group = new Group(id, name.getText().toString(), new ArrayList<>());

            GroupDAO groupDAO = new GroupDAO();
            groupDAO.initialize(() -> groupDAO.create(group, check1 -> {
                if (check1.get() == 0) {
                    Log.e("CREATE GROUP", "Could not create the group");
                    return;
                }
                Log.v("CREATE GROUP", "Successfully created a group");
                Connect connect = new Connect();
                connect.initialize(() -> connect.addGroupToUser(id, email, check2 -> {
                    if (check2.get() == 0)
                        Log.e("ADD GROUP TO USER", "Could not add the group to the user");
                    else Log.v("ADD GROUP TO USER", "Added the group to the user");
                }));
            }));
        });
        return rootView;
    }
}

