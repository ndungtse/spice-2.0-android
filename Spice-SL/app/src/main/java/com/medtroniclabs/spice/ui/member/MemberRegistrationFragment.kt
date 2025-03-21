package com.medtroniclabs.spice.ui.member

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams.AddNewMember
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams.EditNewMember
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.startBackgroundOfflineSync
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.getBooleanAsString
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DefinedParams.DOB
import com.medtroniclabs.spice.common.DefinedParams.HOUSEHOLD_MEMBER_REGISTRATION
import com.medtroniclabs.spice.common.DefinedParams.HouseholdHead
import com.medtroniclabs.spice.common.DefinedParams.MemberID
import com.medtroniclabs.spice.common.DefinedParams.No
import com.medtroniclabs.spice.common.DefinedParams.Yes
import com.medtroniclabs.spice.common.DefinedParams.female
import com.medtroniclabs.spice.common.DefinedParams.male
import com.medtroniclabs.spice.common.EntityMapper.getResultSpinnerMapList
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentMemberRegistrationBinding
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.config.DefinedParams
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Month
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Week
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.titleSuffix
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.headPhoneNumberCategory
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.no
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.villageId
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.yes
import com.medtroniclabs.spice.mappingkey.MemberRegistration
import com.medtroniclabs.spice.mappingkey.MemberRegistration.dateOfBirth
import com.medtroniclabs.spice.mappingkey.MemberRegistration.gender
import com.medtroniclabs.spice.mappingkey.MemberRegistration.householdHeadRelationship
import com.medtroniclabs.spice.mappingkey.MemberRegistration.isPregnant
import com.medtroniclabs.spice.mappingkey.MemberRegistration.isValidMinAge
import com.medtroniclabs.spice.mappingkey.MemberRegistration.isValidRelationAge
import com.medtroniclabs.spice.mappingkey.MemberRegistration.name
import com.medtroniclabs.spice.mappingkey.MemberRegistration.otherFamilyMember
import com.medtroniclabs.spice.mappingkey.MemberRegistration.phoneNumber
import com.medtroniclabs.spice.mappingkey.MemberRegistration.phoneNumberCategory
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.DateOfBirth
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.errorSuffix
import com.medtroniclabs.spice.ui.dialog.SuccessDialogFragment
import com.medtroniclabs.spice.ui.home.AssessmentToolsActivity
import com.medtroniclabs.spice.ui.household.HouseholdActivity
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams
import com.medtroniclabs.spice.ui.household.summary.HouseholdSummaryActivity
import com.medtroniclabs.spice.ui.household.viewmodel.HouseRegistrationViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MemberRegistrationFragment : BaseFragment(), FormEventListener, View.OnClickListener {

    private lateinit var binding: FragmentMemberRegistrationBinding
    private lateinit var formGenerator: FormGenerator
    private val memberRegistrationViewModel: MemberRegistrationViewModel by activityViewModels()
    private val householdRegistrationViewModel: HouseRegistrationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMemberRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        setListener()
        initializeFlow()
        attachObserver()
        handleAddNewMember()
        val eventType =
            if (householdRegistrationViewModel.isMemberRegistration || householdRegistrationViewModel.memberID != -1L) {
                if (householdRegistrationViewModel.isMemberRegistration)
                    AddNewMember
                else
                    EditNewMember
            } else
                AnalyticsDefinedParams.MemberRegistration

        memberRegistrationViewModel.getHouseholdHeadDob(householdRegistrationViewModel.householdId)
        householdRegistrationViewModel.eventName = eventType
        memberRegistrationViewModel.setUserJourney(eventType)

        onPhuAddMember()
        withLocationCheck({}, shouldHideProgress = true)
    }

    private fun initializeFlow() {
        arguments?.let {
            memberRegistrationViewModel.medicalReviewFlow =
                it.getBoolean(MedicalReviewDefinedParams.MEDICAL_REVIEW_ADD_MEMBER)
        } ?: false
        if (memberRegistrationViewModel.medicalReviewFlow) {
            binding.bottomNavigationView.gone()
        } else {
            binding.bottomNavigationView.visible()
        }
        memberRegistrationViewModel.getFormData(HOUSEHOLD_MEMBER_REGISTRATION)
    }


    private fun attachObserver() {
        if (memberRegistrationViewModel.medicalReviewFlow) {
            householdRegistrationViewModel.memberVillageListResponse.observe(viewLifecycleOwner) { resourceState ->
                when (resourceState.state) {
                    ResourceState.SUCCESS -> {
                        resourceState.data?.let { data ->
                            memberRegistrationViewModel.villageDetails =
                                data.response as List<VillageEntity>
                            formGenerator.spinnerDataInjection(data, getResultSpinnerMapList(data))
                        }
                    }

                    ResourceState.LOADING -> {
                        (activity as BaseActivity?)?.showLoading()
                    }

                    ResourceState.ERROR -> {
                        (activity as BaseActivity?)?.hideLoading()
                    }
                }
            }
        }

        memberRegistrationViewModel.memberRegistrationLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as BaseActivity?)?.showLoading()
                }

                ResourceState.ERROR -> {
                    (activity as BaseActivity?)?.hideLoading()

                }

                ResourceState.SUCCESS -> {
                    val (title, startDate) = if (memberRegistrationViewModel.addNewMember) {
                        val type =
                            if (householdRegistrationViewModel.isMemberRegistration && householdRegistrationViewModel.memberID != -1L) {
                                EditNewMember
                            } else {
                                AddNewMember
                            }
                        type to (UserDetail.startDateTime ?: "")
                    } else {
                        AnalyticsDefinedParams.HouseholdCreation to (arguments?.getString(
                            AnalyticsDefinedParams.StartDate
                        ) ?: "")
                    }
                    memberRegistrationViewModel.setAnalyticsData(
                        startDate,
                        eventName = title,
                        isCompleted = true
                    )

                    (activity as BaseActivity?)?.hideLoading()
                    resourceState.data?.let {
                        if (arguments?.getBoolean(HouseholdDefinedParams.isPhuWalkInsFlow) == true) {
                            requireActivity().startBackgroundOfflineSync()
                            val existingFragment =
                                childFragmentManager.findFragmentByTag(
                                    SuccessDialogFragment.TAG
                                )
                            if (existingFragment == null) {
                                SuccessDialogFragment.newInstance(isPhuLink = true)
                                    .show(childFragmentManager, SuccessDialogFragment.TAG)
                            }
                        } else {
                            launchSummaryOrAssessmentPage()
                        }

                    }
                }
            }
        }

        memberRegistrationViewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resources ->
            when (resources.state) {
                ResourceState.LOADING -> {
                    (activity as? BaseActivity)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as? BaseActivity)?.hideLoading()
                    resources.data?.let { data ->
                        val formFieldsType = object : TypeToken<FormResponse>() {}.type
                        val formFields: FormResponse = Gson().fromJson(data, formFieldsType)
                        formGenerator.populateViews(formFields.formLayout)
                        handleRelationshipSpinner()
                    }
                }

                ResourceState.ERROR -> {
                    (activity as? BaseActivity)?.hideLoading()
                }
            }
        }

        memberRegistrationViewModel.memberDetailsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as BaseActivity?)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as BaseActivity?)?.hideLoading()
                    resourceState.data?.let { data ->
                        autoPopulateDetails(data)
                    }
                }

                ResourceState.ERROR -> {
                    (activity as BaseActivity?)?.hideLoading()
                }
            }
        }

        memberRegistrationViewModel.addnewMemberReq.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as BaseActivity?)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as BaseActivity?)?.hideLoading()
                    setFragmentResult(
                        MedicalReviewDefinedParams.MEMBER_REG, bundleOf(
                            MedicalReviewDefinedParams.Notes to true
                        )
                    )
                }

                ResourceState.ERROR -> {
                    (activity as BaseActivity?)?.hideLoading()
                }
            }
        }
    }

    private fun launchSummaryOrAssessmentPage() {
        requireActivity().startBackgroundOfflineSync()
        if (memberRegistrationViewModel.startAssessment != null && memberRegistrationViewModel.startAssessment!!) {
            val intent = Intent(requireActivity(), AssessmentToolsActivity::class.java)
            memberRegistrationViewModel.memberRegistrationLiveData.value?.data.let {
                intent.putExtra(MemberID, it ?: -1)
            }
            intent.putExtra(DOB, memberRegistrationViewModel.memberDob)
            startActivity(intent)
            (activity as HouseholdActivity).finish()
        } else {
            if (!householdRegistrationViewModel.isMemberRegistration) {
                val intent = Intent(
                    requireActivity(), HouseholdSummaryActivity::class.java
                )
                intent.putExtra(
                    HouseholdDefinedParams.ID,
                    memberRegistrationViewModel.selectedHouseholdId
                )
                intent.putExtra(
                    HouseholdDefinedParams.isFromHouseHoldRegistration,
                    memberRegistrationViewModel.memberDetailsLiveData.value?.data?.id == null
                )
                startActivity(intent)
            }
            (activity as HouseholdActivity).finish()
        }
    }

    private fun launchHouseholdSummary() {

        (activity as HouseholdActivity).finish()
    }

    private fun autoPopulateDetails(details: HouseholdMemberEntity) {
        details.householdId?.let { id ->
            householdRegistrationViewModel.householdId = id
        }
        formGenerator.getViewByTag(name)?.let { view ->
            formGenerator.setValueForView(details.name, view)
        }

        val title = if (details.householdHeadRelationship.isEmpty() ||
            !details.householdHeadRelationship.equals(HouseholdHead, true)) {
            getString(R.string.relationship_to_household_head)
        } else {
            getString(R.string.relationship_to_household)
        }
        updateRelationShipSpinnerTitle(title)

        val canDisableHHRelation = !(arguments?.getBoolean(HouseholdDefinedParams.isPhuWalkInsFlow) == true || details.householdHeadRelationship.isEmpty())

        if (canDisableHHRelation) {
            formGenerator.getViewByTag(householdHeadRelationship)?.let { view ->
                val relationship =
                    if (details.householdHeadRelationship.contains(getString(R.string.separator_hyphen))) {
                        details.householdHeadRelationship.substringBefore(getString(R.string.separator_hyphen))
                    } else details.householdHeadRelationship
                view.isEnabled = false
                formGenerator.setValueForView(relationship, view)
            }
            formGenerator.getViewByTag(otherFamilyMember)?.let { view ->
                val relationship =
                    if (details.householdHeadRelationship.contains(getString(R.string.separator_hyphen))) {
                        details.householdHeadRelationship.substringAfter(getString(R.string.separator_hyphen))
                    } else details.householdHeadRelationship
                formGenerator.disableView(view, requireContext())
                formGenerator.setValueForView(relationship, view)
            }
        } else {
            val view =
                formGenerator.getViewByTag(DefinedParams.HouseholdHeadRelationship) as AppCompatSpinner
            (view.adapter as CustomSpinnerAdapter).removeItemById(HouseholdHead)
        }

        formGenerator.getViewByTag(phoneNumber)?.let { view ->
            formGenerator.setValueForView(details.phoneNumber, view)
        }
        formGenerator.getViewByTag(phoneNumberCategory)?.let { view ->
            formGenerator.setValueForView(details.phoneNumberCategory, view)
        }
        details.gender.let {
            when (it) {
                male -> {
                    singleSelectValueOption(
                        male,
                        gender
                    )
                }

                female -> {
                    singleSelectValueOption(
                        female,
                        gender
                    )
                }

                else -> {}
            }
            if (details.gender.isNotBlank()) {
                formGenerator.disableSingleSelection(gender)
            }
        }
        details.isPregnant?.let {
            when (getBooleanAsString(it)) {
                yes -> {
                    singleSelectValueOption(
                        Yes.lowercase(),
                        isPregnant
                    )
                }

                no -> {
                    singleSelectValueOption(
                        No.lowercase(),
                        isPregnant
                    )
                }
            }
        }
        details.dateOfBirth.let {
            val dateOfBirth =
                DateUtils.convertDateFormat(it, DATE_FORMAT_yyyyMMddHHmmssZZZZZ, DATE_ddMMyyyy)
            val dateDob = DateUtils.convertStringToDate(it, DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
            formGenerator.getViewByTag(MemberRegistration.dateOfBirth)?.let { view ->
                if (memberRegistrationViewModel.isPhuWalkInsFlow == false && dateOfBirth.isNotBlank()) {
                    formGenerator.disableView(view, requireContext())
                }
                formGenerator.setValueForView(dateOfBirth, view)
            }

            dateDob?.let { dob ->
                formGenerator.fillDetailsOnDatePickerSet(dob, false)
            }
                formGenerator.getViewByTag(DateOfBirth + errorSuffix)?.apply {
                    visibility = View.GONE
                }

        }
    }


    private fun singleSelectValueOption(value: String, key: String) {
        formGenerator.getViewByTag("${value}_${key}")
            ?.let { view ->
                if (view is TextView) {
                    view.isSelected = true
                    view.performClick()
                }
            }
    }


    private fun setListener() {
        binding.btnSubmit.setOnClickListener(this)
        binding.btnStartAssessment.setOnClickListener(this)
        binding.btnSubmitPhu.setOnClickListener(this)
    }

    private fun initializeView() {
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, null, this, binding.scrollView, translate = SecuredPreference.getIsTranslationEnabled()
        ) { map, id->
            if (id == DateOfBirth) {
                val month = map["month"] as? Int
                val week = map["week"] as? Int
                if (month in 0..11 && week in 0..4) {
                    formGenerator.getViewByTag(DateOfBirth + errorSuffix)?.apply {
                        visibility = View.GONE
                    }
                } else {
                    formGenerator.getViewByTag(DateOfBirth + errorSuffix)?.apply {
                        visibility = View.VISIBLE
                    }.takeIf { it is TextView }?.let { textView ->
                        (textView as TextView).text =
                            getString(R.string.please_select_a_valid_value_month)
                    }
                }
            }
        }
    }

    private fun updateRelationShipSpinnerTitle(title: String) {
        val spinnerTitle =
            formGenerator.getViewByTag(DefinedParams.HouseholdHeadRelationship + titleSuffix)
        spinnerTitle?.let {
            val tvTitle = it as TextView
            tvTitle.text = title
        }
    }

    private fun handleRelationshipSpinner() {
        val view =
            formGenerator.getViewByTag(DefinedParams.HouseholdHeadRelationship) as AppCompatSpinner
        householdRegistrationViewModel.householdEntityDetail?.let {
            if (it.id == 0L) {
                updateRelationShipSpinnerTitle(getString(R.string.relationship_to_household))
                val index =
                    (view.adapter as CustomSpinnerAdapter).getIndexOfItemById(HouseholdHead)
                view.setSelection(index, true)
                view.isEnabled = false
                householdRegistrationViewModel.householdEntityDetail?.let { details ->
                    formGenerator.getViewByTag(phoneNumber)?.let { view ->
                        formGenerator.setValueForView(details.headPhoneNumber, view)
                        updateMobileNumberCategoryForHead(details.headPhoneNumberCategory)
                    }
                }
            }
        } ?: kotlin.run {
            if (householdRegistrationViewModel.memberID == -1L) {
                updateRelationShipSpinnerTitle(getString(R.string.relationship_to_household_head))
                (view.adapter as CustomSpinnerAdapter).removeItemById(HouseholdHead)
            }
        }
    }

    private fun updateMobileNumberCategoryForHead(category: String?) {
        category?.let {
            val view = formGenerator.getViewByTag(headPhoneNumberCategory) as AppCompatSpinner
            val index = (view.adapter as CustomSpinnerAdapter).getIndexOfItemById(it)
            view.setSelection(index, true)
        }
    }

    private fun handleAddNewMember() {
        memberRegistrationViewModel.addNewMember =
            arguments?.getBoolean(AddNewMember, false) ?: false
        if (memberRegistrationViewModel.addNewMember) {
            UserDetail.startDateTime = CommonUtils.getCurrentDateTimeInLocalTime()
            UserDetail.eventName = AddNewMember
        } else {
            UserDetail.startDateTime = CommonUtils.getCurrentDateTimeInLocalTime()
            UserDetail.eventName = EditNewMember
        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        if (localDataCache is String) {
            householdRegistrationViewModel.loadVillageDataCacheByType(id, localDataCache)
        }

    }

    override fun onPopulate(targetId: String) {
    }

    override fun onCheckBoxDialogueClicked(
        id: String, serverViewModel: FormLayout, resultMap: Any?
    ) {
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?
    ) {
    }


    private fun showInValidDob(message: String) {
        formGenerator.getViewByTag(DateOfBirth + errorSuffix)?.apply {
            visibility = View.VISIBLE
        }.takeIf { it is TextView }?.let { textView->(textView as TextView).text=
            message
        }
    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        resultMap?.let { map ->
            if (memberRegistrationViewModel.startAssessment == true) {
                memberRegistrationViewModel.setUserJourney(AnalyticsDefinedParams.STARTASSESSMENTTRIGGERED)
            } else {
                memberRegistrationViewModel.setUserJourney(AnalyticsDefinedParams.SUBMITBUTTONTRIGGERED)
            }
            // Hide Error message
            formGenerator.getViewByTag(DateOfBirth + errorSuffix)?.apply {
                visibility = View.GONE
            }

            val month = map[Month] as? Int
            val week = map[Week] as? Int
            // Month and Week field validation
            if (month !in 0..11 || week !in 0..4) {
                showInValidDob(getString(R.string.please_select_a_valid_value_month))
                return
            }

            // Add member from medical review
            if (memberRegistrationViewModel.medicalReviewFlow) {
                memberRegistrationViewModel.addNewMember(map, formGenerator,location=householdRegistrationViewModel.getCurrentLocation()
                )
                return
            }

            // Household Member Create or Edit
            val dob = map[dateOfBirth] as String
            if (householdRegistrationViewModel.isMemberRegistration || householdRegistrationViewModel.memberID != -1L) {
                val relation = map[householdHeadRelationship] as String

                // Showing warning for only new member
                if (householdRegistrationViewModel.isMemberRegistration) {
                    val headDob = memberRegistrationViewModel.householdHeadDobLiveData.value
                    isValidRelationAge(requireContext(), dob, relation, headDob)?.let { validAgeErrorMessage ->
                        showInValidDob(validAgeErrorMessage)
                        return
                    }
                }

                if (memberRegistrationViewModel.isPhuWalkInsFlow == true) {
                    householdRegistrationViewModel.updateMemberAsAssigned(
                        arguments?.getLong(
                            com.medtroniclabs.spice.common.DefinedParams.FhirMemberID
                        )
                    )
                }

                val location = Location("").apply {
                    latitude = SecuredPreference.getDouble(SecuredPreference.EnvironmentKey.CURRENT_LATITUDE.name)
                    longitude = SecuredPreference.getDouble(SecuredPreference.EnvironmentKey.CURRENT_LONGITUDE.name)
                }

                  memberRegistrationViewModel.registerMember(
                      map,
                      householdRegistrationViewModel.householdId,
                      location= location
                  )
                return
            }

            // Household with Member create
            val memberDOB =
                LocalDate.parse(dob, DateTimeFormatter.ofPattern(DATE_FORMAT_yyyyMMddHHmmssZZZZZ))
            if (!isValidMinAge(memberDOB)) {
                showInValidDob(getString(R.string.age_validation_household_head))
                return
            }

            householdRegistrationViewModel.householdEntityDetail?.let { householdEntity ->
                // For Household head
                memberRegistrationViewModel.registerHouseThenMember(
                    householdEntity,
                    map,
                    householdRegistrationViewModel.getCurrentLocation(),
                    householdRegistrationViewModel.initialValue,
                    householdRegistrationViewModel.signatureFilename
                )
            }
        }
    }

    override fun onRenderingComplete() {
        val view = formGenerator.getViewByTag(villageId + formGenerator.rootSuffix)
        val relationSipView =
            formGenerator.getViewByTag(MedicalReviewDefinedParams.HH_RELATIONSHIP + formGenerator.rootSuffix)
        if (memberRegistrationViewModel.medicalReviewFlow) {
            view?.visible()
            relationSipView?.gone()
        } else {
            view?.gone()
            relationSipView?.visible()
            if (householdRegistrationViewModel.memberID != -1L) {
                memberRegistrationViewModel.getMemberDetailsByID(householdRegistrationViewModel.memberID)
            }
        }
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
        formGenerator.handlePregnancyCardBasedOnAgeAndWeeks()
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnStartAssessment -> {
                withLocationCheck({
                memberRegistrationViewModel.startAssessment = true
                formGenerator.formSubmitAction(v)})
            }

            R.id.btnSubmit -> {
                withLocationCheck({
                    memberRegistrationViewModel.startAssessment = false
                    formGenerator.formSubmitAction(v)
                })
            }

            R.id.btnSubmitPhu -> {
                withLocationCheck({
                    memberRegistrationViewModel.startAssessment = false
                    formGenerator.formSubmitAction(v)
                })
            }
        }
    }


    // on MR add new member submit
    fun medicalReviewAddMember(v: View) {
        formGenerator.formSubmitAction(v)
    }

    private fun onPhuAddMember() {
        memberRegistrationViewModel.isPhuWalkInsFlow =
            arguments?.getBoolean(HouseholdDefinedParams.isPhuWalkInsFlow, false)
        if (memberRegistrationViewModel.isPhuWalkInsFlow == true) {
            binding.bottomNavigationView.gone()
            binding.bottomNavigationViewPhuSubmit.visible()
        }
        val scrollView = binding.scrollView
        val bottomNavigationView = binding.bottomNavigationView
        val bottomNavigationViewPhuSubmit = binding.bottomNavigationViewPhuSubmit

        bottomNavigationView.viewTreeObserver.addOnGlobalLayoutListener {
            val layoutParams = scrollView.layoutParams as ConstraintLayout.LayoutParams

            if (bottomNavigationView.visibility == View.GONE) {
                layoutParams.bottomToTop = bottomNavigationViewPhuSubmit.id
            } else {
                layoutParams.bottomToTop = bottomNavigationView.id
            }
            scrollView.layoutParams = layoutParams
        }
    }

    fun getEnteredInputs(): Boolean {
        return formGenerator.getResultMap().isNotEmpty()
    }


}