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

public class AlbumTab extends Fragment {

    GridView gvImageFolders;
    ArrayList<ImageFolder> imageFolderList = new ArrayList<>();
    OnFragmentInteractionListener callback;

    public static AlbumTab newInstance(String strArg1) {
        AlbumTab fragment = new AlbumTab();
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
        this.imageFolderList = getImageFoldersList();
        ImageFoldersGridViewAdapter adapter = new ImageFoldersGridViewAdapter(getActivity(),imageFolderList);
        gvImageFolders.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        RelativeLayout albumTabLayout =  (RelativeLayout)  inflater.inflate(R.layout.album_tab_layout, null);
        gvImageFolders = albumTabLayout.findViewById(R.id.gv_imageFolders);
        gvImageFolders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ImageGridView.class);
                intent.putExtra("imageFolder", imageFolderList.get(position));
                startActivity(intent);
            }
        });
       /* this.imageFolderList = getImageFoldersList();
        ImageFoldersGridViewAdapter adapter = new ImageFoldersGridViewAdapter(getActivity(),imageFolderList);
        gvImageFolders.setAdapter(adapter);*/
        return albumTabLayout;
    }



    public ImageFolder getImageFolderByName(String name)
    {
        ArrayList <ImageFolder> imageFolders = new ArrayList<>();
        imageFolders = getImageFoldersList();

        for(int i=0;i<imageFolders.size();i++)
        {
            if(imageFolders.get(i).getFolderName().equals(name))
            {
                return imageFolders.get(i);
            }
        }
        return null;
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
        cursor = getActivity().getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String message);
    }




}
