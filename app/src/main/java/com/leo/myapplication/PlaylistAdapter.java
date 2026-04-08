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

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private List<Playlist> playlistList = new ArrayList<>();
    private OnPlaylistClickListener listener;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    public void setOnPlaylistClickListener(OnPlaylistClickListener listener) {
        this.listener = listener;
    }

    public void setPlaylistList(List<Playlist> playlistList) {
        this.playlistList = playlistList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_playlist, parent, false);
        return new PlaylistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist currentPlaylist = playlistList.get(position);
        holder.textViewName.setText(currentPlaylist.getName());

        if (currentPlaylist.getImageUrl() != null && !currentPlaylist.getImageUrl().isEmpty()) {
            Picasso.get().load(currentPlaylist.getImageUrl()).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.music_image_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onPlaylistClick(currentPlaylist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        ImageView imageView;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.playlist_name);
            imageView = itemView.findViewById(R.id.playlist_image);
        }
    }
}