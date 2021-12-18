package com.example.finalproject.beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class User implements Parcelable {
    private int id;
    private String fName; //User first name.
    private String lName; //User last name.
    private String emailAddress;
    private String password;
    private String profilePicturePath; //User profile picture path.

    public User(int id, String fName, String lName, String emailAddress, String password, String profilePicturePath) {
        this.id = id;
        this.fName = fName;
        this.lName = lName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.profilePicturePath = profilePicturePath;
    }

    protected User(Parcel in) {
        id = in.readInt();
        fName = in.readString();
        lName = in.readString();
        emailAddress = in.readString();
        password = in.readString();
        profilePicturePath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(fName);
        dest.writeString(lName);
        dest.writeString(emailAddress);
        dest.writeString(password);
        dest.writeString(profilePicturePath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

}
