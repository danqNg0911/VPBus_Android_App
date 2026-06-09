package com.example.vpbus.ui.BottomSheetPage;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.vpbus.ui.fragments.FavoritePlacesFragment;
import com.example.vpbus.ui.fragments.RecentTripsFragment;

public class MainPersonalPageAdapter extends FragmentStateAdapter {

    public MainPersonalPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FavoritePlacesFragment();
            case 1:
                return new RecentTripsFragment();
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
