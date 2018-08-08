package com.example.malgosia.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.malgosia.inventoryapp.data.BookContract.BookEntry;


/**
 * Display list of books which where entered into the bookstore catalog
 */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BOOK_LOADER = 0;

    BookCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the Floating Action Button which leads to EditorActivity
        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with book data
        ListView bookListView = (ListView) findViewById(R.id.list);
        // Find and set empty view on the ListView, so that it only shows when the list has no books
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        // Setup the adapter to create the list item for each row of book data in the Cursor
        // There is no book data until the loader finishes so null is passed for the cursor
        mCursorAdapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(mCursorAdapter);

        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create new Intent to go to Editor Activity
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // Form the content URI that represents the book that was clicked
                // by appending the "id" (passed as input to this method) onto the
                // BookEntry#CONTENT_URI
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentBookUri);

                // Launch the EditorActivity to display the data for the current book
                startActivity(intent);
            }
        });

        // Start the loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);

    }

    private void insertBook() {
        // Create a ContentValues object where column names are the keys,
        // and book Red Riding Hood attributes are the values.

        ContentValues contentValues = new ContentValues();

        contentValues.put(BookEntry.COLUMN_PRODUCT_NAME, "The Lion, the Witch and the Wardrobe");
        contentValues.put(BookEntry.COLUMN_PRICE, 5.99);
        contentValues.put(BookEntry.COLUMN_QUANTITY, 2);
        contentValues.put(BookEntry.COLUMN_SUPPLIER_NAME, "John Smith");
        contentValues.put(BookEntry.COLUMN_SUPPLIER_PHONE, "509 234 809");

        // Insert a new row for "The Lion, the Witch and the Wardrobe" into the provider using the ContentResolver.
        // Use the BookEntry#CONTENT_URI to indicate that we want to insert into the books database table.
        // Receive the new content URI that will allow to access that book data in the future.
        Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, contentValues);
    }

    /**
     * Helper method to delete all books in the database.
     */
    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + getString(R.string.rows_deleted));
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
                return true;

            // Respond to a click on Delete all books
            case R.id.action_delete_all_entries:
                deleteAllBooks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle args) {
        // Define the projection which describes which columns from the database will be used
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE};

        // This Loader will execute the ContentProvider's query method on the background thread
        return new CursorLoader(this,   // Parent activity is context
                BookEntry.CONTENT_URI,          // Provider content URI to query
                projection,                     // columns to include in resulting cursor
                null,                 // no selection clause
                null,             // no selection arguments
                null);               // default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the BookCursorAdapter with this new cursor containing updated book data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data should be deleted
        mCursorAdapter.swapCursor(null);
    }
}
