package com.example.musicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.model.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final List<Song> songList;
    private int selectedPosition = 0;
    private final OnSongClickListener clickListener;

    public interface OnSongClickListener {
        void onSongClick(Song song, int position);
    }

    public SongAdapter(List<Song> songList, OnSongClickListener clickListener) {
        this.songList = songList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.bind(song, position, selectedPosition == position);

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();
            if (selectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
                clickListener.onSongClick(song, selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public void setSelectedPosition(int position) {
        int previousSelected = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousSelected);
        notifyItemChanged(selectedPosition);
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvSongNumber;
        private final TextView tvSongTitle;
        private final TextView tvSongArtist;
        private final TextView tvSongDuration;
        private final View playingIndicator;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSongNumber    = itemView.findViewById(R.id.tvSongNumber);
            tvSongTitle     = itemView.findViewById(R.id.tvSongTitle);
            tvSongArtist    = itemView.findViewById(R.id.tvSongArtist);
            tvSongDuration  = itemView.findViewById(R.id.tvSongDuration);
            playingIndicator = itemView.findViewById(R.id.playingIndicator);
        }

        public void bind(Song song, int position, boolean isSelected) {
            tvSongNumber.setText(String.valueOf(position + 1));
            tvSongTitle.setText(song.getTitle());
            tvSongArtist.setText(song.getArtist());
            tvSongDuration.setText(song.getDuration());

            // Highlight the currently playing song
            if (isSelected) {
                itemView.setBackgroundResource(R.drawable.bg_selected_song);
                playingIndicator.setVisibility(View.VISIBLE);
            } else {
                itemView.setBackgroundResource(R.drawable.bg_normal_song);
                playingIndicator.setVisibility(View.GONE);
            }
        }
    }
}
