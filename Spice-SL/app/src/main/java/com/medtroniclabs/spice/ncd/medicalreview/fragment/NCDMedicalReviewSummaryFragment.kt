package com.medtroniclabs.spice.ncd.medicalreview.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentNcdMedicalReviewSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.data.MRSummaryResponse
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.NCDMedicalReviewActivity
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDMRAlertDialog
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewSummaryViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewViewModel

class NCDMedicalReviewSummaryFragment : BaseFragment(),View.OnClickListener,
    NCDMRAlertDialog.DialogCallback {

    private lateinit var binding: FragmentNcdMedicalReviewSummaryBinding
    private val viewModel: NCDMedicalReviewSummaryViewModel by activityViewModels()
    private val medicalReviewViewModel: NCDMedicalReviewViewModel by activityViewModels()
    private var datePickerDialog: DatePickerDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNcdMedicalReviewSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "NCDMedicalReviewSummaryFragment"
        fun newInstance(visitId: String?,menu:String?): NCDMedicalReviewSummaryFragment {
            return NCDMedicalReviewSummaryFragment().apply {
                arguments = Bundle().apply {
                    putString(NCDMRUtil.EncounterReference, visitId)
                    putString(NCDMRUtil.MENU_ID, menu)
                }
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }
    private fun getVisitId(): String? {
        return arguments?.getString(NCDMRUtil.EncounterReference)
    }

    private fun getConfirmDiagnosisType(): List<String> {
        return NCDMRUtil.getConfirmDiagnoses(arguments?.getString(NCDMRUtil.MENU_ID))
    }

    private fun attachObservers() {
        viewModel.summaryResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    // navigate to summary
                    resourceState.data?.let {
                        populateData(it)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                    showErrorDialog(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                    )
                }
            }
        }
    }

    private fun initView() {
        SecuredPreference.getUserDetails()?.let {
            binding.tvClinicalName.text = requireContext().getString(
                R.string.firstname_lastname,
                it.firstName?.capitalizeFirstChar()
                    ?: getString(R.string.empty),
                it.lastName ?: getString(R.string.empty)
            )
        }
        binding.tvDateOfReviewValue.text = DateUtils.convertDateTimeToDate(
            DateUtils.getTodayDateDDMMYYYY(),
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_ddMMyyyy
        )
        medicalReviewViewModel.createMedicalReview.value?.data?.let {
            withNetworkAvailability(online = {
                viewModel.fetchSummaryResponse(it.copy(patientVisitId = getVisitId(), diagnosisType = getConfirmDiagnosisType()))
            })
        }
        binding.tvNextMedicalReviewLabel.markMandatory()
        binding.tvNextMedicalReviewLabelText.safeClickListener(this)
        binding.btnConfirmDiagnosis.safeClickListener(this)
    }

    private fun populateData(data: MRSummaryResponse) {
        binding.apply {
            tvObstetricsExaminationText.text = CommonUtils.combineText(
                data.physicalExams,
                data.physicalExamComments,
                getString(R.string.hyphen_symbol)
            )
            tvChiefComplaintsText.text = CommonUtils.combineText(
                data.complaints,
                data.compliantComments,
                getString(R.string.hyphen_symbol)
            )
            tvClinicalNotesText.text =
                data.clinicalNote?.takeIf { it.isNotBlank() } ?: getString(R.string.hyphen_symbol)
            if (!viewModel.nextFollowupDate.isNullOrBlank()) {
                binding.tvNextMedicalReviewLabelText.text = viewModel.nextFollowupDate
            }
            tvPrescrptionText.text = CommonUtils.combineText(
                data.prescriptions,
                "",
                getString(R.string.hyphen_symbol)
            )
            tvInvestigationText.text = CommonUtils.formatListToString(
                data.investigations,
                getString(R.string.hyphen_symbol)
            )
            tvDiagnosisText.text = CommonUtils.combineText(
                data.confirmDiagnosis?.diagnosis?.mapNotNull { it.name },
                data.confirmDiagnosis?.diagnosisNotes.takeIf { it?.isNotBlank() == true },
                getString(R.string.hyphen_symbol)
            )
            if(!data.confirmDiagnosis?.diagnosis.isNullOrEmpty()) {
                binding.btnConfirmDiagnosis.gone()
            } else {
                binding.btnConfirmDiagnosis.visible()
            }
        }
    }

    private fun showDatePickerDialog() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.tvNextMedicalReviewLabelText.text.isNullOrBlank())
            yearMonthDate =
                DateUtils.convertedMMMToddMM(binding.tvNextMedicalReviewLabelText.text.toString())

        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                minDate = DateUtils.getTomorrowDate(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.tvNextMedicalReviewLabelText.text = DateUtils.convertDateTimeToDate(
                    stringDate,
                    DateUtils.DATE_FORMAT_ddMMyyyy,
                    DateUtils.DATE_ddMMyyyy
                )
                viewModel.nextFollowupDate = binding.tvNextMedicalReviewLabelText.text.toString()
                datePickerDialog = null
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.tvNextMedicalReviewLabelText.id -> {
                showDatePickerDialog()
            }
            binding.btnConfirmDiagnosis.id -> {
                withNetworkAvailability(online = {
                    (requireActivity() as? NCDMedicalReviewActivity)?.showConfirmDiagnoses()
                })
            }
        }
    }

    fun validateInput(): Boolean {
        val value = binding.tvNextMedicalReviewLabelText.text?.trim().toString()
        if (value.isBlank()) {
            binding.tvNextMedicalReviewError.visible()
            binding.tvNextMedicalReviewLabelText.requestFocus()
            return false
        }
        binding.tvNextMedicalReviewError.invisible()
        return true
    }

    fun handleConfirmDiagnoses(): Boolean {
        return if (viewModel.summaryResponse.value?.data?.confirmDiagnosis?.diagnosis.isNullOrEmpty()) {
            showErrorDialog(
                message = getString(R.string.no_confirm_diagnosis_warning), true,
                Pair(false, false)
            )
            false
        } else {
            true
        }
    }

    fun showErrorDialog(
        message: String,
        showConfirm: Boolean = false,
        showYesNo: Pair<Boolean, Boolean> = Pair(true, true)
    ) {
        val existingDialog = childFragmentManager.findFragmentByTag(NCDMRAlertDialog.TAG)
        if (existingDialog == null) {
            NCDMRAlertDialog.newInstance(
                getString(R.string.alert),
                message = message,
                showYesNoClose = Triple(showYesNo.first, showYesNo.second, true),
                showConfirm = showConfirm,
                callback = this
            ).show(childFragmentManager, NCDMRAlertDialog.TAG)
        }
    }

    override fun onYesClicked() {
        // not used
    }

    override fun onConfirmDiagnosisClicked() {
        withNetworkAvailability(online = {
            (requireActivity() as? NCDMedicalReviewActivity)?.showConfirmDiagnoses()
        })
    }
}