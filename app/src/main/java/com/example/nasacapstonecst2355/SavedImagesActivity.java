package com.example.nasacapstonecst2355;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class SavedImagesActivity extends AppCompatActivity {
    //list view to display saved images
    private ListView lvSavedImages;
    //list to store image data
    private ArrayList<String> savedImageList;
    //adapter for lsit view
    private ArrayAdapter<String> adapter;
    //nav drawer layout
    private DrawerLayout drawerLayout;
    //nav drawer toggle
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_images);

        //toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //nav drawer layout and toggle
        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            //activity navigations
            if (itemId == R.id.nav_main) {
                startActivity(new Intent(SavedImagesActivity.this, MainActivity.class));
            } else if (itemId == R.id.nav_about) {
                startActivity(new Intent(SavedImagesActivity.this, AboutActivity.class));
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(SavedImagesActivity.this, SettingsActivity.class));
            }
            //close nav drawer after selecting item
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        //initializing list view and the adapter
        lvSavedImages = findViewById(R.id.lvSavedImages);
        savedImageList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, savedImageList);
        lvSavedImages.setAdapter(adapter);

        //loading saved images from db
        loadSavedImages();

        //long click listener for list view to delete images from saved
        lvSavedImages.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String item = savedImageList.get(position);
                String date = item.split(" \\| ")[0];

                //delete selected image from db
                SQLiteDatabase db = new DatabaseHelper(SavedImagesActivity.this).getWritableDatabase();
                int rowsDeleted = db.delete(DatabaseHelper.TABLE_NAME,
                        DatabaseHelper.COLUMN_DATE + " = ?",
                        new String[]{date});
                if (rowsDeleted > 0) {
                    Toast.makeText(SavedImagesActivity.this, "Image deleted", Toast.LENGTH_SHORT).show();
                    //reload saved images to update with deleted image
                    loadSavedImages();
                }
                db.close();
                return true;
            }
        });

        //expand listview to provide more details
        lvSavedImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = savedImageList.get(position);
                String[] parts = item.split(" \\| ");
                String url = parts[1];
                Toast.makeText(SavedImagesActivity.this, "Details: This image is from " + url, Toast.LENGTH_LONG).show();
            }
        });
    }

    //load saved images from db and update lsit view
    private void loadSavedImages() {
        SQLiteDatabase db = new DatabaseHelper(this).getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME,
                new String[]{DatabaseHelper.COLUMN_DATE, DatabaseHelper.COLUMN_URL},
                null, null, null, null, null);

        savedImageList.clear();
        while (cursor.moveToNext()) {
            int dateColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE);
            int urlColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_URL);

            if (dateColumnIndex != -1 && urlColumnIndex != -1) {
                String date = cursor.getString(dateColumnIndex);
                String url = cursor.getString(urlColumnIndex);
                savedImageList.add(date + " | " + url);
            } else {
                Log.e("SavedImagesActivity", "Column index not found.");
            }
        }
        cursor.close();
        db.close();
        adapter.notifyDataSetChanged();
    }

}
