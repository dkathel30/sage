package com.example.sage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sage_app.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "chat";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PROMPT = "prompt";
    public static final String COLUMN_RESPONSE = "response";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PROMPT + " TEXT, "
                + COLUMN_RESPONSE + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertChat(String prompt, String response) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROMPT, prompt);
        values.put(COLUMN_RESPONSE, response);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<ChatItem> getAllChats() {
        List<ChatItem> chatList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                ChatItem chat = new ChatItem();
                chat.setPrompt(cursor.getString(cursor.getColumnIndex(COLUMN_PROMPT)));
                chat.setResponse(cursor.getString(cursor.getColumnIndex(COLUMN_RESPONSE)));
                chatList.add(chat);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return chatList;
    }
}
