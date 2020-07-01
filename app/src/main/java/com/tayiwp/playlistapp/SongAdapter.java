package com.tayiwp.playlistapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class SongAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Song> objects;
    int current_song_number = 0;

    public int colorBackground, colorSelected;

    SongAdapter(Context context) {
        ctx = context;
        objects = new ArrayList<>();
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setObjects(HashMap<Integer, Song> songs) {
        ArrayList<Song> objects = new ArrayList<>();
        if (songs != null) {
            for (int song_number: songs.keySet()) {
                Song song = songs.get(song_number);
                if (song.getSong_link() == "") {
                    continue;
                }
                song.setSong_number(song_number);
                objects.add(song);
            }
        }
        this.objects = objects;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int i) {
        return objects.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.song, parent, false);
        }

        Song p = (Song) getItem(position);

        StringBuilder s = new StringBuilder();
        s.append(p.getSong_number());
        ((TextView) view.findViewById(R.id.tvSongNumber)).setText(s);
        ((TextView) view.findViewById(R.id.tvSongTitle)).setText(p.getSong_title());
        ((TextView) view.findViewById(R.id.tvSongUsername)).setText(p.getSong_username());

        if (p.getSong_number() == current_song_number) {
            view.setBackgroundColor(colorSelected);
        }
        else {
            view.setBackgroundColor(colorBackground);
        }
        return view;
    }

    public void setCurrentSongNumber(int songNumber) {
        if (current_song_number != songNumber) {
            current_song_number = songNumber;
            notifyDataSetChanged();
        }
    }
}
