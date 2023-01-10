package com.example.biketracker.UI.group;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.biketracker.DB.DAOs.GroupDAO;
import com.example.biketracker.DB.DAOs.UserDAO;
import com.example.biketracker.DB.SaveSharedPreference;
import com.example.biketracker.DB.Schemas.Group;
import com.example.biketracker.R;
import com.example.biketracker.UI.device.DevicesFragment;

import org.bson.types.ObjectId;

import java.util.ArrayList;

public class CreateGroupFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_group, container, false);

        UserDAO userDAO = new UserDAO();
        userDAO.initialize();

        GroupDAO groupDAO = new GroupDAO();
        groupDAO.initialize();

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

            groupDAO.create(group, check1 -> {
                if (check1.get() == 0) {
                    Log.e("CREATE GROUP", "Could not create the group");
                    return;
                }
                Log.v("CREATE GROUP", "Successfully created a group");
                userDAO.addGroupToUser(id, email, check2 -> {
                    if (check2.get() == 0) {
                        Log.e("ADD GROUP TO USER", "Could not add the group to the user");
                        return;
                    }
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerViewGroupsAndDevices, GroupsFragment.class, null)
                            .setReorderingAllowed(true)
                            .addToBackStack("name")
                            .commit();

                });
            });
        });
        return rootView;
    }
}

