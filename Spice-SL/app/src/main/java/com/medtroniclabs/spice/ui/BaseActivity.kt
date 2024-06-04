package com.medtroniclabs.spice.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.snackbar.Snackbar
import com.medtroniclabs.spice.BuildConfig
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityBaseBinding
import com.medtroniclabs.spice.databinding.ErrorLayoutBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
open class BaseActivity : SpiceRootActivity() {
    private lateinit var binding: ActivityBaseBinding

    @Inject
    lateinit var connectivityManager: ConnectivityManager


    private var downX: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListener()
    }



    fun setMainContentView(
        view: View,
        isToolbarVisible: Boolean = false,
        title: String? = null,
        //Pair(Home, Back)
        homeAndBackVisibility: Pair<Boolean?, Boolean?> = Pair(false, true),
        callback: (() -> Unit?)? = null,
        callbackHome: (() -> Unit?)? = null,
        homeIcon: Drawable? = null,
        callbackMore: ((view: View) -> Unit?)? = null,
    ) {
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
            if (callback != null)
                callback.invoke()
            else
                finish()
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

    /**
     * Receiver for session expired broadcasts from [Retrofit API].
     */


    private fun checkVisibility(isVisible: Boolean): Int {
        return if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    private fun redirectToHome() {
        startAsNewActivity(Intent(this, LandingActivity::class.java))
    }

    fun startAsNewActivity(intent: Intent) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun showVerticalMoreIcon(canVisible: Boolean, callback: ((view: View) -> Unit?)? = null) {
        binding.ivMore.visibility = if (canVisible) View.VISIBLE else View.GONE
        binding.ivMore.safeClickListener {
            callback?.invoke(it)
        }
    }

    fun setTitle(title: String) {
        binding.titleToolbar.text = title
    }

    fun getString(): String {
        return binding.titleToolbar.text.toString()
    }

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
    }

    fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }

    fun showErrorSnackBar(
        text: String,
    ) {
        val rootView = findViewById<View>(android.R.id.content) ?: return
        val snackBar = Snackbar.make(rootView, "", Snackbar.LENGTH_LONG)
        val binding = ErrorLayoutBinding.inflate(layoutInflater)
        binding.tvErrorMessage.text = text
        snackBar.view.setBackgroundColor(Color.TRANSPARENT)
        val snackBarLayout = snackBar.view as Snackbar.SnackbarLayout
        snackBarLayout.setPadding(0, 0, 0, 0)
        snackBarLayout.addView(binding.root)
        snackBar.show()
    }

    inline fun <reified fragment : Fragment> replaceFragmentInId(
        id: Int,
        bundle: Bundle? = null,
        tag: String? = null,
        isAdd: Boolean= false
    ) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            if (isAdd)
                add<fragment>(id , args = bundle, tag = tag)
            else
                replace<fragment>(
                id,
                args = bundle,
                tag = tag
            )
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
                //Was it a scroll - If skip all
                if (abs(downX - x) > 5) {
                    return super.dispatchTouchEvent(event)
                }
                val reducePx = 2
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                //Bounding box is to big, reduce it just a little bit
                outRect.inset(reducePx, reducePx)
                touchEvent(v, x, y, reducePx, outRect)
            }
        }
        return super.dispatchTouchEvent(event)
    }


    private fun touchEvent(v: EditText, x: Int, y: Int, reducePx: Int, outRect: Rect) {
        if (!outRect.contains(x, y)) {
            v.clearFocus()
            var touchTargetIsEditText = false
            //Check if another editText has been touched
            for (vi in v.rootView.touchables) {
                if (vi is EditText) {
                    val clickedViewRect = Rect()
                    vi.getGlobalVisibleRect(clickedViewRect)
                    //Bounding box is to big, reduce it just a little bit
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

    fun showTurnOnGPSDialog() {
        showErrorDialogue(
            title = getString(R.string.gps_disabled_title),
            message = getString(R.string.gps_disabled_message),
            positiveButtonName = getString(R.string.ok),
        ) {
            if (it) {
                val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(settingsIntent)
            }
        }
    }

    fun showAllowLocationServiceDialog() {
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
                    null
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
}