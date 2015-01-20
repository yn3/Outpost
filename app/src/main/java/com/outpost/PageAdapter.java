package com.outpost;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by andre on 04.01.2015.
 */
class PageAdapter extends FragmentPagerAdapter {

    private final List<Fragment> fragments;

    public PageAdapter(FragmentManager manager, List<Fragment> fragments) {
        super(manager);
        this.fragments = fragments;

    }

    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }
}
