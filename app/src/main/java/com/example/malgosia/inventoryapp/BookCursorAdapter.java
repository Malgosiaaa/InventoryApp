package com.example.malgosia.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.malgosia.inventoryapp.data.BookContract.BookEntry;

/**
 * Adapter is created to populate items of our LisView with data from cursor
 */
public class BookCursorAdapter extends CursorAdapter {


    /**
     * Constructs a new BookCursorAdapter
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view.
     *
     * @param context is the app context
     * @param cursor  is the cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  is the parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Bind the book data (in the current row pointed to by cursor) to the given
     * list item layout
     *
     * @param view    is the existing view, returned earlier by newView() method
     * @param context is the app context
     * @param cursor  is the cursor from which adapter gets the data. The cursor is already moved to the
     *                correct row.
     */

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find views that will be modified in the list item layout
        // Find the parent layout to sent intent on it
        // it will be used to update the quantity in editor after modification with usage of sale btn
        LinearLayout parentLayout = view.findViewById(R.id.parent_linear_layout);
        TextView nameTextView = view.findViewById(R.id.product_name);
        TextView priceTextView = view.findViewById(R.id.product_price);
        final TextView quantityTextView = view.findViewById(R.id.product_quantity);
        Button saleButton = view.findViewById(R.id.btn_sale);

        // Find the columns of books attributes that match to views
        int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);

        // Set column indexes to variables
        final int itemId = cursor.getInt(idColumnIndex);
        String name = cursor.getString(nameColumnIndex);
        double price = cursor.getDouble(priceColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);

        // Set the quantity with singular or plural item description
        // Use the spaces to avoid app crash according to NumberFormatException
        if (quantity <= 1) {
            quantityTextView.setText(quantity + context.getResources().getString(R.string.item));
        } else {
            quantityTextView.setText(quantity + context.getResources().getString(R.string.items));
        }

        // Update the name and priceTextViews with the attributes for the current book
        nameTextView.setText(name);
        priceTextView.setText(String.valueOf(price));

        // Use intent to update quantity value in EditorActivity after pressing the sale button
        parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditorActivity.class);
                Uri currentStockUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, itemId);
                intent.setData(currentStockUri);
                context.startActivity(intent);
            }
        });

        // Setup the sale button which will decrease quantity by 1
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String quantityView = quantityTextView.getText().toString();
                // split text in quantityTextView to avoid app crash
                // according to NumberFormatException connected with int and String typed in one line
                String[] splittedQuantity = quantityView.split(" ");
                int quantity = Integer.parseInt(splittedQuantity[0]);

                if (quantity == 0) {
                    Toast.makeText(context, R.string.no_product_in_stock,
                            Toast.LENGTH_SHORT).show();
                } else if (quantity > 0) {
                    quantity -= 1;
                    String quantityString = Integer.toString(quantity);

                    // Create new content values where key is COLUMN_QUANTITY
                    // and attribute is the quantityString
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(BookEntry.COLUMN_QUANTITY, quantityString);

                    Uri currentStockUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, itemId);

                    int rowsAffected = context.getContentResolver().update(currentStockUri,
                            contentValues,
                            null,
                            null);

                    // Check how many rows were affected to indicate singular
                    // or plural quantity description
                    if (rowsAffected != 0) {
                        if (quantity <= 0) {
                            quantityTextView.setText
                                    (quantity + context.getResources().getString(R.string.item));
                        } else {
                            quantityTextView.setText
                                    (quantity + context.getResources().getString(R.string.items));
                        }
                    } else {
                        Toast.makeText(context, R.string.product_update_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
}

