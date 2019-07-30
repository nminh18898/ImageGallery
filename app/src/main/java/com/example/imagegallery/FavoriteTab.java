package com.example.imagegallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.example.imagegallery.adapter.ImageFoldersGridViewAdapter;
import com.example.imagegallery.database.GalleryDatabase;

import java.util.ArrayList;

public class FavoriteTab extends Fragment {
    GridView gvImageFavoriteFolders;
    ArrayList<ImageFolder> imageFavoriteFolderList = new ArrayList<>();
    OnFragmentInteractionListener callback;

    public static FavoriteTab newInstance(String strArg1) {
        FavoriteTab fragment = new FavoriteTab();
        Bundle bundle = new Bundle();
        bundle.putString("arg1", strArg1);
        fragment.setArguments(bundle);
        return fragment;
    }// newInstance

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
    public void onResume() {
        super.onResume();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        RelativeLayout favoriteTabLayout =  (RelativeLayout)  inflater.inflate(R.layout.favorite_tab_layout, null);
        gvImageFavoriteFolders = favoriteTabLayout.findViewById(R.id.gv_imageFavoriteFolders);
        gvImageFavoriteFolders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ImageGridView.class);
                intent.putExtra("imageFolder", imageFavoriteFolderList.get(position));
                startActivity(intent);
            }
        });

        GalleryDatabase db;
        db=new GalleryDatabase(getActivity());

        imageFavoriteFolderList = db.getFolderAndImage();
        ImageFoldersGridViewAdapter adapter = new ImageFoldersGridViewAdapter(getActivity(),imageFavoriteFolderList);
        gvImageFavoriteFolders.setAdapter(adapter);


        return favoriteTabLayout;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String message);
    }
}
