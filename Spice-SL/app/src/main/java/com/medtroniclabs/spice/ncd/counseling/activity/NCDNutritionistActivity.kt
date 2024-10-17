package com.medtroniclabs.spice.ncd.counseling.activity

import android.os.Bundle
import com.medtroniclabs.spice.databinding.ActivityNcdNutritionistBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.assessment.dialog.BPLogListDialog
import com.medtroniclabs.spice.ui.BaseActivity

class NCDNutritionistActivity : BaseActivity() {

    private lateinit var binding: ActivityNcdNutritionistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdNutritionistBinding.inflate(layoutInflater)
        setMainContentView(binding.root, isToolbarVisible = true)

        binding.btnAdd.safeClickListener {
            val ncdLifestyleDialog = NCDLifestyleDialog.newInstance { }
            ncdLifestyleDialog.show(supportFragmentManager, NCDLifestyleDialog.TAG)
        }
    }
}