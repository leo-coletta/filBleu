package com.leo.myapplication;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class Playlist implements Parcelable {
    private String id;
    private String name;
    private String imageUrl;
    private List<String> songIds;

    public Playlist() {
        this.songIds = new ArrayList<>();
    }

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
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getSongIds() { return songIds; }
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
