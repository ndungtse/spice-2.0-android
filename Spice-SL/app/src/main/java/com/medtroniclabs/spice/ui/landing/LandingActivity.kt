package com.medtroniclabs.spice.ui.landing

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.medtroniclabs.spice.BuildConfig
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityLandingBinding
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.boarding.LoginActivity
import com.medtroniclabs.spice.ui.home.HomeScreenFragment
import com.medtroniclabs.spice.ui.landing.viewmodel.LandingViewModel


class LandingActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    DrawerLayout.DrawerListener, View.OnClickListener, OnDialogDismissListener {

    lateinit var binding: ActivityLandingBinding

    private val viewModel: LandingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        if (!SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.ISLOGGEDIN.name) && !SecuredPreference.getBoolean(
                SecuredPreference.EnvironmentKey.ISMETALOADED.name
            )
        ) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeDrawerView()
        updateSideBarFooter()
        initView()
    }

    private fun initView() {
    }
    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backHandelFlow()
            }
        }

    private fun backHandelFlow() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            finish()
        }
    }

    private fun initializeDrawerView() {
        onNavigationItemSelected(binding.navView.menu.findItem(R.id.home))
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

            R.id.profile -> {
                val profileDialogFragment =
                    supportFragmentManager.findFragmentByTag(ProfileDialogFragment.TAG)
                profileDialogFragment ?: ProfileDialogFragment.newInstance()
                    .show(supportFragmentManager, ProfileDialogFragment.TAG)
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
            R.id.offline_sync -> {
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.clParentLayout,
                        OfflineSyncFragment(),
                        OfflineSyncFragment.TAG
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
    }

    override fun onDrawerOpened(drawerView: View) {
    }

    override fun onDrawerClosed(drawerView: View) {
    }

    override fun onDrawerStateChanged(newState: Int) {
    }

    override fun onClick(v: View) {
    }

    override fun onDialogDismissListener() {
        val homeMenuItem = binding.navView.menu.findItem(R.id.home)
        onNavigationItemSelected(homeMenuItem)
    }
}