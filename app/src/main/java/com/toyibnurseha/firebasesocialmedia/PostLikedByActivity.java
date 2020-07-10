package com.toyibnurseha.firebasesocialmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.toyibnurseha.firebasesocialmedia.adapters.AdapterUsers;
import com.toyibnurseha.firebasesocialmedia.models.ModelUsers;

import java.util.ArrayList;
import java.util.List;

public class PostLikedByActivity extends AppCompatActivity {

    String postId;
    RecyclerView recyclerView;

    private List<ModelUsers> usersList;
    private AdapterUsers adapterUsers;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_liked_by);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Liked By");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        recyclerView = findViewById(R.id.rv_liked_by);

        firebaseAuth = FirebaseAuth.getInstance();

        actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail());

        //get the postId
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        usersList = new ArrayList<>();

        //get the list of UID of users who liked the post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Likes");
        ref.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String hisUid = ""+ds.getRef().getKey();

                    //get user info from each id
                    getUsers(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUsers(String hisUid) {
        //get information by uid
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                            usersList.add(modelUsers);
                        }

                        adapterUsers = new AdapterUsers(PostLikedByActivity.this, usersList);
                        recyclerView.setAdapter(adapterUsers);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}