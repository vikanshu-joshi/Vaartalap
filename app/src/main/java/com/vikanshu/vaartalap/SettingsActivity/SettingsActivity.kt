package com.vikanshu.vaartalap.SettingsActivity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.*
import com.vikanshu.vaartalap.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var manager: SharedPreferences
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.settings)
        toolbar.setTitleTextColor(resources.getColor(R.color.white))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        manager = PreferenceManager.getDefaultSharedPreferences(this)
        Preference.SummaryProvider<EditTextPreference> {
            it.text
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()
        return true
    }
}