package com.medtroniclabs.spice.ui.landing

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.navigation.NavigationView
import com.medtroniclabs.spice.BuildConfig
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.cancelAllWorker
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.isVisible
import com.medtroniclabs.spice.appextensions.scheduleSyncWorker
import com.medtroniclabs.spice.appextensions.syncWorkerName
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.app.analytics.upload.UploadWorker
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils.getAppVersion
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils.getFileUploadTime
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils.updateUserIdIfEmpty
import com.medtroniclabs.spice.app.analytics.utils.UserDetail
import com.medtroniclabs.spice.appextensions.convertToLocalDateTime
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams.REFRESH_FRAGMENT
import com.medtroniclabs.spice.common.RoleConstant
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityLandingBinding
import com.medtroniclabs.spice.network.NetworkConstants
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.ChooseSiteDialogueFragment
import com.medtroniclabs.spice.ui.boarding.LoginActivity
import com.medtroniclabs.spice.ui.home.HomeScreenFragment
import com.medtroniclabs.spice.ui.landing.viewmodel.LandingViewModel
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientSearchFragment
import java.util.concurrent.TimeUnit


class LandingActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    DrawerLayout.DrawerListener, View.OnClickListener, OnDialogDismissListener {

    lateinit var binding: ActivityLandingBinding

    private val viewModel: LandingViewModel by viewModels()
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

        // Initiate schedule worker
        startSyncWorker()

        binding = ActivityLandingBinding.inflate(layoutInflater)
        splashScreen.setKeepOnScreenCondition { false }
        setContentView(binding.root)
        initializeDrawerView()
        updateSideBarFooter()
        schedulePeriodicUploadWork(this)
        UserDetail.updateUserIdIfEmpty(SecuredPreference.getUserId().toString())
        UserDetail.getAppVersion(BuildConfig.VERSION_NAME)
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backHandelFlow()
            }
        }

    private fun backHandelFlow() {
        viewModel.setAnalyticsData(
            UserDetail.startDateTime,
            eventName = AnalyticsDefinedParams.HouseholdCreation,
            exitReason = AnalyticsDefinedParams.CancelButtonClicked,
            isCompleted = false
        )
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            finish()
        }
    }

    private fun startSyncWorker() {
        val userRole = SecuredPreference.getUserDetails().roles.joinToString { it.name }
        if (userRole.contains(RoleConstant.COMMUNITY_HEALTH_WORKER)) {
            scheduleSyncWorker()
            checkBGSyncStatus()
        }
    }

    private fun initializeDrawerView() {
        val menu: Menu = binding.navView.menu
        val menuItemToRemove: MenuItem? = menu.findItem(R.id.offline_sync)
        if ((!CommonUtils.isChw()) && menuItemToRemove != null) {
            menu.removeItem(menuItemToRemove.itemId)
        }

        val changeFacilityMenuItem: MenuItem? = menu.findItem(R.id.changeFacility)

        if ( !CommonUtils.isProvider()  && changeFacilityMenuItem != null) {
            menu.removeItem(changeFacilityMenuItem.itemId)
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
        binding.appBarBottom.tvAppVersion.text = this.getString(
            R.string.firstname_lastname,
            getString(R.string.app_version),
            getBuildVersion()
        )
    }

    private fun getBuildVersion(): String {
        val index = BuildConfig.VERSION_NAME.indexOf('-')
        return if (index != -1) {
            BuildConfig.VERSION_NAME.substring(0, index)
        } else {
            BuildConfig.VERSION_NAME
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                if (binding.appBarMain.includeMainContent.syncingHolder.isVisible()) {
                    showSyncInProgressWarning()
                    return true
                } else {
                    showErrorDialogue(
                        getString(R.string.alert),
                        getString(R.string.logout_alert),
                        positiveButtonName = getString(R.string.yes),
                        cancelBtnName = getString(R.string.no),
                        isNegativeButtonNeed = true
                    ) { isPositive ->
                        if (isPositive && SecuredPreference.logout()) {
                            cancelAllWorker()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            val homeMenuItem = binding.navView.menu.findItem(R.id.home)
                            selectNavigationMenu(homeMenuItem)
                        }
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
                goToOfflineSyncPage()
                return true
            }

            R.id.privacy_policy -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                val homeMenuItem = binding.navView.menu.findItem(R.id.home)
                selectNavigationMenu(homeMenuItem)
                return true
            }

            R.id.changeFacility -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                val chooseSiteDialogueFragment = ChooseSiteDialogueFragment.newInstance()
                chooseSiteDialogueFragment.show(
                    supportFragmentManager,
                    ChooseSiteDialogueFragment.TAG
                )
                return true
            }
        }
        selectNavigationMenu(item)
        displayScreen(item.itemId)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun goToOfflineSyncPage() {
        if (binding.appBarMain.includeMainContent.syncingHolder.isVisible()) {
            showSyncInProgressWarning()
        } else {
            startActivity(Intent(this, OfflineSyncActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun showSyncInProgressWarning() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.background_sync_in_progress),
            isNegativeButtonNeed = false
        ) {}
    }

    private fun displayScreen(id: Int) {
        when (id) {
            R.id.home -> {
                if (CommonUtils.isRolePresent()) {
                    binding.appBarMain.tvTitle.text = getString(R.string.search_patient)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    replaceFragmentIfExists<PatientSearchFragment>(
                        R.id.fragmentContainerView,
                        bundle = null,
                        tag = PatientSearchFragment.TAG
                    )
                } else {
                    binding.appBarMain.tvTitle.text = getString(R.string.home_title)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val refreshFragment =
            intent.getBooleanExtra(REFRESH_FRAGMENT, false)
        if (refreshFragment == true) {
            val fragment = supportFragmentManager.findFragmentByTag(PatientSearchFragment.TAG)
            fragment?.let {
                supportFragmentManager.beginTransaction().remove(it).commit()
            }
            replaceFragmentInId<PatientSearchFragment>(
                R.id.fragmentContainerView,
                tag = PatientSearchFragment.TAG
            )
        }
    }

    private fun checkBGSyncStatus() {
        val workManager = WorkManager.getInstance(this)
        workManager.getWorkInfosForUniqueWorkLiveData(syncWorkerName).observe(this) {
            if (!it.isNullOrEmpty() && it[0].state == WorkInfo.State.RUNNING) {
                binding.appBarMain.includeMainContent.syncingHolder.visible()
            } else {
                binding.appBarMain.includeMainContent.syncingHolder.gone()
            }
        }
    }

    private fun schedulePeriodicUploadWork(context: Context) {
        val periodicWorkRequest = PeriodicWorkRequest.Builder(
            UploadWorker::class.java, 1, TimeUnit.DAYS
        ).apply {
            setInputData(periodicUploaderInputData())
            setInitialDelay(getFileUploadTime(), TimeUnit.MILLISECONDS)
            setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
        }.build()

        WorkManager.getInstance(context).enqueue(periodicWorkRequest)
    }

    private fun periodicUploaderInputData(): Data {
        return Data.Builder().apply {
            putString(
                DefinedParams.BaseUrl,
                NetworkConstants.BASE_URL
            )
            putAll(
                mapOf(
                    DefinedParams.Authorization to SecuredPreference.getString(
                        SecuredPreference.EnvironmentKey.TOKEN.toString()
                    )
                )
            )
            putString(
                DefinedParams.LastSyncDate,
                lastSyncDate()
            )
        }.build()
    }

    private fun lastSyncDate(): String {
        val longSyncedAt =
            SecuredPreference.getLong(SecuredPreference.EnvironmentKey.LAST_SYNCED_AT.name)
        return if (longSyncedAt != 0L) longSyncedAt.convertToLocalDateTime() else "--"
    }
}