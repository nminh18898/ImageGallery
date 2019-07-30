package com.example.imagegallery;

import java.io.Serializable;
import java.util.ArrayList;

public class ImageFolder implements Serializable {
   // String folderPath;
    String folderName;
    ArrayList<String> imagePath;
    boolean isFavorite = false;
    boolean isVideo = false;

    /*public String getFolderPath() {
        return this.folderPath;
    }

    public void setFolderPath(String folder_path) {
        this.folderPath = folder_path;
    }*/

    public ArrayList<String> getImagePath() {
        return this.imagePath;
    }

    public void setImagePath(ArrayList<String> image_path) {
        this.imagePath = image_path;
    }

    public String getFolderName() {
        return this.folderName;
    }

    public void setFolderName(String folder_name) {
        this.folderName = folder_name;
    }

    public void setFavorite(boolean isFavorite)
    {
        this.isFavorite = isFavorite;
    }

    public void setVideo(boolean isVideo){this.isVideo = isVideo;}

    public boolean isVideo (){return  this.isVideo;}
}
