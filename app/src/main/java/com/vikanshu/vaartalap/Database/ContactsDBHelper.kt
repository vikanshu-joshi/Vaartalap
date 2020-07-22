package com.vikanshu.vaartalap.Database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DATABASE_NAME = "contacts.db"
private const val DATABASE_VERSION = 1
private const val TABLE_NAME = "CONTACTS"
private const val COLUMN_NAME = "NAME"
private const val COLUMN_NUMBER = "NUMBER"
private const val COLUMN_UID = "UID"
private const val COLUMN_IMAGE = "IMAGE"

class ContactsDBHelper(ctx: Context) :
    SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val SQL_CREATE_TABLE =
            "CREATE TABLE $TABLE_NAME ($COLUMN_UID TEXT PRIMARY KEY, $COLUMN_NAME TEXT NOT NULL, $COLUMN_NUMBER INTEGER" +
                    " NOT NULL, $COLUMN_IMAGE TEXT NOT NULL)"
        db?.execSQL(SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}