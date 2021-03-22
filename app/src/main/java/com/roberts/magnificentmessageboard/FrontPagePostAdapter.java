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
 * Adapter to control posts on the front page
 */
public class FrontPagePostAdapter extends RecyclerView.Adapter<FrontPagePostAdapter.FrontPagePostHolder> {
    private List<Post> postList;
    private final LayoutInflater mInflater;
    ListItemClickListener mOnClickListener;

    /**
     * Interface for what to do upon tapping a post
     */
    public interface  ListItemClickListener {
        void onListItemClick(int item, View itemView, Post post);
    }

    /**
     * Initializer
     * @param context {@link ListItemClickListener}. Should also be a context.
     * @param posts List of posts to work with
     */
    public FrontPagePostAdapter(ListItemClickListener context, List<Post> posts) {
        mInflater = LayoutInflater.from((Context) context);
        mOnClickListener = context;
        postList = posts;
    }

    /**
     * Add a post to the {@link RecyclerView}
     * @param post a {@link Post} object
     */
    public void AddPost(Post post) {
        postList.add(post);
    }

    /**
     * Triggers an update of the size
     */
    public void UpdateSize() {
        notifyItemChanged(postList.size());
    }

    /**
     * Run on creating a new listing
     * @param parent
     * @param viewType
     * @return new {@link FrontPagePostHolder}
     */
    @Override
    public FrontPagePostAdapter.FrontPagePostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.frontpage_post, parent, false);
        return new FrontPagePostHolder(mItemView, this);
    }

    /**
     * On binding a new holder
     * @param holder  {@link FrontPagePostHolder} that we are modifying
     * @param position Position of it in the list
     */
    @Override
    public void onBindViewHolder(FrontPagePostAdapter.FrontPagePostHolder holder, int position) {
        Post mCurrent = postList.get(position);
        holder.postId.setText(""+mCurrent.Id);
        holder.postContent.setText(mCurrent.Content);
        holder.postHash.setText(mCurrent.Hash);
        // Alternating background colors
        holder.itemView.setBackgroundColor(position % 2 == 1 ? Color.LTGRAY : Color.WHITE);
        holder.post = mCurrent;
    }

    /**
     * @return the amount of items in the list.
     */
    @Override
    public int getItemCount() {
        return postList.size();
    }

    /**
     * Empty the adapter
     */
    public void clear() {
        postList = new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Sub-class to hold the post's display
     */
    class FrontPagePostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView postContent;
        public final TextView postHash;
        public final TextView postId;
        public Post post;

        final FrontPagePostAdapter mAdapter;

        /**
         * Constructor
         * @param itemView Parent view
         * @param adapter Adapter we're working with
         */
        public FrontPagePostHolder(View itemView, FrontPagePostAdapter adapter) {
            super(itemView);
            postContent = itemView.findViewById(R.id.post_content);
            postHash = itemView.findViewById(R.id.post_hash);
            postId = itemView.findViewById(R.id.post_id);

            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        /**
         * Run on tap of the post
         * @param v View of the post
         */
        @Override
        public void onClick(View v) {
            mOnClickListener.onListItemClick(getAdapterPosition(), v, post);
        }
    }

}
