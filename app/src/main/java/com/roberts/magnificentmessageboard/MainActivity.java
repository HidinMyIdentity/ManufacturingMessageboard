package com.roberts.magnificentmessageboard;
/*
 * @author Robert Roberts
 */
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements FrontPagePostAdapter.ListItemClickListener {
    private FrontPagePostAdapter frontPageAdapter;
    private RecyclerView frontPage;

    public static final String VIEWPOSTID = "com.roberts.magnificentmessageboard.viewpost";

    // used for static toasts
    private static Context context;

    /**
     * Create the main activity
     * @param savedInstanceState {@link Bundle} of saved state
     */
    @SuppressLint("ApplySharedPref")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Globals.SetPreferences(getPreferences(Context.MODE_PRIVATE));
        // Generate tripcode if non-existent
        SharedPreferences prefs = Globals.GetPreferences();
        if (!prefs.contains(Globals.PREF_TRIPCODE)) {
            String trip = UUID.randomUUID().toString();
            prefs.edit().putString(Globals.PREF_TRIPCODE, trip).commit();
        }

        frontPage = findViewById(R.id.main_view);

        // Initializing RecyclerView stuff
        frontPageAdapter = new FrontPagePostAdapter(this, new ArrayList<>());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        frontPage.setLayoutManager(layoutManager);
        frontPage.setAdapter(frontPageAdapter);

        MainActivity.context = getApplicationContext();
        LoadFrontPage(0);

    }

    /**
     * Intentionally exposing context. Information was meant to be free!
     * @return the current context, or null if not set;
     */
    public static Context getAppContext() {
        return MainActivity.context;
    }

    /**
     * Toast a string
     * @param stringid ID of the string to toast
     */
    public void makeToast(int stringid) {
        Toast.makeText(this, stringid, Toast.LENGTH_SHORT).show();
    }

    /**
     * Load the front page
     * @param offset Unused.
     */
    public void LoadFrontPage(int offset) {
        // Ensure we're connected to the internet
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        if (networkInfo != null && networkInfo.isConnected()) {
            // Actually get the posts if we are
            getPosts(offset);
        } else {
            makeToast(R.string.not_connected);
        }
    }


    /**
     * Actually fetch the posts
     * @param offset Unused
     */
    public void getPosts(int offset) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // AsyncTask is deprecated
        executor.execute(() -> {
            ArrayList<Post> posts =  NetworkUtils.GetFrontPage(offset);

            if (posts != null) {
                for (Post post: posts) {
                    frontPageAdapter.AddPost(post);
                }
                runOnUiThread(() -> frontPageAdapter.UpdateSize());
            }
        });
    }

    /**
     * Reload displayed posts
     */
    public void RefreshPosts() {
        frontPageAdapter.clear();
        getPosts(0);
    }

    /**
     * Reloads displayed posts
     * @param v Not used
     */
    public void RefreshPosts(MenuItem v) {
        RefreshPosts();
    }


    /**
     * Create a new post
     * @param content String containing the content of the post
     */
    public void CreateNewPost(String content) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            int pid = NetworkUtils.CreateNewPost(content);

            if (pid != -1) {
                runOnUiThread(this::RefreshPosts);
            }
        });
    }

    /**
     * Initialize the menu for the front page
     * @param menu Assumed to be the menu of the actvity
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.frontpage_menu, menu);
        return true;
    }


    /**
     * Run on pressing the button to make a new post
     * @param view Button's view
     */
    public void NewPostButton(View view) {
        // Credit to https://stackoverflow.com/a/10904665
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Post");

        // Create input box
        final EditText input = new EditText(this);
        // Specifically make it multiline
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(input);

        // Upon submit, create a new post
        builder.setPositiveButton("Submit", (dialog, which) -> CreateNewPost(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Create a {@link SinglePostActivity} upon tapping a post
     * @param item Item position (?)
     * @param itemView View of the item clicked
     * @param post {@link Post} associated with the view
     */
    @Override
    public void onListItemClick(int item, View itemView, Post post) {
        Intent intent = new Intent(MainActivity.this, SinglePostActivity.class);
        intent.putExtra(VIEWPOSTID, post);
        startActivity(intent);
    }
}