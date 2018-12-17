package com.eeb.musicplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private Context context;
    private List<Song> songList;

    public SongAdapter(Context context, List<Song> songList) {
        this.context = context;
        this.songList = songList;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_layout, null);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder songViewHolder, int i) {
        Song song = songList.get(i);

        songViewHolder.songTitle.setText(song.getTitle());
        songViewHolder.songLength.setText(song.getTime());
        //TODO: songViewHolder.pin.setChecked(song.isPinned());
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    class SongViewHolder extends RecyclerView.ViewHolder {

        TextView songTitle, songLength;
        ToggleButton pin;

        public SongViewHolder(View view) {
            super(view);

            songTitle = view.findViewById(R.id.songTitle);
            songLength = view.findViewById(R.id.songLength);
            pin = view.findViewById(R.id.pin);
        }
    }
}
