package com.example.imagegallery;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imagegallery.adapter.DestinationFoldersChosenAdapter;
import com.example.imagegallery.adapter.FavoriteFoldersChosenAdapter;

import java.io.File;
import java.util.ArrayList;

public class DestinationFolderChosen extends AppCompatActivity {

    GridView gvDestinationFolder;
    String imagePath;
    ArrayList<ImageFolder> destinationFolderList = new ArrayList<>();
    Toolbar toolbar;
    TextView title;
    boolean isCopy;
    boolean isVideo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_folder_chosen);

        toolbar = findViewById(R.id.destination_folder_chosen_toolbar);
        gvDestinationFolder = findViewById(R.id.gv_imageDestinationFolders);
        title = findViewById(R.id.destination_folder_chosen_tvTitle);

        title.setText(getResources().getString(R.string.select_destination_folders));

        imagePath =  getIntent().getExtras().getString("imagePath");
        isCopy = getIntent().getExtras().getBoolean("isCopy");
        isVideo = getIntent().getExtras().getBoolean("isVideo");

        if(isVideo)
        {
            destinationFolderList = getVideoFoldersList();
        }
        else
        {
            destinationFolderList = getImageFoldersList();
        }
        setAdapter();

        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        gvDestinationFolder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String inDirectory = getDirectoryFromPath(imagePath);
                String fileName = getNameFromPath(imagePath);
                String outDirectory = getDirectoryFromPath(destinationFolderList.get(position).getImagePath().get(0));

                if(isCopy)
                {
                    CopyFile(inDirectory,fileName,outDirectory);
                }
                else
                {
                    if(inDirectory.equals(outDirectory))
                    {
                        Toast.makeText(DestinationFolderChosen.this,
                                getResources().getString(R.string.can_not_move_file_to_the_same_destination),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    MoveFile(inDirectory,fileName,outDirectory);
                }

                Intent intent = new Intent(DestinationFolderChosen.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });



    }

    private String getDirectoryFromPath(String path)
    {
        File file = new File(path);
        String dir = file.getParent();
        return dir;
    }

    private String getNameFromPath(String path)
    {
        File file = new File(path);
        String name = file.getName();
        return name;
    }

    private void setAdapter()
    {
        DestinationFoldersChosenAdapter adapter = new DestinationFoldersChosenAdapter(this,destinationFolderList);
        gvDestinationFolder.setAdapter(adapter);
    }

    public ArrayList <ImageFolder> getVideoFoldersList() {
        ArrayList <ImageFolder> videoFolderList = new ArrayList<>();

        videoFolderList.clear();
        boolean isFolderExist = false;
        int folderPosition = 0;
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;

        String absolutePathOfImage = null;
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Thumbnails.DATA};

        final String orderBy = MediaStore.Video.Media.DATE_TAKEN;
        cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            for (int i = 0; i < videoFolderList.size(); i++) {
                if (videoFolderList.get(i).getFolderName().equals(cursor.getString(column_index_folder_name))) {
                    isFolderExist = true;
                    folderPosition = i;
                    break;
                } else {
                    isFolderExist = false;
                }
            }

            if (isFolderExist) {
                ArrayList<String> videoPath = new ArrayList<>();
                videoPath.addAll( videoFolderList.get(folderPosition).getImagePath());
                videoPath.add(absolutePathOfImage);
                videoFolderList.get(folderPosition).setImagePath(videoPath);

            } else {
                ArrayList<String> videoPath = new ArrayList<>();
                videoPath.add(absolutePathOfImage);
                ImageFolder videoFolder = new ImageFolder();
                videoFolder.setFolderName(cursor.getString(column_index_folder_name));
                videoFolder.setImagePath(videoPath);
                videoFolder.setVideo(true);
                videoFolderList.add(videoFolder);
            }


        }


        return videoFolderList;
    }

    public ArrayList <ImageFolder> getImageFoldersList() {
        ArrayList <ImageFolder> imageFolderList = new ArrayList<>();

        imageFolderList.clear();
        boolean isFolderExist = false;
        int folderPosition = 0;
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;

        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Thumbnails.DATA};

        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            for (int i = 0; i < imageFolderList.size(); i++) {
                if (imageFolderList.get(i).getFolderName().equals(cursor.getString(column_index_folder_name))) {
                    isFolderExist = true;
                    folderPosition = i;
                    break;
                } else {
                    isFolderExist = false;
                }
            }

            if (isFolderExist) {
                ArrayList<String> imagePath = new ArrayList<>();
                imagePath.addAll( imageFolderList.get(folderPosition).getImagePath());
                imagePath.add(absolutePathOfImage);
                imageFolderList.get(folderPosition).setImagePath(imagePath);

            } else {
                ArrayList<String> imagePath = new ArrayList<>();
                imagePath.add(absolutePathOfImage);
                ImageFolder imageFolder = new ImageFolder();
                imageFolder.setFolderName(cursor.getString(column_index_folder_name));
                imageFolder.setImagePath(imagePath);

                imageFolderList.add(imageFolder);
            }


        }


        for (int i = 0; i < imageFolderList.size(); i++) {
            Log.e("FOLDER", imageFolderList.get(i).getFolderName());
            for (int j = 0; j < imageFolderList.get(i).getImagePath().size(); j++) {
                Log.e("FILE", imageFolderList.get(i).getImagePath().get(j));
            }
        }

        return imageFolderList;
    }

    private void CopyFile(String inPath, String fileName, String outPath)
    {


        FileManipulator mFile = new FileManipulator(this);
        inPath = inPath.substring(1)+"/";
        outPath = outPath.substring(1)+"/";

        mFile.copyFile(inPath,fileName,outPath);

    }

    private void MoveFile(String inPath, String fileName, String outPath)
    {
        FileManipulator mFile = new FileManipulator(this);
        inPath = inPath.substring(1)+"/";
        outPath = outPath.substring(1)+"/";

        boolean isMoved = mFile.moveFile(inPath,fileName,outPath);
        if (isMoved)
        {
            Toast.makeText(this,getResources().getString(R.string.move_file_successfully),Toast.LENGTH_SHORT).show();
        }

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(new File(inPath+fileName))));


    }



}
