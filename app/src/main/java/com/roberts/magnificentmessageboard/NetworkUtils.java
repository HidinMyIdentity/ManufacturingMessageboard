package com.roberts.magnificentmessageboard;
/*
 * @author Robert Roberts
 */
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class NetworkUtils {

    // Api base
    private final static String API_SERVER = "messageboard.hoppy.haus";

    private final static String API_ENDPOINT_FRONT_PAGE = "getFrontPage.php";
    private final static String API_ENDPOINT_NEW_POST = "newPost.php";
    private final static String API_ENDPOINT_REPLIES = "getReplies.php";


    /**
     * Class to encapsulate the response of a request
     */
    public static class QuickRequestResponse {
        private final HttpURLConnection response;
        private String data = "";

        /**
         * Initializes with the connection from quickRequest, not intended to be used elsewhere
         * @param connection HttpURLConnection from quickRequest
         * @throws IOException Upon any unaccounted network behavior
         */
        public QuickRequestResponse(HttpURLConnection connection) throws IOException {
            response = connection;
            BufferedReader br;
            try {
                // For some reason if not perfect, the input stream will be null and errorstream will
                // be used. This isn't STDIN/STDERR, Java.
                if (connection.getInputStream() != null) {
                    br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                } else {
                    br = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
                }
                String line;
                // Read the data line-by-line into the internal data value
                while ((line = br.readLine()) != null) {
                    data += line;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                // leave data empty
            }


            connection.disconnect();
        }

        public JSONObject toJson() {
            try {
                return new JSONObject(this.data);
            } catch (JSONException e) {
                return null;
            }

        }

        /**
         * get the response object
         * @return
         */
        public HttpURLConnection getResponse() {
            return response;
        }

        /**
         * Get the content of the response
         * @return content from the response
         */
        public String getData() {
            return data;
        }
    }


    /**
     * Method to reduce boilerplate of getting a URL
     * @param url URL to get
     * @param method HTTP method to use
     * @param redirects Follow redirects?
     * @return QuickRequestResponse containing the data
     * @throws IOException Upon network error
     */
    public static QuickRequestResponse quickRequest(String url, String method, boolean redirects) throws IOException {
        URL requestURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("User-Agent", "MagnificentMessageboard (S-Robert.Roberts@lwtech.edu) 1.0");
        connection.setInstanceFollowRedirects(redirects);
        connection.connect();
        return new QuickRequestResponse(connection);
    }

    /**
     * Make a POST request
     * @param url URL to POST to
     * @param data Hashmap of data we're passing
     * @return QuickRequestResponse encapsulating the response
     * @throws IOException on network issue
     */
    public static QuickRequestResponse quickPostRequest(String url, HashMap<String, String> data) throws IOException {
        URL requestURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        // Why is Java utf-16 by default...
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("User-Agent", "MagnificentMessageboard (S-Robert.Roberts@lwtech.edu) 1.0");

        // Build the POST data in a urlencoded format
        String params = "";
        for (String key: data.keySet()) {
            params += String.format("%s=%s&", key, URLEncoder.encode(data.get(key), "UTF-8"));
        }
        params = params.replaceFirst("&$", "");
        // Convert data into a byte array
        byte[] bParams = params.getBytes(StandardCharsets.UTF_8);

        connection.setRequestProperty("Content-Length", Integer.toString(bParams.length));
        connection.setFixedLengthStreamingMode(bParams.length);
        connection.connect();

        OutputStream out = connection.getOutputStream();
        out.write(bParams);
        out.close();

        return new QuickRequestResponse(connection);
    }

    /**
     * Get the front page of the forum
     * @param offset Unused
     * @return An ArrayList of {@link Post}s
     */
    public static ArrayList<Post> GetFrontPage(int offset) {
        try {
            ArrayList<Post> posts = new ArrayList<Post>();

            Uri.Builder uri = new Uri.Builder();
            // Build the url
            uri.scheme("http").encodedAuthority(API_SERVER).appendPath(API_ENDPOINT_FRONT_PAGE).appendQueryParameter("offset", ""+offset);
            QuickRequestResponse response = quickRequest(uri.toString(), "GET", false);
            JSONObject frontPage = response.toJson();
            if (frontPage == null) {
                Globals.ShowToast("Error: Something is wrong with the server/network");
                return null;
            } else if (frontPage.has("error")) {
                // If something is wrong in the request
                Globals.ShowToast("Error: " + frontPage.getString("error"));
                return null;
            } else {
                // Build the array
                JSONArray content = frontPage.getJSONArray("response");
                for (int i = 0; i < content.length(); i++) {
                    JSONObject post = content.getJSONObject(i);
                    posts.add(new Post(post));
                }
            }
            return posts;
        } catch (IOException | JSONException e) {
            Globals.ShowToast("Error: Something is wrong with the server/network");
            return null;
        }
    }

    /**
     * Get replies to a post
     * @param rowid Post id
     * @return List of replies to the post
     */
    public static ArrayList<Post> GetReplies(int rowid) {
        try {
            ArrayList<Post> posts = new ArrayList<Post>();

            Uri.Builder uri = new Uri.Builder();
            // Build the url
            uri.scheme("http").encodedAuthority(API_SERVER).appendPath(API_ENDPOINT_REPLIES).appendQueryParameter("rowid", ""+rowid);
            QuickRequestResponse response = quickRequest(uri.toString(), "GET", false);
            JSONObject replies = response.toJson();
            if (replies == null) {
                Globals.ShowToast("Error: Something is wrong with the server/network");
                return null;
            } else if (replies.has("error")) {
                Globals.ShowToast("Error: " + replies.getString("error"));
                return null;

            } else {
                JSONArray content = replies.getJSONArray("response");
                for (int i = 0; i < content.length(); i++) {
                    JSONObject post = content.getJSONObject(i);
                    posts.add(new Post(post));
                }
            }
            return posts;
        } catch (IOException | JSONException e) {
            Globals.ShowToast("Error: Something is wrong with the server/network");
            return null;
        }
    }

    /**
     * Create a new top-level post
     * @param content String to submit
     * @return ID of the new post
     */
    public static int CreateNewPost(String content) {
        return CreateNewPost(content, -1);
    }

    /**
     * Create a new post
     * @param content String to submit
     * @param reply ID of the post we're replying to, or -1
     * @return ID of the new post
     */
    public static int CreateNewPost(String content, int reply) {
        try {
            Uri.Builder uri = new Uri.Builder();
            uri.scheme("http").encodedAuthority(API_SERVER).appendPath(API_ENDPOINT_NEW_POST);
            // Load POST variables
            HashMap<String, String> postData = new HashMap<>();
            postData.put("content", content);
            postData.put("reply", Integer.toString(reply));
            postData.put("tripcode", Globals.GetPreferences().getString(Globals.PREF_TRIPCODE, ""));

            QuickRequestResponse response = quickPostRequest(uri.toString(), postData);
            JSONObject success = response.toJson();
            if (success == null) {
                return -1;
            } else if (success.has("error")) {
                Globals.ShowToast("Error: " + success.getString("error"));
                return -1;
            } else {
                JSONObject jsonSuccess = success.getJSONObject("response");
                return jsonSuccess.getInt("rowid");
            }
        } catch (IOException | NumberFormatException | JSONException e) {
            Globals.ShowToast("Error: Something is wrong with the server/network");
            return -1;
        }
    }
}
