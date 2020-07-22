package com.vikanshu.vaartalap.Database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DATABASE_NAME = "logs.db"
private const val DATABASE_VERSION = 1
private const val TABLE_NAME = "LOGS"
private const val COLUMN_ID = "ID"
private const val COLUMN_NAME = "NAME"
private const val COLUMN_NUMBER = "NUMBER"
private const val COLUMN_TIMESTAMP = "TIMESTAMP"
private const val COLUMN_UID = "UID"
private const val COLUMN_TYPE = "TYPE"
private const val COLUMN_START = "START_TIME"
private const val COLUMN_END = "END_TIME"

class LogDBHelper(ctx: Context) : SQLiteOpenHelper(ctx,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase?) {
        val SQL_CREATE_TABLE =
            "CREATE TABLE $TABLE_NAME ($COLUMN_ID TEXT PRIMARY KEY, $COLUMN_NAME TEXT NOT NULL, $COLUMN_NUMBER INTEGER" +
                    " NOT NULL, $COLUMN_UID TEXT NOT NULL, $COLUMN_TYPE TEXT NOT NULL, $COLUMN_TIMESTAMP INTEGER NOT NULL," +
                    " $COLUMN_START INTEGER NOT NULL, $COLUMN_END INTEGER NOT NULL)"
        db?.execSQL(SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}