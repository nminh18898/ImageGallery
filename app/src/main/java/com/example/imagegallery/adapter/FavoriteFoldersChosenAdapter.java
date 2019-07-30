package com.example.imagegallery.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.imagegallery.ImageFolder;
import com.example.imagegallery.R;

import java.util.ArrayList;

public class FavoriteFoldersChosenAdapter extends BaseAdapter {
    Context context;
    ArrayList<ImageFolder> imageFolders;


    public FavoriteFoldersChosenAdapter(Context context,  ArrayList<ImageFolder> image_folders)
    {
        this.context = context;

        this.imageFolders = image_folders;


    }

    @Override
    public int getCount() {
        return imageFolders.size();
    }

    @Override
    public Object getItem(int position) {
        return imageFolders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //View-Holder pattern
    private static class ViewHolder {
        TextView tv_folderName, tv_folderSize;
        ImageView iv_image;
    } // end of view holder

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View item = convertView;

        if(item == null)
        {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            holder = new ViewHolder();
            item = inflater.inflate(R.layout.custom_image_folder_gridview,null);
            holder.iv_image = item.findViewById(R.id.iv_gridview_image);
            holder.tv_folderName = item.findViewById(R.id.tv_gridview_folderName);
            holder.tv_folderSize = item.findViewById(R.id.tv_gridview_folderSize);
            item.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) item.getTag();
        }


        if(position == 0) // create new album
        {
            holder.tv_folderName.setEnabled(false);
            holder.tv_folderSize.setText(R.string.create_new_album);

            Glide.with(context).load(R.drawable.new_folder_white_background)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.iv_image);
        }
        else {
            holder.tv_folderName.setText(imageFolders.get(position).getFolderName());
            holder.tv_folderSize.setText(String.valueOf(imageFolders.get(position).getImagePath().size()));
            Glide.with(context).load(imageFolders.get(position).getImagePath().get(0))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.iv_image);
        }

        return item;
    }
}
