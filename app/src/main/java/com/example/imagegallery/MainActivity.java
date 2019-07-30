package com.example.imagegallery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.imagegallery.adapter.AlbumPagerAdapter;

public class MainActivity extends AppCompatActivity implements AlbumTab.OnFragmentInteractionListener,
        FavoriteTab.OnFragmentInteractionListener, VideoTab.OnFragmentInteractionListener{
    TabLayout tabLayout;
    ViewPager viewPager;
    private static final int REQUEST_PERMISSIONS = 100;
    AlbumPagerAdapter myPagerAdapter;
    boolean isGranted = false;
    int currentTab = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.mainAlbumPager);
        myPagerAdapter = new AlbumPagerAdapter(getSupportFragmentManager(),3);
        tabLayout = findViewById(R.id.mainTabLayout);

        tabLayout.setupWithViewPager(viewPager);


        // asking for permission
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED )
        ) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_CONTACTS)) && (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_CONTACTS)) && (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.CAMERA))

            ) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.please_allow_this_app_to_read_or_write_to_your_storage), Toast.LENGTH_LONG).show();

            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.CAMERA},
                        REQUEST_PERMISSIONS);
            }
        }else {
            isGranted = true;

        }

    }

    @Override
    public void onResume() {
        super.onResume();



        if(isGranted) {
            currentTab = viewPager.getCurrentItem();
            viewPager.setAdapter(myPagerAdapter);
            viewPager.setCurrentItem(currentTab);
        }
        //viewPager.setAdapter(myPagerAdapter);

    }




    @Override
    public void onFragmentInteraction(String message) {
        if(message.equals("Album"))
        {
            //Toast.makeText(this, "Album", Toast.LENGTH_SHORT).show();
        }

        if(message.equals("Favorites"))
        {
            //Toast.makeText(this, "Favorites", Toast.LENGTH_SHORT).show();
        }

        if(message.equals("Video"))
        {
            //Toast.makeText(this, "Favorites", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        isGranted = true;
                    }
                    else
                    {
                        isGranted = false;
                    }
                }
            }
        }

        if(isGranted) {
            viewPager.setAdapter(myPagerAdapter);
        }
        else {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.please_allow_this_app_to_read_or_write_to_your_storage), Toast.LENGTH_LONG).show();
        }
    }
}
