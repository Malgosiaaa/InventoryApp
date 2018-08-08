package com.example.malgosia.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.malgosia.inventoryapp.data.BookContract.BookEntry;

import static android.text.TextUtils.*;


/**
 * Allows user to enter new book or edit existing one
 */

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the book data loader
     */
    private static final int EXISTING_BOOK_LOADER = 0;

    /**
     * Content URI for the existing book (null if it concerns a new book)
     */
    private Uri mCurrentBookUri;

    /**
     * Edit Text to enter the product name
     */
    private EditText mNameEditText;

    /**
     * Edit Text to enter the product price
     */
    private EditText mProductPriceEditText;

    /**
     * Edit Text to enter, upgrade or downgrade the quantity
     */
    private EditText mQuantityValueEditText;

    /**
     * Button to increment quantity
     */
    private Button mIncrementButton;

    /**
     * Button to decrement quantity
     */
    private Button mDecrementButton;

    /**
     * Edit Text to enter supplier name
     */
    private EditText mSupplierName;

    /**
     * Edit Text to enter supplier phone number
     */
    private EditText mSupplierPhone;

    /**
     * Button to call on supplier phone number
     */
    private ImageButton mCallSupplier;

    /**
     * Boolean flag that keeps track of whether the book has been edited (true) or not (false)
     */
    private boolean mBookHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, if so change the mBookHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if there will be new book created or edited an existing one.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // If the intent DOES NOT contain a book content URI, then we are
        // creating a new one
        if (mCurrentBookUri == null) {
            // This is a new book, so change the app bar to show "Add a new Book"
            setTitle(getString(R.string.editor_activity_title_new_book));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing book, so change app bar to show "Edit Book"
            setTitle(getString(R.string.editor_activity_title_edit_book));

            // Initialize a loader to read the book data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        // Find all relevant views in activity_editor.xml
        mNameEditText = findViewById(R.id.edit_product_name);
        mProductPriceEditText = findViewById(R.id.edit_product_price);
        mQuantityValueEditText = findViewById(R.id.quantity_value);
        mSupplierName = findViewById(R.id.edit_supplier_name);
        mSupplierPhone = findViewById(R.id.edit_supplier_phone_number);
        mIncrementButton = findViewById(R.id.btn_increment);
        mDecrementButton = findViewById(R.id.btn_decrement);
        mCallSupplier = findViewById(R.id.btn_call_supplier);


        // Setup OnTouchListeners on all the input fields, so it will be possible to determine if the user
        // has touched or modified them. This will let us recognize if there are unsaved changes
        // or not, if the user tries to leave the editor without saving changes
        mNameEditText.setOnTouchListener(mTouchListener);
        mProductPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityValueEditText.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierPhone.setOnTouchListener(mTouchListener);

        // Set listener to increment button to allow user upgrade the quantity
        mIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int quantityLocal = 0;
                String quantityString = mQuantityValueEditText.getText().toString().trim();

                // Check if the quantity EditText is empty
                if (!quantityString.isEmpty()) {
                    quantityLocal = Integer.parseInt(quantityString);
                    quantityLocal++;
                }
                mQuantityValueEditText.setText(String.valueOf(quantityLocal));
            }
        });

        // Set listener to decrement button to allow user downgrade the quantity
        mDecrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int quantityLocal = 0;
                String quantityString = mQuantityValueEditText.getText().toString().trim();

                // Check if the quantity EditText is empty
                if (!quantityString.isEmpty()) {
                    quantityLocal = Integer.parseInt(quantityString);
                    if (quantityLocal > 0) {
                        quantityLocal--;
                    } else {
                        Toast.makeText(EditorActivity.this, R.string.toast_quantity_cannot_be_negative, Toast.LENGTH_SHORT).show();
                    }
                }
                mQuantityValueEditText.setText(String.valueOf(quantityLocal));
            }
        });

        // Set listener to call using supplier button
        mCallSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get telephone number from EditText and set intent to make a call
                String supplierNumber = mSupplierPhone.getText().toString().trim();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + supplierNumber));

                // Check if the user's device has an telephone app to receive that intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Get the user input from the editor and save it in the database
     */
    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mProductPriceEditText.getText().toString().trim();
        String quantityString = mQuantityValueEditText.getText().toString().trim();
        String supplierName = mSupplierName.getText().toString().trim();
        String supplierPhone = mSupplierPhone.getText().toString().trim();

        // Check if this is supposed to be a new book
        // and check if all the fields in the editor are blank
        if (mCurrentBookUri == null &&
                isEmpty(nameString) && isEmpty(priceString) && isEmpty(quantityString) &&
                isEmpty(supplierName) && isEmpty(supplierPhone)) {
            // Since no fields were modified, we can return early without creating a new book.
            // There is no need to create ContentValues and no need to do do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values
        ContentValues contentValues = new ContentValues();

        // Check if the product name EditText isn't empty
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, R.string.enter_product_name, Toast.LENGTH_SHORT).show();
            return;
        } else {
            contentValues.put(BookEntry.COLUMN_PRODUCT_NAME, nameString);
        }
        // Check if the product price EditText isn't empty
        if (!priceString.isEmpty()) {
            double price = Double.parseDouble(priceString);
            if (price >= 0) {
                contentValues.put(BookEntry.COLUMN_PRICE, price);
            }
        } else {
            Toast.makeText(this, R.string.enter_product_price, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check if the quantity EditText isn't empty and prevent user to enter negative value
        int quantityValue;
        if (!quantityString.isEmpty()) {
            quantityValue = Integer.parseInt(quantityString);
            if (quantityValue < 0) {
                quantityValue = 0;
            }
            contentValues.put(BookEntry.COLUMN_QUANTITY, quantityValue);
        }

        // Check if the supplier EditText isn't empty
        if (TextUtils.isEmpty(supplierName)) {
            Toast.makeText(this, R.string.enter_supplier_name, Toast.LENGTH_SHORT).show();
            return;
        } else {
            contentValues.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
        }
        // Check if the supplier phone EditText isn't empty
        if (TextUtils.isEmpty(supplierPhone)) {
            Toast.makeText(this, R.string.enter_phone_number, Toast.LENGTH_SHORT).show();
            return;
        } else {
            contentValues.put(BookEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);
        }

        // Determine if this is a new or existing book by checking if mCurrentBookUri is null or not
        if (mCurrentBookUri == null) {
            // This is a NEW book, so insert it into the provider,
            // returning the content URI for the new book.
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, contentValues);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.toast_msg_error), Toast.LENGTH_SHORT).show();
            } else {
                // If insertion was successful display a toast with the row ID.
                Toast.makeText(this, getString(R.string.toast_msg_success), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this book already exist, so update it with content URI: mCurrentBookUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentBookUri will already identify the correct row in the database that
            // will be modified.
            int rowsAffected = getContentResolver().update(mCurrentBookUri, contentValues, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_book_update_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful display a toast.
                Toast.makeText(this, getString(R.string.editor_book_update_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu options in menu_editor.xml
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book so hide the "Delete" option.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save book into the database
                saveProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the book hasn't been change, continue with navigating up to parent activity
                // which is the MainActivity
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if the user didn't saved some changes, setup a dialog to warn the user.
                // Create a click listener to handle confirmation that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button being pressed
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the book table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE};

        // This Loader will execute the ContentProvider's query method on the background thread
        return new CursorLoader(this,   // Parent activity is context
                BookEntry.CONTENT_URI,          // Query the content URI of current book
                projection,                     // columns to include in resulting cursor
                null,                 // no selection clause
                null,             // no selection arguments
                null);               // default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Return here if cursor is null or if there is 1 row in it
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Move to the first row of the cursor and read data from it
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes which are needed
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE);

            // Extract the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            // Update the views visible on the screen with the values from the database
            mNameEditText.setText(name);
            mProductPriceEditText.setText(Double.toString(price));
            mQuantityValueEditText.setText(Integer.toString(quantity));
            mSupplierName.setText(supplierName);
            mSupplierPhone.setText(supplierPhone);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear all the data from the input fields
        mNameEditText.setText("");
        mProductPriceEditText.setText("");
        mQuantityValueEditText.setText("");
        mSupplierName.setText("");
        mSupplierPhone.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if she/he leave the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the continue and cancel buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.editor_discard_warning);
        builder.setPositiveButton(R.string.editor_discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.editor_keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, close the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message and set click listeners
        // for the continue and cancel buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.editor_delete_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, display message that there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast with that info
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
