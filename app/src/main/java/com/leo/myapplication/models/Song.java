package com.leo.myapplication.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * Représente un morceau de musique (chanson) dans l'application.
 * <p>
 * Contient toutes les métadonnées nécessaires à l'affichage et à la lecture d'une piste audio,
 * y compris son statut de favori. Implémente {@link Parcelable} pour permettre le transfert
 * de cet objet entre différentes Activités.
 * </p>
 */
public class Song implements Parcelable {
    private String id;
    private String title;
    private String artist;
    private String coverUrl;
    private String audioUrl;
    private String imageUrl;
    private boolean favorite = false;

    /**
     * Constructeur vide requis pour Firestore et l'instanciation de base.
     */
    public Song() {}

    /**
     * Constructeur utilisé par l'interface Parcelable pour recréer l'objet depuis un Parcel.
     *
     * @param in Le Parcel contenant les données sérialisées de la chanson.
     */
    protected Song(Parcel in) {
        id = in.readString();
        title = in.readString();
        artist = in.readString();
        coverUrl = in.readString();
        audioUrl = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    /** @return L'identifiant unique de la musique. */
    public String getId() { return id; }

    /** @param id L'identifiant unique à assigner. */
    public void setId(String id) { this.id = id; }

    /** @return Le titre de la musique. */
    public String getTitle() { return title; }

    /** @param title Le titre de la musique. */
    public void setTitle(String title) { this.title = title; }

    /** @return Le nom de l'artiste. */
    public String getArtist() { return artist; }

    /** @param artist Le nom de l'artiste. */
    public void setArtist(String artist) { this.artist = artist; }

    /** @return L'URL de la pochette de l'album. */
    public String getCoverUrl() { return coverUrl; }

    /** @param coverUrl L'URL de la pochette de l'album. */
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    /** @return L'URL du fichier audio pour la lecture. */
    public String getAudioUrl() { return audioUrl; }

    /** @param audioUrl L'URL du fichier audio. */
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    /** @return L'URL de l'image (format alternatif). */
    public String getImageUrl() { return imageUrl; }

    /** @param imageUrl L'URL de l'image. */
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /**
     * Indique si la musique a été ajoutée aux favoris par l'utilisateur.
     *
     * @return {@code true} si la musique est en favori, {@code false} sinon.
     */
    public boolean isFavorite() { return favorite; }

    /**
     * Modifie le statut de favori de la musique.
     *
     * @param favorite {@code true} pour mettre en favori, {@code false} pour retirer.
     */
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(artist);
        parcel.writeString(coverUrl);
        parcel.writeString(audioUrl);
        parcel.writeString(imageUrl);
    }
}