package com.toyibnurseha.firebasesocialmedia.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
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
import com.toyibnurseha.firebasesocialmedia.PostDetailActivity;
import com.toyibnurseha.firebasesocialmedia.R;
import com.toyibnurseha.firebasesocialmedia.models.ModelNotification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.NotifViewHolder> {

    private Context context;
    private ArrayList<ModelNotification> notificationsList;

    private FirebaseAuth firebaseAuth;

    public AdapterNotification(Context context, ArrayList<ModelNotification> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public AdapterNotification.NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_notifications, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterNotification.NotifViewHolder holder, int position) {
        //get and set data

        final ModelNotification model = notificationsList.get(position);
        String name = model.getsName();
        final String notification = model.getNotification();
        String image = model.getsImage();
        final String timestamp = model.getTimestamp();
        final String senderUid = model.getsUid();
        final String pId = model.getpId();

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        final String pTime = DateFormat.format("ddd/MM/yyyy hh:mm aa", calendar).toString();

        //we will get the name, email, image of the user of notification from his uid
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(senderUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String image = ""+ds.child("image").getValue();
                            String email = ""+ds.child("email").getValue();

                            //add to model
                            model.setsName(name);
                            model.setsEmail(email);
                            model.setsImage(image);

                            //set to views
                            holder.nameTv.setText(name);

                            try {
                                Picasso.get().load(image).placeholder(R.drawable.ic_default_img_rv).into(holder.avatarIv);
                            } catch (Exception e) {
                                holder.avatarIv.setImageResource(R.drawable.ic_default_img_rv);
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //set to view
        holder.notificationTv.setText(notification);
        holder.timeTv.setText(pTime);

        //click notification to open post
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId); //get post detail with id
                context.startActivity(intent);
            }
        });

        //long press to show delete notification
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this notification ?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //delete notif
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid()).child("Notifications").child(timestamp).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //deleted
                                        Toast.makeText(context, "Notification deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //candel
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });


    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    static class NotifViewHolder extends RecyclerView.ViewHolder {

        ImageView avatarIv;
        TextView nameTv, notificationTv, timeTv;

        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            notificationTv = itemView.findViewById(R.id.notificationsTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
