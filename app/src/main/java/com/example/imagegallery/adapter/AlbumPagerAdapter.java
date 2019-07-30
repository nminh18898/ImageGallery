package com.example.imagegallery.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.Toast;

import com.example.imagegallery.AlbumTab;
import com.example.imagegallery.FavoriteTab;
import com.example.imagegallery.VideoTab;

import java.util.ArrayList;

public class AlbumPagerAdapter extends FragmentStatePagerAdapter {
    int numTabs;
    AlbumTab tab1;
    VideoTab tab2;
    FavoriteTab tab3;
    int currentItem=0;

    public AlbumPagerAdapter (FragmentManager fm, int NumberOfTabs)
    {
        super(fm);
        this.numTabs = NumberOfTabs;
    }

    @Override
    public Fragment getItem(int i) {
        switch(i)
        {
            case 0:
                tab1 = tab1.newInstance("Album-tab");
                return tab1;
            case 1:
                tab2 = tab2.newInstance("Video-Tab");
                return  tab2;
            case 2:
                tab3 = tab3.newInstance("Favorite-Tab");
                return  tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position){
            case 0:
                return "Picture";
            case 1:
                return "Video";
            case 2:
                return "Favorite";
            default:
                return "";
        }
    }
}
