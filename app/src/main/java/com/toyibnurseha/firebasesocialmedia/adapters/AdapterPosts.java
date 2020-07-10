package com.toyibnurseha.firebasesocialmedia.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.toyibnurseha.firebasesocialmedia.AddPostActivity;
import com.toyibnurseha.firebasesocialmedia.PostDetailActivity;
import com.toyibnurseha.firebasesocialmedia.R;
import com.toyibnurseha.firebasesocialmedia.ThereProfileActivity;
import com.toyibnurseha.firebasesocialmedia.models.ModelPost;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.PostsViewHolder> {

    Context context;
    List<ModelPost> postList;

    String myUid;

    private DatabaseReference likesRef; //for likes database node
    private DatabaseReference postRef; //reference of posts

    boolean mProcessLike = false;

    public AdapterPosts(Context context, List<ModelPost> postList, String myUid) {
        this.context = context;
        this.postList = postList;
        this.myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        this.postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);

        return new PostsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostsViewHolder holder, final int position) {
        //get data
        final String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        final String uDp = postList.get(position).getuDp();
        final String pId = postList.get(position).getpId();
        final String pTitle = postList.get(position).getpTitle();
        final String pDescription = postList.get(position).getpDescr();
        final String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes(); //contains total number of likes for post
        String pComments = postList.get(position).getpComments(); //contains total number of comments for post

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        final String pTime = DateFormat.format("ddd/MM/yyyy hh:mm aa", calendar).toString();

        //set data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
        holder.pLikesTv.setText(pLikes + " Likes");
        holder.pCommentsTv.setText(pComments + " Comments");

        //set likes for each post
        setLikes(holder, pId);

        //set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img_rv).into(holder.uPictureIv);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //set posts image
        if (pImage.equals("noImage")) {
            holder.pImageIv.setVisibility(View.GONE);
        } else {
            holder.pImageIv.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(pImage).into(holder.pImageIv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //handle button clicks
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, uid, myUid, pId, pImage);
            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if currently signed in user has not liked it before
                //increase value by 1, otherwise decrese value by 1
                final int pLikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike = true;
                //get id of the post clicked
                final String postIde = postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (mProcessLike) {
                            if (snapshot.child(postIde).hasChild(myUid)) {
                                //already liked, so remove like (-1)
                                postRef.child(postIde).child("pLikes").setValue("" + (pLikes - 1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;
                            } else {
                                //not liked, like it
                                postRef.child(postIde).child("pLikes").setValue("" + (pLikes + 1));
                                likesRef.child(postIde).child(myUid).setValue("Liked"); //set any value
                                mProcessLike = false;

                                addToHisNotification(""+uid, ""+pId, "Liked your post");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId); //get post detail with id
                context.startActivity(intent);
            }
        });

        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // some containts only text, and some contains image and text, so we will handle them both //
                //get image from imageView
                BitmapDrawable bitmapDrawable= (BitmapDrawable) holder.pImageIv.getDrawable();
                if (bitmapDrawable == null){
                    //post without image
                    shareTextOnly(pTitle, pDescription);
                } else {
                    //post with image
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pDescription, bitmap);
                }
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });

        //click like count to start postLikedByActivity
        holder.pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });
    }

    private void addToHisNotification(String hisUid, String pId, String messageNotification){
        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", messageNotification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        add success
//                        Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //add failed
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {

        String shareBody = pTitle +"\n"+ pDescription;

        //first we will save this image in cache, get the saved image uri
        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs(); //create if not exist
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.toyibnurseha.firebasesocialmedia.fileprovider", file);

        } catch (Exception e) {
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return uri;
    }

    private void shareTextOnly(String pTitle, String pDescription) {
        //concetenate title and description to shate
        String shareBody = pTitle +"\n"+ pDescription;

        //share Intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); //in case you share via and email app
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);; //text to share
        context.startActivity(Intent.createChooser(sIntent, "Share Via")); //message to show in share dialog
    }

    private void setLikes(final PostsViewHolder holder, final String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)) {
                    //user has liked this post
                    /*
                    To indicate that the post is liked by this (signedIn) user
                    change drawable left icon of like button
                    change text of like button from "Like" to "Liked"
                     */
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    holder.likeBtn.setText("Liked");
                } else {
                    //user has not liked this post
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0);
                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option in only posts of currently signed-in user
        if (uid.equals(myUid)) {
            //add items
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }

        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Detail");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    //delete clicked
                    beginDelete(pId, pImage);
                } else if (id == 1) {
                    //start AddPostActivity
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                } else if (id == 2) {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId); //get post detail with id
                    context.startActivity(intent);
                }
                return false;
            }
        });

        //show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        //post can be with or without image

        if (pImage.equals("noImage")) {
            //posts is without image
            deleteWithoutImage(pId);
        } else {
            //post with image
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(final String pId, String pImage) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting..");

        /*
        1. delete image using uri
        2. delete from database using post id
         */

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete database

                        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    ds.getRef().removeValue(); //remove values from firebase where pId matches
                                    //deleted
                                    Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                    pd.dismiss();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                pd.dismiss();
                                Toast.makeText(context, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting..");

        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue(); //remove values from firebase where pId matches
                    //deleted
                    Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
                Toast.makeText(context, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostsViewHolder extends RecyclerView.ViewHolder {

        ImageView uPictureIv, pImageIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
        ImageButton moreBtn;
        Button likeBtn, commentBtn, shareBtn;
        LinearLayout profileLayout;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);

        }
    }

}
