package com.axxezo.registerdemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
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
    private static final String TABLE_PEOPLE = "PEOPLE";
    private static final String TABLE_REGISTERS = "REGISTERS";

    //table structure
    //login

    //records
    private static final String REGISTER_ID = "id";
    private static final String REGISTER_DATE = "date";
    private static final String REGISTER_PDA = "pda";
    private static final String REGISTER_PERSON = "person";
    private static final String REGISTER_ALLOW = "allow";
    private static final String REGISTER_SYNC = "sync";


    private static final String[] REGISTERS_COLUMNS = {REGISTER_ID, REGISTER_DATE, REGISTER_PERSON, REGISTER_PDA, REGISTER_ALLOW, REGISTER_SYNC};

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "Eulen";

    // SQL statement to create the differents tables

    private String CREATE_REGISTERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_REGISTERS + " ( " +
            REGISTER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            REGISTER_PERSON + " INTEGER, " +
            REGISTER_ALLOW + " INTEGER, " +
            REGISTER_PDA + " TEXT, " +
            REGISTER_DATE + " INTEGER, " +
            REGISTER_SYNC + " INTEGER); ";

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
        db.execSQL(CREATE_REGISTERS_TABLE);
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

    public boolean update_register(long date) {
        //log_app log = new log_app();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        int i = 0;
        try {
            db.beginTransactionNonExclusive();
            values.put(REGISTER_SYNC, 1);
            // 3. updating row
            i = db.update(TABLE_REGISTERS, //table
                    values, // column/value
                    REGISTER_DATE + "=" + date, // where
                    null);
            db.setTransactionSuccessful();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
          //  log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        } finally {
            // 4. close
            db.endTransaction();
        }
        if (i > 0) return true;
        else return false;
    }


    public Cursor select(String select) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(select, null);
            if (cursor != null && cursor.getCount() > 0)
                cursor.moveToFirst();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public void insert(String insert) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransactionNonExclusive();
            db.execSQL(insert);
            db.setTransactionSuccessful();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

    }

    public List get_desynchronized_registers() {

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_REGISTERS, // a. table
                        REGISTERS_COLUMNS, // b. column names
                        REGISTER_SYNC + "=0", // c. selections
                        null, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. get all
        cursor.moveToFirst();
        List<String> records = new ArrayList<>();

        while (cursor.isAfterLast() == false) {
            records.add(
                    cursor.getInt(0) + ";" + //ID
                            cursor.getString(cursor.getColumnIndex(REGISTER_PERSON)) + ";" + //PERSON
                            cursor.getInt(cursor.getColumnIndex(REGISTER_ALLOW)) + ";" + //ALLOW
                            cursor.getString(cursor.getColumnIndex(REGISTER_PDA)) + ";" + //PDA
                            cursor.getLong(cursor.getColumnIndex(REGISTER_DATE)) + ";" + //DATETIME
                            cursor.getInt(cursor.getColumnIndex(REGISTER_SYNC)) + ";" //SYNC
            );
            cursor.moveToNext();
        }

        cursor.close();
        return records;
    }

    public int register_desync_count() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT " + REGISTER_ID + " FROM " +
                    TABLE_REGISTERS + " WHERE " + REGISTER_SYNC + "=0;", null);
            //db.close();
            return cursor.getCount();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}