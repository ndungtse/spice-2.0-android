package org.medtroniclabs.uhis.ui.medicalreview.tb.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.setDialogPercent
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.SpiceLocationManager
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.data.model.MedicalReviewEncounter
import org.medtroniclabs.uhis.data.model.MotherNeonateAncRequest
import org.medtroniclabs.uhis.data.model.PatientTypeCreateRequest
import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto
import org.medtroniclabs.uhis.databinding.FragmentPatientStatusDialogBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.TagListCustomView
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.DialogDismissListenerForTb
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.PATIENT_TYPE
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.PATIENT_TYPE_HYPHEN
import org.medtroniclabs.uhis.ui.medicalreview.tb.viewmodel.PatientTypeViewModel
import org.medtroniclabs.uhis.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PatientTypeFragment : DialogFragment(), View.OnClickListener {
    var listener: DialogDismissListenerForTb? = null
    private lateinit var binding: FragmentPatientStatusDialogBinding
    private val viewModel: PatientTypeViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by activityViewModels()
    private lateinit var chipTag: TagListCustomView

    companion object {
        const val TAG = "PatientTypeFragment"

        fun newInstance() = PatientTypeFragment()
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
        patientViewModel.setUserJourney(AnalyticsDefinedParams.PATIENTTYPEDIALOUGE)
    }

    private fun getCurrentLocation() {
        SpiceLocationManager(requireContext()).getCurrentLocation {
            viewModel.lastLocation = it
        }
    }

    private fun attachObservers() {
        viewModel.getPatientType.observe(viewLifecycleOwner) {
            viewModel.setPatientType(PATIENT_TYPE)
        }
        viewModel.getPatientTypeLiveData.observe(viewLifecycleOwner) { list ->
            val chipList = list.map { item ->
                ChipViewItemModel(
                    id = item.id,
                    name = item.name,
                    type = item.type,
                    value = item.value,
                )
            }
            initChipView(ArrayList(chipList))
        }

        viewModel.createPatientType.observe(viewLifecycleOwner) {
            if (it.state == ResourceState.SUCCESS) {
                listener?.onDialogDismissedForTb(isPatientType = true)
                dismiss()
            }
            if (it.state == ResourceState.ERROR) {
                (requireActivity() as? BaseActivity)?.showErrorDialogue(
                    title = getString(R.string.alert),
                    message = getString(R.string.something_went_wrong_try_later),
                    positiveButtonName = getString(R.string.ok),
                ) {
                }
            }
        }
    }

    private fun initChipView(chipList: ArrayList<ChipViewItemModel>) {
        binding.diagnosisChip.isSingleSelection = true
        viewModel.getPatientType.value?.data?.get(PATIENT_TYPE_HYPHEN)?.let { type ->
            if (type is String && type.isNotBlank()) {
                chipList.filter { it.value == type }.takeIf { it.isNotEmpty() }?.let { list ->
                    viewModel.patientTypeChip = ArrayList(list)
                }
            }
        }
        chipTag = TagListCustomView(
            binding.root.context,
            binding.diagnosisChip,
            otherSingleSelect = true,
        ) { _, _, _ ->
            viewModel.patientTypeChip = ArrayList(chipTag.getSelectedTags())
            validateInput()
        }
        chipTag.setIsOtherNotStartWith(true)
        chipTag.addChipItemList(chipList, viewModel.patientTypeChip)
    }

    private fun validateInput() {
        binding.btnOkay.isEnabled = chipTag.getSelectedTags().isNotEmpty()
    }

    private fun setupUI() {
        binding.tvTitle.text = getString(R.string.patient_type)
        binding.btnOkay.text = getString(R.string.save).uppercase()
        with(binding) {
            listOf(tvDiagnosisLbl, tvSiteError, tvSiteLbl, siteChip).forEach { it.gone() }
            listOf(
                btnCancel,
                btnOkay,
                ivClose,
            ).forEach { it.safeClickListener(this@PatientTypeFragment) }
            binding.btnOkay.isEnabled = false
        }
        viewModel.getPatientType(MotherNeonateAncRequest(memberId = patientViewModel.getPatientMemberId()))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnOkay.id -> submitPatientType()
            else -> {
                patientViewModel.setUserJourney(AnalyticsDefinedParams.CANCELBUTTONTRIGGERED)
                dismiss()
            }
        }
    }

    private fun submitPatientType() {
        patientViewModel.setUserJourney(AnalyticsDefinedParams.SAVEBUTTONTRIGGERED)
        viewModel.createPatientType(
            PatientTypeCreateRequest(
                encounter = createEncounter(),
                patientReference = patientViewModel.getPatientFHIRId(),
                stringValue = chipTag.getSelectedTags().first().value,
            ),
        )
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
        setDialogPercent(if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 65 else width, 50)
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
