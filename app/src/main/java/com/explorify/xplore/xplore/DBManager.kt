package com.explorify.xplore.xplore

import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log

import com.google.android.gms.maps.model.LatLng

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by nikao on 1/15/2017.
 */

internal class DBManager(private val mContext: Context, private val DB_NAME: String = "reserveDB.db",
                         private val TABLE: String = General.getCurrentTable(mContext))
    : SQLiteOpenHelper(mContext, DB_NAME, null, 1) {

    //Path of the database that will be created
    private val DB_PATH = "/data/data/${mContext.packageName}/databases/$DB_NAME"

    //Column names of the database
    private val ID_COLUMN_NAME = "id"
    private val NAME_COLUMN_NAME = "name"
    private val DESCRIPTION_COLUMN_NAME = "description"
    private val DIFFICULTY_COLUMN_NAME = "difficulty"
    private val FLORA_COLUMN_NAME = "flora"
    private val FAUNA_COLUMN_NAME = "fauna"
    private val EQUIPMENT_COLUMN_NAME = "equipment"
    private val EXTRATAGS_COLUMN_NAME = "extratags"
    private val IMAGE_COLUMN_NAME = "image"
    private val LATITUDE_COLUMN_NAME = "latitude"
    private val LONGITUDE_COLUMN_NAME = "longitude"

    private val rowCount: Int

    private var DataBase: SQLiteDatabase

    init {
        //Creating database
        DataBase = readableDatabase //openDataBase()?

        //Copy DB data
        try { CopyBytes(mContext.assets.open(DB_NAME), DB_PATH) }
        catch (e: IOException) { Log.println(Log.ERROR, "errors", "Could not copy data") }

        openDataBase()
        rowCount = initRowCount()
        close()

    }

    //Custom extension so writing "null" every time becomes unnecessary
    fun SQLiteDatabase.doQuery(q: String) = this.rawQuery(q,null)

    fun initRowCount(table: String = TABLE): Int {
        val cursor = DataBase.doQuery("SELECT $ID_COLUMN_NAME FROM $table")
        try{
            cursor.moveToLast()
            return cursor.getInt(0)
        }
        catch (e: Exception){
            Log.println(Log.ERROR, "errors", "initRowCount failed")
            return 0
        }
        finally {
            cursor.close()
        }
    }


    //Assembles and returns Reserve object from Database by Id
    fun getReserve(id: Int, context: Context = mContext, table: String = TABLE) =
            Reserve(
                id,                                         //Id
                getInt(id, DIFFICULTY_COLUMN_NAME, table),  //Difficulty
                getStr(id, NAME_COLUMN_NAME, table),        //Name
                getStr(id, DESCRIPTION_COLUMN_NAME, table), //Description
                getStr(id, FLORA_COLUMN_NAME, table),       //Flora
                getStr(id, FAUNA_COLUMN_NAME, table),       //Fauna
                getStr(id, EQUIPMENT_COLUMN_NAME, table),   //Equipment
                getStr(id, EXTRATAGS_COLUMN_NAME, table),   //Extratags
                getLatLng(id, table),                       //Location
                getImage(id, context, table)                //Drawable
            )


    //Finds a String by Id in Database and returns it
    fun getStr(id: Int, column: String, table: String = TABLE): String {
        val cursor = DataBase.doQuery("SELECT $column FROM $table WHERE $ID_COLUMN_NAME = $id")
        try {
            cursor.moveToNext()
            return cursor.getString(0)
        }
        catch (e: Exception) {
            Log.println(Log.ERROR, "database", "Could not load String from cursor")
            return ""
        }
        finally {
            cursor.close()
        }
    }

    //Finds a Double by Id in Database and returns it
    fun getDouble(id: Int, column: String, table: String = TABLE): Double {
        val cursor = DataBase.doQuery("SELECT $column FROM $table WHERE $ID_COLUMN_NAME = $id")
        try{
            cursor.moveToFirst()
            return cursor.getDouble(0)
        }
        catch (e: Exception){
            Log.println(Log.ERROR, "database", "Could not load Double from cursor")
            return 0.0
        }
        finally {
            cursor.close()
        }
    }

    //Finds an Integer by Id in Database and returns it
    fun getInt(id: Int, column: String, table: String = TABLE): Int {
        val cursor = DataBase.doQuery("SELECT $column FROM $table WHERE $ID_COLUMN_NAME = $id")
        try {
            cursor.moveToFirst()
            return cursor.getInt(0)
        }
        catch (e: Exception){
            Log.println(Log.ERROR, "database", "Could not load Integer from cursor")
            return 0
        }
        finally {
            cursor.close()
        }
    }

    //Finds Lat and Lng Doubles by Id and returns a new LatLng
    fun getLatLng(id: Int, table: String = TABLE) =
            LatLng(
                getDouble(id, LATITUDE_COLUMN_NAME, table),
                getDouble(id, LONGITUDE_COLUMN_NAME, table))


    //Gets the resource ID of the drawable with specified name
    fun convertFromDrawableNameToId(drawableName: String, context: Context = mContext) =
            context.resources.getIdentifier(drawableName, "drawable", context.packageName)

    //Finds image name in Database by Id, finds its corresponding drawable and returns the drawable
    @Suppress("DEPRECATION")
    fun getImage(id: Int, context: Context = mContext, table: String = TABLE): Drawable {
        val tempImageId = convertFromDrawableNameToId(getStr(id, IMAGE_COLUMN_NAME, table), context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return context.resources.getDrawable(tempImageId, context.theme)
        else
            return context.resources.getDrawable(tempImageId)
    }

    //Finds the Id of an entry by every field in Database and returns it
    fun getIdFromQuery(query: String, table: String): List<Int>? {
        val answers = ArrayList<Int>()

        //TODO add categorized search
        //TODO add all tags search
        //Use string query to search for reserves and output IDs
        val cursor = DataBase.doQuery("SELECT $ID_COLUMN_NAME FROM $table WHERE $NAME_COLUMN_NAME"+
                " LIKE '%$query%'"+
                " or $FLORA_COLUMN_NAME LIKE '%$query%'"+
                " or $FAUNA_COLUMN_NAME LIKE '%$query%'"+
                " or $EXTRATAGS_COLUMN_NAME LIKE '%$query%'"+
                " or $EQUIPMENT_COLUMN_NAME LIKE '%$query%'")

        try {
            if (cursor.moveToFirst()) {
                do {
                    answers.add(cursor.getInt(0))
                } while (cursor.moveToNext())
                return answers
            }
            else return null
        }
        catch (e: Exception){
            Log.println(Log.ERROR, "database", "getIdFromQuery failed")
            return null
        }
        finally {
            cursor.close()
        }
    }


    @Throws(SQLException::class)
    fun openDataBase() {
        this.close()
        DataBase = this.readableDatabase
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {}

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}
}
