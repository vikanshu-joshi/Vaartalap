package com.vikanshu.vaartalap.SettingsActivity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import com.vikanshu.vaartalap.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var manager: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        manager = PreferenceManager.getDefaultSharedPreferences(this)
        Preference.SummaryProvider<EditTextPreference> {
            it.text
        }
    }
}