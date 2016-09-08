package com.example.paul.fourdo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

/**
 * Created by Paul on 24/04/2015.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    // Tab titles
    private static final int NUM_TABS = 2;
    private String[] tabTitles = {"Today", "Someday"};
    private Tab[] tabs = new Tab[NUM_TABS]; // Array to hold .Tab instances

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        Bundle bundle = new Bundle();
        bundle.putInt("tabIndex", index);

        switch(index){
            case 0:
                tabs[0] = new Tab();
                tabs[0].setArguments(bundle);
                return tabs[0];
            case 1:
                tabs[1] = new Tab();
                tabs[1].setArguments(bundle);
                return tabs[1];
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return NUM_TABS;
    }

    public Tab getTabInstance(int position){
        return tabs[position];
    }

}

