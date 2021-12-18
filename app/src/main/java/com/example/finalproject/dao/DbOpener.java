package com.example.finalproject.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpener extends SQLiteOpenHelper {

    protected final static String DATABASE_NAME = "DB";
    protected final static int VERSION_NUM = 1;
    //User table information
    public final static String USER_TABLE_NAME = "USER";
    public final static String COL_USER_ID = "ID";
    public final static String COL_FIRSTNAME = "FirstName";
    public final static String COL_LASTNAME = "LastName";
    public final static String COL_EMAIL_ADDRESS = "EmailAddress";
    public final static String COL_PASSWORD = "Password";
    public final static String COL_PROFILE_PICTURE_PATH = "ProfilePicturePath";

    //User image table information
    public final static String IMAGE_TABLE_NAME = "IMAGE";
    public final static String COL_IMAGE_ID = "ID";
    public final static String COL_IMAGE_USER_ID = "UserID";
    public final static String COL_IMAGE_LONGITUDE = "Longitude";
    public final static String COL_IMAGE_LATITUDE = "Latitude";
    public final static String COL_IMAGE_DATE = "Date";
    public final static String COL_IMAGE_URL = "URL";
    public final static String COL_IMAGE_IN_FAVOURITE = "InFavourite";
    public final static String COL_IMAGE_IS_DOWNLOADED = "IsDownloaded";

    public DbOpener(Context ctx)
    {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }

    //This function gets called if no database file exists.
    @Override
    public void onCreate(SQLiteDatabase db)
    {


        //Creating User Table
        db.execSQL("CREATE TABLE " + USER_TABLE_NAME + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + COL_FIRSTNAME + " text NOT NULL,"
                + COL_LASTNAME + " text NOT NULL,"
                + COL_EMAIL_ADDRESS + " text NOT NULL UNIQUE,"
                + COL_PASSWORD + " text NOT NULL,"
                + COL_PROFILE_PICTURE_PATH + " text);");

        //Creating User history Table
        db.execSQL("CREATE TABLE " + IMAGE_TABLE_NAME + " ("
                + COL_IMAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + COL_IMAGE_USER_ID + " INTEGER NOT NULL,"
                + COL_IMAGE_LONGITUDE + " text NOT NULL,"
                + COL_IMAGE_LATITUDE + " text NOT NULL,"
                + COL_IMAGE_DATE + " text NOT NULL,"
                + COL_IMAGE_URL + " text NOT NULL,"
                + COL_IMAGE_IN_FAVOURITE + " INTEGER,"
                + COL_IMAGE_IS_DOWNLOADED + " INTEGER,"
                + " FOREIGN KEY (" + COL_IMAGE_USER_ID + ") REFERENCES " +USER_TABLE_NAME+ "(" +COL_USER_ID+ "));");
    }


    //this function gets called if the database version on your device is lower than VERSION_NUM
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {   //Drop the old tables:
        db.execSQL( "DROP TABLE IF EXISTS " + USER_TABLE_NAME + ";");
        db.execSQL( "DROP TABLE IF EXISTS " + IMAGE_TABLE_NAME + ";");
        //Create the new table:
        onCreate(db);
    }

    //this function gets called if the database version on your device is higher than VERSION_NUM
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {   //Drop the old table:
        db.execSQL( "DROP TABLE IF EXISTS " + USER_TABLE_NAME + ";");
        db.execSQL( "DROP TABLE IF EXISTS " + IMAGE_TABLE_NAME + ";");
        //Create the new table:
        onCreate(db);
    }
}