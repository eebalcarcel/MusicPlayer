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

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private Context context;
    private List<Track> trackList;

    public TrackAdapter(Context context, List<Track> trackList) {
        this.context = context;
        this.trackList = trackList;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_layout, null);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder trackViewHolder, int i) {
        Track track = trackList.get(i);

        trackViewHolder.trackTitle.setText(track.getTitle());
        trackViewHolder.trackLength.setText(track.getTime());
        //TODO: trackViewHolder.pin.setChecked(track.isPinned());
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    class TrackViewHolder extends RecyclerView.ViewHolder {

        TextView trackTitle, trackLength;
        ToggleButton pin;

        public TrackViewHolder(View view) {
            super(view);

            trackTitle = view.findViewById(R.id.trackTitle);
            trackLength = view.findViewById(R.id.trackLength);
            pin = view.findViewById(R.id.pin);
        }
    }
}
