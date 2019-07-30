package com.example.imagegallery;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.example.imagegallery.adapter.ImageFoldersGridViewAdapter;

import java.util.ArrayList;

public class VideoTab extends Fragment {
    GridView gvVideoFolders;
    ArrayList<ImageFolder> videoFolderList = new ArrayList<>();
    OnFragmentInteractionListener callback;

    public static VideoTab newInstance(String strArg1) {
        VideoTab fragment = new VideoTab();
        Bundle bundle = new Bundle();
        bundle.putString("arg1", strArg1);
        fragment.setArguments(bundle);
        return fragment;
    }// newInstance

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callback = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        callback = null; // avoid leaking
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.videoFolderList = getVideoFoldersList();
        ImageFoldersGridViewAdapter adapter = new ImageFoldersGridViewAdapter(getActivity(),videoFolderList);
        gvVideoFolders.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        RelativeLayout videoTabLayout =  (RelativeLayout)  inflater.inflate(R.layout.album_tab_layout, null);
        gvVideoFolders = videoTabLayout.findViewById(R.id.gv_imageFolders);
        gvVideoFolders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ImageGridView.class);
                intent.putExtra("imageFolder", videoFolderList.get(position));
                startActivity(intent);
            }
        });
       /* this.imageFolderList = getImageFoldersList();
        ImageFoldersGridViewAdapter adapter = new ImageFoldersGridViewAdapter(getActivity(),imageFolderList);
        gvImageFolders.setAdapter(adapter);*/
        return videoTabLayout;
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
        cursor = getActivity().getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

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


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String message);
    }
}
