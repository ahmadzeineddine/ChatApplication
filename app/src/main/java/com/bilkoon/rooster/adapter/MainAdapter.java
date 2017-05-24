package com.bilkoon.rooster.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bilkoon.rooster.fragment.ContactsFragment;
import com.bilkoon.rooster.fragment.ConversationsFragment;
import com.bilkoon.rooster.fragment.StudentsHallFragment;


/**
 * Created by Ahmed on 5/1/2017.
 */

public class MainAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public MainAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ConversationsFragment tab1 = new ConversationsFragment();
                tab1.TAG = "ConversationsFragment";
                return tab1;
            case 1:
                StudentsHallFragment tab2 = new StudentsHallFragment();
                tab2.TAG = "StudentsHallFragment";
                return tab2;
            case 2:
                ContactsFragment tab3 = new ContactsFragment();
                tab3.TAG = "ContactsFragment";
                return tab3;
            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
