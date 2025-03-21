package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.Contact_Trace_Updated
import com.medtroniclabs.spice.common.DefinedParams.MedicationFollowUp
import com.medtroniclabs.spice.common.DefinedParams.SymptomsFollowUp
import com.medtroniclabs.spice.common.DefinedParams.TB
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentAssessmentTBBinding
import com.medtroniclabs.spice.db.entity.RxBuddyDetails
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.RxBuddy
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.DateOfOnset
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.SleepLocation
import com.medtroniclabs.spice.ui.assessment.referrallogic.ReferralResultGenerator
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class AssessmentTBFragment : BaseFragment(), FormEventListener, View.OnClickListener {

    private lateinit var binding: FragmentAssessmentTBBinding
    private lateinit var formGenerator: FormGenerator
    private val viewModel: AssessmentViewModel by activityViewModels()
    private var isContactTrace:Boolean? = null
    private var isTBPatient:Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isContactTrace = arguments?.getBoolean(DefinedParams.CONTACT_TRACING,false)
        isTBPatient =  arguments?.getBoolean(DefinedParams.isTbPatient,false)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAssessmentTBBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(isTBPatient == true) {
            viewModel.getRxBuddyDetails(viewModel.selectedHouseholdMemberId.toString())
            viewModel.insertTreatmentDetails()
        }else{
            loadFragments()
        }
        viewModel.getMemberDetailsById()
        attachObservers()
        setListeners()
        viewModel.getNearestHealthFacility()
        viewModel.setUserJourney(AnalyticsDefinedParams.TBAssessement)
    }

    private fun loadFragments(rxBuddyDetails: RxBuddyDetails?=null){
        initView(rxBuddyDetails)
        viewModel.getFormData(DefinedParams.TB.lowercase(), isContactTrace, isTBPatient,rxBuddyDetails != null)
    }

    private fun setListeners() {
        binding.btnSubmit.safeClickListener(this)
    }

    private fun attachObservers() {

        viewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        formGenerator.populateViews(data.formLayout)
                        viewModel.setAnalyticsData(
                            UserDetail.startDateTime,
                            eventType = AnalyticsDefinedParams.TBAssessement,
                            eventName = AnalyticsDefinedParams.AssessmentCreation
                        )
                    }
                }

                ResourceState.ERROR -> {
                   hideProgress()
                }
            }
        }

        viewModel.otherHouseholdMemberLiveData.observe(viewLifecycleOwner){ resourceState ->
            when(resourceState.state){
                ResourceState.LOADING -> {

                }
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { data ->
                        val view =
                            formGenerator.getViewByTag(RxBuddy.selectHouseholdMember) as? AppCompatSpinner
                        if (view != null) {
                            (view.adapter as CustomSpinnerAdapter).setData(data)
                        }
                    }
                }
                ResourceState.ERROR -> {

                }
            }
        }

        viewModel.rxBuddyDetailsLiveData.observe(viewLifecycleOwner){ resourceState ->
            when(resourceState.state){
                ResourceState.LOADING -> {
                    showProgress()
                }
                ResourceState.ERROR -> {
                    hideProgress()
                    loadFragments()
                }
                ResourceState.SUCCESS -> {
                    hideProgress()
                    loadFragments(
                        resourceState.data
                    )
                }
            }
        }
    }

    private fun initView(rxBuddyDetails: RxBuddyDetails?) {
        replaceFragmentInId<BioDataFragment>(
            binding.bioDataFragmentContainer.id,
            tag = BioDataFragment.TAG
        )
        isTBPatient?.let {
            if(it){
                replaceFragmentInId<TBTreatmentFragment>(
                    binding.treatmentDetailsFragmentContainer.id,
                    tag = TBTreatmentFragment.TAG
                )
                rxBuddyDetails?.let {
                    binding.scrollView.viewTreeObserver.addOnGlobalLayoutListener {
                        binding.scrollView.requestLayout()
                    }
                    val bundle = Bundle().apply {
                        putString(DefinedParams.RxBuddyName,rxBuddyDetails?.name)
                        putString(DefinedParams.RxRelationShip,rxBuddyDetails?.relationship)
                        putString(DefinedParams.RxPhoneNo,rxBuddyDetails?.phoneNumber)
                        rxBuddyDetails?.isMonitorSheetProvider?.let { it1 ->
                            putBoolean(DefinedParams.RxMonitoringSheetProvided,
                                it1
                            )
                        }
                    }
                    replaceFragmentInId<TBRxBuddyFragment>(
                        binding.rxBuddyDetailsFragmentContainer.id,
                        bundle = bundle,
                        tag = TBRxBuddyFragment.TAG
                    )
                    replaceFragmentInId<TBFollowUpFragment>(
                        binding.rxBuddyFollowUpFragmentContainer.id,
                        tag = TBFollowUpFragment.TAG
                    )
                }
            }
        }
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, null, this, binding.scrollView,
            translate = SecuredPreference.getIsTranslationEnabled()
        ) { map, id ->
            when (id) {
                DateOfOnset -> {
                    formGenerator.getViewByTag(id)?.let {
                        val dob = viewModel.memberDetailsLiveData.value?.data?.dateOfBirth
                        if (!dob.isNullOrEmpty() && map.containsKey(id)) {
                            val selectedDate = map[id].toString()
                            val isValid = DateUtils.compareDates(dob, selectedDate)
                            formGenerator.getViewByTag(id + AssessmentDefinedParams.errorSuffix)?.apply {
                                visibility = if (isValid) View.GONE else View.VISIBLE
                            }.takeIf { it is TextView }?.let { textView->(textView as TextView).text=
                                getString(R.string.the_day_s_should_be_less_than_age)
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "AssessmentTBFragment"
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
    }

    override fun onPopulate(targetId: String) {
    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        serverViewModel: FormLayout,
        resultMap: Any?
    ) {
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?
    ) {}

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        val dob = viewModel.memberDetailsLiveData.value?.data?.dateOfBirth
        if(resultMap?.containsKey(RxBuddy.selectHouseholdMember) == true
            && resultMap.containsKey(RxBuddy.relationshipToPatient)
            && resultMap.containsKey(RxBuddy.hasProvidedMonitoringSheet)){
            val rxBuddyName =  if(resultMap.containsKey(RxBuddy.rxBuddyName)) resultMap[RxBuddy.rxBuddyName] as String else null
            val countryCode = SecuredPreference.getPhoneNumberCode()
            val phone = if(resultMap.containsKey(RxBuddy.rxBuddyPhoneNumber)) resultMap[RxBuddy.rxBuddyPhoneNumber] as String else null
            val rxBuddyPhoneNumber = if(phone.isNullOrEmpty()) null else "+$countryCode $phone"
            val isMonitorSheetProviderText = resultMap[RxBuddy.hasProvidedMonitoringSheet] as String
            val isSheetProvider = isMonitorSheetProviderText.equals(getString(R.string.yes),true)

            viewModel.insertRxBuddyDetails(
                rxBuddyId = null,
                patientMemberId = viewModel.selectedHouseholdMemberId.toString(),
                memberId = resultMap[RxBuddy.selectHouseholdMember].toString(),
                name = rxBuddyName,
                phoneNumber = rxBuddyPhoneNumber,
                relationShip = resultMap[RxBuddy.relationshipToPatient] as String,
                isMonitorSheetProvider = isSheetProvider
            )

            resultMap.let { details ->
                val result = serverData?.let {
                    FormResultComposer().groupValues(
                        context = requireContext(),
                        serverData = it,
                        details,
                        TB.lowercase()
                    )
                }
                val map =  result?.second as? HashMap<Any,Any>
                val assessmentDetail = StringConverter.convertGivenMapToString(map) ?: ""
                viewModel.assessmentStringLiveData.postValue(assessmentDetail)
            }
            return
        }
        serverData?.forEach { view ->
            if (view?.id == SleepLocation) {
                val option = view.optionsList
                val sleepLocationId = resultMap?.get(SleepLocation)
                option?.forEach { item ->
                    if (item[DefinedParams.id] == sleepLocationId) {
                        item[DefinedParams.name]?.let {
                            resultMap?.put(SleepLocation, it)
                        }
                    }
                }
            }
        }
        resultMap?.let { details ->
            dob?.let { dobDate ->
                val isValid = if (details.containsKey(DateOfOnset)) {
                    val selectedDate = details[DateOfOnset].toString()
                    DateUtils.compareDates(dobDate, selectedDate)
                } else {
                    true
                }
                formGenerator.getViewByTag("${DateOfOnset}${AssessmentDefinedParams.errorSuffix}")?.apply {
                    visibility = if (isValid) View.GONE else View.VISIBLE
                }.takeIf { it is TextView }?.let { textView ->
                    (textView as TextView).text =
                        getString(R.string.the_day_s_should_be_less_than_age)
                }

                if (isValid) {
                    val referralResult =
                        ReferralResultGenerator().calculateTBReferralResult(details)
                    val result = serverData?.let {
                        FormResultComposer().groupValues(
                            context = requireContext(),
                            serverData = it,
                            details,
                            TB.lowercase()
                        )
                    }
                    result?.second?.let {
                        viewModel.memberDetailsLiveData.value?.data?.id?.let { hhmId ->
                            viewModel.updateTBContactTraceStatus(
                                hhmId = hhmId,
                                tbContactTracingStatus = Contact_Trace_Updated
                            )
                        }
                        viewModel.setUserJourney(AnalyticsDefinedParams.SUBMITBUTTONTRIGGERED)
                        viewModel.saveAssessment(it, referralResult, viewModel.menuId)
                    }
                }
            }
        }
    }

    override fun onRenderingComplete() {
        viewModel.getOtherHouseholdMemberExcludeTBPatient()
    }

    override fun onUpdateInstruction(id: String, selectedId: Any?) {

    }

    override fun onInformationHandling(
        id: String,
        noOfDays: Int,
        enteredDays: Int?,
        resultMap: HashMap<String, Any>?
    ) {
    }

    override fun onAgeCheckForPregnancy() {

    }

    override fun handleMandatoryCondition(serverData: FormLayout?) {

    }

    override fun onAgeUpdateListener(
        age: Int,
        serverData: List<FormLayout?>?,
        resultHashMap: HashMap<String, Any>
    ) {
        /*
       Never used
        */
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnSubmit.id -> {
                val fragment = childFragmentManager.findFragmentById(binding.rxBuddyFollowUpFragmentContainer.id)
                if (fragment is TBFollowUpFragment){
                    val tbFragmentIsValid = fragment.validInput()
                    if (tbFragmentIsValid){
                        val isAnyOfSymptomsWorse = viewModel.rxBuddyFollowUpResultHashMap[SymptomsFollowUp] as String
                        val isMedication = viewModel.rxBuddyFollowUpResultHashMap[MedicationFollowUp] as String
                        viewModel.insertRxBuddyFollowUp(
                            rxBuddyId = null,
                            patientMemberId = viewModel.selectedHouseholdMemberId.toString(),
                            monitoringSheetDate = "12/03/2025",
                            isSymptomsWorse = isAnyOfSymptomsWorse.equals(getString(R.string.yes),true),
                            isMedication = isMedication.equals(getString(R.string.yes),true)
                        )
                        return
                    }
                }else {
                    withLocationCheck({
                        viewModel.fetchCurrentLocation(requireContext())
                        formGenerator.formSubmitAction(view)
                    })
                }
            }
        }
    }

    fun getCurrentAnsweredStatus(): Boolean {
        return formGenerator.getResultMap().isNotEmpty()
    }
}