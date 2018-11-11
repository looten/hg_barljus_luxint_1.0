package com.example.hg_bar;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Looten on 2015-05-19.
 */
public class DBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DBHandler";

    private static DBHandler sInstance;

    private static final String DATABASE_NAME = "drinks.db";

    public static final String TABLE_DRINKS = "drinks";
    public static final String TABLE_BOTTLES = "bottles";
    public static int status;
    public static final int SEND_DONE = 0x0001;
    //private static int color_code = 256;
    public static final int bottle_code_min = 256;
    //private static int color_change = 1;
    //private static final int color_cap = 1536;
    //private static final int color_min = 256;
    //private static ArrayList<String> drink_list;
    //public static boolean send_status = true;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_BOTTLES = "bottles";
    //public static final String COLUMN_AMOUNTS = "amounts";
    public static final String COLUMN_COUNT = "count";
    public static final String COLUMN_WEEK = "week";
    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_INFO = "info";
    public static final String COLUMN_TYPE = "type";

    public static synchronized DBHandler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DBHandler(context.getApplicationContext());
        }
        return sInstance;
    }

    private DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_DRINKS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT," +
                COLUMN_BOTTLES + " TEXT," +
                //COLUMN_AMOUNTS + " TEXT," +
                COLUMN_COUNT + " INTEGER," +
                COLUMN_WEEK + " INTEGER," +
                COLUMN_INFO + " TEXT," +
                COLUMN_TYPE + " TEXT " + " )";

        String query_ = "CREATE TABLE " + TABLE_BOTTLES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BOTTLES + " TEXT, " +
                COLUMN_CODE + " INTEGER " + " )";

        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.execSQL(query_);
        //sqLiteDatabase.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP_TABLE_IF_EXISTS" + TABLE_DRINKS);
        db.execSQL("DROP_TABLE_IF_EXISTS" + TABLE_BOTTLES);
        onCreate(db);
    }

    public void clearDatabase() {
        //drink_list.clear();
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DRINKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOTTLES);
        onCreate(db);
    }


    public Drink getRow(String clicked_drink) {
        SQLiteDatabase db = getWritableDatabase();
        // String[] colummns = {COLUMN_ID, COLUMN_NAME, COLUMN_BOTTLES, COLUMN_AMOUNTS, COLUMN_COUNT, COLUMN_INFO, COLUMN_TYPE};
        String[] colummns = {COLUMN_ID, COLUMN_NAME, COLUMN_BOTTLES, COLUMN_COUNT, COLUMN_INFO, COLUMN_TYPE};
        Cursor c = db.query(TABLE_DRINKS, colummns, null, null, null, null, null);
        String name = "", bottles = "", amounts = "", info = "";
        String type = "";

        int id = 0;
        int count;
        int bottle;
        int index1 = c.getColumnIndex(COLUMN_ID);
        int index2 = c.getColumnIndex(COLUMN_NAME);
        int index3 = c.getColumnIndex(COLUMN_BOTTLES);

        int index5 = c.getColumnIndex(COLUMN_COUNT);
        int index6 = c.getColumnIndex(COLUMN_INFO);
        int index7 = c.getColumnIndex(COLUMN_TYPE);

        Drink D = new Drink();
        Map<String, String> out = new HashMap<String, String>();
        ArrayList<Integer> bottles_send = new ArrayList<>();
        ContentValues cv = new ContentValues();
        JSONObject json = new JSONObject();

        while (c.moveToNext()) {
            if (c.getString(index2).equals(clicked_drink)) {

                id = c.getInt(index1);
                name = c.getString(index2);
                bottles = c.getString(index3);
                type = c.getString(index7);

                try {
                    Log.d(TAG, "bottles " + bottles);

                    json = new JSONObject(bottles);

                    //Functions.parse(json,out);

                    JSONObject json_bottles = json.getJSONObject("bottles");

                    Iterator x = json_bottles.keys();
                    JSONArray jsonArray = new JSONArray();

                    while (x.hasNext()){
                        String key = (String) x.next();
                        Log.d(TAG, key + " <-> " + json_bottles.get(key));

                        bottle = findBottle(key);

                        if (bottle >= bottle_code_min)
                            bottles_send.add(bottle);

                        //jsonArray.put(json_bottles.get(key));
                    }

                    /*Iterator it = out.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        Log.d(TAG, " " + pair.getKey());

                        bottle = findBottle(pair.getKey().toString());

                        if (bottle >= bottle_code_min)
                            bottles_send.add(bottle);
                        it.remove(); // avoids a ConcurrentModificationException
                    }
                    */

                    //Functions.printMap(out);


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                bottles_send.add(SEND_DONE);

                Log.d(TAG, "Koder:");
                for (int s1 : bottles_send)
                    Log.d(TAG, " " + s1);

                //amounts = c.getString(index4);
                count = c.getInt(index5) + 1;
                info = c.getString(index6);

                cv.put(COLUMN_NAME, name);
                cv.put(COLUMN_BOTTLES, bottles);
                //cv.put(COLUMN_AMOUNTS, amounts);
                cv.put(COLUMN_TYPE, type);
                cv.put(COLUMN_COUNT, count);

                if (!db.isOpen())
                    db = getWritableDatabase();
                db.update(TABLE_DRINKS, cv, "_id" + "=" + id, null);

                D = new Drink(name, json, count, info, type, bottles_send);
                break;
            }
        }
        c.close();
        db.close();
        return D;
    }

    public ArrayList<String> getTop() {
        SQLiteDatabase db = getReadableDatabase();
        String[] colummns = {COLUMN_ID, COLUMN_NAME, COLUMN_COUNT};
        String empty = "Listan är tom";
        Cursor c = db.query(TABLE_DRINKS, colummns, null, null, null, null, COLUMN_COUNT + " DESC");
        ArrayList<String> drinks = new ArrayList<>();
        int index2 = c.getColumnIndex(COLUMN_NAME);
        int index3 = c.getColumnIndex(COLUMN_COUNT);
        int nr = 1;
        while (c.moveToNext() && nr < 11 && c.getInt(index3) > 0) {
            drinks.add(c.getString(index2));
            nr++;
        }
        c.close();
        db.close();
        return drinks;
    }

    public int findBottle(String bottle) {
        SQLiteDatabase db = getReadableDatabase();

        String[] colummns = {COLUMN_ID, COLUMN_BOTTLES, COLUMN_CODE};

        int code = 0;
        Cursor c = db.query(TABLE_BOTTLES, colummns, null, null, null, null, null);

        int index2 = c.getColumnIndex(COLUMN_BOTTLES);
        int index3 = c.getColumnIndex(COLUMN_CODE);
        bottle = bottle.replaceAll("\\s", "");
        String tmp;
        bottle.charAt(0);
        while (c.moveToNext()) {
            tmp = c.getString(index2).replaceAll("\\s", "");

            if (tmp.equals(bottle)) {
                code = c.getInt(index3);
                break;
            }
        }
        c.close();
        db.close();
        return code;

    }

    public String getType(String drink) {
        SQLiteDatabase db = getReadableDatabase();
        String[] colummns = {COLUMN_ID, COLUMN_NAME, COLUMN_TYPE};
        Cursor c = db.query(TABLE_DRINKS, colummns, null, null, null, null, null);
        int index2 = c.getColumnIndex(COLUMN_NAME);
        int index3 = c.getColumnIndex(COLUMN_TYPE);
        String type;
        while (c.moveToNext()) {
            if (c.getString(index2).equals(drink)) {
                type = c.getString(index3);

                db.close();
                c.close();
                return type;
            }
        }
        db.close();
        c.close();
        return "null";

    }

    public String getRandom(String type) {
        SQLiteDatabase db = getReadableDatabase();
        String[] colummns = {COLUMN_ID, COLUMN_NAME, COLUMN_TYPE};
        Cursor c = db.query(TABLE_DRINKS, colummns, null, null, null, null, null);
        ArrayList<String> drinks = new ArrayList<>();
        int index2 = c.getColumnIndex(COLUMN_NAME);
        int index3 = c.getColumnIndex(COLUMN_TYPE);
        int nr;

        if (!type.equals("R")) {
            while (c.moveToNext()) {
                if (c.getString(index3).equals(type)) {
                    drinks.add(c.getString(index2));
                }
            }
            db.close();
            c.close();

            return randomDrink(drinks);

        } else {
            while (c.moveToNext()) {
                drinks.add(c.getString(index2));
            }
            db.close();
            c.close();

            return randomDrink(drinks);
        }
    }

    private String randomDrink(ArrayList<String> drinks) {
        String drink = "";
        int nr;
        Random rand = new Random();
        if (drinks.size() > 0) {
            nr = drinks.size();
            int randomNum = rand.nextInt(nr);
            drink = drinks.get(randomNum);
        } else {
            drink = "";
        }

        return drink;
    }

    public void resetTopTen() {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        int i = 0;
        cv.put(COLUMN_COUNT, i);
        db.update(TABLE_DRINKS, cv, null, null);
        db.close();

    }

    //For att fa flaskor SAMT drinkar till listor (flaskor i add drink)
    public ArrayList<String> databaseToString() {
        //  if (drink_list == null || drink_list.isEmpty()) {
        ArrayList<String> drinks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String[] colummns = {COLUMN_ID, COLUMN_NAME};
        // if (drink_list == null) {
        Cursor c = db.query(TABLE_DRINKS, colummns, null, null, null, null, COLUMN_NAME + " ASC");

        int index = c.getColumnIndex(COLUMN_NAME);
        while (c.moveToNext()) {
            drinks.add(c.getString(index));
        }
        db.close();
        c.close();
        //  drink_list = drinks;
        return drinks;
        //} else {
        //   return drink_list;
        //}
    }

    public ArrayList<String> bottlesToString() {
        ArrayList<String> drinks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String[] colummns = {COLUMN_ID, COLUMN_BOTTLES};
        Cursor c = db.query(TABLE_BOTTLES, colummns, null, null, null, null, COLUMN_BOTTLES + " ASC");
        int index = c.getColumnIndex(COLUMN_BOTTLES);
        while (c.moveToNext()) {
            drinks.add(c.getString(index));
        }
        db.close();
        c.close();
        return drinks;
    }

    //snabbval
    public int setLongClickQuickChoice(String drink) {
        SQLiteDatabase db = getWritableDatabase();
        //String[] colummns = {COLUMN_ID, COLUMN_NAME, COLUMN_BOTTLES, COLUMN_AMOUNTS, COLUMN_COUNT, COLUMN_WEEK,};
        String[] colummns = {COLUMN_ID, COLUMN_NAME, COLUMN_BOTTLES, COLUMN_COUNT, COLUMN_WEEK,};
        String name;
        Cursor c = db.query(TABLE_DRINKS, colummns, null, null, null, null, null);
        int index1 = c.getColumnIndex(COLUMN_ID);
        int index2 = c.getColumnIndex(COLUMN_NAME);
        int index3 = c.getColumnIndex(COLUMN_BOTTLES);
        //int index4 = c.getColumnIndex(COLUMN_AMOUNTS);
        int index5 = c.getColumnIndex(COLUMN_COUNT);
        int index6 = c.getColumnIndex(COLUMN_WEEK);
        String bottles;
        int id;
        int count;
        ContentValues cv = new ContentValues();
        while (c.moveToNext()) {
            if (c.getString(index2).equals(drink)) {
                id = c.getInt(index1);
                name = c.getString(index2);
                bottles = c.getString(index3);
                //amounts = c.getString(index4);
                count = c.getInt(index5);
                cv.put(COLUMN_NAME, name);
                cv.put(COLUMN_BOTTLES, bottles);
                //cv.put(COLUMN_AMOUNTS, amounts);
                cv.put(COLUMN_COUNT, count);
                if (c.getInt(index6) == 0) {
                    cv.put(COLUMN_WEEK, 1);
                    db.update(TABLE_DRINKS, cv, "_id" + "=" + id, null);
                    db.close();
                    return 1;
                } else {
                    cv.put(COLUMN_WEEK, 0);
                    db.update(TABLE_DRINKS, cv, "_id" + "=" + id, null);
                    db.close();
                    return 0;
                }
            }
        }
        c.close();
        db.close();
        return 0;
    }

    public int setQuickChoice(String drink, int val) {
        SQLiteDatabase db = getWritableDatabase();
        //String[] colummns = {COLUMN_ID, COLUMN_NAME, COLUMN_BOTTLES, COLUMN_AMOUNTS, COLUMN_COUNT, COLUMN_WEEK,};
        String[] colummns = {COLUMN_ID, COLUMN_NAME, COLUMN_BOTTLES, COLUMN_COUNT, COLUMN_WEEK,};
        String name;
        Cursor c = db.query(TABLE_DRINKS, colummns, null, null, null, null, null);
        int index1 = c.getColumnIndex(COLUMN_ID);
        int index2 = c.getColumnIndex(COLUMN_NAME);
        int index3 = c.getColumnIndex(COLUMN_BOTTLES);
        //int index4 = c.getColumnIndex(COLUMN_AMOUNTS);
        int index5 = c.getColumnIndex(COLUMN_COUNT);
        String bottles;
        int id;
        int count;
        ContentValues cv = new ContentValues();
        while (c.moveToNext()) {
            if (c.getString(index2).equals(drink)) {
                id = c.getInt(index1);
                name = c.getString(index2);
                bottles = c.getString(index3);
                //amounts = c.getString(index4);
                count = c.getInt(index5);
                cv.put(COLUMN_NAME, name);
                cv.put(COLUMN_BOTTLES, bottles);
                //cv.put(COLUMN_AMOUNTS, amounts);
                cv.put(COLUMN_COUNT, count);
                cv.put(COLUMN_WEEK, val);
                db.update(TABLE_DRINKS, cv, "_id" + "=" + id, null);

            }
        }
        c.close();
        db.close();
        return 0;
    }

    public ArrayList<String> getQuickChoice() {
        SQLiteDatabase db = getReadableDatabase();
        String[] colummns = {COLUMN_ID, COLUMN_NAME, COLUMN_WEEK};
        ArrayList<String> drinks = new ArrayList<>();

        Cursor c = db.query(TABLE_DRINKS, colummns, null, null, null, null, COLUMN_NAME + " ASC");
        int index2 = c.getColumnIndex(COLUMN_NAME);
        int index3 = c.getColumnIndex(COLUMN_WEEK);
        while (c.moveToNext()) {
            if (c.getInt(index3) == 1) {
                drinks.add(c.getString(index2));
            }
        }
        db.close();
        c.close();
        return drinks;
    }

    public int getFileSize() {
        status = 1;
        int rows = 0;
        try {
            int r1 = countLines("drinkoutput.txt");
            int r2 = countLines("spritoutput.txt");
            rows = r1 + r2;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public int countLines(String str) throws IOException {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, str);
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

    public void fileDrinks(int max) {
        // drink_list.clear();
        File sdcard = Environment.getExternalStorageDirectory();
        String name;
        String type;
        //String amounts = "";
        String info;
        int count = 0;
        String tmp;
        String nr_temp;
        int nr;

        File file = new File(sdcard, "drinkoutput.txt");
        int curr_row = 1;
        ArrayList<String> bottles = new ArrayList<>();
        ArrayList<String> amounts = new ArrayList<>();
        JSONObject bottles_amounts = new JSONObject();
        ArrayList<Integer> bottles_send = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        try {
            BufferedReader br = new BufferedReader(new BufferedReader(new FileReader(file)));
            BOMSkipper.skip(br);

            Scanner s = new Scanner(br);
            s.useDelimiter(";");
            while (s.hasNext()) {
                nr_temp = s.next();

                tmp = nr_temp.replaceAll("\\s", "");
                nr = Integer.parseInt(tmp);

                name = s.next();

                for (int i = 0; i < nr; i++) {
                    tmp = s.next();
                    bottles.add(tmp);
                }
                for (int i = 0; i < nr; i++) {
                    tmp = s.next();
                    amounts.add(tmp);
                }

                for (int i = 0; i < bottles.size(); i++) {
                    try {
                        bottles_amounts.put(bottles.get(i), amounts.get(i));
                        Log.d(TAG, bottles.get(i) + amounts.get(i));
                    } catch (JSONException e) {

                    }

                }
                for (int i = 0; i < bottles_amounts.names().length(); i++) {
                    try {
                        Log.v(TAG, "key = " + bottles_amounts.names() + " value = " + bottles_amounts.get(bottles_amounts.names().getString(i)));

                    } catch (JSONException e) {

                    }

                }
                //Log.d(TAG, bottles_amounts.);

                info = s.next();

                type = s.next();
                type = type.toUpperCase();

                //Drink D = new Drink(name, bottles, amounts, count, info, type, bottles_send);
                Drink D = new Drink(name, bottles_amounts, count, info, type, bottles_send);
                addDrinks(D, db);
                bottles_amounts = new JSONObject();
                bottles.clear();
                amounts.clear();
                //amounts = "";

                double i = (double) curr_row / (double) max;
                status = (int) Math.round(i * 100);

                curr_row++;
            }
            s.close();
            db.close();
        } catch (IOException e) {
            Log.e(TAG, "Nagot fick fel vid fillasning");
            db.close();
        }
        fileBottles(curr_row, max);
    }

    public void fileBottles(int curr_row, int max) {
        File sdcard = Environment.getExternalStorageDirectory();
        String bottle;
        int code;
        String code_temp;
        String tmp;
        File file = new File(sdcard, "spritoutput.txt");
        SQLiteDatabase db = getWritableDatabase();


        try {
            BufferedReader br = new BufferedReader(new BufferedReader(new FileReader(file)));
            BOMSkipper.skip(br);

            Scanner s = new Scanner(br);
            s.useDelimiter(";");
            while (s.hasNext()) {
                bottle = s.next();
                bottle = bottle.replaceAll("\\s", "");

                tmp = s.next();
                code_temp = tmp.replaceAll("\\s", "");
                code = Integer.parseInt(code_temp);

                addBottles(bottle, code, db);

                double i = (double) curr_row / (double) max;
                status = (int) Math.round(i * 100);

                curr_row++;
            }
            s.close();
            db.close();
        } catch (IOException e) {
            Log.e(TAG, "Nagot fick fel vid fillasning");
            db.close();
        }
        if (status > 100) {
            status = 100;
            delay(100);
        }
    }

    public void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addDrinks(Drink D, SQLiteDatabase db) {
        JSONObject json = new JSONObject();
        //JSONArray jsonArray = new JSONArray();

        try {
            json.put("bottles", D.getBottles());
            // jsonArray.put(D.getBottles());

        } catch (JSONException e) {

            e.printStackTrace();
        }

        String arrayList = json.toString();
        //String arrayList = jsonArray.toString();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, D.getName());
        values.put(COLUMN_BOTTLES, arrayList);
        //values.put(COLUMN_AMOUNTS, D.getAmounts());
        values.put(COLUMN_COUNT, D.getCount());
        values.put(COLUMN_INFO, D.getInfo());
        values.put(COLUMN_TYPE, D.getType());
        db.insert(TABLE_DRINKS, null, values);
    }

    public void addBottles(String bottle, int code, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOTTLES, bottle);
        values.put(COLUMN_CODE, code);
        db.insert(TABLE_BOTTLES, null, values);

    }
/*
    public Drink getOrup(){
        SQLiteDatabase db = getReadableDatabase();
        String[] colummns = {COLUMN_ID, COLUMN_BOTTLES, COLUMN_CODE};

        int code = 0;
        Cursor c = db.query(TABLE_BOTTLES, colummns, null, null, null, null, null);

        int index2 = c.getColumnIndex(COLUMN_BOTTLES);
        int index3 = c.getColumnIndex(COLUMN_CODE);
        Drink D;
        ArrayList<Integer> bottles_send = new ArrayList<>();
        ArrayList<String> bottles_array = new ArrayList<>();

        while (c.moveToNext()) {
            bottles_array.add(c.getString(index2));
            bottles_send.add(c.getInt(index3));
        }
        bottles_send.add(SEND_DONE);
        for(int t : bottles_send)
            System.out.println(t);

        System.out.println("langd: " + bottles_send.size());
        c.close();
        db.close();

        D = new Drink("", bottles_array, "", 0, "", "", bottles_send);

        return D;
    }
    */
}


/*
    public int changeColor() {
        if (color_code >= color_cap) {
            color_code = color_min;
            color_change = 1;
        } else {
            byte[] bytes = ByteBuffer.allocate(4).putInt(color_code).array();
            bytes[2]++;
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            color_code = bb.getInt();
        }
        return color_code;
    }
*/

    /* public boolean isTableExists() {
         SQLiteDatabase db = getWritableDatabase();
         Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + DATABASE_NAME + "'", null);
         if (cursor != null) {
             if (cursor.getCount() > 0) {
                 cursor.close();
                 return true;
             }
             cursor.close();
         }
         return false;
     }
 */


    /*public void deleteDrink(String drinkName) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_DRINKS, "name = ?", new String[]{drinkName});
        db.close();
    }
*/


    /*public int checkCode(int code) {
        if (code <= 255) {
            byte[] bytes = ByteBuffer.allocate(4).putInt(code).array();
            bytes[2] += color_change;
            color_change++;
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            code = bb.getInt();
            return code;
        } else {
            return code;
        }
    }
*/
 /*
    public boolean resendBottles(String clicked_drink) {
        SQLiteDatabase db = getWritableDatabase();

        String[] colummns = {COLUMN_ID, COLUMN_NAME, COLUMN_BOTTLES};
        Cursor c = db.query(TABLE_DRINKS, colummns, null, null, null, null, null);
        String bottles;

        byte[] bytes = new byte[16];

        int bottle;

        int index2 = c.getColumnIndex(COLUMN_NAME);
        int index3 = c.getColumnIndex(COLUMN_BOTTLES);
            while (c.moveToNext()) {
                if (c.getString(index2).equals(clicked_drink)) {

                    bottles = c.getString(index3);
                    JSONObject json;
                    try {
                        json = new JSONObject(bottles);

                        String jArray = json.getString("bottles");
                        String tmp = jArray.replaceAll("\\]", "");
                        tmp = tmp.replaceAll("\\[", "");
                        String[] tokens = tmp.split(",");
                        int nr = 0;
                        byte[] tmp_byte;
                        for (int i = 0; i < tokens.length; ++i) {
                            bottle = findBottle(tokens[i]);
                            if (bottle != 0) {
                                //  tmp_byte = ByteBuffer.allocate(4).putInt(checkCode(bottle)).array();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            db.close();
            c.close();

            return true;

    }
*/
    /*public class DbBitmapUtility {
        // convert from bitmap to byte array
        public  byte[] getBytes(Bitmap bitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        }

        // convert from byte array to bitmap
        public  Bitmap getImage(byte[] image) {
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        }
    }
*/






