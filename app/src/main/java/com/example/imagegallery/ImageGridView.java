package com.example.imagegallery;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.example.imagegallery.adapter.ImageFoldersGridViewAdapter;
import com.example.imagegallery.adapter.ImageGridViewAdapter;

public class ImageGridView extends AppCompatActivity {

    ImageFolder imageFolder;
    GridView gvImage;
    Toolbar toolbar;
    TextView tvTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid_view);
        imageFolder = (ImageFolder) getIntent().getExtras().getSerializable("imageFolder");
        gvImage = findViewById(R.id.gvImage);

        toolbar = findViewById(R.id.image_gridview_toolbar);
        tvTitle = findViewById(R.id.tvTitle);

        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        tvTitle.setText(imageFolder.getFolderName());

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        gvImage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("imageFolder", imageFolder);
                bundle.putInt("position", position);

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ImagePreviewSlider newFragment = ImagePreviewSlider.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateGallery();
    }

    private void updateGallery()
    {
        ImageGridViewAdapter adapter = new ImageGridViewAdapter(ImageGridView.this,imageFolder.getImagePath());
        gvImage.setAdapter(adapter);
    }
}
