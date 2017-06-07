package com.explorify.xplore.xplore

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import com.google.android.gms.maps.model.LatLng

import java.io.IOException
import java.util.ArrayList

/**
 * Created by nikao on 1/15/2017.
 */

internal class DBManager(private val mContext: Context,
                         DB_NAME: String = "reserveDB.db",
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
        DataBase = readableDatabase

        //Copy DB data
        try {
            CopyBytes(mContext.assets.open(DB_NAME), DB_PATH)
        } catch (e: IOException) {
            Log.println(Log.ERROR, "errors", "Could not copy data")
        }

        openDataBase()
        rowCount = initRowCount()
        close()

    }

    fun initRowCount(table: String = TABLE): Int {
        val cursor = DataBase.doQuery("SELECT $ID FROM $table")
        try {
            cursor.moveToLast()
            return cursor.getInt(0)
        } catch (e: Exception) {
            Log.println(Log.ERROR, "errors", "initRowCount failed")
            return 0
        } finally {
            cursor.close()
        }
    }

    //Gets the resource ID of the drawable with specified name
    fun convertFromDrawableNameToId(drawableName: String, context: Context = mContext) =
            context.resources.getIdentifier(drawableName, "drawable", context.packageName)

    //Custom extension of cursor that gets the imageId from it
    fun Cursor.getImageId(context: Context = mContext) =
            convertFromDrawableNameToId(this.getString(this.getColumnIndex(IMAGE)), context)

    //Custom extension so writing "null" every time becomes unnecessary
    fun SQLiteDatabase.doQuery(q: String) = this.rawQuery(q, null)

    //Assembles and returns Reserve object from Database by Id (FASTER)
    fun getReserve(id: Int, table: String = TABLE): Reserve {
        val cursor = DataBase.doQuery("SELECT * FROM $table WHERE $ID = $id")
        try {
            cursor.moveToNext()
            return Reserve(
                    cursor.getInt(cursor.getColumnIndex(ID)),
                    cursor.getInt(cursor.getColumnIndex(DIFFICULTY)),
                    cursor.getString(cursor.getColumnIndex(NAME)),
                    cursor.getString(cursor.getColumnIndex(DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndex(FLORA)),
                    cursor.getString(cursor.getColumnIndex(FAUNA)),
                    cursor.getString(cursor.getColumnIndex(EQUIPMENT)),
                    cursor.getString(cursor.getColumnIndex(EXTRATAGS)),
                    LatLng(cursor.getDouble(cursor.getColumnIndex(LATITUDE)),
                            cursor.getDouble(cursor.getColumnIndex(LONGITUDE))),
                    cursor.getImageId()
            )
        } catch (e: Exception) {
            Log.println(Log.ERROR, "database", "Could not load Reserve from cursor")
            return Reserve()
        } finally {
            cursor.close()
        }
    }

    fun getReserveCard(id: Int, table: String = TABLE): ReserveCard {
        val cursor = DataBase.doQuery("SELECT * FROM $table WHERE $ID = $id")
        try{
            cursor.moveToNext()
            return ReserveCard(
                    id,
                    cursor.getString(cursor.getColumnIndex(NAME)),
                    convertFromDrawableNameToId(cursor.getString(cursor.getColumnIndex(IMAGE))
                    )//TODO icon id
            )
        } catch (e: Exception){
            Log.println(Log.ERROR, "database", "Could not load ReserveCard from cursor")
            return ReserveCard()
        } finally {
            cursor.close()
        }
    }

    fun getAllReserveCards() {
        //TODO do this
    }

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



/*    //Finds image name in Database by Id, finds its corresponding drawable and returns the drawable
    @Suppress("DEPRECATION")
    fun getImage(id: Int, context: Context = mContext, table: String = TABLE): Drawable {
        val tempImageId = convertFromDrawableNameToId(getStr(id, IMAGE, table), context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return context.resources.getDrawable(tempImageId, context.theme)
        else
            return context.resources.getDrawable(tempImageId)
    }*/

    fun getImageId(id: Int, context: Context = mContext, table: String = TABLE): Int =
            convertFromDrawableNameToId(getStr(id, IMAGE, table), context)

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
