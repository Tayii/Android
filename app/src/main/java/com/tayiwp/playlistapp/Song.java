package com.tayiwp.playlistapp;

public class Song {
    private int song_number;
    private String song_username;
    private String song_title;
    private String song_link;
    private int start_time;

    public String getSong_username() {
        return song_username;
    }

    public void setSong_username(String song_username) {
        this.song_username = song_username;
    }

    public String getSong_title() {
        return song_title;
    }

    public void setSong_title(String song_title) {
        this.song_title = song_title;
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

    public void setSong_link(String song_link) {
        this.song_link = song_link;
    }

    public int getStart_time() {
        return start_time;
    }

    public void setStart_time(int start_time) {
        this.start_time = start_time;
    }
}
