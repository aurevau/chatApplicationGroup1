package com.example.chatapplication.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.example.chatapplication.R
import com.example.chatapplication.adapter.DashboardActivityViewPagerAdapter

import com.example.chatapplication.databinding.ActivityDashboardBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {
    private val usersFragment = UsersFragment()
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var spinner: AppCompatSpinner

    private lateinit var viewPager: ViewPager2
    private lateinit var pageChangeCallback: ViewPager2.OnPageChangeCallback

    private lateinit var headerText: TextView
    private lateinit var bottomNav: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        headerText = binding.tvHeader
        bottomNav = binding.bottomNavigation
        spinner = binding.menuSpinner
        viewPager = binding.fragmentContainer

        val viewPagerAdapter = DashboardActivityViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
        pageChangeCallback = object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNav.menu[position].isChecked = true
            }
        }

        viewPager.registerOnPageChangeCallback(pageChangeCallback)


        val menuCategories = resources.getStringArray(R.array.menu_spinner)

        val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, menuCategories)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) return
                if (menuCategories[position] == "Logout") {
                    finish()
//                    FirebaseUtil.logout()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_allChats -> {
                    headerText.text = getString(R.string.chats)
                    viewPager.currentItem = 0
                    true
                }
                R.id.navigation_allUsers -> {
                    headerText.text = getString(R.string.users)
                    viewPager.currentItem = 1
                    true
                }
                else -> false
            }
        }

    }
}