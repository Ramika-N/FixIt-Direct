package com.ramika.fixitdirect.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Wishlist.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "wishlist";

    // Columns
    private static final String COL_ID = "id";
    private static final String COL_PRODUCT_ID = "product_id";
    private static final String COL_TITLE = "title";
    private static final String COL_PRICE = "price";
    private static final String COL_IMAGE = "image_url";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PRODUCT_ID + " TEXT UNIQUE, " +
                COL_TITLE + " TEXT, " +
                COL_PRICE + " REAL, " +
                COL_IMAGE + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addToWishlist(String productId, String title, double price, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PRODUCT_ID, productId);
        values.put(COL_TITLE, title);
        values.put(COL_PRICE, price);
        values.put(COL_IMAGE, image);

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    public void removeFromWishlist(String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_PRODUCT_ID + "=?", new String[]{productId});
    }

    public boolean isInWishlist(String productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_PRODUCT_ID + "=?", new String[]{productId});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public Cursor getAllWishlistItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
}