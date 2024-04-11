package com.example.nasacapstonecst2355;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.nasacapstonecst2355.DatabaseHelper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //setting ui elements
    private DatePicker datePicker;
    private Button btnFetchImage, btnSaveImage;
    private TextView tvDate, tvImageUrl;
    private ImageView ivNasaImage;
    private ProgressBar progressBar;
    private EditText etCustomDate;

    //navigation drawer
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    //date format api request
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    //shared preferences for storing images that user saves
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //navigation view with menu item click listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_saved_images) {
                startActivity(new Intent(MainActivity.this, SavedImagesActivity.class));
            } else if (itemId == R.id.nav_about) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        //initialize ui elements
        datePicker = findViewById(R.id.datePicker);
        btnFetchImage = findViewById(R.id.btnFetchImage);
        btnSaveImage = findViewById(R.id.btnSaveImage);
        tvDate = findViewById(R.id.tvDate);
        tvImageUrl = findViewById(R.id.tvImageUrl);
        ivNasaImage = findViewById(R.id.ivNasaImage);
        progressBar = findViewById(R.id.progressBar);
        etCustomDate = findViewById(R.id.etCustomDate);


        sharedPreferences = getSharedPreferences("NASA_IMAGES", MODE_PRIVATE);

        //button click listeners
        btnFetchImage.setOnClickListener(v -> fetchNasaImageOfTheDay());
        btnSaveImage.setOnClickListener(v -> saveCurrentImage());
        tvImageUrl.setOnClickListener(v -> openUrl(tvImageUrl.getText().toString()));

        //date picker max date to today so user can't select an invalid future date
        Calendar calendar = Calendar.getInstance();
        long maxDate = calendar.getTimeInMillis();
        datePicker.setMaxDate(maxDate);
    }

    //fetching NASA image of the day for the date
    private void fetchNasaImageOfTheDay() {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();

        final String date = String.format(Locale.US, "%d-%02d-%02d", year, month, day);
        new FetchImageTask().execute(date);
    }

    //asynctask to fetch the image from the api
    private class FetchImageTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String date = params[0];
            //api
            String urlString = "https://api.nasa.gov/planetary/apod?api_key=DgPLcIlnmN0Cwrzcg3e9NraFaYLIDI68Ysc6Zh3d&date=" + date;
            Log.d("FetchImageTask", "Request URL: " + urlString);
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    Log.d("FetchImageTask", "Response: " + response.toString());
                    return response.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("FetchImageTask", "Error fetching image: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String imageUrl = jsonObject.getString("url");
                    tvDate.setText(dateFormat.format(Calendar.getInstance().getTime()));
                    tvImageUrl.setText(imageUrl);
                    Picasso.get().load(imageUrl).into(ivNasaImage);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("FetchImageTask", "Error parsing JSON: " + e.getMessage());
                }
            }
        }
    }

    //save current image date, url
    private void saveCurrentImage() {
        String date = tvDate.getText().toString();
        String imageUrl = tvImageUrl.getText().toString();
        if (!date.isEmpty() && !imageUrl.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_DATE, date);
            values.put(DatabaseHelper.COLUMN_URL, imageUrl);

            SQLiteDatabase db = new DatabaseHelper(this).getWritableDatabase();
            long id = db.insert(DatabaseHelper.TABLE_NAME, null, values);
            if (id == -1) {
                Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
            }
            db.close();
        }
    }


    //if user wants to open the url in built in browser
    private void openUrl(String url) {
        if (!url.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    }

    //inflate options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //menu item selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_help) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.help)
                    .setMessage(R.string.help_text)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
