package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams.IS_CD4
import com.medtroniclabs.spice.common.DefinedParams.IS_CD4_PERCENTAGE
import com.medtroniclabs.spice.data.resource.CD4DetailsResponse
import com.medtroniclabs.spice.databinding.FragmentCD4DialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel

class CD4DialogFragment : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentCD4DialogBinding
    private lateinit var cd4Adapter: CD4Adapter
    private val viewModel: HivViewModel by activityViewModels()
    private val patientViewModel: PatientDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCD4DialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.hivCD4DetailLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    binding.loadingProgress.visible()
                }

                ResourceState.SUCCESS -> {
                    binding.loadingProgress.gone()
                    resource.data?.let { cd4Details ->
                        if (cd4Details.isNotEmpty()) {
                            binding.rvBmiList.visible()
                            binding.tvNoHistory.gone()
                            initAdapter(cd4Details)
                        } else {
                            binding.rvBmiList.gone()
                            binding.tvNoHistory.visible()
                        }
                    }
                }

                ResourceState.ERROR -> {
                    binding.loadingProgress.gone()
                }
            }
        }
    }

    companion object {
        const val TAG = "CD4DialogFragment"

        fun newInstance(
            isCD4: Boolean,
            isCD4Percentage: Boolean,
        ): CD4DialogFragment {
            val args = Bundle()
            args.putBoolean(IS_CD4, isCD4)
            args.putBoolean(IS_CD4_PERCENTAGE, isCD4Percentage)
            val fragment = CD4DialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private fun initView() {
        binding.ivClose.safeClickListener(this)
        binding.btnClose.safeClickListener(this)
        binding.tvCd4.text = if (arguments?.getBoolean(IS_CD4) == true) {
            getString(R.string.cd4)
        } else {
            getString(R.string.cd4_percentage_symbol)
        }
        binding.tvBmiLabel.text = if (arguments?.getBoolean(IS_CD4) == true) {
            getString(R.string.cd4)
        } else {
            getString(R.string.cd4_percentage_symbol)
        }
        patientViewModel.patientDetailsLiveData.value?.data?.let { response ->
            viewModel.getHivCD4Details(
                response.id,
                isCD4 = arguments?.getBoolean(IS_CD4) ?: false,
                isCD4Percentage = arguments?.getBoolean(
                    IS_CD4_PERCENTAGE,
                ) ?: false,
            )
        }
    }

    private fun initAdapter(cd4List: ArrayList<CD4DetailsResponse>) {
        cd4Adapter = CD4Adapter(
            cd4List,
            isCD4 = arguments?.getBoolean(IS_CD4) ?: false,
            isCD4Percentage = arguments?.getBoolean(
                IS_CD4_PERCENTAGE,
            ) ?: false,
        )
        binding.rvBmiList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBmiList.adapter = cd4Adapter
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
            }

            R.id.ivClose -> {
                dismiss()
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
}
