package com.medtroniclabs.spice.ncd.counseling.activity

import android.os.Bundle
import com.medtroniclabs.spice.databinding.ActivityNcdCounselorBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.assessment.dialog.BPLogListDialog
import com.medtroniclabs.spice.ui.BaseActivity

class NCDCounselorActivity : BaseActivity() {

    private lateinit var binding: ActivityNcdCounselorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdCounselorBinding.inflate(layoutInflater)
        setMainContentView(binding.root, isToolbarVisible = true)

        binding.btnAdd.safeClickListener {
            val ncdCounselingDialog = NCDCounselingDialog.newInstance { }
            ncdCounselingDialog.show(supportFragmentManager, NCDCounselingDialog.TAG)
        }
    }
}