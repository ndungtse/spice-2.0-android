package com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.setExpandableText
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.MotherNeonateAncSummaryModel
import com.medtroniclabs.spice.databinding.FragmentMotherNeonateAncSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.convertNullableDoubleToString
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.convertWeight
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.viewmodel.MotherNeonateSummaryViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums

class MotherNeonateAncSummary : BaseFragment(),View.OnClickListener {

    private lateinit var binding: FragmentMotherNeonateAncSummaryBinding
    var adapter: CustomSpinnerAdapter? = null
    private var datePickerDialog: DatePickerDialog? = null
    val viewModel: MotherNeonateSummaryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMotherNeonateAncSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "MotherNeonateSummary"
        fun newInstance(): MotherNeonateAncSummary {
            return MotherNeonateAncSummary()
        }

        fun newInstance(encounterId: String?,fhirId: String?): MotherNeonateAncSummary {
            val fragment = MotherNeonateAncSummary()
            val bundle = Bundle()
            bundle.putString(DefinedParams.EncounterId, encounterId)
            bundle.putString(DefinedParams.FhirId, fhirId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.ancMetaLiveDataForPatientStatus.observe(viewLifecycleOwner) {
            val statusList = ArrayList<Map<String, Any>>()
            for (item in it) {
                statusList.add(
                    hashMapOf<String, Any>(
                        DefinedParams.NAME to item.name,
                        DefinedParams.id to item.id.toString(),
                        DefinedParams.value to (item.value ?: item.name)
                    )
                )
            }
            setSpinner(statusList)
        }
        viewModel.motherNeonateAncSummary.observe(viewLifecycleOwner) { resourceState ->
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

    private fun populate(motherNeonateSummaryModel: MotherNeonateAncSummaryModel) {
        with(binding) {
            tvAncVisitText.text = motherNeonateSummaryModel.visitNumber
                ?: requireContext().getString(R.string.hyphen_symbol)
            tvWeightText.text = convertWeight(motherNeonateSummaryModel.weight,requireContext())
            tvBPText.text = if (motherNeonateSummaryModel.systolic == null && motherNeonateSummaryModel.diastolic == null) {
                getString(R.string.hyphen_symbol)
            } else {
                MotherNeonateUtil.calculateBp(motherNeonateSummaryModel.systolic, motherNeonateSummaryModel.diastolic, requireContext())
            }
            val obstetricsExaminationText = combineText(
                motherNeonateSummaryModel.obstetricExaminations,
                motherNeonateSummaryModel.obstetricExaminationNotes
            )
            tvObstetricsExaminationText.setExpandableText(
                fullText = obstetricsExaminationText,
                moreColorResId = R.color.purple_700,
                title = tvObstetricsExaminationLabel.text.toString()
            )

            val presentingComplaintsText = combineText(
                motherNeonateSummaryModel.presentingComplaints,
                motherNeonateSummaryModel.presentingComplaintsNotes
            )
            tvPresentingComplaintsText.setExpandableText(
                fullText = presentingComplaintsText,
                moreColorResId = R.color.purple_700,
                title = tvPresentingComplaintsLabel.text.toString()
            )
            tvBMIText.text =
                convertNullableDoubleToString(motherNeonateSummaryModel.bmi, requireContext())
            val clinicalNotes = motherNeonateSummaryModel.clinicalNotes

            if (clinicalNotes.isNullOrEmpty()) {
                tvClinicalNotesText.text = requireContext().getString(R.string.hyphen_symbol)
            } else {
                tvClinicalNotesText.setExpandableText(
                    fullText = clinicalNotes,
                    moreColorResId = R.color.purple_700,
                    title = tvClinicalNotesLabel.text.toString()
                )
            }

            tvFundalHeightText.text =
                MotherNeonateUtil.convertCMS(motherNeonateSummaryModel.fundalHeight, requireContext())
            tvFetalHeartRateText.text =
                MotherNeonateUtil.convertBeatsPerMinute(motherNeonateSummaryModel.fetalHeartRate, requireContext())

            tvPrescrptionText.text = motherNeonateSummaryModel.prescriptions?.let { CommonUtils.createPrescription(it) }?.takeIf { it.isNotEmpty() }
                ?: requireContext().getString(R.string.hyphen_symbol)
        }
    }

    private fun combineText(items: List<String?>?, notes: String?): String {
        val combinedText = StringBuilder()
        items?.filterNotNull()?.takeIf { it.isNotEmpty() }?.joinToString(separator = ",")?.let {
            combinedText.append(it)
        }
        if (!notes.isNullOrEmpty()) {
            if (combinedText.isNotEmpty()) {
                combinedText.append(",")
            }
            combinedText.append(notes)
        }
        return if (combinedText.isNotEmpty()) combinedText.toString() else getString(R.string.hyphen_symbol)
    }

    private fun initView() {
        binding.tvNextMedicalReviewLabel.markMandatory()
        binding.tvPatientStatus.markMandatory()
        viewModel.fetchMotherNeonateSummary(arguments?.getString(DefinedParams.EncounterId),arguments?.getString(DefinedParams.FhirId))
        binding.tvClinicalName.text = requireContext().getString(
            R.string.firstname_lastname,
            SecuredPreference.getUserDetails().firstName,
            SecuredPreference.getUserDetails().lastName
        )
        binding.tvDateOfReviewValue.text = DateUtils.convertDateTimeToDate(
            DateUtils.getTodayDateDDMMYYYY(),
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_ddMMyyyy
        )
        viewModel.setAncReqToGetMetaForPatientStatus(MedicalReviewTypeEnums.patient_status.name)
        adapter = CustomSpinnerAdapter(requireContext())
        binding.tvNextMedicalReviewLabelText.safeClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.tvNextMedicalReviewLabelText.id -> {
                showDatePickerDialog()
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
                viewModel.checkSubmitBtn()
                datePickerDialog = null
            }
        }
    }

    private fun setSpinner(statusList: ArrayList<Map<String, Any>>) {
        adapter?.setData(statusList)
        var defaultPosition = 0
        for ((index, patientStatus) in statusList.withIndex()) {
            if ((patientStatus[DefinedParams.value] as? String).equals(
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
                    val selectedItem = adapter?.getData(position = pos)
                    selectedItem?.let {
                        val selectedId = it[DefinedParams.id] as String?
                        val selectedPatientStatus = it[DefinedParams.value] as String?
                        if (selectedId != DefinedParams.DefaultID) {
                            viewModel.patientStatus = selectedPatientStatus
                            handleRecoveredState()
                        } else {
                            viewModel.patientStatus = null
                        }
                        viewModel.checkSubmitBtn()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    fun handleRecoveredState() {
        if (viewModel.patientStatus?.contains(ReferralStatus.Recovered.name) == true) {
            binding.tvNextMedicalReviewLabelText.text = ""
            binding.tvNextMedicalReviewLabelText.isEnabled = false
            binding.tvNextMedicalReviewError.invisible()
        } else {
            binding.tvNextMedicalReviewLabelText.isEnabled = true
        }
    }

    fun validateInput(): Boolean {
        val value = binding.tvNextMedicalReviewLabelText.text?.trim().toString()
        if (value.isBlank()) {
            if (viewModel.patientStatus?.contains(ReferralStatus.Recovered.name) == true) {
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