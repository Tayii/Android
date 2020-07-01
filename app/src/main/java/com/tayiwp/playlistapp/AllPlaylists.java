package com.tayiwp.playlistapp;

import java.util.HashMap;

public class AllPlaylists {
    private String current_playlisttype;
    private int current_songnumber;
    private HashMap<String, HashMap<Integer, Song>> playlists;

    public String getCurrent_playlisttype() {
        return current_playlisttype;
    }

    public int getCurrent_songnumber() {
        return current_songnumber;
    }

    public HashMap<String, HashMap<Integer, Song>> getPlaylists() {
        return playlists;
    }
}
