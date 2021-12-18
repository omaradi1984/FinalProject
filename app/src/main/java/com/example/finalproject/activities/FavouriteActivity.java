package com.example.finalproject.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.beans.Image;
import com.example.finalproject.beans.User;
import com.example.finalproject.dao.DbOpener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class FavouriteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "FavouriteActivity";
    private static final int REQUEST_FAVOURITE = 3;

    int userId;
    User currentUser;
    Intent currentIntent;

    ListView favouriteView;

    SQLiteDatabase db;

    private ArrayList<Image> imageArrayList = new ArrayList<>();

   FavouriteListAdapter myListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        Log.d(TAG, "OnCreate: Started.");

        currentIntent = getIntent();
        currentUser = currentIntent.getParcelableExtra("currentUser");
        userId = currentUser.getId();
   //     imageArrayList = currentIntent.getParcelableArrayListExtra("favourite");

        //This gets the toolbar from the layout:
        Toolbar tBar = findViewById(R.id.toolbar);

        //This loads the toolbar, which calls onCreateOptionsMenu below:
        setSupportActionBar(tBar);

        //For NavigationDrawer:
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer, tBar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        myListAdapter = new FavouriteListAdapter(imageArrayList);
        favouriteView = findViewById(R.id.favourite_list_view);

        favouriteView.setAdapter(myListAdapter);

        //Function to refresh screen when pulling
        SwipeRefreshLayout refreshLayout = findViewById(R.id.Refresher);
        refreshLayout.setOnRefreshListener(()->{});
        refreshLayout.setRefreshing(false);

        //Load previous messages from database
        loadDataFromDatabase();

        favouriteView.setOnItemLongClickListener((parent, b, pos, id)->{
            Image selectedPicture = imageArrayList.get(pos);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.DialogueTItle)
                    .setMessage("Do you really want to remove the image from your favourite list?")
                    .setPositiveButton(R.string.PositiveButton, (click, arg)-> {
                        removeFromFavourite(selectedPicture);
                        imageArrayList.remove(pos);
                        myListAdapter.notifyDataSetChanged();
                        Snackbar.make(favouriteView, "Image has been deleted.", BaseTransientBottomBar.LENGTH_LONG).show();
                    })
                    .setNegativeButton(R.string.NegativeButton, (click, arg) ->{
                    });
            builder.create().show();
            return false;
        });
    }

    private void loadDataFromDatabase(){
        //get a database connection:
        DbOpener Opener = new DbOpener(this);
        db = Opener.getWritableDatabase();

        //query all the results from the database:
        Cursor results = db.rawQuery("SELECT " +
                                    "ID, " +
                                    "Longitude, " +
                                    "Latitude, " +
                                    "Date, " +
                                    "URL " +
                                    "FROM IMAGE " +
                                    "WHERE UserID = " + userId +"  AND InFavourite = 1;", null);

        printCursor(results, db.getVersion());

        //find the column indices:
        int idColIndex = results.getColumnIndex(DbOpener.COL_IMAGE_ID);
        int longitudeColIndex = results.getColumnIndex(DbOpener.COL_IMAGE_LONGITUDE);
        int latitudeColIndex = results.getColumnIndex(DbOpener.COL_IMAGE_LATITUDE);
        int dateColIndex = results.getColumnIndex(DbOpener.COL_IMAGE_DATE);
        int urlColIndex = results.getColumnIndex(DbOpener.COL_IMAGE_URL);

        //iterate over the results, return true if there is a next item:
        while(results.moveToNext())
        {
            int id = results.getInt(idColIndex);
            String longitude = results.getString(longitudeColIndex);
            String latitude = results.getString(latitudeColIndex);
            String date = results.getString(dateColIndex);
            String url = results.getString(urlColIndex);

            //add the favourite images to the array list:
            imageArrayList.add(new Image.ImageBuilder(id, longitude, latitude, date).url(url).build());
        }
    }

    protected void removeFromFavourite(Image image)
    {
        ContentValues cv = new ContentValues();
        image.setIsFavourite(0);
        cv.put(DbOpener.COL_IMAGE_IN_FAVOURITE,image.getIsFavourite());
        db.update(DbOpener.IMAGE_TABLE_NAME, cv, DbOpener.COL_IMAGE_ID + "= ?", new String[] {Long.toString(image.getId())});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;

        switch(item.getItemId())
        {
            //what to do when the menu item is selected:
            case R.id.logout_item:
                this.finish();
                break;
            case R.id.image_item:
                intent = new Intent(this, ImageGrabber.class);
                intent.putExtra("currentUser", currentUser);
                startActivityForResult(intent, REQUEST_FAVOURITE);
                break;
            case R.id.profile_item:
                intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivityForResult(intent, REQUEST_FAVOURITE);
                break;
            case R.id.weather_item:
                intent = new Intent(this, WeatherForecast.class);
                startActivity(intent);
                break;
            case R.id.favourite_item:
                break;
           // case R.id.history_item:
             //   intent = new Intent(this, History.class);
            //    startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;

        switch(item.getItemId())
        {
            //what to do when the menu item is selected:
            case R.id.logout_item:
                this.finish();
                break;
            case R.id.image_item:
                intent = new Intent(this, ImageGrabber.class);
                intent.putExtra("currentUser", currentUser);
                startActivityForResult(intent, REQUEST_FAVOURITE);
                break;
            case R.id.profile_item:
                intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivityForResult(intent, REQUEST_FAVOURITE);
                break;
            case R.id.weather_item:
                intent = new Intent(this, WeatherForecast.class);
                startActivity(intent);
                break;
            case R.id.favourite_item:
                break;
         //   case R.id.history_item:
                //   intent = new Intent(this, History.class);
                //    startActivity(intent);
        }

        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class FavouriteListAdapter extends BaseAdapter {
        public ArrayList<Image> imageArrayList;

        public FavouriteListAdapter(ArrayList<Image> imageArrayList) {
            this.imageArrayList = imageArrayList;
        }

        @Override
        public int getCount() {
            return imageArrayList.size();
        }

        public Object getItem(int position) {
            return imageArrayList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View old, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            Image image = imageArrayList.get(position);

            View view = inflater.inflate(R.layout.image_list_layout, parent, false);

            //populate image details into the image list layout
            ((TextView) view.findViewById(R.id.id)).setText(String.valueOf(image.getId()));
            ((TextView) view.findViewById(R.id.longitude)).setText(image.getLongitude());
            ((TextView) view.findViewById(R.id.latitude)).setText(image.getLatitude());
            ((TextView) view.findViewById(R.id.date)).setText(image.getDate());
            (view.findViewById(R.id.image)).setBackground(getImage(image.getUrl()));

            return view;
        }
    }

    protected void printCursor(Cursor c, int version){
        int totalCol = c.getColumnCount();
        String[] colNames = c.getColumnNames();
        int totalResults = c.getCount();

        Log.i("Database version: ", String.valueOf(version));
        Log.i("Total columns: ", String.valueOf(totalCol));
        for (String name : colNames) {
            Log.i("Column name: ", name);
            Log.i("Message content: ", name);
        }
        Log.i("Total records: ", String.valueOf(totalResults));
    }

    private Drawable getImage(String _url)
    {
        //Prepare to download image
        URL url;
        InputStream in;
        BufferedInputStream buf;

        try {
            url = new URL(_url);
            in = url.openStream();

            // Read the inputstream
            buf = new BufferedInputStream(in);

            // Convert the BufferedInputStream to a Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(buf);
            if (in != null) {
                in.close();
                buf.close();
            }
            return new BitmapDrawable(bitmap);
        } catch (Exception e) {
            Log.e("Error reading file", e.toString());
        }
        return null;
    }
}