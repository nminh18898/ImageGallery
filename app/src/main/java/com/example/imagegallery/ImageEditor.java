package com.example.imagegallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class ImageEditor extends AppCompatActivity {

    PhotoView photo;
    String imagePath;
    Toolbar topToolbar, bottomToolbar;
    //Bitmap bitmapOrigin;
    //Bitmap bitmapSample;
    PhotoEditor photoOrigin;
    PhotoEditor photoSample;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editor);
        photo = findViewById(R.id.photoView);
        imagePath = getIntent().getExtras().getString("imagePath");
        topToolbar = (Toolbar) findViewById(R.id.topToolbar);
        bottomToolbar = (Toolbar) findViewById(R.id.bottomToolbar);
        createToolbar();

       /* Glide.with(this)
                .load(imagePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(photo);*/
        //photo.setImageURI(Uri.parse(imagePath));



        photoOrigin = new PhotoEditor(imagePath);
        photo.setImageBitmap(photoOrigin.getBitmapPhoto());

    }

    private void createToolbar() {
        //topToolbar.inflateMenu(R.menu.image_menu);
        bottomToolbar.inflateMenu(R.menu.editor_bottom_toolbar);


        topToolbar.setNavigationIcon(R.drawable.ic_action_back);
        topToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        bottomToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.action_highlight:
                       // photo.setImageBitmap(photoOrigin.doGrayscale());
                        photo.setImageBitmap(photoOrigin.blurImage(ImageEditor.this));
                        break;
                    case R.id.action_edit_photo_round_corners:
                        photo.setImageDrawable(photoOrigin.roundConers(getResources(), 100));
                        break;

                }
                return true;
            }
        });
    }






}
