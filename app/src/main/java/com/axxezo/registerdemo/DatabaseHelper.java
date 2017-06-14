package com.axxezo.registerdemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by axxezo on 14/11/2016.
 */

public class DatabaseHelper extends SQLiteOpenHelper {


    //context
    private Context context;
    private static DatabaseHelper sInstance;
    private SQLiteDatabase db;

    //create a unique instance of DB

    public static synchronized DatabaseHelper getInstance(Context context) {
        //one single instance of DB
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    //Table names
    private static final String TABLE_PEOPLE = "LOGIN";
    private static final String TABLE_RECORDS = "RECORDS";

    //table structure
    //login

    //records
    private static final String RECORD_ID = "id";
    private static final String RECORD_DATETIME = "datetime";
    private static final String RECORD_PERSON_DOC = "person_document";
    private static final String RECORD_PORT_REGISTRY = "register_city";
    private static final String RECORD_PDA = "pda";
    private static final String RECORD_SYNC = "sync";


    private static final String[] RECORDS_COLUMNS = {RECORD_ID, RECORD_DATETIME, RECORD_PERSON_DOC, RECORD_PORT_REGISTRY, RECORD_SYNC};

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "registerDemo";

    // SQL statement to create the differents tables

    private String CREATE_RECORDS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_RECORDS + " ( " +
            RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            RECORD_DATETIME + " TEXT, " +
            RECORD_PERSON_DOC + " INTEGER, " +
            RECORD_PORT_REGISTRY + " TEXT, " +
            RECORD_PDA + " TEXT, " +
            RECORD_SYNC + " INTEGER); ";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public void DatabaseHelper() {
        db.enableWriteAheadLogging();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //first create the tables
        db.execSQL(CREATE_RECORDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if it existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLE);
        //create fresh tables
        this.onCreate(db);
    }

    /**
     * CRUD operations (create "add", read "get", update, delete)
     */

    public String selectFirst(String Query) {
        String firstElement = "";
//        log_app log = new log_app();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            db.beginTransactionNonExclusive();
            cursor = db.rawQuery(Query, null);
            cursor.moveToFirst();
            if (cursor.getCount() == 0)
                return Query = "";
            else
                firstElement = cursor.getString(0);
            db.setTransactionSuccessful();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
            //log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        } finally {
            db.endTransaction();
        }
        if (cursor != null) {
            cursor.close();
        }
        return firstElement;
    }

    public String removeAccent(String str) {
        String texto = Normalizer.normalize(str, Normalizer.Form.NFD);
        texto = texto.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        texto = texto.replaceAll("[|?*<\":>+\\[\\]/'`¨´]", "");
        return texto;
    }

    public void update_record(int id) {
        //log_app log = new log_app();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        int i = 0;
        try {
            db.beginTransactionNonExclusive();
            values.put(RECORD_SYNC, 1);
            // 3. updating row
            i = db.update(TABLE_RECORDS, //table
                    values, // column/value
                    RECORD_ID + "=" + id, // where
                    null);
            db.setTransactionSuccessful();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
          //  log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        } finally {
            // 4. close
            db.endTransaction();
        }
        if (i > 0) Log.d("Local Record updated", String.valueOf(id));
        else Log.e("Error updating record", String.valueOf(id));
    }


    public Cursor select(String select) {
        SQLiteDatabase db = this.getWritableDatabase();
       // log_app log = new log_app();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(select, null);
            if (cursor != null && cursor.getCount() > 0)
                cursor.moveToFirst();
            //cursor.moveToFirst();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
      //      log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        }
        return cursor;
    }

    public void insert(String insert) {
        SQLiteDatabase db = this.getWritableDatabase();
      //  log_app log = new log_app();
        try {
            db.beginTransactionNonExclusive();
            db.execSQL(insert);
            db.setTransactionSuccessful();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
      //      log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        } finally {
            db.endTransaction();
        }

    }

    public ArrayList<String> selectAsList(String qry, int position) {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> list = new ArrayList<String>();
  //      log_app log = new log_app();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(qry, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                list.add(cursor.getString(position));
            }
        } catch (android.database.SQLException e) {
            e.printStackTrace();
  //          log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    public int record_desync_count() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + RECORD_ID + " FROM " + TABLE_RECORDS +
                " WHERE " + RECORD_SYNC + "=0;", null);
        int count = cursor.getCount();
        if (cursor != null)
            cursor.close();
        return count;
    }
}