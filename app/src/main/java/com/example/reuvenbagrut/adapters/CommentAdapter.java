package com.example.reuvenbagrut.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.models.Comment;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private FirebaseFirestore db;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.commentText.setText(comment.getText());
        holder.commentTime.setText(comment.getFormattedTime());

        // Load user information
        if (comment.getUserId() != null) {
            db.collection("users").document(comment.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("displayName");
                        String userPhotoUrl = documentSnapshot.getString("photoUrl");
                        
                        holder.userName.setText(userName != null ? userName : "Anonymous");
                        
                        if (userPhotoUrl != null && !userPhotoUrl.isEmpty()) {
                            Glide.with(holder.itemView.getContext())
                                .load(userPhotoUrl)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .error(R.drawable.ic_profile_placeholder)
                                .circleCrop()
                                .into(holder.userImage);
                        } else {
                            holder.userImage.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    } else {
                        holder.userName.setText("Anonymous");
                        holder.userImage.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                })
                .addOnFailureListener(e -> {
                    holder.userName.setText("Anonymous");
                    holder.userImage.setImageResource(R.drawable.ic_profile_placeholder);
                });
        }
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName;
        TextView commentText;
        TextView commentTime;

        CommentViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.commentUserImage);
            userName = itemView.findViewById(R.id.commentUserName);
            commentText = itemView.findViewById(R.id.commentText);
            commentTime = itemView.findViewById(R.id.commentTime);
        }
    }
} 