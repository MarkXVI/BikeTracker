package com.example.biketracker.UI.group;

import android.os.Bundle;
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
            EditText groupName = rootView.findViewById(R.id.editTextCreateGroupName);
            if (groupName.length() == 0) return;
            ObjectId groupId = new ObjectId();
            Group group = new Group(groupId, groupName.getText().toString(), new ArrayList<>());
            groupDAO.create(group, check1 -> {
                if (check1.get() == 0) return;
                userDAO.addGroupToUser(groupId, SaveSharedPreference.getEmail(getContext()), check2 -> {
                    if (check2.get() == 0) return;
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerViewGroupsAndDevices, GroupsFragment.class, null)
                            .setReorderingAllowed(true)
                            .addToBackStack("name")
                            .commit();
                });
            });
        });

        Button btnCreateGroupBack = rootView.findViewById(R.id.buttonCreateGroupBack);
        btnCreateGroupBack.setOnClickListener(view -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerViewGroupsAndDevices, GroupsFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("name")
                    .commit();
        });
        return rootView;
    }
}

