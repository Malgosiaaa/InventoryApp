package com.example.malgosia.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.malgosia.inventoryapp.data.BookContract.BookEntry;

public class BooksDbHelper extends SQLiteOpenHelper {

    /**
     * Name of the database file
     */
    private static String DATABASE_NAME = "bookstore.db";

    /**
     * Number of the database version. After changing the schema of the database number must be incremented.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs the new instance od BooksDbHelper
     *
     * @param context is the context of the app
     */
    public BooksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This method is called when database is created for the first time
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create the String which contains the SQL statement needed to create the Books table
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
                + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + BookEntry.COLUMN_PRICE + " DOUBLE NOT NULL, "
                + BookEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, "
                + BookEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + BookEntry.COLUMN_SUPPLIER_PHONE + " TEXT NOT NULL );";
        // Execute above SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_BOOKS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
