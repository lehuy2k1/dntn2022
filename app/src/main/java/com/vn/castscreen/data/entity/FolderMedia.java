package com.vn.castscreen.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class FolderMedia implements Parcelable {
    private String name;
    private ArrayList<String> listPath;

    public FolderMedia(String name, ArrayList<String> listPath) {
        this.name = name;
        this.listPath = listPath;
    }

    public FolderMedia() {
    }

    protected FolderMedia(Parcel in) {
        name = in.readString();
        listPath = in.createStringArrayList();
    }

    public static final Creator<FolderMedia> CREATOR = new Creator<FolderMedia>() {
        @Override
        public FolderMedia createFromParcel(Parcel in) {
            return new FolderMedia(in);
        }

        @Override
        public FolderMedia[] newArray(int size) {
            return new FolderMedia[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getListPath() {
        return listPath;
    }

    public void setListPath(ArrayList<String> listPath) {
        this.listPath = listPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeStringList(listPath);
    }
}
