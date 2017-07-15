package com.xplore.database

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import com.google.android.gms.maps.model.LatLng
import com.xplore.General
import com.xplore.reserve.Reserve
import com.xplore.reserve.ReserveCard

import java.io.IOException
import java.util.ArrayList

/**
* Created by Nikaoto on 1/15/2017.
* TODO write description of this class - what it does and why.
*/

internal class DBManager(private val mContext: Context,
                         DB_NAME: String = "reserveDB.db",
                         private val TABLE: String = General.DB_TABLE)
    : SQLiteOpenHelper(mContext, DB_NAME, null, 1) {

    //Path of the database that will be created
    private val DB_PATH = "/data/data/${mContext?.packageName}/databases/$DB_NAME"
    val GENERAL_TABLE = "general"

    companion object ColumnNames {
        val ID = "id"
        val NAME = "name"
        val DIFFICULTY = "difficulty"
        val FLORA = "flora"
        val FAUNA = "fauna"
        val EQUIPMENT = "equipment"
        val EXTRATAGS = "extratags"

        //General Table
        val TYPE = "type"
        val DESCRIPTION = "description"
        val IMAGE = "image"
        val LATITUDE = "latitude"
        val LONGITUDE = "longitude"
    }

    val rowCount: Int
    private var DataBase: SQLiteDatabase

    init {
        //Creating database
        DataBase = readableDatabase

        //Copy DB data
        try {
            CopyBytes.copy(mContext.assets.open(DB_NAME), DB_PATH)
        } catch (e: IOException) {
            Log.println(Log.ERROR, "errors", "Could not copy data")
        }

        openDataBase()
        rowCount = initRowCount()
        close()

    }

    fun initRowCount(table: String = TABLE): Int {
        val cursor = DataBase.doQuery("SELECT ${ID} FROM $table")
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

    //Shorter getData extension functions
    fun Cursor.getStr(column: String) = this.getString(this.getColumnIndex(column))
    fun Cursor.getInt(column: String) = this.getInt(this.getColumnIndex(column))
    fun Cursor.getDouble(column: String) = this.getDouble(this.getColumnIndex(column))

    //Gets the resource ID of the drawable with specified name
    fun convertFromDrawableNameToId(drawableName: String, context: Context = mContext) =
            context.resources.getIdentifier(drawableName, "drawable", context.packageName)

    //Custom extension of cursor that gets the imageId from it
    fun Cursor.getImageId(context: Context = mContext) =
            convertFromDrawableNameToId(this.getStr(IMAGE), context)

    //Custom extension so writing "null" every time becomes unnecessary
    fun SQLiteDatabase.doQuery(q: String) = this.rawQuery(q, null)

    //Assembles and returns Reserve object from Database by Id (FASTER)
    fun getReserve(id: Int, table: String = TABLE): Reserve {
        val cursor = DataBase.doQuery("SELECT * FROM $table WHERE ${ID} = $id")
        val gcursor = DataBase.doQuery("SELECT * FROM $GENERAL_TABLE WHERE ${ID} = $id")
        try {
            cursor.moveToFirst()
            gcursor.moveToFirst()

            return Reserve(
                    cursor.getInt(ID),
                    gcursor.getInt(DIFFICULTY),
                    cursor.getStr(NAME),
                    cursor.getStr(DESCRIPTION),
                    cursor.getStr(FLORA),
                    cursor.getStr(FAUNA),
                    cursor.getStr(EQUIPMENT),
                    cursor.getStr(EXTRATAGS),
                    LatLng(gcursor.getDouble(LATITUDE),
                            gcursor.getDouble(LONGITUDE)),
                    gcursor.getImageId(),
                    gcursor.getInt(TYPE))
        } catch (e: Exception) {
            Log.println(Log.ERROR, "database", "Could not load Reserve from cursor")
            return Reserve()
        } finally {
            cursor.close()
            gcursor.close()
        }
    }

    fun getReserveCard(id: Int, table: String = TABLE): ReserveCard {
        val cursor = DataBase.doQuery("SELECT ${NAME} FROM $table WHERE ${ID} = $id")
        val gcursor = DataBase.doQuery("SELECT ${IMAGE}, ${TYPE} FROM $GENERAL_TABLE WHERE ${ID} = $id")
        try{
            cursor.moveToFirst()
            gcursor.moveToFirst()
            return ReserveCard(
                    id,
                    cursor.getStr(NAME),
                    gcursor.getImageId(),
                    gcursor.getInt(TYPE))
        } catch (e: Exception){
            Log.println(Log.ERROR, "database", "Could not load ReserveCard from cursor")
            return ReserveCard()
        } finally {
            cursor.close()
            gcursor.close()
        }
    }

    //Load all reserveCards from DB
    fun getAllReserveCards(table: String = TABLE): ArrayList<ReserveCard> {
        val results = ArrayList<ReserveCard>(rowCount)
        val cursor = DataBase.doQuery("SELECT ${NAME} FROM $table")
        val gcursor = DataBase.doQuery("SELECT ${IMAGE}, ${TYPE} FROM $GENERAL_TABLE")
        try{
            if(cursor.moveToFirst() && gcursor.moveToFirst()){
                var index = 0
                do {
                    results.add(
                            ReserveCard(
                                    index,
                                    cursor.getStr(NAME),
                                    gcursor.getImageId(),
                                    gcursor.getInt(TYPE)
                            )
                    )
                    index++
                } while (cursor.moveToNext() && gcursor.moveToNext())
            }
            return results
        } catch (e: Exception){
            Log.println(Log.ERROR, "database", "Could not load ReserveCards from cursor")
            return results
        } finally {
            cursor.close()
            gcursor.close()
        }
    }

    //Finds a String by Id in Database and returns it
    fun getStr(id: Int, column: String, table: String = TABLE): String {
        val cursor = DataBase.doQuery("SELECT $column FROM $table WHERE ${ID} = $id")
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
        val cursor = DataBase.doQuery("SELECT $column FROM $table WHERE ${ID} = $id")
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
        val cursor = DataBase.doQuery("SELECT $column FROM $table WHERE ${ID} = $id")
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

    fun getImageId(id: Int, context: Context? = mContext, table: String = TABLE): Int {
        if (context != null) {
            return convertFromDrawableNameToId(getStr(id, IMAGE, table), context)
        } else return 0
    }


    //Finds the Id of an entry by every field in Database and returns it
    fun getIdFromQuery(query: String, table: String): List<Int> {
        val answers = ArrayList<Int>()

        //TODO add categorized search
        //Use string query to search for reserves and output IDs
        val cursor = DataBase.doQuery("SELECT ${ID} FROM $table WHERE ${NAME}"+
                " LIKE '%$query%'"+
                " or ${FLORA} LIKE '%$query%'"+
                " or ${FAUNA} LIKE '%$query%'"+
                " or ${EXTRATAGS} LIKE '%$query%'"+
                " or ${EQUIPMENT} LIKE '%$query%'")

        try {
            if (cursor.moveToFirst()) {
                do {
                    answers.add(cursor.getInt(0))
                } while (cursor.moveToNext())
            }
        }
        catch (e: Exception){
            Log.println(Log.ERROR, "database", "getIdFromQuery failed")
        }
        finally {
            cursor.close()
            return answers
        }
    }

    //Searching for reserve IDs by name
    fun getIdFromName(nameQuery: String, table: String = TABLE): List<Int> {
        val answers = ArrayList<Int>()

        //Searching the database by names
        val cursor = DataBase.doQuery("SELECT ${ID} FROM $table WHERE ${NAME} LIKE '%$nameQuery%'")
        try {
            if (cursor.moveToFirst()) {
                do {
                    answers.add(cursor.getInt(0))
                } while (cursor.moveToNext())
            }
        }
        catch (e: Exception){
            Log.println(Log.ERROR, "database", "getIdFromName failed")
        }
        finally {
            cursor.close()
            return answers
        }
    }

    @Throws(SQLException::class)
    fun openDataBase() {
        DataBase = this.readableDatabase
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) { }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) { }
}
