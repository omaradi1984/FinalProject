package com.example.finalproject.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {
    private final long id;
    private final String longitude;
    private final String latitude;
    private final String date;
    private String url;
    private int isFavourite;
    private int isDownloaded;

    private Image(ImageBuilder builder){
        this.id = builder.id;
        this.longitude = builder.longitude;
        this.latitude = builder.latitude;
        this.date = builder.date;
        this.url = builder.url;
        this.isFavourite = builder.isFavourite;
        this.isDownloaded = builder.isDownloaded;
    }

    public Image(long id, String longitude, String latitude, String date, String url, int isFavourite, int isDownloaded) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.date = date;
        this.url = url;
        this.isFavourite = isFavourite;
        this.isDownloaded = isDownloaded;
    }

    protected Image(Parcel in) {
        id = in.readLong();
        longitude = in.readString();
        latitude = in.readString();
        date = in.readString();
        url = in.readString();
        isFavourite = in.readInt();
        isDownloaded = in.readInt();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

    public int getIsDownloaded() {
        return isDownloaded;
    }

    public void setIsDownloaded(int isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    public int getIsFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(int favourite) {
        isFavourite = favourite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(longitude);
        dest.writeString(latitude);
        dest.writeString(date);
        dest.writeString(url);
        dest.writeInt(isFavourite);
        dest.writeInt(isDownloaded);
    }

    public static class ImageBuilder
    {
        private final long id;
        private final String longitude;
        private final String latitude;
        private final String date;
        private String url;
        private int isFavourite;
        private int isDownloaded;

        public ImageBuilder(long id, String longitude, String latitude, String date) {
            this.id = id;
            this.longitude = longitude;
            this.latitude = latitude;
            this.date = date;
        }

        public ImageBuilder url(String url){
            this.url = url;
            return this;
        }

        public ImageBuilder isFavourite(int isFavourite){
            this.isFavourite = isFavourite;
            return this;
        }

        public ImageBuilder isDownloaded(int isDownloaded){
            this.isDownloaded = isDownloaded;
            return this;
        }

        public Image build(){
            Image image = new Image(this);
            return image;
        }
    }
}
