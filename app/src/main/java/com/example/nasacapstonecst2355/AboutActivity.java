package com.example.nasacapstonecst2355;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class AboutActivity extends AppCompatActivity {
    //setting drawer layout for navigation drawer
    private DrawerLayout drawerLayout;
    //setting toggle for drawer
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        //toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //initialize layout and toggle
        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //navigation view with menu item click listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            //navigating to activities based on the menu item
            if (itemId == R.id.nav_main) {
                startActivity(new Intent(AboutActivity.this, MainActivity.class));
            } else if (itemId == R.id.nav_saved_images) {
                startActivity(new Intent(AboutActivity.this, SavedImagesActivity.class));
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(AboutActivity.this, SettingsActivity.class));
            }
            //close the navigation drawer after selecting an item
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
}
