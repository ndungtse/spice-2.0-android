package org.medtroniclabs.uhis.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.snackbar.Snackbar
import org.medtroniclabs.uhis.BuildConfig
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsUtils
import org.medtroniclabs.uhis.appextensions.isFineAndCoarseLocationPermissionGranted
import org.medtroniclabs.uhis.appextensions.isGpsEnabled
import org.medtroniclabs.uhis.appextensions.loadAsGif
import org.medtroniclabs.uhis.appextensions.resetImageView
import org.medtroniclabs.uhis.appextensions.setVisible
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.DefinedParams.REFRESH_FRAGMENT
import org.medtroniclabs.uhis.common.LocaleHelper
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.databinding.ActivityBaseBinding
import org.medtroniclabs.uhis.databinding.ErrorLayoutBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ncd.medicalreview.prescription.dialog.CommentsAlertDialog
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.network.utils.ConnectivityManager
import org.medtroniclabs.uhis.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
open class BaseActivity : SpiceRootActivity() {
    private lateinit var binding: ActivityBaseBinding

    private val baseViewModel: BaseViewModel by viewModels()

    private var downX: Int = 0

    /**
     * Boolean variable holding whether translation is enabled or not for the app.
     * If it is true that means user is using the app in second language (other than english)
     */
    protected var isTranslationEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isTranslationEnabled = SecuredPreference.getIsTranslationEnabled()
        CommonUtils.applyInsets(
            this,
            binding.root,
            binding.fakeStatusBar,
            binding.fakeNavBar,
            false,
        )
        UserDetail.startDateTime = AnalyticsUtils.getCurrentDateTimeInLocalTime()
        setListener()
    }

    fun setMainContentView(
        view: View,
        isToolbarVisible: Boolean = false,
        title: String? = null,
        // Pair(Home, Back)
        homeAndBackVisibility: Pair<Boolean?, Boolean?> = Pair(false, true),
        callback: (() -> Unit?)? = null,
        callbackHome: (() -> Unit?)? = null,
        homeIcon: Drawable? = null,
        callbackMore: ((view: View) -> Unit?)? = null,
    ) {
        binding.titleToolbar.maxLines = 2
        binding.titleToolbar.ellipsize = TextUtils.TruncateAt.END

        if (isToolbarVisible) {
            binding.toolbar.visibility = View.VISIBLE
        } else {
            binding.toolbar.visibility = View.GONE
        }
        title?.let {
            binding.titleToolbar.text = it
        }

        binding.frameBaseLayout.addView(view)

        homeAndBackVisibility.first?.let {
            binding.ivHome.visibility = checkVisibility(it)
        }

        homeAndBackVisibility.second?.let {
            binding.ivBack.visibility = checkVisibility(it)
        }

        binding.ivBack.safeClickListener {
            if (callback != null) {
                callback.invoke()
            } else {
                finish()
            }
        }
        if (homeIcon != null) {
            binding.ivHome.setImageDrawable(homeIcon)
        } else {
            binding.ivHome.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_img))
        }
        binding.ivHome.safeClickListener {
            if (homeIcon == null) {
                if (callbackHome == null) {
                    redirectToHome()
                } else {
                    callbackHome.invoke()
                }
            } else {
                callbackMore?.invoke(binding.ivHome)
            }
        }
    }

    private fun checkVisibility(isVisible: Boolean): Int = if (isVisible) View.VISIBLE else View.INVISIBLE

    fun redirectToHome() {
        baseViewModel.setUserJourney(AnalyticsDefinedParams.ONHOMEBUTTONTRIGGERED)
        startAsNewActivity(Intent(this, LandingActivity::class.java))
    }

    fun startAsNewActivity(intent: Intent) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun startActivityWithoutSplashScreen() {
        val intent = Intent(this, LandingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra(REFRESH_FRAGMENT, true)
        startActivity(intent)
        finish()
    }

    fun showVerticalMoreIcon(
        canVisible: Boolean,
        callback: ((view: View) -> Unit?)? = null,
    ) {
        binding.ivMore.setVisible(canVisible)
        binding.ivMore.safeClickListener {
            callback?.invoke(it)
        }
    }

    fun setTitle(title: String) {
        binding.titleToolbar.text = title
    }

    fun getString(): String = binding.titleToolbar.text.toString()

    fun hideHomeButton(status: Boolean) {
        if (status) {
            binding.ivHome.visibility = View.INVISIBLE
        } else {
            binding.ivHome.visibility = View.VISIBLE
        }
    }

    fun hideBackButton() {
        binding.ivBack.visibility = View.INVISIBLE
    }

    fun showBackButton() {
        binding.ivBack.visibility = View.VISIBLE
    }

    fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
        binding.loaderImage.apply {
            loadAsGif(R.drawable.loader_spice)
        }
    }

    fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
        binding.loaderImage.apply {
            resetImageView()
        }
    }

    fun showErrorSnackBar(
        text: String,
        default: Int = Snackbar.LENGTH_LONG,
        durationInMillis: Int? = null,
    ) {
        val rootView = findViewById<View>(android.R.id.content) ?: return
        val snackBar = Snackbar.make(rootView, "", default)
        val binding = ErrorLayoutBinding.inflate(layoutInflater)
        binding.tvErrorMessage.text = text
        snackBar.view.setBackgroundColor(Color.TRANSPARENT)
        val snackBarLayout = snackBar.view as ViewGroup
        snackBarLayout.setPadding(0, 0, 0, 0)
        snackBarLayout.addView(binding.root)
        durationInMillis?.let {
            snackBar.duration = durationInMillis
        }
        snackBar.show()
    }

    inline fun <reified fragment : Fragment> replaceFragmentInId(
        id: Int,
        bundle: Bundle? = null,
        tag: String? = null,
        isAdd: Boolean = false,
    ) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            if (isAdd) {
                add<fragment>(id, args = bundle, tag = tag)
            } else {
                replace<fragment>(
                    id,
                    args = bundle,
                    tag = tag,
                )
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            downX = event.rawX.toInt()
        }
        if (event.action == MotionEvent.ACTION_UP) {
            val v = currentFocus
            if (v is EditText) {
                val x = event.rawX.toInt()
                val y = event.rawY.toInt()
                // Was it a scroll - If skip all
                if (abs(downX - x) > 5) {
                    return super.dispatchTouchEvent(event)
                }
                val reducePx = 2
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                // Bounding box is too big, reduce it just a little bit
                outRect.inset(reducePx, reducePx)
                touchEvent(v, x, y, reducePx, outRect)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun touchEvent(
        v: EditText,
        x: Int,
        y: Int,
        reducePx: Int,
        outRect: Rect,
    ) {
        if (!outRect.contains(x, y)) {
            v.clearFocus()
            var touchTargetIsEditText = false
            // Check if another editText has been touched
            for (vi in v.rootView.touchables) {
                if (vi is EditText) {
                    val clickedViewRect = Rect()
                    vi.getGlobalVisibleRect(clickedViewRect)
                    // Bounding box is too big, reduce it just a little bit
                    clickedViewRect.inset(reducePx, reducePx)
                    if (clickedViewRect.contains(x, y)) {
                        touchTargetIsEditText = true
                        break
                    }
                }
            }
            if (!touchTargetIsEditText) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
            }
        }
    }

    fun showTurnOnGPSDialog(isNegativeButtonNeed: Boolean = false) {
        showErrorDialogue(
            title = getString(R.string.gps_disabled_title),
            message = getString(R.string.gps_disabled_message),
            positiveButtonName = getString(R.string.ok),
            isNegativeButtonNeed = isNegativeButtonNeed,
        ) {
            if (it) {
                val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(settingsIntent)
            }
        }
    }

    fun showAllowLocationServiceDialog(isNegativeButtonNeed: Boolean = false) {
        showErrorDialogue(
            title = getString(R.string.gps_disabled_title),
            message = getString(R.string.gps_disabled_message),
            positiveButtonName = getString(R.string.ok),
            isNegativeButtonNeed = isNegativeButtonNeed,
        ) {
            if (it) {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts(
                    "package",
                    BuildConfig.APPLICATION_ID,
                    null,
                )
                intent.data = uri
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        connectivityManager.registerConnectionObserver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterConnectionObserver(this)
    }

    private fun setListener() {
        binding.loadingProgress.safeClickListener {
        }
    }

    inline fun <reified F : Fragment> replaceFragmentIfExists(
        id: Int,
        bundle: Bundle? = null,
        tag: String? = null,
    ) {
        val existingFragment = supportFragmentManager.findFragmentByTag(tag)

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            if (existingFragment != null) {
                // Fragment exists, replace it
                replace(id, existingFragment, tag)
            } else {
                // Fragment does not exist, create a new instance and replace it
                replace<F>(id, args = bundle, tag = tag)
            }
        }
    }

    inline fun <reified fragment : Fragment> replaceFragmentOrCreateNewFragment(
        id: Int,
        bundle: Bundle? = null,
        tag: String? = null,
        isAdd: Boolean = false,
    ) {
        val fragmentManager = supportFragmentManager
        val existingFragment = fragmentManager.findFragmentByTag(tag)

        if (existingFragment == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                if (isAdd) {
                    add<fragment>(id, args = bundle, tag = tag)
                } else {
                    replace<fragment>(
                        id,
                        args = bundle,
                        tag = tag,
                    )
                }
            }
        }
    }

    inline fun <reified F : Fragment> FragmentActivity.createNewFragmentOnly(
        containerId: Int,
        bundle: Bundle? = null,
        tag: String? = null,
        isAdd: Boolean = false,
    ) {
        val fragmentManager = supportFragmentManager

        fragmentManager.commit {
            setReorderingAllowed(true)

            // Always remove existing fragment by tag
            tag?.let { tagStr ->
                fragmentManager.findFragmentByTag(tagStr)?.let { existingFragment ->
                    remove(existingFragment)
                }
            }

            // Always create a new instance
            if (isAdd) {
                add<F>(containerId, args = bundle, tag = tag)
            } else {
                replace<F>(containerId, args = bundle, tag = tag)
            }
        }
    }

    fun getFragmentById(
        fragmentManager: FragmentManager,
        fragmentId: Int,
    ): Fragment? = fragmentManager.findFragmentById(fragmentId)

    fun withNetworkCheck(
        connectivityManager: ConnectivityManager,
        onNetworkAvailable: () -> Unit,
        onNetworkNotAvailable: (() -> Unit?)? = null,
    ) {
        if (connectivityManager.isNetworkAvailable()) {
            onNetworkAvailable()
        } else {
            showErrorDialogue(
                getString(R.string.error),
                getString(R.string.no_internet_error),
                isNegativeButtonNeed = false,
            ) {
                if (it && onNetworkNotAvailable != null) {
                    onNetworkNotAvailable()
                }
                hideLoading()
            }
        }
    }

    fun withNetworkAvailability(
        online: () -> Unit,
        offline: () -> Unit = {},
        isErrorShow: Boolean = true,
    ) {
        connectivityManager.isNullableNetworkAvailable()?.let { isNetworkAvailable ->
            if (isNetworkAvailable) {
                online()
            } else {
                if (isErrorShow) {
                    showErrorDialogue(
                        getString(R.string.error),
                        getString(R.string.no_internet_error),
                        isNegativeButtonNeed = false,
                    ) {}
                }
                offline()
            }
        }
    }

    fun <T> handleResourceState(
        resourceState: Resource<T>,
        onSuccess: () -> Unit,
        onSuccessParam: (T) -> Unit = {},
        onBackPressPopStack: () -> Unit,
        onError: () -> Unit = {
            showErrorDialogue(
                title = getString(R.string.alert),
                message = getString(R.string.something_went_wrong_try_later),
                positiveButtonName = getString(R.string.ok),
            ) {
                if (it) {
                    onBackPressPopStack()
                }
            }
        },
    ) {
        when (resourceState.state) {
            ResourceState.LOADING -> {
                showLoading()
            }

            ResourceState.ERROR -> {
                hideLoading()
                onError()
            }

            ResourceState.SUCCESS -> {
                hideLoading()
                onSuccess()
                resourceState.data?.let(onSuccessParam)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.mypatient_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = super.onOptionsItemSelected(item)

    fun onHomeClick(callbackHome: (() -> Unit?)? = null) {
        binding.ivHome.safeClickListener {
            if (callbackHome == null) {
                redirectToHome()
            } else {
                callbackHome.invoke()
            }
        }
    }

    fun hiddenBackButton() {
        binding.ivBack.visibility = checkVisibility(false)
    }

    fun addOrReuseFragment(
        containerId: Int,
        fragmentTag: String,
        fragmentInstance: Fragment,
        bundle: Bundle? = null,
    ) {
        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        val transaction = supportFragmentManager.beginTransaction()
        if (existingFragment == null) {
            // Add bundle if provided
            if (bundle != null) {
                fragmentInstance.arguments = bundle
            }
            transaction
                .add(containerId, fragmentInstance, fragmentTag)
        }
        transaction.commit()
    }

    fun replaceFragment(
        containerId: Int,
        fragmentTag: String,
        fragment: Fragment,
    ) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        // Check if the fragment with the given tag already exists
        val existingFragment = fragmentManager.findFragmentByTag(fragmentTag)

        // If the fragment exists, remove it
        if (existingFragment != null) {
            transaction.remove(existingFragment)
        }

        // Add a new instance of the fragment
        transaction.replace(containerId, fragment, fragmentTag)

        // Commit the transaction
        transaction.commit()
    }

    fun showAlertDialogWithComments(
        title: String? = null,
        message: String,
        isNegativeButtonNeed: Boolean = false,
        showComment: Boolean = true,
        errorMessage: String = getString(R.string.default_user_input_error),
        buttonName: Pair<String, String> = Pair(
            getString(R.string.ok),
            getString(R.string.cancel),
        ),
        callback: ((isPositiveResult: Boolean, reason: String?) -> Unit),
    ) {
        val dialog = CommentsAlertDialog.newInstance(
            this,
            title,
            isNegativeButtonNeed,
            Pair(buttonName.first, buttonName.second),
            callback = callback,
            showComment = showComment,
            message = Pair(message, errorMessage),
        )
        dialog.show(supportFragmentManager, CommentsAlertDialog.TAG)
    }

    fun setRedRiskPatient(isRedRiskPatient: Boolean?) {
        binding.ivRedAlert.setVisible(isRedRiskPatient == true)
        binding.tvRedAlert.setVisible(isRedRiskPatient == true)
    }

    override fun attachBaseContext(newBase: Context?) {
        try {
            super.attachBaseContext(
                newBase?.let {
                    LocaleHelper.onAttach(
                        it,
                        org.medtroniclabs.uhis.common.CommonUtils
                            .parseUserLocale(),
                    )
                },
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun withLocationCheck(
        onLocationAvailable: () -> Unit,
        onLocationNotAvailable: (() -> Unit)? = null,
    ) {
        when {
            !isGpsEnabled() -> {
                showTurnOnGPSDialog()
                onLocationNotAvailable?.invoke()
            }

            !isFineAndCoarseLocationPermissionGranted() -> {
                requestLocationPermissions { permissionsGranted ->
                    if (permissionsGranted) {
                        onLocationAvailable()
                    } else {
                        showErrorDialogue(
                            title = getString(R.string.gps_disabled_title),
                            message = getString(R.string.gps_disabled_message),
                            positiveButtonName = getString(R.string.ok),
                        ) {
                            if (it) {
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri = Uri.fromParts(
                                    "package",
                                    BuildConfig.APPLICATION_ID,
                                    null,
                                )
                                intent.data = uri
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                onLocationNotAvailable?.invoke()
                            }
                        }
                    }
                }
            }

            else -> onLocationAvailable()
        }
    }

    private var locationPermissionResultCallback: ((Boolean) -> Unit)? = null

    private fun requestLocationPermissions(onResult: (Boolean) -> Unit) {
        locationPermissionResultCallback = onResult
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    // Permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val finePermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarsePermission = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            locationPermissionResultCallback?.invoke(finePermission && coarsePermission)
        }

    inline fun <reified T : DialogFragment> FragmentActivity.showDialogIfNotPresent(
        tag: String,
        dialogProvider: () -> T,
    ) {
        val existingFragment = supportFragmentManager.findFragmentByTag(tag)
        if (existingFragment == null) {
            dialogProvider().show(supportFragmentManager, tag)
        }
    }
}
