package com.example.imagegallery.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.example.imagegallery.ImageFolder;

import java.io.File;
import java.util.ArrayList;

public class GalleryDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME ="GalleryManager";
    public static final int DATABASE_VERSION = 1;
   // private static final String TABLE_FOLDER_FAVORITE ="FolderFavorite";
    private static final String TABLE_IMAGE_FAVORITE ="ImageFavorite";
    private static final String FOLDER_NAME ="folder_name";
    private static final String IMAGE_PATH ="image_path";

    Context context;

    public GalleryDatabase(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        /*String sql1= "create table TABLE_FOLDER_FAVORITE " +
                "( " +
                "_id integer primary key autoincrement, " +
                "folder_name text" +
                ")";*/


        String sql= "create table " + TABLE_IMAGE_FAVORITE +
                "( " +
                "_id integer primary key autoincrement, " +
                "folder_name text," +
                "image_path text" +
                ")";

        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("drop table if exists TABLE_FOLDER_FAVORITE");
        db.execSQL("drop table if exists " + TABLE_IMAGE_FAVORITE);
        this.onCreate(db);
    }

    public boolean insertImageFavorite(String folderName, String imagePath)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value=new ContentValues();
        value.put(FOLDER_NAME, folderName);
        value.put(IMAGE_PATH, imagePath);
        return db.insert(TABLE_IMAGE_FAVORITE,null, value)>0;
    }

    public boolean deleteImageFavorite(String folderName, String imagePath)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_IMAGE_FAVORITE,FOLDER_NAME + "=? AND "
                + IMAGE_PATH + "=?",new String[]{folderName,imagePath})>0;
    }


    public ArrayList<ImageFolder> getFolderAndImage()
    {
        ArrayList<ImageFolder> folderList = new ArrayList<ImageFolder>();
        SQLiteDatabase db=this.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from " + TABLE_IMAGE_FAVORITE,null);
        if(cursor.moveToFirst())
        {
            int column_index_folder_name, column_index_image_path;
            column_index_folder_name = cursor.getColumnIndexOrThrow(FOLDER_NAME);
            column_index_image_path = cursor.getColumnIndexOrThrow(IMAGE_PATH);
            boolean isFolderExist = false;
            int folderPosition = 0;
            String absolutePathOfImage = null;

            do{
                absolutePathOfImage = cursor.getString(column_index_image_path);

                File file = new File(absolutePathOfImage);
                if(file.exists())
                {
                    for (int i = 0; i < folderList.size(); i++) {
                        if (folderList.get(i).getFolderName().equals(cursor.getString(column_index_folder_name))) {
                            isFolderExist = true;
                            folderPosition = i;
                            break;
                        } else {
                            isFolderExist = false;
                        }
                    }

                    if (isFolderExist) {
                        ArrayList<String> imagePath = new ArrayList<>();
                        imagePath.addAll(folderList.get(folderPosition).getImagePath());
                        imagePath.add(absolutePathOfImage);
                        folderList.get(folderPosition).setImagePath(imagePath);

                    } else {
                        ArrayList<String> imagePath = new ArrayList<>();
                        imagePath.add(absolutePathOfImage);
                        ImageFolder imageFolder = new ImageFolder();
                        imageFolder.setFolderName(cursor.getString(column_index_folder_name));
                        imageFolder.setImagePath(imagePath);
                        imageFolder.setFavorite(true);
                        folderList.add(imageFolder);
                    }
                }
                else {
                    SQLiteDatabase database = this.getWritableDatabase();
                    db.delete(TABLE_IMAGE_FAVORITE,IMAGE_PATH + "=?",new String[]{absolutePathOfImage});
                }


            }while (cursor.moveToNext());
        }

        return folderList;
    }
}
