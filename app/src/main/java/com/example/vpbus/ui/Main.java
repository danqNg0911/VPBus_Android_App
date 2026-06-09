package com.example.vpbus.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.vpbus.R;
import com.example.vpbus.ui.BottomSheetPage.MainPersonalPageAdapter;
import com.example.vpbus.util.NavigationUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class Main extends AppCompatActivity {
    private static final String GOOGLE_FORM_URL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.suggested);
        Button route = findViewById(R.id.routeButton);
        Button stops = findViewById(R.id.stopsButton);
        Button search = findViewById(R.id.searchButton);
        Button review = findViewById(R.id.reviewButton);

        setupPersonalTabs();

        route.setOnClickListener(view ->
                NavigationUtil.goTo(this, RouteActivity.class)
        );

        stops.setOnClickListener(view ->
                NavigationUtil.goTo(this, StopsActivity.class)
        );

        search.setOnClickListener(view ->
                NavigationUtil.goTo(this, CheckSearch.class)
        );

        review.setOnClickListener(view -> openReviewForm());
    }

    private void setupPersonalTabs() {
        TabLayout tabLayout = findViewById(R.id.personalTabLayout);
        ViewPager2 viewPager = findViewById(R.id.personalViewPager);

        MainPersonalPageAdapter adapter = new MainPersonalPageAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("\u0110\u1ecba \u0111i\u1ec3m y\u00eau th\u00edch");
                    break;
                case 1:
                    tab.setText("Chuy\u1ebfn \u0111i g\u1ea7n \u0111\u00e2y");
                    break;
            }
        }).attach();
    }

    private void openReviewForm() {
        if (GOOGLE_FORM_URL.isEmpty()) {
            Toast.makeText(this, "Ch\u01b0a c\u1ea5u h\u00ecnh link Google Form", Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_FORM_URL)));
    }
}
