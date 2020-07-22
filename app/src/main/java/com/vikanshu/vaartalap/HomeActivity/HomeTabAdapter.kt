package com.vikanshu.vaartalap.HomeActivity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.vikanshu.vaartalap.HomeActivity.HomeFragments.ContactsFragment
import com.vikanshu.vaartalap.HomeActivity.HomeFragments.LogsFragment

class HomeViewPagerAdapter(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        if(position == 0)
            return LogsFragment()
        return ContactsFragment()
    }

    override fun getCount(): Int {
        return 2
    }

}