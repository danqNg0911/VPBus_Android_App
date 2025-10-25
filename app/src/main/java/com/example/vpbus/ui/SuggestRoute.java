package com.example.vpbus.ui;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;

import com.example.vpbus.R;
import com.example.vpbus.util.NavigationUtil;

public class SuggestRoute extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.search_trip);

        Button back = findViewById(R.id.backButton);

        back.setOnClickListener(view ->
                NavigationUtil.goTo(this, CheckSearch.class)
        );
    }
}
