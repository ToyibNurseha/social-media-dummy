package com.toyibnurseha.firebasesocialmedia.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.toyibnurseha.firebasesocialmedia.ChatActivity;
import com.toyibnurseha.firebasesocialmedia.R;
import com.toyibnurseha.firebasesocialmedia.ThereProfileActivity;
import com.toyibnurseha.firebasesocialmedia.models.ModelUsers;

import java.util.HashMap;
import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    Context context;
    List<ModelUsers> usersList;

    FirebaseAuth firebaseAuth;
    String myUid;

    public AdapterUsers(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_user, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {


        final String hisUid = usersList.get(position).getUid();
        String userImage = usersList.get(position).getImage();
        final String userName = usersList.get(position).getName();
        String userEmail = usersList.get(position).getEmail();

        //check if each user if is blocked or not
        checkIsBlocked(hisUid, holder, position);

        //set data
        holder.mNameTv.setText(userName);
        holder.mEmailTv.setText(userEmail);
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img_rv).into(holder.avatarIv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            //profile clicked
                            Intent intent = new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid", hisUid);
                            context.startActivity(intent);
                        }
                        if (which==1){
                            //chat clicked
                            /*
                            click user from user list to start chatting messaging
                            start activity by putting UID of receiver
                            we will use that uid to identify the user we are gonna chat
                             */
                            imBlockedOrNot(hisUid);
                        }
                    }
                });
                builder.create().show();
            }
        });

//        click to block
        holder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usersList.get(position).isBlocked()){
                    unBlockUser(hisUid, holder);
                }else {
                    blockUser(hisUid, holder);
                }
            }
        });
    }

    private void imBlockedOrNot(final String hisUid){
        /*
        1. check if sender (current user) is blocked by receiver or not
        2. logic : if uid of the sendet (current user) exists in "Blockedusers" of receiver then sender is blocked, otherwise not
        3. if blocked then just display a message e.g. You're blocked by that user, can't send message
        4. if not blocked simply start chat activity
         */
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                Toast.makeText(context, "You're blocked by the user", Toast.LENGTH_SHORT).show();
                                //don't proceed further
                                return;
                            }
                        }
                        //not blocked, start activity
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid", hisUid);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsBlocked(String hisUid, final MyHolder holder, final int position) {
        //check each user if blocked or not
        // if uid of the user exists in "BlockedUsers" then that user is blocked, otherwise not

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                holder.blockIv.setImageResource(R.drawable.ic_blocked);
                                usersList.get(position).setBlocked(true);
                            }else {
                                Toast.makeText(context, "wtf", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser(String hisUid, final MyHolder holder) {
        //block the user by adding uid to current user's "BlockedUsers" node

        //put values in hashmap to put in db
        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child("uid").setValue(hisUid)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        holder.blockIv.setImageResource(R.drawable.ic_blocked);
                        Toast.makeText(context, "Blocked Successfuly", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Blocked Fail due to: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unBlockUser(String hisUid, final MyHolder holder) {
        //unblock by removing uid from current user's "Blocked users" node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()) {
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                holder.blockIv.setImageResource(R.drawable.ic_blocked);
                                                Toast.makeText(context, "Unblock Successed", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    //view holder class
    static class MyHolder extends RecyclerView.ViewHolder{

        ImageView avatarIv, blockIv;
        TextView mNameTv, mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            blockIv = itemView.findViewById(R.id.blockIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTv);

        }
    }
}
