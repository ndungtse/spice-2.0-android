package com.mdtlabs.ncd.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mdtlabs.ncd.databinding.ActivityBaseBinding

/**
 * Base activity for all activity
 */
open class BaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListener()
    }

    private fun setListener() {
        binding.loadingProgress.setOnClickListener {

        }
    }


    fun startAsNewActivity(intent: Intent) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    /**
     * method to load screen layout
     * @param view child layout root
     */
    fun setMainContentView(view: View) {
        binding.baseFlContent.addView(view)
    }


    fun showLoading(){
        binding.loadingProgress.visibility = View.VISIBLE
    }

    fun hideLoading(){
        binding.loadingProgress.visibility = View.GONE
    }

}