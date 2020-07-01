package com.tayiwp.playlistapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerUtils;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private boolean bPlaylistSync = true;
    private boolean bRefreshPlaylist = true;
    private boolean bSongAutoswitch = true;
    private boolean bYoutubePlayerReady = false;

    SharedPreferences sPref;
    int currentSongNumber = 0, maxSongNumber = 1;
    Song currentSong;
    HashMap<Integer, Song> currentPlaylist;
    MyTask mt;

    TextInputEditText teStreamerName;
    YouTubePlayerView youTubePlayerView;
    YouTubePlayerCallback youTubePlayerCallbackLoadCurrentVideo;
    TextView tvCurrentSongNumber, tvMaxSongNumber, tvCurrentSongUsername;
    Button btnPrev, btnNext;
    TextView tvCurrentPlaylistType;
    ProgressBar pbPlaylistLoading, pbPlaylistUpdating;
    int pbPlaylistUpdatingProgress = 0;
    SongAdapter adapter;
    ListView lvPlaylist;
    YouTubePlayerTracker youTubePlayerTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        teStreamerName = findViewById(R.id.teStreamerName);

        teStreamerName.setOnEditorActionListener(
                new TextInputEditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event != null &&
                                        event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            if (event == null || !event.isShiftPressed()) {
                                savePreferenceByString(R.string.teStreamerNameHint, teStreamerName.getText().toString());
                                loadStreamerPlaylist(false);
                                return true;
                            }
                        }
                        return false;
                    }
                }
        );

        youTubePlayerView = findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayerTracker = new YouTubePlayerTracker();
                youTubePlayer.addListener(youTubePlayerTracker);
                bYoutubePlayerReady = true;
                loadStreamerPlaylist(false);
                toggleAsyncPlaylistLoadingSafe(false);
            }

            @Override
            public void onStateChange(YouTubePlayer youTubePlayer, PlayerConstants.PlayerState state) {
                if (state.equals(PlayerConstants.PlayerState.ENDED) && bSongAutoswitch) {
                    switchToSongSafe(currentSongNumber + 1);
                }
                super.onStateChange(youTubePlayer, state);
            }
        });

        youTubePlayerCallbackLoadCurrentVideo = new YouTubePlayerCallback() {
            @Override
            public void onYouTubePlayer(YouTubePlayer youTubePlayer) {
                playCurrentSongCallback(youTubePlayer);
            }
        };

        tvCurrentSongNumber = findViewById(R.id.tvSongNumber);
        tvMaxSongNumber = findViewById(R.id.tvMaxSongNumber);
        tvCurrentSongUsername = findViewById(R.id.tvSongUsername);

        btnPrev = findViewById(R.id.btnPrevSong);
        btnNext = findViewById(R.id.btnNextSong);

        tvCurrentPlaylistType = findViewById(R.id.tvCurrentPlaylistType);

        pbPlaylistLoading = findViewById(R.id.pbPlaylistLoading);
        pbPlaylistUpdating = findViewById(R.id.pbPlaylistUpdating);
        pbPlaylistLoading.setVisibility(View.INVISIBLE);

        adapter = new SongAdapter(this);
        adapter.colorSelected = getResources().getColor(R.color.colorSelectedItem);
        adapter.colorBackground = getResources().getColor(R.color.colorBackground);
        lvPlaylist = findViewById(R.id.lvPlaylist);
        lvPlaylist.setAdapter(adapter);
        lvPlaylist.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Song item = (Song) adapterView.getItemAtPosition(i);
                switchToSongSafe(item.getSong_number());
            }
        });

        loadPreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.test_menu, menu);
        menu.findItem(R.id.miPlaylistSync).setChecked(bPlaylistSync);
        menu.findItem(R.id.miRefreshPlaylist).setChecked(bRefreshPlaylist);
        menu.findItem(R.id.miSongAutoswitch).setChecked(bSongAutoswitch);
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            menu.findItem(R.id.miRefreshPlaylist).setVisible(!bPlaylistSync);
            menu.findItem(R.id.miSongAutoswitch).setVisible(!bPlaylistSync);
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miPlaylistSync:
                bPlaylistSync = !bPlaylistSync;
                item.setChecked(bPlaylistSync);
                savePreferenceByString(R.string.miPlaylistSync, bPlaylistSync);
                updateButtonEnabled();
                toggleAsyncPlaylistLoadingSafe(true);
                return true;
            case R.id.miRefreshPlaylist:
                bRefreshPlaylist = !bRefreshPlaylist;
                item.setChecked(bRefreshPlaylist);
                savePreferenceByString(R.string.miRefreshPlaylist, bRefreshPlaylist);
                toggleAsyncPlaylistLoadingSafe(true);
                return true;
            case R.id.miSongAutoswitch:
                bSongAutoswitch = !bSongAutoswitch;
                item.setChecked(bSongAutoswitch);
                savePreferenceByString(R.string.miSongAutoswitch, bSongAutoswitch);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void toggleAsyncPlaylistLoadingSafe(boolean refresh_now) {
        if (!bYoutubePlayerReady) {
            return;
        }
        if (bPlaylistSync || bRefreshPlaylist) {
            if (mt == null) {
                if (refresh_now) {
                    setPbPlaylistUpdatingProgress(100);
                }
                mt = new MyTask();
                mt.execute();
            }
        }
        else {
            mt = null;
        }
    }

    void setPbPlaylistUpdatingProgress(int updatingProgress) {
        pbPlaylistUpdatingProgress = updatingProgress;
        if (pbPlaylistUpdatingProgress <= 100) {
            pbPlaylistUpdating.setProgress(pbPlaylistUpdatingProgress);
        }
    }

    class MyTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                while (bPlaylistSync || bRefreshPlaylist) {
                    TimeUnit.MILLISECONDS.sleep(5 * 1000 / 100);
                    if (pbPlaylistUpdatingProgress == 100) {
                        loadStreamerPlaylist(true);
                    }
                    if (pbPlaylistUpdatingProgress <= 100) {
                        setPbPlaylistUpdatingProgress(pbPlaylistUpdatingProgress + 1);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setPbPlaylistUpdatingProgress(0);
            return null;
        }
    }

    void loadStreamerPlaylist(final boolean safe) {
        if (!safe) {
            pbPlaylistLoading.setVisibility(View.VISIBLE);
        }
        String streamer_name = teStreamerName.getText().toString();
        String url = "http://tayi-wp.ddns.net/"
                .concat(streamer_name)
                .concat("/playlist/");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ServerApi serverApi = retrofit.create(ServerApi.class);
        Call<AllPlaylists> playlists = serverApi.getPlaylists();

        playlists.enqueue(new Callback<AllPlaylists>() {
            @Override
            public void onResponse(Call<AllPlaylists> call, Response<AllPlaylists> response) {
                if (safe) {
                    updatePlaylistViewSafe(response.body());
                    updateCurrentSongSafe();
                    playCurrentSongSafe();
                }
                else {
                    pbPlaylistLoading.setVisibility(View.INVISIBLE);
                    updatePlaylistView(response.body());
                    updateCurrentSong();
                    playCurrentSong();
                }
                setPbPlaylistUpdatingProgress(0);
            }

            @Override
            public void onFailure(Call<AllPlaylists> call, Throwable t) {
                if (!safe) {
                    pbPlaylistLoading.setVisibility(View.INVISIBLE);
                }
                showError(t);
                setPbPlaylistUpdatingProgress(0);
            }
        });
    }

    void updatePlaylistViewSafe(AllPlaylists allPlaylists) {
        if (bPlaylistSync || bRefreshPlaylist) {
            updatePlaylistView(allPlaylists);
        }
    }

    void updatePlaylistView(AllPlaylists allPlaylists) {
        if (allPlaylists == null) {
            currentPlaylist = null;
            currentSongNumber = 0;
            maxSongNumber = 0;
        }
        else {
            currentPlaylist = allPlaylists
                    .getPlaylists()
                    .get(allPlaylists.getCurrent_playlisttype());
            currentSongNumber = allPlaylists.getCurrent_songnumber();
        }
        if (currentPlaylist == null) {
            tvCurrentPlaylistType.setText("not found");
            adapter.setObjects(null);
        }
        else {
            tvCurrentPlaylistType.setText(allPlaylists.getCurrent_playlisttype());
            adapter.setObjects(currentPlaylist);
            maxSongNumber = Collections.max(currentPlaylist.keySet());
        }
        if (maxSongNumber > 0) {
            tvMaxSongNumber.setText(String.valueOf(maxSongNumber));
        }
        else {
            tvMaxSongNumber.setText("");
        }
    }

    void updateCurrentSongSafe() {
        if (bPlaylistSync) {
            updateCurrentSong();
        }
    }

    void updateCurrentSong() {
        StringBuilder s = new StringBuilder();
        s.append(currentSongNumber);
        tvCurrentSongNumber.setText(s.toString());
        if (currentPlaylist == null) {
            currentSong = null;
        }
        else {
            currentSong = currentPlaylist.get(currentSongNumber);
        }
        if (currentSong == null) {
            tvCurrentSongUsername.setText("");
        }
        else {
            tvCurrentSongUsername.setText(currentSong.getSong_username());
        }
        adapter.setCurrentSongNumber(currentSongNumber);
    }

    void playCurrentSongSafe() {
        if (bPlaylistSync) {
            playCurrentSong();
        }
    }

    void playCurrentSong() {
        if (bYoutubePlayerReady) {
            youTubePlayerView.getYouTubePlayerWhenReady(youTubePlayerCallbackLoadCurrentVideo);
        }
    }

    void playCurrentSongCallback(YouTubePlayer youTubePlayer) {
        if (currentSong == null) {
            YouTubePlayerUtils.loadOrCueVideo(youTubePlayer, getLifecycle(), "", 0);
            return;
        }
        if (!currentSong.getSong_link().equals(youTubePlayerTracker.getVideoId())) {
            YouTubePlayerUtils.loadOrCueVideo(youTubePlayer, getLifecycle(), currentSong.getSong_link(), currentSong.getStart_time());
        }
    }

    void switchToSongSafe(int song_number) {
        if (bPlaylistSync) {
            return;
        }
        if (song_number == 0 || song_number > maxSongNumber + 1) {
            return;
        }
        switchToSong(song_number);
    }

    void switchToSong(int song_number) {
        currentSongNumber = song_number;
        updateButtonEnabled();
        updateCurrentSong();
        playCurrentSong();
    }

    public void btnPrevSongOnClick(View v) {
        switchToSongSafe(currentSongNumber - 1);
    }

    public void btnNextSongOnClick(View v) {
        switchToSongSafe(currentSongNumber + 1);
    }

    void updateButtonEnabled() {
        btnPrev.setEnabled(!bPlaylistSync && currentSongNumber > 1);
        btnNext.setEnabled(!bPlaylistSync && currentSongNumber < maxSongNumber + 1);
    }

    void savePreferenceByString(int string_id, String preference) {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(getString(string_id), preference);
        ed.apply();
    }

    void savePreferenceByString(int string_id, boolean preference) {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean(getString(string_id), preference);
        ed.apply();
    }

    void loadPreferences() {
        sPref = getPreferences(MODE_PRIVATE);
        String savedStreamerName = sPref.getString(getString(R.string.teStreamerNameHint), "");
        teStreamerName.setText(savedStreamerName);
        bPlaylistSync = sPref.getBoolean(getString(R.string.miPlaylistSync), false);
        bRefreshPlaylist = sPref.getBoolean(getString(R.string.miRefreshPlaylist), false);
        bSongAutoswitch = sPref.getBoolean(getString(R.string.miSongAutoswitch), false);
        if (savedStreamerName.length() > 0) {
            Toast.makeText(this, "Last session loaded", Toast.LENGTH_SHORT).show();
        }
    }

    void showError(Throwable t) {
        Toast.makeText(this, "failure " + t, Toast.LENGTH_LONG).show();
    }
}