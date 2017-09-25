package com.xplore

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.xplore.database.CopyBytes
import com.xplore.database.DBManager
import java.io.IOException

/**
 * Created by Nika on 9/25/2017.
 * TODO write description of this class - what it does and why.
 */

class IliauniDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {

    private val TAG = "iliauniDB"

    companion object {
        val DB_NAME = "iliauni.db"
    }

    private val DB_PATH = "/data/data/${context.packageName}/databases/$DB_NAME"

    val rowCount: Int
    private var DataBase: SQLiteDatabase

    init {
        //Creating database
        DataBase = readableDatabase

        //Copy DB data
        try {
            CopyBytes.copy(context.assets.open(DB_NAME), DB_PATH)
        } catch (e: IOException) {
            Log.println(Log.ERROR, "errors", "Could not copy data")
        }

        openDataBase()
        rowCount = initRowCount()
        close()
    }

    @Throws(SQLException::class)
    fun openDataBase() {
        DataBase = this.readableDatabase
    }


    fun initRowCount(): Int {
        val cursor = DataBase.rawQuery("SELECT ${Stand.COLUMN_ID} FROM ${Stand.TABLE_NAME}", null)
        try {
            cursor.moveToLast()
            return cursor.getInt(0)
        } catch (e: Exception) {
            Log.e(TAG, "initRowCount failed")
            return 0
        } finally {
            cursor.close()
        }
    }

    private fun Cursor.int(columnName: String) = this.getInt(this.getColumnIndex(columnName))
    private fun Cursor.str(columnName: String) = this.getString(this.getColumnIndex(columnName))
    private fun Cursor.dbl(columnName: String) = this.getDouble(this.getColumnIndex(columnName))

    fun getAllStands(): ArrayList<Stand> {
        val stands = ArrayList<Stand>(rowCount)
        val cursor = DataBase.rawQuery("SELECT * FROM ${Stand.TABLE_NAME}", null)
        try {
            if (cursor.moveToFirst()) {
                do {
                    stands.add(
                            Stand(cursor.int(Stand.COLUMN_ID),
                            cursor.str(Stand.COLUMN_NAME),
                            cursor.str(Stand.COLUMN_DESCRIPTION),
                            cursor.getBlob(cursor.getColumnIndex(Stand.COLUMN_IMAGE)),
                            cursor.dbl(Stand.COLUMN_LAT),
                            cursor.dbl(Stand.COLUMN_LNG))
                    )
                } while (cursor.moveToNext())
            }
            return stands
        } catch (e: Exception) {
            Log.e(TAG, "Could not load all stands from cursor")
            return stands
        } finally {
            cursor.close()
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        //
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //
    }

}