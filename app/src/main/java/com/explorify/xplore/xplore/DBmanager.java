package com.explorify.xplore.xplore;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikao on 1/15/2017.
 */

public class DBmanager extends SQLiteOpenHelper {

    //Name of the database that will be created
    private static String DB_NAME = "reserveDB.db";

    //Path of the database that will be created
    private static String DB_PATH = "/data/data/com.explorify.xplore.xplore/databases/"+DB_NAME;

    private SQLiteDatabase reserveDataBase;
    private final Context myContext;

    //Column names of the 'reserveDB' database
    private final String NAME_COLUMN_NAME = "name";
    private final String DESCRIPTION_COLUMN_NAME = "description";
    private final String DIFFICULTY_COLUMN_NAME = "difficulty";
    private final String FLORA_COLUMN_NAME = "flora";
    private final String FAUNA_COLUMN_NAME = "fauna";
    private final String EQUIPMENT_COLUMN_NAME = "equipment";
    private final String EXTRATAGS_COLUMN_NAME = "extratags";
    private final String IMAGE_COLUMN_NAME = "image";
    private final String LATITUDE_COLUMN_NAME = "latitude";
    private final String LONGITUDE_COLUMN_NAME = "longitude";

    public DBmanager(Context context) {
        super(context,DB_NAME, null,1);
        this.myContext = context;
    }

//    Copies the database of reserves from the assets folder to android's
//    default database path (so other classes can later read it)
    public void createDataBase() {
        this.getReadableDatabase();
        try{
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        OutputStream myOutput = new FileOutputStream(DB_PATH);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while((length = myInput.read(buffer))>0)
            myOutput.write(buffer,0,length);

        //close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }
        catch(IOException e){
            Log.println(Log.ERROR,"errors","Could not copy db");
        }
    }

    public double getDoubleFromDB(String table, int id, String column)
    {
        //Use element ID to find string data
        Cursor cursor = reserveDataBase.rawQuery("SELECT "+column+" FROM "+table+" WHERE _id="+String.valueOf(id),null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor.getDouble(0);
    }

    public int getIntFromDB(String table, int id, String column)
    {
        //Use element ID to find string data
        Cursor cursor = reserveDataBase.rawQuery("SELECT "+column+" FROM "+table+" WHERE _id="+String.valueOf(id),null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public String getStrFromDB(String table, int id, String column)
    {
        //Use element ID to find integer data
        Cursor cursor = reserveDataBase.rawQuery("SELECT "+column+" FROM "+table+" WHERE _id="+String.valueOf(id),null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor.getString(0);
    }

    public LatLng getLatLngFromDB(String table, int id)
    {
        return new LatLng(getDoubleFromDB(table,id,LATITUDE_COLUMN_NAME),getDoubleFromDB(table,id,LONGITUDE_COLUMN_NAME));
    }

    public Reserve getReserve(int id, Context context)
    {
        String table = General.getCurrentTable(context);
        Reserve res = new Reserve();

        res.setName(getStrFromDB(table,id,NAME_COLUMN_NAME));
        res.setDescription(getStrFromDB(table,id,DESCRIPTION_COLUMN_NAME));
        res.setFlora(getStrFromDB(table,id,FLORA_COLUMN_NAME));
        res.setFauna(getStrFromDB(table,id,FAUNA_COLUMN_NAME));
        res.setEquipment(getStrFromDB(table,id,EQUIPMENT_COLUMN_NAME));
        res.setExtratags(getStrFromDB(table,id,EXTRATAGS_COLUMN_NAME));
        res.setDifficulty(getIntFromDB(table,id,DIFFICULTY_COLUMN_NAME));
        res.setImage(getStrFromDB(table,id,IMAGE_COLUMN_NAME));
        res.setLocation(getLatLngFromDB(table,id));
        res.setDrawable(getReserveImage(table,id,context));

        reserveDataBase.close();

        return res;
    }

    public Drawable getReserveImage(String table, int id, Context context)
    {
        openDataBase();

        Resources resources = context.getResources();

        int tempImageId = resources.getIdentifier(getStrFromDB(table, id, getImageColumnName()),"drawable","com.explorify.xplore.xplore");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return resources.getDrawable(tempImageId, context.getTheme());
        else
            return resources.getDrawable(tempImageId);
    }

    public List<Integer> getIdFromQuery(String query, String table)
    {
        List<Integer> answers = new ArrayList<Integer>();

        //TODO add categorized search
        //TODO add all tags search
        //Use string query to search for reserves and output IDs
        Cursor cursor = reserveDataBase.rawQuery("SELECT _id FROM "+table+" WHERE "+NAME_COLUMN_NAME+" LIKE '%"+query+"%' " +
                "or "+FLORA_COLUMN_NAME+" LIKE '%"+query+"%' " +
                "or "+FAUNA_COLUMN_NAME+" LIKE '%"+query+"%' " +
                "or "+EXTRATAGS_COLUMN_NAME+" LIKE '%"+query+"%' " +
                "or "+EQUIPMENT_COLUMN_NAME+" LIKE '%"+query+"%'",null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                do {
                    answers.add(cursor.getInt(0));
                }
                while (cursor.moveToNext());

                return answers;
            }

            else return null;
        }
        else return null;
    }


    public void openDataBase() throws SQLException
    {
        this.close();
        reserveDataBase = this.getReadableDatabase();
    }

    @Override
    public synchronized  void close()
    {
        if(reserveDataBase !=null)
            reserveDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public String getImageColumnName()
    {
        return IMAGE_COLUMN_NAME;
    }

    public String getNameColumnName()
    {
        return NAME_COLUMN_NAME;
    }

}
