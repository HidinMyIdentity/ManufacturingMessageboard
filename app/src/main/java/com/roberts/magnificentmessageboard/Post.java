package com.roberts.magnificentmessageboard;
/*
 * @author Robert Roberts
 */
import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Encapsulation of a post
 */
public class Post implements Serializable {
    public final String Hash;
    public final String Content;
    public final int Id;
    public final int Reply;



    /**
     * Create a {@link Post} from a JSON object
     * @param json
     * @throws JSONException
     */
    public Post(@NonNull JSONObject json) throws JSONException {
        Hash = json.getString("hash");
        Content = json.getString("content");
        Id = json.getInt("rowid");
        Reply = json.getInt("reply");
    }
}
