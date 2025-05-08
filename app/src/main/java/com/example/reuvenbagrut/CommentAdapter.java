package com.example.reuvenbagrut;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
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
        holder.userName.setText(comment.getUserName());
        holder.commentText.setText(comment.getText());
        
        // Format and set timestamp
        if (comment.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            holder.commentTime.setText(sdf.format(comment.getTimestamp()));
        }
        
        // Load user profile image
        if (comment.getUserPhotoUrl() != null && !comment.getUserPhotoUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(comment.getUserPhotoUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(holder.userImage);
        } else {
            holder.userImage.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
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