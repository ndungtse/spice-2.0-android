package com.medtroniclabs.spice.ui.medicalreview.underfiveyears

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.resource.ExaminationResult
import com.medtroniclabs.spice.databinding.FragmentUnderFiveYearsTreatmentSummarryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.ExaminationDetail
import com.medtroniclabs.spice.model.medicalreview.SummaryDetails
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.undertwomonths.fragment.UnderTwoMonthsTreatmentSummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.mypatients.adapter.ExaminationSummaryAdapter

class UnderFiveYearsTreatmentSummaryFragment : BaseFragment(), View.OnClickListener {


    private lateinit var binding: FragmentUnderFiveYearsTreatmentSummarryBinding
    private var datePickerDialog: DatePickerDialog? = null
    private val presentingComplaintsViewModel: PresentingComplaintsViewModel by activityViewModels()
    private val summaryViewModel: UnderFiveYearTreatmentSummaryViewModel by activityViewModels()
    private lateinit var examinationSummaryAdapter: ExaminationSummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUnderFiveYearsTreatmentSummarryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "UnderFiveYearsTreatmentSummaryFragment"
        fun newInstance() = UnderFiveYearsTreatmentSummaryFragment()

        fun newInstance(
            encounterId: String?, patientReference: String?
        ): UnderTwoMonthsTreatmentSummaryFragment {
            val fragment = UnderTwoMonthsTreatmentSummaryFragment()
            val bundle = Bundle()
            bundle.putString(DefinedParams.EncounterId, encounterId)
            bundle.putString(DefinedParams.PatientReference, patientReference)
            fragment.arguments = bundle
            return fragment
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListeners()
        attachObserver()
    }

    private fun initView() {
        summaryViewModel.getUnderFiveYearsSummaryDetails(
            CreateUnderTwoMonthsResponse(
                encounterId = arguments?.getString(DefinedParams.EncounterId) ?: "",
                patientReference = arguments?.getString(DefinedParams.PatientReference) ?: ""
            )
        )

        examinationSummaryAdapter = ExaminationSummaryAdapter()
        binding.rvExaminationList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExaminationList.adapter = examinationSummaryAdapter
    }

    fun attachObserver() {
        summaryViewModel.summaryDetailsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let {
                        renderSummaryDetails(it)
                    }
                    summaryViewModel.setRefreshing(false)
                }
            }
        }
    }

    private fun renderSummaryDetails(details: SummaryDetails) {
        binding.tvPresentingComplaints.text = presentingComplaintsViewModel.enteredComplaintNotes
            .takeIf { it.isNotEmpty() }
            ?: getString(R.string.separator_double_hyphen)
        binding.tvClinicalNotes.text = details.clinicalNotes.toString()
        binding.tvClinicalName.text = requireContext().getString(
            R.string.firstname_lastname,
            SecuredPreference.getUserDetails().firstName,
            SecuredPreference.getUserDetails().lastName
        )
        if (details.patientStatus.isNullOrEmpty()) {
            binding.tvPatientStatus.text = getString(R.string.empty__)
        } else {
            binding.tvPatientStatus.text = details.patientStatus.toString()
        }

        binding.tvDateOfReviewValue.text = DateUtils.convertDateTimeToDate(
            DateUtils.getTodayDateDDMMYYYY(),
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_ddMMyyyy
        )
        examinationList(details)
    }

    private fun examinationList(details: SummaryDetails) {
        val diseaseList = details.examination?.entries?.mapIndexed { index, (key, value) ->
            ExaminationResult(index + 1, key, convertExaminationDetailsToString(value))
        } ?: emptyList()
        examinationSummaryAdapter.updateData(diseaseList)
        if (diseaseList.isEmpty()) {
            binding.rvExaminationList.gone()
            binding.tvExaminationEmptyValue.visible()
        } else {
            binding.rvExaminationList.visible()
            binding.tvExaminationEmptyValue.invisible()
        }
    }

    private fun convertExaminationDetailsToString(details: List<ExaminationDetail>): List<String> {
        return details.map { detail ->
            val title = detail.title ?: ""
            val value = detail.value ?: ""
            "$title : $value"
        }
    }

    private fun setListeners() {
        binding.etNextVisitDate.safeClickListener(this)
    }


    private fun showDatePickerDialog() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.etNextVisitDate.text.isNullOrBlank()) yearMonthDate =
            DateUtils.convertedMMMToddMM(binding.etNextVisitDate.text.toString())
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(context = requireContext(),
                minDate = DateUtils.getTomorrowDate(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.etNextVisitDate.text = DateUtils.convertDateTimeToDate(
                    stringDate, DateUtils.DATE_FORMAT_ddMMyyyy, DateUtils.DATE_ddMMyyyy
                )
                summaryViewModel.nextVisitDate =
                    binding.etNextVisitDate.text.toString().trim().ifBlank { null }
                datePickerDialog = null
                binding.tvNextVisitTimeError.gone()
                summaryListener()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.etNextVisitDate.id -> {
                showDatePickerDialog()
            }
        }
    }

    private fun summaryListener() {
        setFragmentResult(
            MedicalReviewDefinedParams.SUMMARY_ITEM, bundleOf(
                MedicalReviewDefinedParams.SUMMARY_ITEM to true
            )
        )
    }

    fun validateInput(): Boolean {
        val value = binding.etNextVisitDate.text?.trim().toString()
        return if (value.isBlank()) {
            binding.tvNextVisitTimeError.visible()
            false
        } else {
            binding.tvNextVisitTimeError.gone()
            true
        }
    }
}


