package com.leo.myapplication;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList = new ArrayList<>();
    private OnSongClickListener listener;
    private OnFavoriteClickListener favoriteListener;

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Song song);
    }

    public void setOnSongClickListener(OnSongClickListener listener) {
        this.listener = listener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteListener = listener;
    }

    public void setSongList(List<Song> songList) {
        this.songList = songList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Assurez-vous d'avoir un layout item_song.xml pour chaque ligne
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song currentSong = songList.get(position);
        holder.textViewTitle.setText(currentSong.getTitle());
        holder.textViewArtist.setText(currentSong.getArtist());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onSongClick(currentSong);
            }
        });

        holder.favoriteIcon.setOnClickListener(v -> {
            if (favoriteListener != null && position != RecyclerView.NO_POSITION) {
                favoriteListener.onFavoriteClick(currentSong);
                if (currentSong.isFavorite()) {
                    holder.favoriteIcon.setImageResource(R.drawable.heart_full);
                } else {
                    holder.favoriteIcon.setImageResource(R.drawable.heart);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewArtist;
        ImageView favoriteIcon;

        public SongViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.music_name_item_song);
            textViewArtist = itemView.findViewById(R.id.artist_name_item_song);
            favoriteIcon = itemView.findViewById(R.id.heart_button);
        }
    }
}