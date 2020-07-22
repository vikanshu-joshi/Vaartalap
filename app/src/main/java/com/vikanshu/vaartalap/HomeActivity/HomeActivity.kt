package com.vikanshu.vaartalap.HomeActivity

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.vikanshu.vaartalap.R

class HomeActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var viewPagerAdapter: HomeViewPagerAdapter
    private val LOGS_TAB = "Logs"
    private val CONTACTS_TAB = "Contacts"


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
}