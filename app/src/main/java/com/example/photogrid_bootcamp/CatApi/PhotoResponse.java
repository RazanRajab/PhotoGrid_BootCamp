package com.example.photogrid_bootcamp.CatApi;

import com.google.gson.annotations.SerializedName;

public class PhotoResponse {
    @SerializedName("url")
    String url;

    public String getUrl() {
        return url;
    }
}
