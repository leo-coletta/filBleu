package com.leo.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList = new ArrayList<>();
    private OnSongClickListener listener;

    private OnRemoveSongListener removeSongListener;
    private boolean showFullHearts = false;

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public interface OnRemoveSongListener {
        void onRemoveSong(Song song);
    }

    public void setOnSongClickListener(OnSongClickListener listener) {
        this.listener = listener;
    }

    public void setOnRemoveSongListener(OnRemoveSongListener listener) {
        this.removeSongListener = listener;
    }

    public void setShowFullHearts(boolean show) {
        this.showFullHearts = show;
        notifyDataSetChanged();
    }

    public void setSongList(List<Song> songList) {
        this.songList = new ArrayList<>(songList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song currentSong = songList.get(position);
        holder.textViewTitle.setText(currentSong.getTitle());
        holder.textViewArtist.setText(currentSong.getArtist());

        if (currentSong.getImageUrl() != null && !currentSong.getImageUrl().isEmpty()) {
            Picasso.get().load(currentSong.getImageUrl()).into(holder.songImageView);
        } else {
            holder.songImageView.setImageResource(R.drawable.music_image_placeholder);
        }

        if (showFullHearts) {
            holder.favoriteIcon.setBackgroundResource(R.drawable.heart_full);
        } else {
            holder.favoriteIcon.setBackgroundResource(R.drawable.heart);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSongClick(currentSong);
        });

        holder.favoriteIcon.setOnClickListener(v -> {
            if (removeSongListener != null) {
                removeSongListener.onRemoveSong(currentSong);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewArtist;
        ImageView favoriteIcon, songImageView;

        public SongViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.music_name_item_song);
            textViewArtist = itemView.findViewById(R.id.artist_name_item_song);
            favoriteIcon = itemView.findViewById(R.id.heart_button);
            songImageView = itemView.findViewById(R.id.music_image_item_song);
        }
    }
}