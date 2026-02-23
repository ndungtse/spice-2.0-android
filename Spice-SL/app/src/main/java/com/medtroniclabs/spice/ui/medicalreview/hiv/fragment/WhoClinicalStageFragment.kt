package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentPatientStatusDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.viewmodel.DiagnosisViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.WhoClinicalStageViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.DialogDismissListenerForTb
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WhoClinicalStageFragment : DialogFragment(), View.OnClickListener {
    var listener: DialogDismissListenerForTb? = null
    private lateinit var binding: FragmentPatientStatusDialogBinding
    private val viewModel: WhoClinicalStageViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by activityViewModels()
    private val diagnosisViewModel: DiagnosisViewModel by activityViewModels()
    private lateinit var chipTag: TagListCustomView

    companion object {
        const val TAG = "WhoClinicalStageFragment"

        fun newInstance() = WhoClinicalStageFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPatientStatusDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        attachObservers()
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        SpiceLocationManager(requireContext()).getCurrentLocation {
            viewModel.lastLocation = it
        }
    }

    private fun attachObservers() {
        viewModel.getWhoStageLiveData.observe(viewLifecycleOwner) { list ->
            val chipList = list.map { item ->
                ChipViewItemModel(
                    id = item.id,
                    name = item.name,
                    type = item.type,
                    value = item.value,
                )
            }
            viewModel.whoStageCreateLiveData.value?.data?.let { response ->
                viewModel.whoStageChip =
                    ArrayList(chipList.filter { it.value.equals(response.stringValue, true) })
            } ?: kotlin.run {
                diagnosisViewModel.hivVitalsDetailLiveData.value?.data?.let { response ->
                    viewModel.whoStageChip = ArrayList(
                        chipList.filter {
                            it.name.equals(
                                response.whoClinicalStage,
                                true,
                            )
                        },
                    )
                }
            }

            initChipView(ArrayList(chipList))
        }

        viewModel.whoStageCreateLiveData.observe(viewLifecycleOwner) { resourcesState ->
            when (resourcesState.state) {
                ResourceState.LOADING -> {
                    binding.loader.visible()
                }

                ResourceState.SUCCESS -> {
                    binding.loader.gone()
                    patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
                        diagnosisViewModel.getHivVitalsDetails(
                            patientReference = details.id,
                            memberId = details.memberId,
                        )
                    }
                    dismiss()
                }

                ResourceState.ERROR -> {
                    binding.loader.gone()
                }
            }
        }

       /* diagnosisViewModel.hivVitalsDetailLiveData.observe(viewLifecycleOwner){resources ->
            when(resources.state){
                ResourceState.LOADING -> {
                    binding.loader.visible()
                }
                ResourceState.SUCCESS -> {
                    binding.loader.gone()
                }
                ResourceState.ERROR -> {
                    binding.loader.gone()
                }
            }
        }*/
    }

    private fun initChipView(chipList: ArrayList<ChipViewItemModel>) {
        binding.diagnosisChip.isSingleSelection = true
        chipTag = TagListCustomView(
            binding.root.context,
            binding.diagnosisChip,
            otherSingleSelect = true,
        ) { _, _, _ ->
            viewModel.whoStageChip = ArrayList(chipTag.getSelectedTags())
            validateInput()
        }
        chipTag.setIsOtherNotStartWith(true)
        chipTag.addChipItemList(chipList, viewModel.whoStageChip)
    }

    private fun validateInput() {
        binding.btnOkay.isEnabled = chipTag.getSelectedTags().isNotEmpty()
    }

    private fun setupUI() {
        binding.tvTitle.text = getString(R.string.who_clinical_stage)
        binding.btnOkay.text = getString(R.string.save).uppercase()
        with(binding) {
            listOf(tvDiagnosisLbl, tvSiteError, tvSiteLbl, siteChip).forEach { it.gone() }
            listOf(
                btnCancel,
                btnOkay,
                ivClose,
            ).forEach { it.safeClickListener(this@WhoClinicalStageFragment) }
            binding.btnOkay.isEnabled = false
        }
        viewModel.setWhoStage(MedicalReviewTypeEnums.whoClinicalStage.name)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnOkay.id -> submitPatientType()
            else -> {
                dismiss()
            }
        }
    }

    private fun submitPatientType() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
            if (chipTag.getSelectedTags().isNotEmpty()) {
                viewModel.createWhoClinicalStage(
                    createEncounter(),
                )
            }
        }
    }

    private fun createEncounter(): MedicalReviewEncounter =
        MedicalReviewEncounter(
            provenance = ProvanceDto(),
            latitude = viewModel.lastLocation?.latitude,
            longitude = viewModel.lastLocation?.longitude,
            patientId = patientViewModel.getPatientId(),
            memberId = patientViewModel.getPatientMemberId(),
            startTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ),
            endTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ),
            villageId = patientViewModel.getVillageId(),
            householdId = patientViewModel.getPatientHouseholdId(),
        )

    private fun handleDialogSize() {
        val width = if (CommonUtils.checkIsTablet(requireContext())) 90 else 90
        setDialogPercent(
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 65 else width,
            50,
        )
    }

    override fun onStart() {
        super.onStart()
        handleDialogSize()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleDialogSize()
    }
}
