package com.vikanshu.vaartalap

import android.content.Context

class UserDataSharedPref(ctx: Context) {


    private var PREF_NAME = "user data"
    private var KEY_NAME = "name"
    private var KEY_IMAGE = "image"
    private var KEY_NUMBER = "number"
    private var KEY_CODE = "code"

    private val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getName(): String? {
        return prefs.getString(KEY_NAME, null)
    }

    fun getImage(): String? {
        return prefs.getString(KEY_IMAGE, "default")
    }

    fun getNumber(): String? {
        return prefs.getString(KEY_NUMBER, null)
    }

    fun getCode(): String? {
        return prefs.getString(KEY_CODE, null)
    }

    fun setName(name: String) {
        val editor = prefs.edit()
        editor.putString(KEY_NAME, name)
        editor.apply()
    }

    fun setImage(image: String) {
        val editor = prefs.edit()
        editor.putString(KEY_IMAGE, image)
        editor.apply()
    }

    fun setNumber(number: String) {
        val editor = prefs.edit()
        editor.putString(KEY_NUMBER, number)
        editor.apply()
    }

    fun setCode(code: String) {
        val editor = prefs.edit()
        editor.putString(KEY_CODE, code)
        editor.apply()
    }

}