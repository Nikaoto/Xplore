package com.explorify.xplore.xplore

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log

import com.google.android.gms.maps.model.LatLng

import java.io.IOException
import java.util.ArrayList

/**
 * Created by nikao on 1/15/2017.
 */

internal class DBManager(private val mContext: Context, private val DB_NAME: String = "reserveDB.db",
                         private val TABLE: String = General.DB_TABLE)
    : SQLiteOpenHelper(mContext, DB_NAME, null, 1) {

    //Path of the database that will be created
    private val DB_PATH = "/data/data/${mContext.packageName}/databases/$DB_NAME"

    companion object ColumnNames {
        val ID = "id"
        val NAME = "name"
        val DESCRIPTION = "description"
        val DIFFICULTY = "difficulty"
        val FLORA = "flora"
        val FAUNA = "fauna"
        val EQUIPMENT = "equipment"
        val EXTRATAGS = "extratags"
        val IMAGE = "image"
        val LATITUDE = "latitude"
        val LONGITUDE = "longitude"
    }

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
        val cursor = DataBase.doQuery("SELECT $ID FROM $table")
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
                id,                             //Id
                getInt(id, DIFFICULTY, table),  //Difficulty
                getStr(id, NAME, table),        //Name
                getStr(id, DESCRIPTION, table), //Description
                getStr(id, FLORA, table),       //Flora
                getStr(id, FAUNA, table),       //Fauna
                getStr(id, EQUIPMENT, table),   //Equipment
                getStr(id, EXTRATAGS, table),   //Extratags
                getLatLng(id, table),           //Location
                getImage(id, context, table)    //Drawable
            )


    //Finds a String by Id in Database and returns it
    fun getStr(id: Int, column: String, table: String = TABLE): String {
        val cursor = DataBase.doQuery("SELECT $column FROM $table WHERE $ID = $id")
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
        val cursor = DataBase.doQuery("SELECT $column FROM $table WHERE $ID = $id")
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
        val cursor = DataBase.doQuery("SELECT $column FROM $table WHERE $ID = $id")
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
                getDouble(id, LATITUDE, table),
                getDouble(id, LONGITUDE, table))


    //Gets the resource ID of the drawable with specified name
    fun convertFromDrawableNameToId(drawableName: String, context: Context = mContext) =
            context.resources.getIdentifier(drawableName, "drawable", context.packageName)

    //Finds image name in Database by Id, finds its corresponding drawable and returns the drawable
    @Suppress("DEPRECATION")
    fun getImage(id: Int, context: Context = mContext, table: String = TABLE): Drawable {
        val tempImageId = convertFromDrawableNameToId(getStr(id, IMAGE, table), context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return context.resources.getDrawable(tempImageId, context.theme)
        else
            return context.resources.getDrawable(tempImageId)
    }

    fun getImageId(id: Int, context: Context = mContext, table: String = TABLE): Int =
            convertFromDrawableNameToId(getStr(id, IMAGE, table))

    //Finds the Id of an entry by every field in Database and returns it
    fun getIdFromQuery(query: String, table: String): List<Int>? {
        val answers = ArrayList<Int>()

        //TODO add categorized search
        //TODO add all tags search
        //Use string query to search for reserves and output IDs
        val cursor = DataBase.doQuery("SELECT $ID FROM $table WHERE $NAME"+
                " LIKE '%$query%'"+
                " or $FLORA LIKE '%$query%'"+
                " or $FAUNA LIKE '%$query%'"+
                " or $EXTRATAGS LIKE '%$query%'"+
                " or $EQUIPMENT LIKE '%$query%'")

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
