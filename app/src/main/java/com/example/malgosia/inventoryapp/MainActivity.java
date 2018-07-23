package com.example.malgosia.inventoryapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.malgosia.inventoryapp.data.BookContract.BookEntry;
import com.example.malgosia.inventoryapp.data.BooksDbHelper;


/**
 * Display list of books which where entered into the bookstore catalog
 */
public class MainActivity extends AppCompatActivity {

    private BooksDbHelper mBooksDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the Floating Action Button which leads to EditorActivity
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        mBooksDbHelper = new BooksDbHelper(this);
    }

    // Show MainActivity with catalog while app is in OnStart
    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    private void displayDatabaseInfo() {
        // Create and/or open a database to read from it
        SQLiteDatabase sqLiteDatabase = mBooksDbHelper.getReadableDatabase();

        // Define the projection which describes which columns from the database will be used
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE};

        // Perform a query on the Books Table
        Cursor cursor = sqLiteDatabase.query(
                BookEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        TextView catalogView = (TextView) findViewById(R.id.catalog_view);

        try {
            // Create the header in TextView which shows
            // information about the number of rows populated with books data
            // Use the while loop to iterate through every row of the cursor
            // and display information in the given order
            catalogView.setText(getString(R.string.table_contains_info) + cursor.getCount() + " books.\n\n");
            catalogView.append(BookEntry._ID + " - " +
                    BookEntry.COLUMN_PRODUCT_NAME + " - " +
                    BookEntry.COLUMN_PRICE + " - " +
                    BookEntry.COLUMN_QUANTITY + " - " +
                    BookEntry.COLUMN_SUPPLIER_NAME + " - " +
                    BookEntry.COLUMN_SUPPLIER_PHONE + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
            int productColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierNameIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int or Decimal value of the word
                // at the current row the cursor is
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(productColumnIndex);
                double currentPrice = cursor.getDouble(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityIndex);
                String currentSupplierName = cursor.getString(supplierNameIndex);
                String currentSupplierPhone = cursor.getString(supplierPhoneIndex);

                // Display values of each column from current row in the cursor in the catalogView TextView
                catalogView.append(("\n" + currentID + " - " +
                        currentName + " - " +
                        currentPrice + " - " +
                        currentQuantity + " - " +
                        currentSupplierName + " - " +
                        currentSupplierPhone));
            }
        } finally {
            // Close the cursor to release its resources
            cursor.close();
        }
    }

    private void insertBook() {
        // Get the data repository in write mode
        SQLiteDatabase db = mBooksDbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(BookEntry.COLUMN_PRODUCT_NAME, "The Lion, the Witch and the Wardrobe");
        contentValues.put(BookEntry.COLUMN_PRICE, 5.99);
        contentValues.put(BookEntry.COLUMN_QUANTITY, 2);
        contentValues.put(BookEntry.COLUMN_SUPPLIER_NAME, "John Smith");
        contentValues.put(BookEntry.COLUMN_SUPPLIER_PHONE, "509 234 809");

        long newRowID = db.insert(BookEntry.TABLE_NAME, null, contentValues);

        Log.v("MainActivity", "New row ID " + newRowID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu menu_catalog.xml
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Clickable menu in the app bar - overflow menu
        switch (item.getItemId()) {
            // Respond to a click on Insert Book details
            case R.id.action_insert_book_data:
                insertBook();
                displayDatabaseInfo();
                return true;

            // Respond to a click on Delete all books
            case R.id.action_delete_all_entries:
                // Do nothing at this stage
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
