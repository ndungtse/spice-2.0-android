package com.medtroniclabs.spice.ui.mypatients.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
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
import com.medtroniclabs.spice.databinding.FragmentUnderTwoMonthsTreatmentSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.ExaminationDetail
import com.medtroniclabs.spice.model.medicalreview.UnderTwoMonthsSummaryDetails
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.undertwomonths.UnderTwoMonthsTreatmentSummaryViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.mypatients.adapter.UnderTwoMonthsTreatmentSummaryAdapter


class UnderTwoMonthsTreatmentSummaryFragment : BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentUnderTwoMonthsTreatmentSummaryBinding
    private var datePickerDialog: DatePickerDialog? = null
    private val presentingComplaintsViewModel: PresentingComplaintsViewModel by activityViewModels()
    private val summaryViewModel: UnderTwoMonthsTreatmentSummaryViewModel by activityViewModels()
    private lateinit var underTwoMonthsTreatmentSummaryAdapter: UnderTwoMonthsTreatmentSummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUnderTwoMonthsTreatmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "UnderTwoMonthsTreatmentSummaryFragment"
        fun newInstance() = UnderTwoMonthsTreatmentSummaryFragment()

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
        spinnerForDeliveryType()
        setListeners()
        attachObserver()
    }

    private fun initView() {
        summaryViewModel.getUnderTwoMonthsSummaryDetails(
            CreateUnderTwoMonthsResponse(
                encounterId = arguments?.getString(DefinedParams.EncounterId) ?: "",
                patientReference = arguments?.getString(DefinedParams.PatientReference) ?: ""
            )
        )
        underTwoMonthsTreatmentSummaryAdapter = UnderTwoMonthsTreatmentSummaryAdapter()
        binding.rvExaminationList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExaminationList.adapter = underTwoMonthsTreatmentSummaryAdapter
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

    private fun renderSummaryDetails(details: UnderTwoMonthsSummaryDetails) {
        if (presentingComplaintsViewModel.enteredComplaintNotes.equals("")) {
            binding.tvPresentingComplaints.text = getString(R.string.separator_double_hyphen)
        } else binding.tvPresentingComplaints.text =
            presentingComplaintsViewModel.enteredComplaintNotes

        binding.tvClinicalNotes.text = details.clinicalNotes.toString()
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
        examinationList(details)
    }

    private fun examinationList(details: UnderTwoMonthsSummaryDetails) {
        val diseaseList = details.examination?.entries?.mapIndexed { index, (key, value) ->
            ExaminationResult(index + 1, key, convertExaminationDetailsToString(value))
        } ?: emptyList()
        underTwoMonthsTreatmentSummaryAdapter.updateData(diseaseList)
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

    private fun spinnerForDeliveryType() {
        val list = arrayListOf<Map<String, Any>>()
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.ID to DefinedParams.DefaultSelectID
            )
        )
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.OnTreatment, DefinedParams.ID to "1"
            )
        )
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.Recovered, DefinedParams.ID to "2"
            )
        )
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.REFERRED, DefinedParams.ID to "3"
            )
        )
        setListenerToDeliveryStatus(list)
    }

    private fun setListenerToDeliveryStatus(list: ArrayList<Map<String, Any>>) {
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.tvPatientStatus.adapter = adapter
        binding.tvPatientStatus.setSelection(0, false)
        binding.tvPatientStatus.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    val selectedItem = adapter.getData(position = position)
                    selectedItem?.let {
                        val selectedName = it[DefinedParams.NAME] as String?
                        if (selectedName != DefinedParams.DefaultIDLabel) {
                            selectedName?.let { name ->
                                summaryViewModel.selectedPatientStatus = name
                            }
                        } else {
                            summaryViewModel.selectedPatientStatus = null
                        }
                    }
                    binding.tvNextVisitTimeError.gone()
                    summaryViewModel.setSubmitBtn()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }

            }
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
        if (value.isBlank()) {
            return if (summaryViewModel.selectedPatientStatus?.contains(DefinedParams.Recovered) == true) {
                binding.etNextVisitDate.text = ""
                binding.tvNextVisitTimeError.gone()
                true
            } else if (summaryViewModel.selectedPatientStatus?.contains(DefinedParams.OnTreatment) == true && value.isBlank()) {
                binding.etNextVisitDate.requestFocus()
                binding.tvNextVisitTimeError.visible()
                false
            } else if (summaryViewModel.selectedPatientStatus?.contains(DefinedParams.REFERRED) == true && value.isBlank()) {
                binding.etNextVisitDate.requestFocus()
                binding.tvNextVisitTimeError.visible()
                false
            } else {
                false
            }
        }
        return true
    }
}