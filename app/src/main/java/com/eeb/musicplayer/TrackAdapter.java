package com.eeb.musicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private final Context context;
    private ArrayList<Track> tracks;
    private RecyclerViewClickListener trackClickedListener;

    public TrackAdapter(Context context, ArrayList<Track> tracks, RecyclerViewClickListener trackClickedListener) {
        this.context = context;
        this.tracks = tracks;
        this.trackClickedListener = trackClickedListener;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.list_layout, null);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder trackViewHolder, int i) {
        Track track = tracks.get(i);

        trackViewHolder.trackTitle.setText(track.getTitle());
        trackViewHolder.trackDuration.setText(track.getTime());
        //TODO: trackViewHolder.pin.setChecked(track.isPinned());
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView trackTitle, trackDuration;
        final ToggleButton pin;

        TrackViewHolder(View view) {
            super(view);

            trackTitle = view.findViewById(R.id.trackTitle);
            trackDuration = view.findViewById(R.id.trackDuration);
            pin = view.findViewById(R.id.pin);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            trackClickedListener.recyclerViewListClicked(v, this.getAdapterPosition());
        }
    }

    interface RecyclerViewClickListener {
        void recyclerViewListClicked(View v, int position);
    }
}

