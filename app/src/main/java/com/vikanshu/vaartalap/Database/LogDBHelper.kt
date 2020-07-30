package com.vikanshu.vaartalap.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.vikanshu.vaartalap.model.ContactsModel
import com.vikanshu.vaartalap.model.LogsModel

private const val DATABASE_NAME = "logs.db"
private const val DATABASE_VERSION = 1
private const val TABLE_NAME = "LOGS"
private const val COLUMN_ID = "ID"
private const val COLUMN_NAME = "NAME"
private const val COLUMN_NUMBER = "NUMBER"
private const val COLUMN_TIMESTAMP = "TIMESTAMP"
private const val COLUMN_UID = "UID"
private const val COLUMN_IMAGE = "IMAGE"
private const val COLUMN_TYPE = "TYPE"
private const val COLUMN_CHANNEL = "CHANNEL"

class LogDBHelper(ctx: Context) : SQLiteOpenHelper(
    ctx,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase?) {
        val SQL_CREATE_TABLE =
            "CREATE TABLE $TABLE_NAME ($COLUMN_ID TEXT PRIMARY KEY, $COLUMN_NAME TEXT NOT NULL, $COLUMN_NUMBER INTEGER" +
                    " NOT NULL, $COLUMN_UID TEXT NOT NULL, $COLUMN_TYPE TEXT NOT NULL, $COLUMN_TIMESTAMP INTEGER NOT NULL," +
                    " $COLUMN_CHANNEL TEXT NOT NULL $COLUMN_IMAGE TEXT NOT NULL)"
        db?.execSQL(SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun getAll(): ArrayList<LogsModel> {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val result = db.rawQuery(query, null)
        val list = ArrayList<LogsModel>()
        while (result.moveToNext()) {
            list.add(
                LogsModel(
                    result.getString(result.getColumnIndexOrThrow(COLUMN_ID)),
                    result.getString(result.getColumnIndexOrThrow(COLUMN_UID)),
                    result.getString(result.getColumnIndexOrThrow(COLUMN_NAME)),
                    result.getString(result.getColumnIndexOrThrow(COLUMN_NUMBER)),
                    result.getString(result.getColumnIndexOrThrow(COLUMN_TYPE)),
                    result.getLong(result.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                    result.getString(result.getColumnIndexOrThrow(COLUMN_CHANNEL)),
                    result.getString(result.getColumnIndexOrThrow(COLUMN_IMAGE))
                )
            )
        }
        result.close()
        db.close()
        return list
    }

    fun store(log: LogsModel) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME, log.NAME)
        contentValues.put(COLUMN_IMAGE, log.IMAGE)
        contentValues.put(COLUMN_UID, log.IMAGE)
        contentValues.put(COLUMN_TYPE, log.TYPE)
        contentValues.put(COLUMN_TIMESTAMP, log.TIME)
        contentValues.put(COLUMN_NUMBER, log.NUMBER)
        contentValues.put(COLUMN_CHANNEL, log.CHANNEL)
        contentValues.put(COLUMN_ID, log.ID)
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }

    fun deleteAll() {
        val db = this.writableDatabase
        db.execSQL("delete from $TABLE_NAME")
        db.close()
    }

    fun exists(number: String): Boolean {
        var storeId = 2
        val db = this.readableDatabase
        val query_params = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_NUMBER = $number"
        val csor = db.rawQuery(query_params, null)
        if (csor.moveToFirst()) {
            do {
                storeId = csor.getInt(csor.getColumnIndexOrThrow(COLUMN_NUMBER))
            } while (csor.moveToNext())
        } else {
            return false
        }
        csor.close()
        db.close()
        return storeId != 2
    }
}