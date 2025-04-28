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
import com.medtroniclabs.spice.appextensions.getLocalDate
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.TB
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentAssessmentTBSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.mappingkey.RxBuddy
import com.medtroniclabs.spice.mappingkey.RxBuddy.rxBuddyMonitoringDates
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants.TB_MENU_ID
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getValueOfKeyFromMap
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.NextFollowupDate
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.TBRxBuddyFollowUp
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        binding.tvNextFollowupDateTitle.visible()
        binding.etNextFollowUpDate.visible()
        binding.btnDone.safeClickListener(this)
        binding.btnStartContactTracing.safeClickListener(this)
        binding.etNextFollowUpDate.safeClickListener(this)
        binding.etNextFollowUpDate.background=null
        binding.etNextFollowUpDate.background= ContextCompat.getDrawable(requireContext(),R.drawable.edittext_background)
        val background = binding.etNextFollowUpDate.background as? GradientDrawable
        background?.setStroke(resources.getDimensionPixelSize(R.dimen._1sdp), ContextCompat.getColor(requireContext(), R.color.edittext_stroke))
    }

    private fun attachObservers() {
        viewModel.nextVisitDateForTBPatientLiveData.observe(viewLifecycleOwner) {
            binding.etNextFollowUpDate.text = it.format(DateTimeFormatter.ofPattern(DateUtils.DATE_ddMMyyyy))
        }

        if(isRxBuddyFollowUp == true){
            binding.emptyErrorMessage.visibility = View.GONE
            binding.parentLayout.visibility = View.VISIBLE
            binding.parentLayout.removeAllViews()
            viewModel.assessmentStringLiveData.value?.let {
                createSummaryView(createTBListSummaryData(it))
            }
        } else {
            viewModel.assessmentStringLiveData.value?.let {
                createSummaryView(createTBListSummaryData(it))
            }
        }
    }

    private fun initView() {
        binding.btnStartContactTracing.gone()
        binding.riskResultLayout.text = getString(R.string.update_contact_tracing_for_other)
        binding.etNextFollowUpDate.text =
            LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern(DateUtils.DATE_ddMMyyyy))
        if(isRxBuddyFollowUp == true){
            binding.tvTitle.text = getString(R.string.follow_up_details)
        } else {
            binding.tvTitle.text = getString(R.string.rx_buddy_details)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnDone -> {
               if (isRxBuddyFollowUp == true) {
                   viewModel.saveRxBuddyFollowUpLiveData.value?.data?.let { id ->
                        viewModel.updateNextVisitDateForRxBuddyFollowUp(getNextVisitDate(), id)
                   }
               } else {
                   viewModel.saveRxBuddyDetails.value?.data?.let { id ->
                       viewModel.updateNextVisitDateForRxBuddyRegister(getNextVisitDate(), id)
                   }
               }
            }
            R.id.btnStartContactTracing -> {

            }
            R.id.etNextFollowUpDate -> {
                showDatePickerDialog()
            }
        }
    }

    private fun getFormattedDates(map:  Map<String, Any>): String {
        val displayFormatter = DateTimeFormatter.ofPattern(DATE_ddMMyyyy)
        val tb = map[TB_MENU_ID.lowercase()] as Map<*, *>
        if (tb.containsKey(TBRxBuddyFollowUp)) {
            val rxBuddyFollowUp = tb[TBRxBuddyFollowUp] as Map<*, *>
            if (rxBuddyFollowUp.containsKey(rxBuddyMonitoringDates)) {
                val dates = rxBuddyFollowUp[rxBuddyMonitoringDates] as List<String>

                val stringDates = dates.map { date ->
                    date.getLocalDate().format(displayFormatter)
                }
                return stringDates.joinToString(", ")
            }
        }

        return ""
    }

    private fun createTBListSummaryData(data: String): MutableList<AssessmentSummaryModel>? {
        val dataMap = StringConverter.stringToMap(data)
        return viewModel.formLayoutsLiveData.value?.data?.formLayout?.filter { it.isSummary == true }?.map { formLayout ->
            val value = if (formLayout.id == rxBuddyMonitoringDates) {
                getFormattedDates(dataMap)
            } else {
               getValueOfKeyFromMap(
                   dataMap,
                   formLayout.id,
                   TB
               )
           }

            AssessmentSummaryModel(
                title = formLayout.titleSummary ?: formLayout.title,
                id = formLayout.id,
                cultureValue = formLayout.titleCulture,
                value = value
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
                    RxBuddy.otherRelationShip -> bindTbSummaryView(getString(R.string.other_relationship),it)

                    RxBuddy.rxBuddyMonitoringDates -> bindTbSummaryView(item.title, it)
                    RxBuddy.isSymptomsGettingWorse -> bindTbSummaryView(item.title, it)
                    RxBuddy.hadReactionToYourMedications -> bindTbSummaryView(item.title, it)
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

    private fun getNextVisitDate(): String {
        val tomorrowDate = binding.etNextFollowUpDate.text.toString()
        return DateUtils.convertDateTimeToDate(
            tomorrowDate,
            DateUtils.DATE_ddMMyyyy,
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            inUTC = true
        )
    }
}