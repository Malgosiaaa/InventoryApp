package com.example.malgosia.inventoryapp;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.malgosia.inventoryapp.data.BookContract.BookEntry;
import com.example.malgosia.inventoryapp.data.BooksDbHelper;


/**
 * Allows user to enter new book or edit existing one
 */

public class EditorActivity extends AppCompatActivity {

    /**
     * Edit Text to enter the product name
     */
    private EditText mNameEditText;

    /**
     * Edit Text to enter the product price
     */
    private EditText mProductPriceEditText;

    /**
     * Spinner to pick quantity
     */
    private Spinner mQuantitySpinner;

    /**
     * Edit Text to enter supplier name
     */
    private EditText mSupplierName;

    /**
     * Edit Text to enter supplier phone number
     */
    private EditText mSupplierPhone;

    private int mQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views in activity_editor.xml
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mProductPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mSupplierName = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierPhone = (EditText) findViewById(R.id.edit_supplier_phone_number);
        mQuantitySpinner = (Spinner) findViewById(R.id.spinner_quantity);

        setupQuantitySpinner();
    }

    /**
     * Setup the dropdown spinner which allows user to pick quantity from 1 to 10 of books which he/she enters
     */
    private void setupQuantitySpinner() {
        // Create adapter for the spinner. It will be populated with the array of values to pick
        ArrayAdapter quantitySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.quantity_values, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        quantitySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter onto Spinner
        mQuantitySpinner.setAdapter(quantitySpinnerAdapter);

        // Set the onItemsSelectedListener to the Spinner
        mQuantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String selection = (String) parent.getItemAtPosition(position);
                mQuantity = Integer.parseInt(selection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Get the user input from the editor and save it in the database
     */
    private void insertProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mProductPriceEditText.getText().toString().trim();
        double price = Double.parseDouble(priceString);
        String supplierName = mSupplierName.getText().toString().trim();
        String supplierPhone = mSupplierPhone.getText().toString().trim();

        // Create database helper
        BooksDbHelper mDbHelper = new BooksDbHelper(this);

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values
        ContentValues contentValues = new ContentValues();
        contentValues.put(BookEntry.COLUMN_PRODUCT_NAME, nameString);
        contentValues.put(BookEntry.COLUMN_PRICE, price);
        contentValues.put(BookEntry.COLUMN_QUANTITY, mQuantity);
        contentValues.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
        contentValues.put(BookEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);

        // Insert a new row for book in the database, returning the ID of that new row
        long newRowId = db.insert(BookEntry.TABLE_NAME, null, contentValues);

        // Show a toast message depending on whether or not the insertion was successful
        if (newRowId == -1) {
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.toast_msg_error), Toast.LENGTH_SHORT).show();
        } else {
            // If insertion was successful display a toast with the row ID.
            Toast.makeText(this, getString(R.string.toast_msg_success) + newRowId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu options in menu_editor.xml
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save book into the database
                insertProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing at this stage
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (MainActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
