package com.medtroniclabs.spice.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityBaseBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

    private fun checkVisibility(isVisible: Boolean): Int {
        return if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    fun redirectToHome() {
        startAsNewActivity(Intent(this, LandingActivity::class.java))
    }

    fun startAsNewActivity(intent: Intent) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun showVerticalMoreIcon(canVisible: Boolean, callback: ((view: View) -> Unit?)? = null) {
        binding.ivMore.visibility = View.GONE
        binding.ivMore.safeClickListener {
            callback?.invoke(it)
        }
    }

    fun setTitle(title: String) {
        binding.titleToolbar.text = title
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

}