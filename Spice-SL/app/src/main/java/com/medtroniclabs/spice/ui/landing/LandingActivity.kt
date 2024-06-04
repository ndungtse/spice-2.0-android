package com.medtroniclabs.spice.ui.landing

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.medtroniclabs.spice.BuildConfig
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityLandingBinding
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.boarding.LoginActivity
import com.medtroniclabs.spice.ui.home.HomeScreenFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientSearchFragment


class LandingActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    DrawerLayout.DrawerListener, View.OnClickListener, OnDialogDismissListener {

    lateinit var binding: ActivityLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }

        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        val isLoggedIn =
            SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.ISLOGGEDIN.name) || SecuredPreference.getBoolean(
                SecuredPreference.EnvironmentKey.ISOFFLINELOGIN.name
            )
        val isMetaLoaded =
            SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.ISMETALOADED.name)

        if (!(isLoggedIn && isMetaLoaded)) {
            startActivity(Intent(this, LoginActivity::class.java))
            splashScreen.setKeepOnScreenCondition { false }
            finish()
            return
        }

        // Check is any offline sync is pending
        val isAnySyncInProgress =
            SecuredPreference.getStringArray(SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
        if (isAnySyncInProgress != null) {
            startActivity(Intent(this, OfflineSyncActivity::class.java))
            splashScreen.setKeepOnScreenCondition { false }
        }

        binding = ActivityLandingBinding.inflate(layoutInflater)
        splashScreen.setKeepOnScreenCondition { false }
        setContentView(binding.root)
        initializeDrawerView()
        updateSideBarFooter()
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
        val menu: Menu = binding.navView.menu
        val menuItemToRemove: MenuItem? = menu.findItem(R.id.offline_sync)
        if (CommonUtils.isProvider() && menuItemToRemove != null) {
            menu.removeItem(menuItemToRemove.itemId)
        }
        onNavigationItemSelected(binding.navView.menu.findItem(R.id.home))
        val toolBar = binding.appBarMain.toolbar
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
                showErrorDialogue(
                    getString(R.string.alert),
                    getString(R.string.logout_alert),
                    positiveButtonName = getString(R.string.yes),
                    cancelBtnName = getString(R.string.no),
                    isNegativeButtonNeed = true
                ) { isPositive ->
                    if (isPositive && SecuredPreference.logout()) {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        val homeMenuItem = binding.navView.menu.findItem(R.id.home)
                        selectNavigationMenu(homeMenuItem)
                    }
                }
            }

            R.id.profile -> {
                val profileDialogFragment =
                    supportFragmentManager.findFragmentByTag(ProfileDialogFragment.TAG)
                profileDialogFragment ?: ProfileDialogFragment.newInstance()
                    .show(supportFragmentManager, ProfileDialogFragment.TAG)
            }

            R.id.offline_sync -> {
                startActivity(Intent(this, OfflineSyncActivity::class.java))
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.privacy_policy ->{
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                val homeMenuItem = binding.navView.menu.findItem(R.id.home)
                selectNavigationMenu(homeMenuItem)
                return true
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
                if (CommonUtils.isProvider()) {
                    binding.appBarMain.tvTitle.text = getString(R.string.search_patient)
                    replaceFragmentInId<PatientSearchFragment>(
                        R.id.fragmentContainerView,
                        tag = PatientSearchFragment.TAG
                    )
                } else {
                    binding.appBarMain.tvTitle.text = getString(R.string.home_title)
                    replaceFragmentInId<HomeScreenFragment>(
                        R.id.fragmentContainerView,
                        tag = HomeScreenFragment.TAG
                    )
                }
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

    override fun onDialogDismissListener(isFinish: Boolean) {
        val homeMenuItem = binding.navView.menu.findItem(R.id.home)
        onNavigationItemSelected(homeMenuItem)
    }
}