package com.example.mohamed.chatapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * Created by mohamed on 4/9/2018.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    private  int numberOfTabs;
    public PagerAdapter(FragmentManager fm,int numberOfTabs ) {
        super(fm);
        this.numberOfTabs=numberOfTabs;
    }
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return  new RequestesFragment();
            case 1:
                return  new ChatsFragment();

            case 2:
                return  new FriendsFragment();

        }
        return null;
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}
