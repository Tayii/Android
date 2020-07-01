package com.tayiwp.playlistapp;

import java.util.HashMap;

public class AllPlaylists {
    private String current_playlisttype;
    private int current_songnumber;
    private HashMap<String, HashMap<Integer, Song>> playlists;

    public String getCurrent_playlisttype() {
        return current_playlisttype;
    }

    public void setCurrent_playlisttype(String current_playlisttype) {
        this.current_playlisttype = current_playlisttype;
    }

    public int getCurrent_songnumber() {
        return current_songnumber;
    }

    public void setCurrent_songnumber(int current_songnumber) {
        this.current_songnumber = current_songnumber;
    }

    public HashMap<String, HashMap<Integer, Song>> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(HashMap<String, HashMap<String, Song>> playlists) {
        HashMap<String, HashMap<Integer, Song>> p2 = new HashMap<>();
        for (String playlistType: playlists.keySet()) {
            HashMap<String, Song> h = playlists.get(playlistType);
            HashMap<Integer, Song> h2 = new HashMap<>();
            for (String songNumber: h.keySet()) {
                h2.put(Integer.parseInt(songNumber), h.get(songNumber));
            }
            p2.put(playlistType, h2);
        }
        this.playlists = p2;
    }
}
