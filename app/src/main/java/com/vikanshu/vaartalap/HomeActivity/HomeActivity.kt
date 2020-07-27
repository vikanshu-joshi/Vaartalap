package com.vikanshu.vaartalap.HomeActivity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.vikanshu.vaartalap.CallingActivity.CallingActivity
import com.vikanshu.vaartalap.R
import com.vikanshu.vaartalap.SettingsActivity.SettingsActivity
import com.vikanshu.vaartalap.TempIncomingService


class HomeActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var viewPagerAdapter: HomeViewPagerAdapter
    private val LOGS_TAB = "Logs"
    private val CONTACTS_TAB = "Contacts"
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.app_name)
        toolbar.setTitleTextColor(resources.getColor(R.color.white))
        setSupportActionBar(toolbar)
        tabLayout = findViewById(R.id.homeTabLayout)
        viewPager = findViewById(R.id.homeViewPager)
        setTabsAndViewPager()
        firestore = FirebaseFirestore.getInstance()
        userPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                val token = task.result?.token
                val number = userPreferences.getString(getString(R.string.preference_key_number),"")
                val data = HashMap<String,Any?>()
                data["token"] = token
                if (number != null)
                    firestore.collection("tokens").document(number).set(data)
            })
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        startService(Intent(this,TempIncomingService::class.java))
    }

    private fun setTabsAndViewPager() {
        // creating logs tab
        val tabLogs = tabLayout.newTab()
        tabLogs.text = LOGS_TAB
        tabLogs.tag = LOGS_TAB

        // creating contacts tab
        val tabContacts = tabLayout.newTab()
        tabContacts.text = CONTACTS_TAB
        tabContacts.tag = CONTACTS_TAB

        // adding both tabs
        tabLayout.addTab(tabLogs)
        tabLayout.addTab(tabContacts)

        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        viewPagerAdapter = HomeViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = viewPagerAdapter
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.settings_home)
            startActivity(Intent(this,SettingsActivity::class.java))
        return super.onOptionsItemSelected(item)
    }
}