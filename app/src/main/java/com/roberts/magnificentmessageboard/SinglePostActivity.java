package com.roberts.magnificentmessageboard;
/*
 * @author Robert Roberts
 */
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Activity for viewing a {@link Post} and it's replies
 */
public class SinglePostActivity extends AppCompatActivity {
    private SinglePostAdapter replyAdapter;
    private Post originalPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlepost);
        Intent intent = getIntent();

        RecyclerView reply_list = findViewById(R.id.sp_replies);

        // Load up the OP's data
        originalPost = (Post) intent.getSerializableExtra(MainActivity.VIEWPOSTID);
        ((TextView) findViewById(R.id.op_id)).setText(""+originalPost.Id);
        ((TextView) findViewById(R.id.op_hash)).setText(originalPost.Hash);
        TextView content = findViewById(R.id.op_content);
        content.setText(originalPost.Content);

        // Initialize Recyclerview stuff
        replyAdapter = new SinglePostAdapter(this, new ArrayList<>());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        reply_list.setLayoutManager(layoutManager);
        reply_list.setAdapter(replyAdapter);

        getPosts(originalPost.Id);

    }

    /**
     * Get replies to the post
     * @param rowid ID of the original post
     */
    public void getPosts(int rowid) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // See the getPosts in MainActivity
        executor.execute(() -> {
            ArrayList<Post> posts =  NetworkUtils.GetReplies(rowid);

            if (posts != null) {
                for (Post post: posts) {
                    replyAdapter.AddPost(post);
                }
                runOnUiThread(() -> replyAdapter.UpdateSize());
            }
        });
    }

    /**
     * Create a new reply to the post
     * @param view View of the button that we tapped
     */
    public void NewReplyButton(View view) {
        // Credit to https://stackoverflow.com/a/10904665
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Reply");
        // Load the text editor
        final EditText input = new EditText(this);
        // Specifically allow multiline
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> CreateNewReply(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Create the options menu for this Activity
     * @param menu Menu for this Activity
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.frontpage_menu, menu);
        return true;
    }

    /**
     * Create a new reply to the post
     * @param content String content of the reply
     */
    private void CreateNewReply(String content) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            int pid = NetworkUtils.CreateNewPost(content, originalPost.Id);
            if (pid != -1) {
                runOnUiThread(this::RefreshPosts);
            }
        });
    }

    /**
     * Clear replies and reload
     */
    private void RefreshPosts() {
        replyAdapter.clear();
        getPosts(originalPost.Id);
    }
    /**
     * Clear replies and reload
     */
    public void RefreshPosts(MenuItem v) {
        RefreshPosts();
    }



}
