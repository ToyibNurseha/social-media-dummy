package com.toyibnurseha.firebasesocialmedia;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.toyibnurseha.firebasesocialmedia.adapters.AdapterUsers;
import com.toyibnurseha.firebasesocialmedia.models.ModelUsers;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUsers> usersList;
    FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_users, container, false);
        recyclerView = v.findViewById(R.id.user_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        firebaseAuth = FirebaseAuth.getInstance();

        //init user list
        usersList = new ArrayList<>();

        //getAll Users
        getAllUsers();

        return v;
    }

    private void getAllUsers() {
        //get current user
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    //get all users except signed in users
                    if (!modelUsers.getUid().equals(firebaseUser.getUid())) {
                        usersList.add(modelUsers);
                    }

                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(), usersList);
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //  inflate options menu

    private void searchUsers(final String text) {
        //get current user
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    //search condition rules
                    // user not current user
                    // the username or email containst text entered in searchview(case sensitive)

                    //get all search users except signed in users
                    if (!modelUsers.getUid().equals(firebaseUser.getUid())) {
                        if (modelUsers.getName().toLowerCase().contains(text.toLowerCase()) || modelUsers.getEmail().toLowerCase().contains(text.toLowerCase())) {
                            usersList.add(modelUsers);
                        }
                    }

                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(), usersList);
                    //refresh adapter
                    adapterUsers.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflateing menu
        inflater.inflate(R.menu.menu_main, menu);

        //hide add post icon
        menu.findItem(R.id.action_add_post).setVisible(false);

        //searchview
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button from keyboard
                if (!TextUtils.isEmpty(query.trim())) {
                    //search text containts text
                    searchUsers(query);
                } else {
                    //search text empty, get all users
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    //search text containts text
                    searchUsers(newText);
                } else {
                    //search text empty, get all users
                    getAllUsers();
                }
                return false;
            }
        });
        //searchView listener

        super.onCreateOptionsMenu(menu, inflater);
    }

    //handle menu item click
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true); //to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get Item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        } else if (id == R.id.action_settings){
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user signed in stay here
            //set email
        } else {
            //user not signed in, go to mainActivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

}