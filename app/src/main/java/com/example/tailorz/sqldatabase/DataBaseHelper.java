package com.example.tailorz.sqldatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.tailorz.tailorModels.TailorDesignModel;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "favorites_database";
    private static final String TABLE_NAME = "favorites_table";

    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DESIGN_ID = "design_id";
    private static final String COLUMN_DESIGN_NAME = "design_name";
    private static final String COLUMN_DESIGN_IMAGE = "design_image";  // Store image as BLOB
    private static final String COLUMN_TAILOR_NAME = "tailor_name";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the favorites_table with tailor phone and address
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DESIGN_ID + " TEXT, " +
                COLUMN_DESIGN_NAME + " TEXT, " +
                COLUMN_DESIGN_IMAGE + " BLOB, " +
                COLUMN_TAILOR_NAME + " TEXT, " +
                "tailor_phone TEXT, " +    // New column
                "tailor_address TEXT)";    // New column

        Log.d("Database", "Creating table with query: " + createTableQuery);
        db.execSQL(createTableQuery);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN tailor_phone TEXT");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN tailor_address TEXT");
                Log.d("Database", "Database upgraded: tailor_phone & tailor_address added.");
            } catch (Exception e) {
                Log.e("Database", "Error upgrading database: " + e.getMessage());
            }
        }
    }

    public void addFavoriteDesign(TailorDesignModel design) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DESIGN_ID, design.getDesign_id());
        values.put(COLUMN_DESIGN_NAME, design.getDesign_name());
        values.put(COLUMN_DESIGN_IMAGE, design.getDesign_image());  // Store the image as byte[]
        values.put(COLUMN_TAILOR_NAME, design.getTailor_username());
        values.put("tailor_phone", design.getTailor_phone());       // Save phone number
        values.put("tailor_address", design.getTailor_address());   // Save address

        long result = db.insert(TABLE_NAME, null, values);

        if (result != -1) {
            Log.d("Database", "Design added successfully: " + design.getDesign_name());
        } else {
            Log.e("Database", "Failed to insert design: " + design.getDesign_name());
        }
        db.close();
    }



    public List<TailorDesignModel> getAllFavoriteDesigns() {
        List<TailorDesignModel> favoriteDesigns = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        Log.d("Database", "Fetching all designs, cursor count: " + cursor.getCount());

        if (cursor.moveToFirst()) {
            do {
                String designID = cursor.getString(cursor.getColumnIndex(COLUMN_DESIGN_ID));
                String designName = cursor.getString(cursor.getColumnIndex(COLUMN_DESIGN_NAME));
                byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex(COLUMN_DESIGN_IMAGE));
                String tailorName = cursor.getString(cursor.getColumnIndex(COLUMN_TAILOR_NAME));
                String tailorPhone = cursor.getString(cursor.getColumnIndex("tailor_phone"));  // Fetch phone
                String tailorAddress = cursor.getString(cursor.getColumnIndex("tailor_address")); // Fetch address

                // Log fetched design info
                Log.d("Database", "Fetched design: " + designName + ", tailor: " + tailorName);

                // Create a new TailorDesignModel with full details
                TailorDesignModel design = new TailorDesignModel(designID, designName, imageBytes,
                        tailorName, tailorPhone, tailorAddress, true);
                favoriteDesigns.add(design);
            } while (cursor.moveToNext());
        } else {
            Log.d("Database", "No designs found in the database.");
        }

        cursor.close();
        db.close();

        return favoriteDesigns;
    }



    public void removeFavoriteDesign(TailorDesignModel design) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_NAME, COLUMN_DESIGN_ID + " = ?", new String[]{String.valueOf(design.getDesign_id())});

        if (rowsAffected > 0) {
            Log.d("Database", "Design removed successfully: " + design.getDesign_name());
        } else {
            Log.e("Database", "Failed to remove design: " + design.getDesign_name());
        }
        db.close();
    }
    public void removeFromFavorites(String designId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Debugging log to check existing IDs in database
        Cursor cursor = db.rawQuery("SELECT design_id FROM favorites_table", null);
        Log.d("Database", "Existing design IDs in DB:");
        while (cursor.moveToNext()) {
            Log.d("Database", cursor.getString(0));
        }
        cursor.close();
        // Attempt to delete
        int rowsAffected = db.delete("favorites_table", "design_id=?", new String[]{designId});

        if (rowsAffected > 0) {
            Log.d("Database", "Design removed from favorites: " + designId);
        } else {
            Log.e("Database", "Failed to remove design from favorites: " + designId + " (No matching record found)");
        }

        db.close();
    }





}
