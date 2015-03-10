package org.psfcerd.blog.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pras on 7/3/15.
 */
public class PostEntryDBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "psfcerd";
    private static final String TABLE_NAME = "posts";
    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_TITLE = "title";
    private static final String COLUMN_NAME_URL = "url";
    private static final String COLUMN_NAME_DESCRIPTION = "description";
    private static final String COLUMN_NAME_PUBLISHED_DATE = "published_date";
    private static final String COLUMN_NAME_HTML_FILE_LOCATION = "html_file_location";

    public PostEntryDBHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME_TITLE + " TEXT NOT NULL," +
                COLUMN_NAME_URL + " TEXT NOT NULL UNIQUE," +
                COLUMN_NAME_DESCRIPTION + " TEXT," +
                COLUMN_NAME_PUBLISHED_DATE + " TEXT," +
                COLUMN_NAME_HTML_FILE_LOCATION + " TEXT" + " )";
        Log.d("DATABASE: ", "Creating Table");
        db.execSQL(CREATE_POSTS_TABLE);
    }

    // Updating Tables
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // Delete the old table if exists
        Log.d("DATABASE:", "Dropping Table");
        String DELETE_POSTS_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(DELETE_POSTS_TABLE);

        // re-create the database
        Log.d("DATABASE: ", "Re-Creating Table");
        onCreate(db);
    }

    // Downgrading tables
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // To Add a new Single PostEntry
    public void addPostEntry(PostEntry postEntry){
        SQLiteDatabase db = this.getWritableDatabase();

        // packing values as key:value structure
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_TITLE, postEntry.getTitle());
        values.put(COLUMN_NAME_URL, postEntry.getUrl());
        values.put(COLUMN_NAME_DESCRIPTION, postEntry.getDescription());
        values.put(COLUMN_NAME_PUBLISHED_DATE, postEntry.getPublishedDate());

        // Inserting a row in table with above detail
        try{
            Log.d("DATABASE: ", "Inserting Row");
            db.insertOrThrow(TABLE_NAME, null, values);
            db.close();
        } catch (SQLiteConstraintException ex) {
            Log.d("Exception: ", ex.getMessage());
        }
    }

    // To retrieve a Single PostEntry
    public PostEntry getPostEntry(String title){
        SQLiteDatabase db = this.getReadableDatabase();

        // Query construction
        String SEARCH_BY_TITLE = "SELECT title, url FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_TITLE + "= \"" + title + "\"";
        Cursor cursor = db.rawQuery(SEARCH_BY_TITLE, null);
        if(cursor != null)
            cursor.moveToFirst();

        PostEntry post_entry = new PostEntry(cursor.getString(0), cursor.getString(1), null, null, null);
        return post_entry;
    }

    // To Delete a Single PostEntry
    public void deletePostEntry(PostEntry postEntry){
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete a row by it's URL which can be uniquely identified.
        db.delete(TABLE_NAME, COLUMN_NAME_URL + " = ?", new String[] {String.valueOf(postEntry.getUrl())});
        db.close();
    }

    // Get all the post entries in reverse order by published date
    public List<PostEntry> getAllEntries(){
        SQLiteDatabase db = this.getReadableDatabase();

        List<PostEntry> entriesList = new ArrayList<PostEntry>();
        String SELECT_QUERY = "SELECT * FROM " + TABLE_NAME;

        Cursor cursor = db.rawQuery(SELECT_QUERY, null);

        if(cursor.moveToFirst()) {
            do {
                PostEntry entry = new PostEntry();
                entry.setTitle(cursor.getString(1));
                entry.setUrl(cursor.getString(2));
                entry.setDescription(cursor.getString(3));
                entry.setPublishedDate(cursor.getString(4));
                entry.setHtmlFileLocation(null);

                entriesList.add(entry);
            } while(cursor.moveToNext());
        }

        return entriesList;
    }

    // Get total number of postEntries
    public int getTotalCount(){
        SQLiteDatabase db = this.getReadableDatabase();

        String countQuery = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

}
