package com.tayiwp.playlistapp;

public class Song {
    private int list_position;
    private int song_number;
    private String song_username;
    private String song_title;
    private String song_link;
    private int start_time;

    public String getSong_username() {
        return song_username;
    }

    public String getSong_title() {
        return song_title;
    }

    public int getSong_number() {
        return song_number;
    }

    public void setSong_number(int song_number) {
        this.song_number = song_number;
    }

    public String getSong_link() {
        return song_link;
    }

    public int getStart_time() {
        return start_time;
    }

    public int getList_position() {
        return list_position;
    }

    public void setList_position(int list_position) {
        this.list_position = list_position;
    }
}
