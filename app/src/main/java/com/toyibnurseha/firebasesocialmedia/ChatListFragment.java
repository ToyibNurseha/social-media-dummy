package com.toyibnurseha.firebasesocialmedia;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
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
import com.toyibnurseha.firebasesocialmedia.adapters.AdapterChatlist;
import com.toyibnurseha.firebasesocialmedia.models.ModelChat;
import com.toyibnurseha.firebasesocialmedia.models.ModelChatlist;
import com.toyibnurseha.firebasesocialmedia.models.ModelUsers;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    DatabaseReference reference;
    RecyclerView recyclerView;
    AdapterChatlist adapterChatlist;
    List<ModelChatlist> chatlistList;
    List<ModelUsers> usersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_chat, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = view.findViewById(R.id.rv_chat);

        chatlistList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChatlist chatlist = ds.getValue(ModelChatlist.class);
                    chatlistList.add(chatlist);

                    loadChats();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void loadChats() {
        usersList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                    for (ModelChatlist chatlist : chatlistList) {
                        if (modelUsers.getUid() != null && modelUsers.getUid().equals(chatlist.getId())) {
                            usersList.add(modelUsers);
                            break;
                        }
                    }

                    adapterChatlist = new AdapterChatlist(getContext(), usersList);
                    recyclerView.setAdapter(adapterChatlist);

                    for (int i = 0; i < usersList.size(); i++) {
                        lastMessage(usersList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat == null) {
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if (sender == null || receiver == null) {
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) &&
                            chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(currentUser.getUid())) {
                        if (chat.getType().equals("image")) {
                            theLastMessage = "Sent a photo";
                        } else {
                            theLastMessage = chat.getMessage();
                        }
                    }
                }
                adapterChatlist.setLastMessageMap(userId, theLastMessage);
                adapterChatlist.notifyDataSetChanged();
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
        } else if (id == R.id.action_settings) {
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