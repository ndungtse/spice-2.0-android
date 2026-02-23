package com.medtroniclabs.spice.ui.medicalreview.tb.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.loadAsGif
import com.medtroniclabs.spice.appextensions.resetImageView
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.databinding.FragmentBmiListDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.ui.medicalreview.tb.BmiListAdapter
import com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel.BmiViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BMIListDialog : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentBmiListDialogBinding
    private val viewModel: BmiViewModel by activityViewModels()
    private val adapter by lazy { BmiListAdapter() }

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBmiListDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    companion object {
        const val TAG = "BMIListDialog"

        fun newInstance(memberID: String?) =
            BMIListDialog().apply {
                this.arguments = Bundle().apply {
                    putString(DefinedParams.MemberID, memberID)
                }
            }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
        viewModel.setUserJourney(AnalyticsDefinedParams.BMIHistoryDialogue)
    }

    fun initView() {
        binding.ivClose.safeClickListener(this)
        binding.btnClose.safeClickListener(this)
        viewModel.fetchBmiList(MotherNeonateAncRequest(memberId = getMemberId()))
        binding.rvBmiList.gone()
        binding.tvNoHistory.visible()
        binding.rvBmiList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBmiList.adapter = adapter
        binding.loadingProgress.safeClickListener {
        }
    }

    private fun getMemberId(): String = arguments?.getString(DefinedParams.MemberID, "") ?: ""

    fun attachObserver() {
        viewModel.getBmiList.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.loadingProgress.visible()
                    binding.loaderImage.apply {
                        loadAsGif(R.drawable.loader_spice)
                    }
                }

                ResourceState.SUCCESS -> {
                    binding.loadingProgress.gone()
                    binding.loaderImage.apply {
                        resetImageView()
                    }
                    resourceState.data?.let {
                        if (it.isNotEmpty()) {
                            binding.rvBmiList.visible()
                            binding.tvNoHistory.gone()
                            adapter.setData(it.toCollection(arrayListOf()))
                        }
                    } ?: kotlin.run {
                        binding.rvBmiList.gone()
                        binding.tvNoHistory.visible()
                    }
                }

                ResourceState.ERROR -> {
                    binding.loadingProgress.gone()
                    binding.loaderImage.apply {
                        resetImageView()
                    }
                    binding.rvBmiList.gone()
                    binding.tvNoHistory.visible()
                }
            }
        }
    }

    private fun handleDialogSize() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val width = if (CommonUtils.checkIsTablet(requireContext())) {
            if (isLandscape) 65 else 90
        } else {
            if (isLandscape) 65 else 90
        }
        setDialogPercent(width)
    }

    override fun onStart() {
        super.onStart()
        handleDialogSize()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleDialogSize()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnClose -> {
                dismiss()
                viewModel.setUserJourney(AnalyticsDefinedParams.CLOSEICONTRIGGERED)
            }

            R.id.ivClose -> {
                dismiss()
                viewModel.setUserJourney(AnalyticsDefinedParams.CLOSEICONTRIGGERED)
            }
        }
    }
}
