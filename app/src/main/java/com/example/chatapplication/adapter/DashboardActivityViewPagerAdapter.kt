package com.example.chatapplication.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chatapplication.ui.RecentChatsFragment
import com.example.chatapplication.ui.UsersFragment

class DashboardActivityViewPagerAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {
    private val usersFragment = UsersFragment()

    private val recentChatsFragment = RecentChatsFragment()

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> recentChatsFragment
            1 -> usersFragment
            else -> recentChatsFragment
        }
    }

    override fun getItemCount(): Int = 2

}