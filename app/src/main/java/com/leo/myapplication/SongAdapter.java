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

/**
 * Adaptateur gérant l'affichage d'une liste d'objets {@link Song} dans un {@link RecyclerView}.
 * <p>
 * Relie les données musicales (titre, artiste, image via Picasso) à la vue {@code item_song}
 * et capte les interactions utilisateurs (clics sur la piste et gestion des favoris).
 * </p>
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList = new ArrayList<>();
    private OnSongClickListener listener;
    private OnRemoveSongListener removeSongListener;
    private boolean showFullHearts = false;

    /** Interface pour capter le clic sur une musique. */
    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    /** Interface pour capter la suppression/retrait des favoris d'une musique. */
    public interface OnRemoveSongListener {
        void onRemoveSong(Song song);
    }

    public void setOnSongClickListener(OnSongClickListener listener) {
        this.listener = listener;
    }

    public void setOnRemoveSongListener(OnRemoveSongListener listener) {
        this.removeSongListener = listener;
    }

    /**
     * Force l'affichage du cœur plein pour tous les éléments de la liste.
     * @param show {@code true} pour remplir les cœurs, {@code false} sinon.
     */
    public void setShowFullHearts(boolean show) {
        this.showFullHearts = show;
        notifyDataSetChanged();
    }

    /**
     * Met à jour la liste des musiques et rafraîchit l'affichage.
     * @param songList La nouvelle liste de musiques.
     */
    public void setSongList(List<Song> songList) {
        this.songList = new ArrayList<>(songList);
        notifyDataSetChanged();
    }

    public List<Song> getSongList() { return songList; }

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
            Picasso.get().load(currentSong.getImageUrl()).into(holder.musicImage);
        } else {
            holder.musicImage.setImageResource(R.drawable.music_image_placeholder);
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
    public int getItemCount() { return songList.size(); }

    /** Vue qui maintient les références des composants d'un item musique. */
    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewArtist;
        ImageView favoriteIcon;
        ImageView musicImage;

        public SongViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.music_name_item_song);
            textViewArtist = itemView.findViewById(R.id.artist_name_item_song);
            favoriteIcon = itemView.findViewById(R.id.heart_button);
            musicImage = itemView.findViewById(R.id.music_image_item_song);
        }
    }
}