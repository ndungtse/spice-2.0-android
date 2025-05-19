package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.setExpandableText
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.toFormattedList
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.model.HivSummaryResponse
import com.medtroniclabs.spice.data.model.TbHistory
import com.medtroniclabs.spice.databinding.FragmentTbSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivImrCmrSummaryViewModel
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.TbSummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel.TbSummaryViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HivImrCmrSummaryFragment: BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentTbSummaryBinding
    val adapter: CustomSpinnerAdapter by lazy { CustomSpinnerAdapter(requireContext()) }
    val patientViewModel: PatientDetailViewModel by activityViewModels()
    private var encounterId: String? = null
    private var fhirId: String? = null
    private var datePickerDialog: DatePickerDialog? = null
    val viewModel: HivImrCmrSummaryViewModel by activityViewModels()
    fun setIds(encounterId: String?, fhirId: String?) {
        this.encounterId = encounterId
        this.fhirId = fhirId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTbSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "HivImrCmrSummaryFragment"
        fun newInstance() =
            HivImrCmrSummaryFragment()

        fun newInstance(encounterId: String?, fhirId: String?): HivImrCmrSummaryFragment {
            val fragment = HivImrCmrSummaryFragment()
            fragment.setIds(encounterId = encounterId, fhirId = fhirId)
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    private fun initView() {
        binding.tvNextMedicalReviewLabel.markMandatory()
        binding.tvPatientStatus.markMandatory()
        binding.tvClinicalName.text = requireContext().getString(
            R.string.firstname_lastname,
            SecuredPreference.getUserDetails()?.firstName,
            SecuredPreference.getUserDetails()?.lastName
        )
        binding.tvDateOfReviewValue.text = DateUtils.convertDateTimeToDate(
            DateUtils.getTodayDateDDMMYYYY(),
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_ddMMyyyy
        )
        binding.tvNextMedicalReviewLabelText.safeClickListener(this)
        viewModel.fetchHivSummaryDetails(
            encounterId, fhirId
        )
        val views = listOf(
            binding.tvSiteLabel, binding.tvSiteSeparator, binding.tvSiteText,
            binding.tvTreatmentText,
            binding.tvTreatmentLabel, binding.tvTreatmentSeparator,
            binding.tvClinicalNotesLabel, binding.tvClinicalNotesSeparator, binding.tvClinicalNotesText
        )
        binding.tvDiagnosesLabel.text = getText(R.string.diagnosis_tb)
        binding.tvComborbiditiesLabel.text = getString(R.string.comorbidities_coinfections)
        views.forEach { it.setVisible(false) }
    }

    private fun attachObserver() {
        viewModel.hivSummary.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let {
                        populate(it)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun populate(data: HivSummaryResponse) {
        with(binding) {
            // Diagnosis Text
            val diagnosisList = data.diagnosis ?: emptyList()
            if (diagnosisList.isNotEmpty()) {
                tvDiagnosesText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.a_red_error
                    )
                )
            }
            tvDiagnosesText.text = diagnosisList
                .filter {
                    it.diseaseCategory.equals(DefinedParams.OtherNotes, ignoreCase = true)
                        .not() && (it.type.equals(
                        DefinedParams.TB,
                        true
                    ) || it.type.isNullOrBlank())
                }
                .map { it.diseaseCategory }
                .distinct()
                .takeIf { it.isNotEmpty() }
                ?.let { CommonUtils.convertListToString(ArrayList(it)) }
                ?: getString(R.string.hyphen_symbol)

            // Presenting Complaints
            tvPresentingText.setExpandableText(
                fullText = CommonUtils.combineText(
                    data.presentingComplaints,
                    data.presentingComplaintsNotes,
                    getString(R.string.hyphen_symbol)
                ),
                moreColorResId = R.color.purple_700,
                title = tvPresentingLabel.text.toString(),
                activity = requireActivity() as BaseActivity
            )

            // Comorbidities
            tvComborbiditiesText.setExpandableText(
                fullText = CommonUtils.combineText(
                    data.comorbiditiesCoinfections,
                    data.comorbiditiesCoinfectionsNotes,
                    getString(R.string.hyphen_symbol)
                ),
                moreColorResId = R.color.purple_700,
                title = tvComborbiditiesLabel.text.toString(),
                activity = requireActivity() as BaseActivity
            )

            // Systematic Examination
            tvGeneralText.setExpandableText(
                fullText = CommonUtils.combineText(
                    data.systemicExaminations?.toFormattedList().takeIf { !it.isNullOrEmpty() }
                        ?: emptyList(),
                    "",
                    getString(R.string.hyphen_symbol)
                ),
                moreColorResId = R.color.purple_700,
                title = tvGeneralLabel.text.toString(),
                activity = requireActivity() as BaseActivity
            )

            // Prescription
            tvPrescrptionText.text = data.prescriptions
                ?.let { CommonUtils.createPrescription(it, requireContext()) }
                ?.takeIf { it.isNotEmpty() }
                ?: getString(R.string.hyphen_symbol)

            // Investigation
            tvInvestigationText.text = data.investigations
                ?.let { CommonUtils.createInvestigation(it, requireContext()) }
                ?.takeIf { it.isNotEmpty() }
                ?: getString(R.string.hyphen_symbol)
        }

        val dropDownList = ArrayList<Map<String, Any>>()
        if (!data.summaryStatus.isNullOrEmpty()) {
            for (item in data.summaryStatus) {
                dropDownList.add(
                    hashMapOf<String, Any>(
                        DefinedParams.NAME to item.name,
                        DefinedParams.Value to item.value
                    )
                )
            }
            setSpinner(dropDownList)
        }
    }

    private fun setSpinner(statusList: ArrayList<Map<String, Any>>) {
        adapter.setData(statusList)
        var defaultPosition = 0
        for ((index, patientStatus) in statusList.withIndex()) {
            if ((patientStatus[DefinedParams.Value] as? String).equals(
                    ReferralStatus.OnTreatment.name,
                    true
                )
            ) {
                defaultPosition = index
            }
        }
        binding.tvPatientStatusSpinner.post {
            binding.tvPatientStatusSpinner.setSelection(defaultPosition, false)
        }
        binding.tvPatientStatusSpinner.adapter = adapter
        binding.tvPatientStatusSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        val selectedPatientStatus = it[DefinedParams.Value] as String?
                        selectedPatientStatus?.let {
                            viewModel.patientStatus = selectedPatientStatus
                        } ?: kotlin.run {
                            viewModel.patientStatus = null
                        }
                        showHideNextVisit()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    private fun showHideNextVisit() {
        if (viewModel.patientStatus?.equals(ReferralStatus.Recovered.name, true) == true || viewModel.patientStatus?.equals(ReferralStatus.Died.name, true) == true) {
            viewModel.nextFollowupDate = null
            binding.tvNextMedicalReviewLabelText.text = ""
            binding.tvNextMedicalReviewLabelText.isEnabled = false
        } else {
            binding.tvNextMedicalReviewLabelText.isEnabled = true
            binding.tvNextMedicalReviewLabelText.text = DateUtils.getFormattedDateAfterMonths(1)
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
        }
    }

    fun validateInput(): Boolean {
        val value = binding.tvNextMedicalReviewLabelText.text?.trim().toString()
        if (value.isBlank()) {
            if (viewModel.patientStatus?.equals(ReferralStatus.Recovered.name, true) == true || viewModel.patientStatus?.equals(ReferralStatus.Died.name, true) == true) {
                binding.tvNextMedicalReviewError.invisible()
                return true
            }
            binding.tvNextMedicalReviewError.visible()
            binding.tvNextMedicalReviewLabelText.requestFocus()
            return false
        }
        binding.tvNextMedicalReviewError.invisible()
        return true
    }
}