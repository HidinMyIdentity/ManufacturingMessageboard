package com.roberts.magnificentmessageboard;
/*
 * @author Robert Roberts
 */
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Adapter for viewing a post and it's replies. Almost identical to {@link FrontPagePostAdapter}
 */
public class SinglePostAdapter extends RecyclerView.Adapter<SinglePostAdapter.SinglePostHolder> {
    private List<Post> postList;
    private final LayoutInflater mInflater;

    /**
     * Constructor
     * @param context Activity context
     * @param posts List of posts, likely empty
     */
    public SinglePostAdapter(Context context, List<Post> posts) {
        mInflater = LayoutInflater.from(context);
        postList = posts;
    }

    /**
     * Add a reply
     * @param post {@link Post} object for the reply
     */
    public void AddPost(Post post) {
        postList.add(post);
    }

    /**
     * Telling the Adapter that it's data has changed
     */
    public void UpdateSize() {
        notifyItemChanged(postList.size());
    }

    /**
     * Run on the addition of a new post
     * @param parent Parent view
     * @param viewType Unknown
     * @return New {@link SinglePostHolder}
     */
    @Override
    public SinglePostAdapter.SinglePostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.postview_post, parent, false);
        return new SinglePostHolder(mItemView);
    }

    /**
     * Run upon binding a new post
     * @param holder The new {@link SinglePostHolder} object
     * @param position Position in the adapter
     */
    @Override
    public void onBindViewHolder(SinglePostAdapter.SinglePostHolder holder, int position) {
        Post mCurrent = postList.get(position);
        holder.postId.setText(""+mCurrent.Id);
        holder.postContent.setText(mCurrent.Content);
        holder.postHash.setText(mCurrent.Hash);
        holder.itemView.setBackgroundColor(position % 2 == 1 ? Color.LTGRAY : Color.WHITE);
    }

    /**
     * @return the amount of replies to the post
     */
    @Override
    public int getItemCount() {
        return postList.size();
    }

    /**
     * Clear the adapter
     */
    public void clear() {
        postList = new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Display-side holder for a {@link Post} object
     */
    class SinglePostHolder extends RecyclerView.ViewHolder {

        public final TextView postContent;
        public final TextView postHash;
        public final TextView postId;

        /**
         * Constructor for the new holder
         * @param itemView Current context (I think the Adapter)
         */
        public SinglePostHolder(View itemView) {
            super(itemView);
            postContent = itemView.findViewById(R.id.sp_content);
            postHash = itemView.findViewById(R.id.sp_hash);
            postId = itemView.findViewById(R.id.sp_id);

        }
    }

}
