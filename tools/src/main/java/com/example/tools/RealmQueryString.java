package com.example.tools;

import org.json.JSONArray;
import org.json.JSONException;

import static com.example.tools.RealmExecuteResultHandler.JSON_FORMAT_ERR;
import static com.example.tools.RealmQueryUtil.getFirstWord;

/**
 * Created by THINK on 2016/10/11.
 */

public class RealmQueryString {
    private String source;

    public RealmQueryString(String source) {
        if (source == null) {
            this.source = "";
        } else {
            this.source = source.trim().toLowerCase();
        }

    }

    public String nextToken() {
        String firstWord = getFirstWord(source);
        source = source.substring(firstWord.length()).trim();
        return firstWord;
    }

    public JSONArray getTailAsJSONArray() {
        JSONArray jsonArray = null;
        if (!source.startsWith("[")) {
            try {
                jsonArray = new JSONArray("[" + source + "]");
            } catch (JSONException e) {
                throw new IllegalArgumentException(JSON_FORMAT_ERR);
            }
        }
        return jsonArray;
    }
}
