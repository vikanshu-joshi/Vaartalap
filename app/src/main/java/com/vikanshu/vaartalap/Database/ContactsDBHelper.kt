package com.vikanshu.vaartalap.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.vikanshu.vaartalap.model.ContactsModel

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
            "CREATE TABLE $TABLE_NAME ($COLUMN_UID TEXT, $COLUMN_NAME TEXT NOT NULL, $COLUMN_NUMBER TEXT" +
                    " NOT NULL PRIMARY KEY, $COLUMN_IMAGE TEXT NOT NULL)"
        db?.execSQL(SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun getAll(): ArrayList<ContactsModel> {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val result = db.rawQuery(query, null)
        val list = ArrayList<ContactsModel>()
        while (result.moveToNext()) {
            list.add(
                ContactsModel(
                    result.getString(result.getColumnIndexOrThrow(COLUMN_NAME)),
                    result.getString(result.getColumnIndexOrThrow(COLUMN_NUMBER)),
                    result.getString(result.getColumnIndexOrThrow(COLUMN_UID)),
                    result.getString(result.getColumnIndexOrThrow(COLUMN_IMAGE))
                )
            )
        }
        result.close()
        db.close()
        return list
    }

    fun store(contact: ContactsModel){
        if(!exists(contact.number)) {
            val db = this.writableDatabase
            val contentValues = ContentValues()
            contentValues.put(COLUMN_NAME, contact.name)
            contentValues.put(COLUMN_IMAGE, contact.image)
            contentValues.put(COLUMN_UID, contact.uid)
            contentValues.put(COLUMN_NUMBER, contact.number)
            db.insert(TABLE_NAME, null, contentValues)
            db.close()
        }
    }

    fun deleteAll(){
        val db = this.writableDatabase
        db.execSQL("delete from $TABLE_NAME")
        db.close()
    }

    fun exists(number: String): Boolean{
        var storeId = 2
        val db = this.readableDatabase
        val query_params = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_NUMBER = $number"
        val csor = db.rawQuery(query_params,null)
        if (csor.moveToFirst()){
            do {
                storeId = csor.getInt(csor.getColumnIndexOrThrow(COLUMN_NUMBER))
            }while (csor.moveToNext())
        }else{
            return false
        }
        csor.close()
        db.close()
        return storeId != 2
    }
}