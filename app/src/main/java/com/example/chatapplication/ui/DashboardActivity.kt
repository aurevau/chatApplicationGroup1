package com.example.chatapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.chatapplication.R
import com.example.chatapplication.adapter.DashboardActivityViewPagerAdapter

import com.example.chatapplication.databinding.ActivityDashboardBinding
import com.example.chatapplication.viewmodel.AuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class DashboardActivity : AppCompatActivity() {
    private val usersFragment = UsersFragment()
    private lateinit var binding: ActivityDashboardBinding

    private val auth = Firebase.auth

    private lateinit var viewPager: ViewPager2
    private lateinit var pageChangeCallback: ViewPager2.OnPageChangeCallback

    private lateinit var authViewModel: AuthViewModel

    private lateinit var headerText: TextView
    private lateinit var bottomNav: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        headerText = binding.tvHeader
        bottomNav = binding.bottomNavigation
        viewPager = binding.fragmentContainer

        val viewPagerAdapter = DashboardActivityViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNav.menu[position].isChecked = true
            }
        }

        viewPager.registerOnPageChangeCallback(pageChangeCallback)

        binding.loggedInUser.text = auth.currentUser?.email

        binding.dropdownMenu.setOnClickListener {
            val wrapper = ContextThemeWrapper(this, R.style.CustomPopupMenu)
            val popupMenu = PopupMenu(wrapper, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_menu -> {false}
                    R.id.menu_profile -> {
                        val intent = Intent(this@DashboardActivity, ProfileActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.menu_logout -> {
                        authViewModel.logOut()
                        // Start WelcomeActivity with CLEAR_TASK
                        val intent = Intent(this@DashboardActivity, WelcomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        true }
                    else -> false
                }
            }

            popupMenu.inflate(R.menu.menu_dropdown)

            try {
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popupMenu)
                mPopup.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(mPopup, true)
            } catch (e: Exception) {
                Log.e("SOUT", "Error showing menu icon: $e")
            } finally {
                popupMenu.show()
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_allChats -> {
                    headerText.text = getString(R.string.header_chats_text)
                    viewPager.currentItem = 0
                    true
                }

                R.id.navigation_allUsers -> {
                    headerText.text = getString(R.string.header_text_users)
                    viewPager.currentItem = 1
                    true
                }

                else -> false
            }
        }


    }
}