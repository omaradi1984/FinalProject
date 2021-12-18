package com.example.finalproject.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.example.finalproject.R;
import com.example.finalproject.beans.Image;
import com.example.finalproject.beans.User;
import com.example.finalproject.dao.DbOpener;
import com.google.android.material.navigation.NavigationView;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ImageGrabber extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ImageGrabber";

    User currentUser;
    Intent currentIntent;

    int userId;

    EditText longitude, latitude;
    TextView datePicker;
    Button getImage, addToFavourite; //, downloadImage
    ImageView imageView;
    ProgressBar progressBar;

    DatePickerDialog datePickerDialog;

    OutputStream outputStream;

    String lon, lat, date;

    String api = "https://api.nasa.gov/planetary/earth/imagery?lon=%1$s&lat=%2$s&date=%3$s&api_key=F7sihRubPGBTaa72eQ6HpMyjiYpn05OwV2MYiupF";

    private final ArrayList<Image> imageArrayList = new ArrayList<>();

    SQLiteDatabase db;

    static final int REQUEST_IMAGE_GRABBER = 2;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grabber);
        Log.d(TAG, "OnCreate: Started.");

        currentIntent = getIntent();
        currentUser = currentIntent.getParcelableExtra("currentUser");
        userId = currentUser.getId();

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

        //Instantiating objects
        //Text fields
        longitude = findViewById(R.id.longitude_field);
        latitude = findViewById(R.id.latitude_field);
        datePicker = findViewById(R.id.date_picker);

        //Image viewer
        imageView = findViewById(R.id.image_viewer);

        //Buttons
        addToFavourite = findViewById(R.id.favourite_button);
        getImage = findViewById(R.id.get_image_button);
      //  downloadImage = findViewById(R.id.download_button);

        progressBar = findViewById(R.id.progressBar);

        //Invoking a calendar instance to show calendar on selecting datepicker field.
        datePicker.setOnClickListener(v -> {
            // calender class's instance and get current date , month and year from calender
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR); // current year
            int mMonth = c.get(Calendar.MONTH); // current month
            int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
            // date picker dialog
            datePickerDialog = new DatePickerDialog(ImageGrabber.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        // set day of month , month and year value in the edit text
                        datePicker.setText(year + "-" + String.format(Locale.US, "%02d", monthOfYear) + "-" + String.format(Locale.US, "%02d", dayOfMonth));

                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        });


        //Getting imagery based on the information provided.
        getImage.setOnClickListener(click -> {
           lon = longitude.getText().toString();
            lat = latitude.getText().toString();
            date = datePicker.getText().toString();
            String newUrl = String.format(api, lon, lat, date);

            //declaring an image id
            long newImageId;

            // Start the getImage task with the given longitude, latitude and date.
            if (lon.isEmpty() || lat.isEmpty() || date.isEmpty()) {
                Toast.makeText(ImageGrabber.this, "Please enter imagery details!", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                new ImageQuery(newUrl, lon, lat, date).execute();
                if(imageView.getDrawable() != null){
                    //inserting new entry in DB and getting new image ID.
                    newImageId = insertInImageDb(userId,lon, lat, date, newUrl);
                    //Building a new image object using a builder.
                    Image newImage = new Image.ImageBuilder(newImageId,lon, lat, date)
                            .url(newUrl)
                            .isFavourite(0)
                            .isDownloaded(0)
                            .build();

                    imageArrayList.add(newImage);

                 //   checkIfImageDownloaded(lon, lat, date);
                    checkIfImageInFavourite(userId, lon, lat, date);
                }else{
                    Toast.makeText(ImageGrabber.this, "There is no imagery associated with the information provided! Try again.",Toast.LENGTH_LONG).show();
                }

            }
        });

       /* downloadImage.setOnClickListener(v -> {
            lon = longitude.getText().toString();
            lat = latitude.getText().toString();
            date = datePicker.getText().toString();
            String newUrl = String.format(api, lon, lat, date);
                    if (ContextCompat.checkSelfPermission(ImageGrabber.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        new ImageDownload(newUrl, lon, lat, date).execute();
                        //downloadImage();
                    } else {
                        askPermission();
                    }
                }
        );*/

        addToFavourite.setOnClickListener(click -> {

            if(imageView.getDrawable() != null && imageArrayList.get(imageArrayList.size()-1).getIsFavourite() == 0){
                addToFavourite(imageArrayList.get(imageArrayList.size()-1));
                Toast.makeText(ImageGrabber.this,"Image added to favourites!", Toast.LENGTH_SHORT).show();
            }else if (imageView.getDrawable() != null && imageArrayList.get(imageArrayList.size()-1).getIsFavourite() == 1){
                removeFromFavourite(imageArrayList.get(imageArrayList.size()-1));
            }
        });
    }
    private void addToFavourite(Image image){

            ContentValues cv = new ContentValues();
            image.setIsFavourite(1);
            cv.put(DbOpener.COL_IMAGE_IN_FAVOURITE,image.getIsFavourite());
            db.update(DbOpener.IMAGE_TABLE_NAME, cv, DbOpener.COL_IMAGE_ID + "= ?", new String[] {Long.toString(image.getId())});
    }

    protected void removeFromFavourite(Image image)
    {
        ContentValues cv = new ContentValues();
        image.setIsFavourite(0);
        cv.put(DbOpener.COL_IMAGE_IN_FAVOURITE,image.getIsFavourite());
        db.update(DbOpener.IMAGE_TABLE_NAME, cv, DbOpener.COL_IMAGE_ID + "= ?", new String[] {Long.toString(image.getId())});
    }

    private void checkIfImageInFavourite(int userId, String lon, String lat, String date) {
        //get a database connection:
        DbOpener dbOpener = new DbOpener(this);
        db = dbOpener.getWritableDatabase();

        //query all the results from the database:
        Cursor results = db.rawQuery("SELECT InFavourite FROM IMAGE where UserID = '" + userId + "' AND Longitude LIKE '"+lon+"'" +
                                    "AND Latitude LIKE '"+lat+"' AND Date LIKE '"+date+"'", null);

        int isFavouriteColIndex = results.getColumnIndex(DbOpener.COL_IMAGE_IN_FAVOURITE);

        while(results.moveToNext()){
            int isFavourite = results.getInt(isFavouriteColIndex);
            if(isFavourite == 0){
                addToFavourite.setBackgroundResource(R.drawable.favourite_before);
            }else if(isFavourite == 1){
                addToFavourite.setBackgroundResource(R.drawable.favourite_after);
            }
        }
        results.close();
    }

    /*private long checkIfImageInHistory(int userId, String lon, String lat, String date) {
        long historyId = 0;
        //get a database connection:
        DbOpener dbOpener = new DbOpener(this);
        db = dbOpener.getWritableDatabase();

        //query all the results from the database:
        Cursor results = db.rawQuery("SELECT * FROM HISTORY where UserID = '" + userId + "' AND Longitude LIKE '"+lon+"'" +
                "AND Latitude LIKE '"+lat+"' AND Date LIKE '"+date+"'", null);

        int idColumnIndex = results.getColumnIndex(DbOpener.COL_IMAGE_ID);

        while(results.moveToNext()){
            historyId = results.getLong(idColumnIndex);
        }
        results.close();
        return historyId;
    }*/

   /* private void askPermission() {
        ActivityCompat.requestPermissions(ImageGrabber.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_IMAGE_GRABBER);
    }*/

   /* private void checkIfImageDownloaded(String lon, String lat, String date){
        // naming file with longitude, latitude and date info.
        String fileName = lon + lat + date;

        File dir = new File(Environment.getExternalStorageDirectory(), "NASA Earth Imagery Database/" + currentUser.getId());

        File file = new File(dir,  fileName + ".PNG");

     /*   if(file.exists()){
           downloadImage.setBackgroundResource(R.drawable.download_after);
        }else{
            downloadImage.setBackgroundResource(R.drawable.download_before);
        }
    }*/

    private void downloadImage() {
        // naming file with longitude, latitude and date info.
        String fileName = lon + lat + date;

        File dir = new File(Environment.getExternalStorageDirectory(), getApplication().getFilesDir() + "/Downloads/" +  currentUser.getId());

        if(!dir.exists()){
            dir.mkdir();
        }

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        File file = new File(dir,  fileName + ".PNG");
        if(!file.exists()){
           createImage(file, bitmap);
            Toast.makeText(ImageGrabber.this, "Image successfully saved!", Toast.LENGTH_SHORT).show();
          //  downloadImage.setBackgroundResource(R.drawable.download_after);
        //    addImageToGallery(file);
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.DownloadConfirmation)
                    .setMessage("The image already exists!" +
                            "\nDo you really want to download it again?")
                    .setPositiveButton(R.string.PositiveButton, (click, arg)->{
                        createImage(file, bitmap);
                        Toast.makeText(ImageGrabber.this, "Image successfully saved!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.NegativeButton, (click, arg)->{
                    });
            builder.create().show();
        }
    }

    private void createImage(File file, Bitmap bitmap){
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        try {
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*private void addImageToGallery(File f) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }*/

   /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_IMAGE_GRABBER){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                downloadImage();
            }else{
                Toast.makeText(ImageGrabber.this,"Please give permission to save images.",Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    /**
     * Simple function to set a Drawable to the image View
     * @param drawable to set imageviewer
     */
    private void setImageToImageViewer(Drawable drawable)
    {
        imageView.setImageDrawable(drawable);
    }

    private long insertInImageDb(int userId, String lon, String lat, String date, String url) {
       long newImageId = 0;

        //get a database connection:
        DbOpener dbOpener = new DbOpener(this);
        db = dbOpener.getWritableDatabase();

        //add to the database and get the new ID
        ContentValues cv = new ContentValues();

        //Provide a value for every database column defined in UserDbOpener.java:
        //put strings in the respective columns:
        cv.put(DbOpener.COL_IMAGE_USER_ID, userId);
        cv.put(DbOpener.COL_IMAGE_LONGITUDE, lon);
        cv.put(DbOpener.COL_IMAGE_LATITUDE, lat);
        cv.put(DbOpener.COL_IMAGE_DATE, date);
        cv.put(DbOpener.COL_IMAGE_URL, url);

            //Insert in the database:
            newImageId = db.insert(DbOpener.IMAGE_TABLE_NAME, null, cv);

           return newImageId;
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
                break;
            case R.id.profile_item:
                intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
                break;
            case R.id.weather_item:
                intent = new Intent(this, WeatherForecast.class);
                startActivity(intent);
                break;
            case R.id.favourite_item:
                intent = new Intent(this, FavouriteActivity.class);
                intent.putExtra("currentUser", currentUser);
                intent.putParcelableArrayListExtra("favourite", imageArrayList);
                startActivityForResult(intent, REQUEST_IMAGE_GRABBER);
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
                break;
            case R.id.profile_item:
                intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivityForResult(intent, REQUEST_IMAGE_GRABBER);
                break;
            case R.id.weather_item:
                intent = new Intent(this, WeatherForecast.class);
                startActivity(intent);
                break;
            case R.id.favourite_item:
                intent = new Intent(this, FavouriteActivity.class);
                intent.putExtra("currentUser", currentUser);
                intent.putParcelableArrayListExtra("favourite", imageArrayList);
                startActivityForResult(intent, REQUEST_IMAGE_GRABBER);
                break;
        //    case R.id.history_item:
                //   intent = new Intent(this, History.class);
                //                    intent.putExtra("currentUser", currentUser);
                //                intent.putParcelableArrayListExtra("favourite", imageArrayList);
                //                startActivityForResult(intent, REQUEST_IMAGE_GRABBER);
        }
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public class ImageQuery extends AsyncTask<String, Integer, Drawable> {

        //Declaring variables
        String longitude;
        String latitude;
        String date;
        String url;

        public ImageQuery(String url, String longitude, String latitude, String date) {
            this.url = url;
            this.longitude = longitude;
            this.latitude = latitude;
            this.date = date;
        }

        @Override
        protected Drawable doInBackground(String... args) {
            // This is done in a background thread

            publishProgress(25);
            publishProgress(50);
            publishProgress(75);

            return getImage(url);
        }

        protected void onProgressUpdate(Integer... args) {
            int value = args[0];
            progressBar.setProgress(value);
        }

        /**
         * Called after the image has been downloaded
         * -> this calls a function on the main thread again
         */
        @Override
        protected void onPostExecute(Drawable image) {
            setImageToImageViewer(image);

            progressBar.setVisibility(View.INVISIBLE);
        }
    }

        public class ImageDownload extends AsyncTask<String, Integer, Drawable> {

            //Declaring variables
            String longitude;
            String latitude;
            String date;
            String url;

            public ImageDownload(String url, String longitude, String latitude, String date) {
                this.url = url;
                this.longitude = longitude;
                this.latitude = latitude;
                this.date = date;
            }

            @Override
            protected Drawable doInBackground(String... args) {
                // This is done in a background thread

                publishProgress(25);
                publishProgress(50);
                publishProgress(75);

                return getImage(url);
            }

            protected void onProgressUpdate(Integer... args) {
                int value = args[0];
                progressBar.setProgress(value);
            }

            /**
             * Called after the image has been downloaded
             * -> this calls a function on the main thread again
             */
            @Override
            protected void onPostExecute(Drawable image) {
                String fileName = longitude + latitude + date;

                File dir = new File(Environment.getExternalStorageDirectory(), getApplication().getFilesDir() + "/Downloads/" +  currentUser.getId());

                if(!dir.exists()) dir.mkdir();

                BitmapDrawable drawable = (BitmapDrawable) image;
                Bitmap bitmap = drawable.getBitmap();

                File file = new File(dir,  fileName + ".png");

                createImage(file, bitmap);

                progressBar.setVisibility(View.INVISIBLE);
            }
        }

            /**
             * download the Image from the url
             *
             * @param _url
             * @return null
             */
            private Drawable getImage(String _url) {
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

    private class HistoryListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return imageArrayList.size();
        }

        public Object getItem(int id) {
            return imageArrayList.get(id);
        }

        public long getItemId(int id) {
            return id;
        }

        public View getView(int id, View old, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            Image image = imageArrayList.get(id);

            View view = inflater.inflate(R.layout.image_list_layout, parent, false);

            //populate image details into the image list layout
            ((TextView) view.findViewById(R.id.id)).setText(String.valueOf(image.getId()));
            ((TextView) view.findViewById(R.id.longitude)).setText(image.getLongitude());
            ((TextView) view.findViewById(R.id.latitude)).setText(image.getLatitude());
            ((TextView) view.findViewById(R.id.date)).setText(image.getDate());
            (view.findViewById(R.id.image)).setTag(image.getUrl());

            return view;
        }
    }
}
