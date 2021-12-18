package com.example.finalproject.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.beans.User;
import com.example.finalproject.dao.DbOpener;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    EditText fNameEdit, lNameEdit, emailAddressEdit, passwordEdit;
    ImageButton imageButton;
    Button saveButton, cancelButton;

    Intent currentIntent, loginIntent, imageGrabberIntent;

    SQLiteDatabase db;
    User currentUser;
    int currentUserId;

    String currentPhotoPath;

    static final int REQUEST_IMAGE_GRABBER = 2;
    static final int REQUEST_IMAGE_CAPTURE = 5;


    public static final String ACTIVITY_NAME = "PROFILE_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //This gets the toolbar from the layout:
        Toolbar tBar = findViewById(R.id.toolbar);

        //This loads the toolbar, which calls onCreateOptionsMenu below:
        setSupportActionBar(tBar);

        //Initializing fields and buttons
        fNameEdit = findViewById(R.id.fNameField);
        lNameEdit = findViewById(R.id.lNameField);
        emailAddressEdit = findViewById(R.id.emailAddressField);
        passwordEdit = findViewById(R.id.passwordField);
        imageButton = findViewById(R.id.profilePictureButton);
        saveButton = findViewById(R.id.profileSaveButton);
        cancelButton = findViewById(R.id.profileCancelButton);

        //Initializing intents
        currentIntent = getIntent();
        loginIntent = new Intent(ProfileActivity.this, MainActivity.class);
        imageGrabberIntent = new Intent(ProfileActivity.this, ImageGrabber.class);

        //Getting current user information
        if(currentIntent.getParcelableExtra("currentUser") != null){
            currentUser = currentIntent.getParcelableExtra("currentUser");
            currentUserId = currentUser.getId();
            fNameEdit.setText(currentUser.getfName());
            lNameEdit.setText(currentUser.getlName());
            emailAddressEdit.setText(currentUser.getEmailAddress());
            passwordEdit.setText(currentUser.getPassword());
        }

        imageButton.setOnClickListener(click -> {
            try {
                dispatchTakePictureIntent();
                galleryAddPic();
               // galleryAddPic();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        saveButton.setOnClickListener(click -> {
                createUpdateUserInDataBase(currentUserId);
                //Exit profile activity
                finish();
        });

        cancelButton.setOnClickListener(click -> finish());
    }

    private void dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (null != takePictureIntent.resolveActivity(getPackageManager())) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.getStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.finalproject.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            imageButton.setImageBitmap(imageBitmap);
        }
    }

    private void createUpdateUserInDataBase(long id) {
        //get a database connection:
        DbOpener dbOpener = new DbOpener(this);
        db = dbOpener.getWritableDatabase();

        //query all the results from the database:
        Cursor results = db.rawQuery("SELECT * FROM USER where ID = '" + id + "'", null);

        //get new user information that were typed/updated
        String fName = fNameEdit.getText().toString();
        String lName = lNameEdit.getText().toString();
        String email = emailAddressEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        String profilePhotoPath = currentPhotoPath;

        //add to the database and get the new ID
        ContentValues cv = new ContentValues();

        //Provide a value for every database column defined in UserDbOpener.java:
        //put strings in the respective columns:
        cv.put(DbOpener.COL_FIRSTNAME, fName);
        cv.put(DbOpener.COL_LASTNAME, lName);
        cv.put(DbOpener.COL_EMAIL_ADDRESS, email);
        cv.put(DbOpener.COL_PASSWORD, password);
        cv.put(DbOpener.COL_PROFILE_PICTURE_PATH, profilePhotoPath);

        //Checking if user ID exists
        if (results.getCount() == 0) {
            //Insert in the database:
            db.insert(DbOpener.USER_TABLE_NAME, null, cv);
            //Confirmation message
            Toast.makeText(this, "Your profile has successfully been created! Please login to use the app.", Toast.LENGTH_LONG).show();
        } else if(results.getCount() > 0) {
            //update record in database:
            db.update(DbOpener.USER_TABLE_NAME, cv,"ID = ?",new String[]{String.valueOf(id)});
            //Confirmation message
            Toast.makeText(this, "Your profile has successfully been updated!", Toast.LENGTH_LONG).show();
            }
        results.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(ACTIVITY_NAME, "In function: onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(ACTIVITY_NAME, "In function: onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(ACTIVITY_NAME, "In function: onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(ACTIVITY_NAME, "In function: onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(ACTIVITY_NAME, "In function: onDestroy()");
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
                startActivity(intent);
                break;
            case R.id.profile_item:
                break;
            case R.id.weather_item:
                intent = new Intent(this, WeatherForecast.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
                break;
            case R.id.favourite_item:
                intent = new Intent(this, FavouriteActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
                break;
        }

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}