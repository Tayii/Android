package com.tayiwp.playlistapp;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ServerApi {

    @GET("command_get.php")
    Call<AllPlaylists> getPlaylists();
}
