package com.example.imagegallery.adapter;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.imagegallery.ImageFolder;
import com.example.imagegallery.ImageGridView;
import com.example.imagegallery.MainActivity;
import com.example.imagegallery.R;
import com.example.imagegallery.VideoPlayer;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.rongi.rotate_layout.layout.RotateLayout;

import java.io.ByteArrayOutputStream;

public class ImageSliderAdapter extends PagerAdapter {

    Fragment fragment;
    ImageFolder imageFolder;
    Toolbar toolbar, bottomToolbar;
    PhotoView image;
    VideoView video;
    CountDownTimer timer;
    MediaController controller;
    Button btPlay;
    public ImageSliderAdapter()
    {
        this.fragment = null;
        this.imageFolder = null;
        this.toolbar = null;
        this.bottomToolbar = null;
        this.timer = null;
    }

    public ImageSliderAdapter(Fragment fragment, ImageFolder imageFolder, Toolbar toolbar,
                              Toolbar bottomToolbar, CountDownTimer timer)
    {
        this.fragment = fragment;
        this.imageFolder = imageFolder;
        this.toolbar = toolbar;
        this.bottomToolbar = bottomToolbar;
        this.timer = timer;
    }

    @Override
    public int getCount() {
        return imageFolder.getImagePath().size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == ((View) o);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        LayoutInflater inflater =  fragment.getLayoutInflater();
        View view;
        view = inflater.inflate(R.layout.image_fullscreen, container, false);


        image = view.findViewById(R.id.photoView);
        btPlay = view.findViewById(R.id.btPlay);

        Glide.with(fragment.getActivity()).load(imageFolder.getImagePath().get(position))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(image);

        final String path = imageFolder.getImagePath().get(position);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemSelected();
            }
        });
        if(imageFolder.isVideo())
        {
            //GetContrastBrightnessFilter((float) brightness / 30f, 2)
            image.setColorFilter(Color.parseColor(("#d3d3d3")), PorterDuff.Mode.DARKEN);
            btPlay.setVisibility(View.VISIBLE);
            btPlay.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent = new Intent(fragment.getActivity(), VideoPlayer.class);
                  intent.putExtra("videoPath", path);
                  fragment.startActivity(intent);
              }
          });
        }
        else {
            btPlay.setVisibility(View.GONE);
        }

        container.addView(view);
        return view;
    }

    private void onItemSelected()
    {
        int toolbarStatus = toolbar.getVisibility();
        if(toolbarStatus == View.VISIBLE)
        {
            slideUp(toolbar, true);
            toolbar.setVisibility(View.INVISIBLE);
            slideDown(bottomToolbar, true);
            bottomToolbar.setVisibility(View.INVISIBLE);


            if(timer != null)
            {
                timer.cancel();
            }
        }
        else
        {
            toolbar.setVisibility(View.VISIBLE);
            slideDown(toolbar, false);




            // toolbar.setTranslationY(toolbar.getHeight()*-1);
            bottomToolbar.setVisibility(View.VISIBLE);
            slideUp(bottomToolbar, false);

            //bottomToolbar.setTranslationY(toolbar.getHeight());
            timer.start();
        }

    }



    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public void slideUp(final Toolbar toolbar, boolean isHide)
    {
        TranslateAnimation animate;
        if (isHide) {
            animate=new TranslateAnimation(
                    0,
                    0,
                    0,
                    toolbar.getHeight() * -1);
        }
        else
        {
            animate=new TranslateAnimation(
                    0,
                    0,
                    toolbar.getHeight(),
                    0);
        }

        animate.setDuration(500);
        //animate.setFillAfter(true);
        toolbar.startAnimation(animate);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //toolbar.setTranslationY(toolbar.getHeight()*-1);
                //toolbar.setY(toolbar.getY()-toolbar.getHeight());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void slideDown(final Toolbar toolbar, boolean isHide)
    {
        TranslateAnimation animate;
        if(isHide) {
            animate = new TranslateAnimation(
                    0,
                    0,
                    0,
                    toolbar.getHeight());

        }
        else
        {
            animate = new TranslateAnimation(
                    0,
                    0,
                    toolbar.getHeight() * -1,
                   0);
        }
        animate.setDuration(500);
        animate.setFillAfter(true);
        toolbar.startAnimation(animate);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
               // toolbar.setTranslationY(toolbar.getHeight());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private ColorMatrixColorFilter GetContrastBrightnessFilter(float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        return new ColorMatrixColorFilter(cm);
    }

}