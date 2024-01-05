package com.medtroniclabs.spice.ui.landing

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.medtroniclabs.spice.BuildConfig
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityLandingBinding
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.home.HomeScreenFragment


class LandingActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    DrawerLayout.DrawerListener, View.OnClickListener {

    private lateinit var binding: ActivityLandingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initializeDrawerView()
        updateSideBarFooter()
    }

    private fun initView() {
        onNavigationItemSelected(binding.navView.menu.findItem(R.id.home))
    }

    private fun initializeDrawerView() {
        val toolBar = binding.appBarMain.toolbar
        setSupportActionBar(toolBar)
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        binding.drawerLayout.addDrawerListener(this)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
    }

    private fun updateSideBarFooter() {
        binding.appBarBottom.tvAppVersion.text =
            "${getString(R.string.app_version)} ${BuildConfig.VERSION_NAME}"
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
            }
        }
        selectNavigationMenu(item)
        displayScreen(item.itemId)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun displayScreen(id: Int) {
        when (id) {
            R.id.home -> {
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.clParentLayout,
                        HomeScreenFragment.newInstance(),
                        HomeScreenFragment.TAG
                    ).commit()
            }
        }
    }

    private fun selectNavigationMenu(item: MenuItem) {
        if (binding.navView.menu.size() > 0) {
            binding.navView.menu.forEach { menuItem ->
                menuItem.isChecked = (menuItem.itemId == item.itemId)
            }
        }
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
//        TODO("Not yet implemented")
    }

    override fun onDrawerOpened(drawerView: View) {
//        TODO("Not yet implemented")
    }

    override fun onDrawerClosed(drawerView: View) {
//        TODO("Not yet implemented")
    }

    override fun onDrawerStateChanged(newState: Int) {
//        TODO("Not yet implemented")
    }

    override fun onClick(v: View?) {
//        TODO("Not yet implemented")
    }
}