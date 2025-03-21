package com.medtroniclabs.spice.ui.assessment.fragment

import android.app.DatePickerDialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.TB
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentAssessmentTBSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.mappingkey.RxBuddy
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getValueOfKeyFromMap
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.NextFollowupDate
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class RxBuddySummaryFragment : BaseFragment(), View.OnClickListener {
    private val viewModel: AssessmentViewModel by activityViewModels()
    lateinit var binding: FragmentAssessmentTBSummaryBinding
    private var datePickerDialog: DatePickerDialog? = null
    private var isRxBuddyFollowUp: Boolean?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isRxBuddyFollowUp = arguments?.getBoolean(DefinedParams.isRxBuddyFollowUp,false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentTBSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListener()
        attachObservers()
    }

    private fun setListener() {
        binding.btnDone.safeClickListener(this)
        binding.btnStartContactTracing.safeClickListener(this)
        binding.etNextFollowUpDate.safeClickListener(this)
        binding.etNextFollowUpDate.background=null
        binding.etNextFollowUpDate.background= ContextCompat.getDrawable(requireContext(),R.drawable.edittext_background)
        val background = binding.etNextFollowUpDate.background as? GradientDrawable
        background?.setStroke(resources.getDimensionPixelSize(R.dimen._1sdp), ContextCompat.getColor(requireContext(), R.color.edittext_stroke))
    }

    private fun attachObservers() {
        if(isRxBuddyFollowUp == true){
            binding.emptyErrorMessage.visibility = View.GONE
            binding.parentLayout.visibility = View.VISIBLE
            binding.parentLayout.removeAllViews()
            createSummaryViewForFollowUp(viewModel.rxBuddyFollowUpResultHashMap)
        }else {
            viewModel.assessmentStringLiveData.value?.let {
                createSummaryView(createTBListSummaryData(it))
            }
        }
    }

    private fun createSummaryViewForFollowUp(rxBuddyFollowUpResult: HashMap<String, Any>) {
        rxBuddyFollowUpResult.forEach{(key,value) ->
            when(key) {
                DefinedParams.MonitoringSheetDate -> bindTbSummaryView(
                    getString(R.string.rx_buddy_monitoring_sheet),
                    value.toString()
                )
                DefinedParams.SymptomsFollowUp -> bindTbSummaryView(
                    getString(R.string.any_of_your_symptoms_getting_worse),
                    value.toString()
                )
                DefinedParams.MedicationFollowUp -> bindTbSummaryView(
                    getString(R.string.have_you_had_a_reaction_to_any_of_your_medications),
                    value.toString()
                )

            }
        }
    }

    private fun initView() {
        binding.btnStartContactTracing.visible()
        binding.riskResultLayout.text = getString(R.string.update_contact_tracing_for_other)
        if(isRxBuddyFollowUp == true){
            binding.tvTitle.text = getString(R.string.follow_up_details)
        } else {
            binding.tvTitle.text = getString(R.string.rx_buddy_details)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnDone -> {

            }
            R.id.btnStartContactTracing -> {

            }
            R.id.etNextFollowUpDate -> {
                showDatePickerDialog()
            }
        }
    }

    private fun createTBListSummaryData(data: String): MutableList<AssessmentSummaryModel>? {
        return viewModel.formLayoutsLiveData.value?.data?.formLayout?.filter { it.isSummary == true }?.map { formLayout ->
            AssessmentSummaryModel(
                title = formLayout.titleSummary ?: formLayout.title,
                id = formLayout.id,
                cultureValue = formLayout.titleCulture,
                value = getValueOfKeyFromMap(
                    StringConverter.stringToMap(data),
                    formLayout.id,
                    TB
                )
            )
        }?.toMutableList()
    }

    private fun createSummaryView(
        listSummaryData: MutableList<AssessmentSummaryModel>?
    ) {
        listSummaryData?.let {summaryData ->
            binding.emptyErrorMessage.visibility = View.GONE
            binding.parentLayout.visibility = View.VISIBLE
            binding.parentLayout.removeAllViews()
            composeTbSummaryView(summaryData)
        } ?: kotlin.run {
            showErrorInSummary()
        }
    }

    private fun composeTbSummaryView(listSummaryData: MutableList<AssessmentSummaryModel>) {
        listSummaryData.forEach { item ->
            item.value?.let {
                when (item.id) {
                    RxBuddy.rxBuddyName -> bindTbSummaryView(item.title, it)
                    RxBuddy.relationshipToPatient -> bindTbSummaryView(getString(R.string.relation_ship_with_patient),it)
                    RxBuddy.rxBuddyPhoneNumber -> bindTbSummaryView(item.title,
                        "+${SecuredPreference.getPhoneNumberCode()} "+it)
                    RxBuddy.hasProvidedMonitoringSheet -> bindTbSummaryView(item.title,it)
                }
            }
        }
    }

    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.parentLayout.visibility = View.GONE
    }

    private fun bindTbSummaryView(title: String?, value: String?, valueTextColor: Int? = null) {
        binding.parentLayout.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title,
                value,
                valueTextColor,
                requireContext()
            )
        )
    }

    private fun showDatePickerDialog() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.etNextFollowUpDate.text.isNullOrBlank())
            yearMonthDate =
                DateUtils.convertedMMMToddMM(binding.etNextFollowUpDate.text.toString())
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                minDate = DateUtils.getTomorrowDate(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.etNextFollowUpDate.text =
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DateUtils.DATE_FORMAT_ddMMyyyy,
                        DateUtils.DATE_ddMMyyyy
                    )
                updateFollowUpDate(binding.etNextFollowUpDate.text.toString().trim())
                datePickerDialog = null
            }
        }
    }

    private fun updateFollowUpDate(date: String) {
        if (date.isNotEmpty()) {
            viewModel.otherAssessmentDetails[NextFollowupDate] =
                DateUtils.convertDateTimeToDate(
                    date,
                    DateUtils.DATE_ddMMyyyy,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    inUTC = true
                )
        }
    }

    companion object{
        const val TAG = "RxBuddySummaryFragment"
        fun newInstance(): RxBuddySummaryFragment {
            return RxBuddySummaryFragment()
        }

    }
}