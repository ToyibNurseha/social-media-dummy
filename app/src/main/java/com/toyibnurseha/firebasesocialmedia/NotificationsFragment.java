package com.toyibnurseha.firebasesocialmedia;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.toyibnurseha.firebasesocialmedia.adapters.AdapterNotification;
import com.toyibnurseha.firebasesocialmedia.models.ModelNotification;

import java.util.ArrayList;


public class NotificationsFragment extends Fragment {

    RecyclerView recyclerView;

    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelNotification> notificationsList;
    private AdapterNotification adapterNotification;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        recyclerView = view.findViewById(R.id.rv_notification);

        firebaseAuth = FirebaseAuth.getInstance();

        getAllNotification();
        return view;
    }

    private void getAllNotification() {
        notificationsList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelNotification modelNotification = ds.getValue(ModelNotification.class);

                            //add to list
                            notificationsList.add(modelNotification);
                        }

                        adapterNotification = new AdapterNotification(getActivity(), notificationsList);
                        recyclerView.setAdapter(adapterNotification);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}