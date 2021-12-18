package com.example.finalproject.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.beans.User;
import com.example.finalproject.dao.DbOpener;

public class MainActivity extends AppCompatActivity {

    //Declaring objects
    Intent imageGrabberIntent, profileIntent;

    Button login, signup;

    EditText emailEdit, passwordEdit;

    User currentUser;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    SQLiteDatabase db;

    static final int REQUEST_MAIN_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences("LoginInfo", MODE_PRIVATE);
        editor = sp.edit();

        String e = (sp.getString("Email address", ""));
        String p = (sp.getString("Password", ""));

        //Initializing fields for email address and password.
        emailEdit = findViewById(R.id.loginEmailAddressField);
        passwordEdit = findViewById(R.id.loginPasswordField);

        emailEdit.setText(e);
        passwordEdit.setText(p);

        //Initializing intents to link to other activities.
        profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        imageGrabberIntent = new Intent(MainActivity.this, ImageGrabber.class);

        //Initializing buttons and setting on click listeners.
        login = findViewById(R.id.loginButton);
        login.setOnClickListener(click -> {
            String email = emailEdit.getText().toString();
            String pass = passwordEdit.getText().toString();

            if (verifyUserInDataBase(email, pass)) {
                currentUser = createCurrentUser(email,pass);

                imageGrabberIntent.putExtra("currentUser", currentUser);
                startActivity(imageGrabberIntent);
            } else {
            Toast.makeText(MainActivity.this, "Email address and/or password is/are wrong or do not exist! Please try again or signup to access the app.",
                    Toast.LENGTH_LONG).show();
        }
        });

        signup = findViewById(R.id.signupButton);
        signup.setOnClickListener(click -> startActivity(profileIntent));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String e = emailEdit.getText().toString();
        String p = passwordEdit.getText().toString();

        if(!e.isEmpty() && !p.isEmpty()){
            editor.putString("Email address", e);
            editor.putString("Password", p);
            editor.commit();
        }
    }

    private boolean verifyUserInDataBase(String emailAddress, String password)
    {
        boolean userExists = false;
        //get a database connection:
        DbOpener dbOpener = new DbOpener(this);
        db = dbOpener.getWritableDatabase();

        //query all the results from the database:
        Cursor results = db.rawQuery("SELECT * FROM USER where EmailAddress like '"+emailAddress+"' AND Password like '"+password+"'", null);

        //The results object has rows of results that match the query.
        //find the column indices:
        int emailColumnIndex = results.getColumnIndex(DbOpener.COL_EMAIL_ADDRESS);
        int passwordColIndex = results.getColumnIndex(DbOpener.COL_PASSWORD);

        //Looping through results to verify match
        while(results.moveToNext()){
            String email = results.getString(emailColumnIndex);
            String pass = results.getString(passwordColIndex);

            if(emailAddress.equalsIgnoreCase(email) && password.equals(pass)){
                userExists = true;
            }
        }
        results.close();
      return userExists;
    }

    private User createCurrentUser(String emailAddress, String password)
    {
        User currentUser = null;

            //get a database connection:
            DbOpener dbOpener = new DbOpener(this);
            db = dbOpener.getWritableDatabase();

            //query all the results from the database:
            Cursor results = db.rawQuery("SELECT ID, FirstName, LastName, EmailAddress, Password, ProfilePicturePath FROM USER where EmailAddress like '"+emailAddress+"' AND Password like '"+password+"'", null);

            //The results object has rows of results that match the query.
            //find the column indices:
            int idColumnIndex = results.getColumnIndex(DbOpener.COL_USER_ID);
            int firstNameColIndex = results.getColumnIndex(DbOpener.COL_FIRSTNAME);
            int lastNameColIndex = results.getColumnIndex(DbOpener.COL_LASTNAME);
            int emailColumnIndex = results.getColumnIndex(DbOpener.COL_EMAIL_ADDRESS);
            int passwordColIndex = results.getColumnIndex(DbOpener.COL_PASSWORD);
            int profilePicColIndex = results.getColumnIndex(DbOpener.COL_PROFILE_PICTURE_PATH);

            //Looping through results to verify match
            while(results.moveToNext()){
                int id = results.getInt(idColumnIndex);
                String fName = results.getString(firstNameColIndex);
                String lName = results.getString(lastNameColIndex);
                String email = results.getString(emailColumnIndex);
                String pass = results.getString(passwordColIndex);
                String profilePicPath = results.getString(profilePicColIndex);

                //Creating user object to pass to other activities.
                currentUser = new User(id, fName, lName, email, pass, profilePicPath);
            }
            results.close();
        return currentUser;
    }
}