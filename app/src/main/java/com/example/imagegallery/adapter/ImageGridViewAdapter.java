package com.example.imagegallery.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.imagegallery.R;

import java.util.ArrayList;

public class ImageGridViewAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> imagePath;

    public ImageGridViewAdapter(Context context,  ArrayList<String> image_path)
    {
        this.context = context;
        this.imagePath = image_path;
    }

    @Override
    public int getCount() {
        return imagePath.size();
    }

    @Override
    public Object getItem(int position) {
        return imagePath.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /*ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setImageURI(Uri.parse(imagePath.get(position)));
            imageView.setLayoutParams(new GridView.LayoutParams(185, 185));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(5, 5, 5, 5);
        } else {
            imageView = (ImageView) convertView;
        }

        Glide.with(context).load("file://" + imagePath.get(position))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView);

        return convertView;*/

        ViewHolder holder;
        View item = convertView;

        if(item == null)
        {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            holder = new ViewHolder();
            item = inflater.inflate(R.layout.custom_image_gridview,null);
            holder.iv_image = item.findViewById(R.id.iv_gridview_image);
            item.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) item.getTag();
        }




        Glide.with(context).load(imagePath.get(position))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .into(holder.iv_image);

        return item;

    }
    private static class ViewHolder {
        ImageView iv_image;
    }

}
