package com.example.instagramclone.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.instagramclone.R
import com.example.instagramclone.fragment.FeedFragment
import com.example.instagramclone.fragment.PostFragment
import com.example.instagramclone.fragment.ProfileFragment
import com.example.instagramclone.fragment.SearchFragment
import com.example.instagramclone.helper.FirebaseConfig
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    private val auth = FirebaseConfig.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // toolbar configuration
        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        toolbar.title = "Instagram"
        setSupportActionBar(toolbar)

        // bottom navigation view configuration
        this.configBottomNavigationView()

    }

    private fun navigateFragment(viewPager: Int, fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(viewPager, fragment).commit()
    }

    private fun configBottomNavigationView() {

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        enableBottomNavigation()
        navigateFragment(R.id.viewPager, FeedFragment())

    }

    private fun enableBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener {

            when (it.itemId) {

                R.id.ic_home -> {
                    navigateFragment(R.id.viewPager, FeedFragment())
                    true
                }

                R.id.ic_search -> {
                    navigateFragment(R.id.viewPager, SearchFragment())
                    true
                }

                R.id.ic_post -> {
                    navigateFragment(R.id.viewPager, PostFragment())
                    true
                }

                R.id.ic_profile -> {
                    navigateFragment(R.id.viewPager, ProfileFragment())
                    true
                }

                else -> {
                    false
                }

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                this.signOut()
                startActivity(Intent(applicationContext, LoginActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signOut() {
        try {
            auth.signOut()
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

}