package com.example.vpbus.ui.BottomSheetPage;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.vpbus.ui.fragments.RouteListStopsFragment;
import com.example.vpbus.ui.fragments.RouteScheduleFragment;

public class RDBottomSheetPageAdapter extends FragmentStateAdapter {

    private String routeShortName;

    public RDBottomSheetPageAdapter(@NonNull FragmentActivity fa, String routeShortName) {
        super(fa);
        this.routeShortName = routeShortName;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new RouteScheduleFragment();  // BIỂU ĐỒ GIỜ
            case 1:
                return RouteListStopsFragment.newInstance(routeShortName);     // ĐÁNH GIÁ
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // số tab
    }
}

