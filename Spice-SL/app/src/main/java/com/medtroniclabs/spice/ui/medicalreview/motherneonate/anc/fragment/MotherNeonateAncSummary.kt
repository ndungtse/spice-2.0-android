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
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.convertNullableDoubleToString
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

        fun newInstance(encounterId: String?): MotherNeonateAncSummary {
            val fragment = MotherNeonateAncSummary()
            val bundle = Bundle()
            bundle.putString(DefinedParams.EncounterId, encounterId)
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
        viewModel.ancMetaLiveDataForBloodGroup.observe(viewLifecycleOwner) {
            val complaintList = ArrayList<Map<String, Any>>()
            for (i in it) {
                complaintList.add(
                    CommonUtils.getOptionMap(i.name, i.name)
                )
            }
            setSpinner(complaintList)
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
            tvWeightText.text = requireContext().getString(R.string.hyphen_symbol)
            val obstetricsExaminationText = combineText(
                motherNeonateSummaryModel.obstetricExaminations,
                motherNeonateSummaryModel.obstetricExaminationNotes
            )
            tvObstetricsExaminationText.text = obstetricsExaminationText

            val presentingComplaintsText = combineText(
                motherNeonateSummaryModel.presentingComplaints,
                motherNeonateSummaryModel.presentingComplaintsNotes
            )
            tvPresentingComplaintsText.text = presentingComplaintsText
            tvBMIText.text =
                convertNullableDoubleToString(motherNeonateSummaryModel.bmi, requireContext())
            tvClinicalNotesText.text = motherNeonateSummaryModel.clinicalNotes
                ?: requireContext().getString(R.string.hyphen_symbol)
            tvFundalHeightText.text = convertNullableDoubleToString(
                motherNeonateSummaryModel.fundalHeight,
                requireContext()
            )
            tvFetalHeartRateText.text = convertNullableDoubleToString(
                motherNeonateSummaryModel.fetalHeartRate,
                requireContext()
            )
        }
    }

    private fun combineText(items: List<String>?, notes: String?): String {
        val combinedText = StringBuilder()
        items?.takeIf { it.isNotEmpty() }?.joinToString(separator = ",")?.let {
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
        viewModel.fetchMotherNeonateSummary(arguments?.getString(DefinedParams.EncounterId))
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
        viewModel.setAncReqToGetMetaForBloodGroup(MedicalReviewTypeEnums.patient_status.name)
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
                binding.tvNextMedicalReviewLabelText.text =
                    DateUtils.convertDateTimeToDate(
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

    private fun setSpinner(complaintList: ArrayList<Map<String, Any>>) {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.id to DefinedParams.DefaultID
            )
        )
        dropDownList.addAll(complaintList)
        adapter?.setData(dropDownList)
        binding.tvPatientStatusSpinner.adapter = adapter
        binding.tvPatientStatusSpinner.setSelection(0, false)
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
                        val selectedBloodGroup = it[DefinedParams.NAME] as String?
                        if (selectedId != DefinedParams.DefaultID) {
                            viewModel.patientStatus = selectedBloodGroup
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