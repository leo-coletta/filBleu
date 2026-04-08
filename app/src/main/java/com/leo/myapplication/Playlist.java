package com.leo.myapplication;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une liste de lecture (Playlist) créée par un utilisateur.
 * Contient les informations de base et les identifiants des chansons ({@link Song}) qu'elle contient.
 */
public class Playlist implements Parcelable {
    private String id;
    private String name;
    private String imageUrl;
    private List<String> songIds;

    /**
     * Constructeur par défaut initialisant une liste de musiques vide.
     */
    public Playlist() {
        this.songIds = new ArrayList<>();
    }

    /**
     * Crée une nouvelle playlist avec un nom et une image.
     *
     * @param name     Le nom de la playlist.
     * @param imageUrl L'URL de l'image d'illustration.
     */
    public Playlist(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.songIds = new ArrayList<>();
    }

    protected Playlist(Parcel in) {
        id = in.readString();
        name = in.readString();
        imageUrl = in.readString();
        songIds = in.createStringArrayList();
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) { return new Playlist(in); }
        @Override
        public Playlist[] newArray(int size) { return new Playlist[size]; }
    };

    /** @return L'identifiant Firestore de la playlist. */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    /** @return Le nom de la playlist. */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** @return L'URL de l'image de couverture. */
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /**
     * Récupère la liste des identifiants des chansons contenues dans cette playlist.
     *
     * @return Une liste de chaînes de caractères représentant les IDs.
     */
    public List<String> getSongIds() { return songIds; }

    /** @param songIds La nouvelle liste d'identifiants de chansons. */
    public void setSongIds(List<String> songIds) { this.songIds = songIds; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeStringList(songIds);
    }
}