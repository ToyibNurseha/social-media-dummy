package com.toyibnurseha.firebasesocialmedia.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.toyibnurseha.firebasesocialmedia.ChatActivity;
import com.toyibnurseha.firebasesocialmedia.R;
import com.toyibnurseha.firebasesocialmedia.models.ModelChatlist;
import com.toyibnurseha.firebasesocialmedia.models.ModelUsers;

import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.ChatlistHolder> {

    Context context;
    List<ModelUsers> userList; //get user info
    private HashMap<String, String> lastMessageMap;

    public AdapterChatlist(Context context, List<ModelUsers> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public AdapterChatlist.ChatlistHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);

        return new ChatlistHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterChatlist.ChatlistHolder holder, int position) {
        final String hisUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String lastMessage = lastMessageMap.get(hisUid);

        //set data
        holder.nameTv.setText(userName);
        if (lastMessage == null || lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        } else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }

        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img_rv).into(holder.profileIv);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.ic_default_img_rv).into(holder.profileIv);
            e.printStackTrace();
        }

        if (userList.get(position).getOnlineStatus().equals("online")){
            //online
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }else {
            //offline
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
            }
        });
    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class ChatlistHolder extends RecyclerView.ViewHolder {

        ImageView profileIv, onlineStatusIv;
        TextView nameTv, lastMessageTv;

        public ChatlistHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);

        }
    }
}
