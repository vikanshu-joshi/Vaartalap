package com.vikanshu.vaartalap.SettingsActivity

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.vikanshu.vaartalap.R


class SettingsPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_fragment)
    }
}