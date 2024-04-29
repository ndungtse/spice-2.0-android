package com.medtroniclabs.spice.ui.mypatient.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setDialogWidthAndHeightAsWrapPercent
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentFollowUpDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.mypatient.viewmodel.FollowUpViewModel

class FollowUpDialogFragment : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentFollowUpDialogBinding
    private val viewModel: FollowUpViewModel by activityViewModels()
    private var menuType: String? = null

    companion object {
        const val TAG = "FollowUpDialogFragment"
        fun newInstance(assessmentType: String): FollowUpDialogFragment {
            val fragment = FollowUpDialogFragment()
            val bundle = Bundle().apply {
                putString("menuType", assessmentType)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            menuType = it.getString("menuType", DefinedParams.HH_VISIT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowUpDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        applyOrientationChange()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.btnCall.safeClickListener(this)
        binding.ivClose.safeClickListener(this)
        with(binding) {
            btnAssessment.visibility =
                if (menuType == DefinedParams.HH_VISIT) View.VISIBLE else View.GONE
        }
    }

    private fun applyOrientationChange() {
        val tabletSize =
            resources.getBoolean(R.bool.isLargeTablet) || resources.getBoolean(R.bool.isTablet)
        if (tabletSize) {
            forPhoneAndTab(65, 75)
        } else {
            forPhoneAndTab(65, 90)
        }

    }

    fun forPhoneAndTab(landscape: Int, portrait: Int) {
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                setDialogWidthAndHeightAsWrapPercent(landscape)
            }

            Configuration.ORIENTATION_PORTRAIT -> {
                setDialogWidthAndHeightAsWrapPercent(portrait)
            }
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivClose -> dismiss()
            R.id.btnCall -> {
                CallResultDialogFragment.newInstance().show(childFragmentManager, CallResultDialogFragment.TAG)
            }

            R.id.btnAssessment -> {
                // Handle button click
            }
        }
    }
}