package org.medtroniclabs.uhis.ui.member

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams.AddNewMember
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams.EditNewMember
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsUtils
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.startBackgroundOfflineSync
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import org.medtroniclabs.uhis.common.DateUtils.DATE_ddMMyyyy
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.common.DefinedParams.DOB
import org.medtroniclabs.uhis.common.DefinedParams.HOUSEHOLD_MEMBER_REGISTRATION
import org.medtroniclabs.uhis.common.DefinedParams.MEMBER_ID
import org.medtroniclabs.uhis.common.DefinedParams.Other
import org.medtroniclabs.uhis.common.DefinedParams.female
import org.medtroniclabs.uhis.common.DefinedParams.isMemberRegistration
import org.medtroniclabs.uhis.common.DefinedParams.male
import org.medtroniclabs.uhis.common.EntityMapper.getResultSpinnerMapList
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.data.model.RecommendedDosageListModel
import org.medtroniclabs.uhis.databinding.FragmentMemberRegistrationBinding
import org.medtroniclabs.uhis.db.entity.HouseholdMemberEntity
import org.medtroniclabs.uhis.db.entity.VillageEntity
import org.medtroniclabs.uhis.formgeneration.FormGenerator
import org.medtroniclabs.uhis.formgeneration.config.DefinedParams.titleSuffix
import org.medtroniclabs.uhis.formgeneration.extension.markMandatory
import org.medtroniclabs.uhis.formgeneration.listener.FormEventListener
import org.medtroniclabs.uhis.formgeneration.model.FormLayout
import org.medtroniclabs.uhis.formgeneration.model.FormResponse
import org.medtroniclabs.uhis.formgeneration.utility.CustomSpinnerAdapter
import org.medtroniclabs.uhis.mappingkey.HouseHoldRegistration.villageId
import org.medtroniclabs.uhis.mappingkey.MemberRegistration
import org.medtroniclabs.uhis.mappingkey.MemberRegistration.dateOfBirth
import org.medtroniclabs.uhis.mappingkey.MemberRegistration.gender
import org.medtroniclabs.uhis.mappingkey.MemberRegistration.isValidMinAge
import org.medtroniclabs.uhis.mappingkey.MemberRegistration.name
import org.medtroniclabs.uhis.mappingkey.MemberRegistration.phoneNumber
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.DateOfBirth
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.errorSuffix
import org.medtroniclabs.uhis.ui.dialog.SuccessDialogFragment
import org.medtroniclabs.uhis.ui.home.AssessmentToolsActivity
import org.medtroniclabs.uhis.ui.household.HouseholdActivity
import org.medtroniclabs.uhis.ui.household.HouseholdDefinedParams
import org.medtroniclabs.uhis.ui.household.summary.HouseholdSummaryActivity
import org.medtroniclabs.uhis.ui.household.viewmodel.HouseRegistrationViewModel
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewDefinedParams
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MemberRegistrationFragment : BaseFragment(), FormEventListener, View.OnClickListener {
    private lateinit var binding: FragmentMemberRegistrationBinding
    private lateinit var formGenerator: FormGenerator
    private val memberRegistrationViewModel: MemberRegistrationViewModel by activityViewModels()
    private val householdRegistrationViewModel: HouseRegistrationViewModel by activityViewModels()

    private var pendingGuardianId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMemberRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        setListener()
        initializeFlow()
        attachObserver()
        handleAddNewMember()
        val eventType =
            if (householdRegistrationViewModel.isMemberRegistration || householdRegistrationViewModel.memberID != -1L) {
                if (householdRegistrationViewModel.isMemberRegistration) {
                    AddNewMember
                } else {
                    EditNewMember
                }
            } else {
                AnalyticsDefinedParams.MemberRegistration
            }

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
                        AnalyticsDefinedParams.HouseholdCreation to (
                            arguments?.getString(
                                AnalyticsDefinedParams.StartDate,
                            ) ?: ""
                        )
                    }
                    memberRegistrationViewModel.setAnalyticsData(
                        startDate,
                        eventName = title,
                        isCompleted = true,
                    )

                    (activity as BaseActivity?)?.hideLoading()
                    resourceState.data?.let {
                        if (arguments?.getBoolean(HouseholdDefinedParams.IS_PHU_WALK_INS_FLOW) == true) {
                            requireActivity().startBackgroundOfflineSync()
                            val existingFragment =
                                childFragmentManager.findFragmentByTag(
                                    SuccessDialogFragment.TAG,
                                )
                            if (existingFragment == null) {
                                SuccessDialogFragment
                                    .newInstance(isPhuLink = true)
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

                        // Check if this is the first member (household just created, no existing members)
                        val isFirstMember = !householdRegistrationViewModel.isMemberRegistration &&
                            householdRegistrationViewModel.memberID == -1L &&
                            householdRegistrationViewModel.householdEntityDetail != null &&
                            householdRegistrationViewModel.householdId == -1L

                        if (isFirstMember) {
                            // Update activity title
                            (activity as? HouseholdActivity)?.setTitle(getString(R.string.household_head_registration))

                            // Update name field label to "Household head name"
                            formGenerator.getViewByTag(MemberRegistration.name + titleSuffix)?.let { view ->
                                if (view is TextView) {
                                    view.text = getString(R.string.household_head_name)
                                    // Check if the field is mandatory and add asterisk if needed
                                    val nameFieldLayout = formGenerator.getServerData()?.find { it.id == MemberRegistration.name }
                                    if (nameFieldLayout?.isMandatory == true) {
                                        view.markMandatory()
                                    }
                                }
                            }

                            // Auto-check isHouseholdHead checkbox and add to result map
                            formGenerator.getViewByTag(MemberRegistration.isHouseholdHead)?.let { view ->
                                if (view is CheckBox) {
                                    view.isChecked = true
                                    // Add the value to the result map
                                    formGenerator.getResultMap()[MemberRegistration.isHouseholdHead] = true
                                }
                            }
                        }

                        // Set up id_type listener to enable national_id
                        formGenerator.getViewByTag(MemberRegistration.idType)?.let { view ->
                            if (view is AppCompatSpinner) {
                                val existingListener = view.onItemSelectedListener
                                view.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(
                                        parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long,
                                    ) {
                                        // Call existing listener first
                                        existingListener?.onItemSelected(parent, view, position, id)

                                        // Enable/disable national_id based on selection
                                        val adapter = parent?.adapter
                                        if (adapter is CustomSpinnerAdapter) {
                                            val selectedItem = adapter.getData(position)
                                            selectedItem?.let { item ->
                                                val selectedId = item[org.medtroniclabs.uhis.formgeneration.config.DefinedParams.ID]
                                                val isDefaultOption = selectedId == "-1" || selectedId == org.medtroniclabs.uhis.common.DefinedParams.DefaultID
                                                formGenerator.getViewByTag(MemberRegistration.nationalId)?.let { nationalIdView ->
                                                    nationalIdView.isEnabled = !isDefaultOption
                                                }
                                            }
                                        }
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                        existingListener?.onNothingSelected(parent)
                                    }
                                }
                            }
                        }
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
                        MedicalReviewDefinedParams.MEMBER_REG,
                        bundleOf(
                            MedicalReviewDefinedParams.Notes to true,
                        ),
                    )
                }

                ResourceState.ERROR -> {
                    (activity as BaseActivity?)?.hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {
                    }
                }
            }
        }

        householdRegistrationViewModel.guardianMembers.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.SUCCESS -> {
                    resource.data?.let { data ->
                        val resultMap = getResultSpinnerMapList(data)
                        resultMap.add(
                            0,
                            formGenerator.createDefaultMap(),
                        )
                        resultMap.add(
                            hashMapOf(
                                DefinedParams.ID to MemberRegistration.ADD_GUARDIAN_ID,
                                DefinedParams.NAME to getString(R.string.add_guardian),
                            ),
                        )
                        formGenerator.spinnerDataInjection(data, resultMap, false)

                        // Set guardian ID if available (for edit mode)
                        pendingGuardianId?.let { guardianId ->
                            if (guardianId != 0L) {
                                formGenerator.getViewByTag(MemberRegistration.ID_GUARDIAN)?.let { view ->
                                    formGenerator.setValueForView(guardianId, view)
                                }
                            }
                            pendingGuardianId = null // Clear after setting
                        }
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

    private fun launchSummaryOrAssessmentPage() {
        requireActivity().startBackgroundOfflineSync()
        if (memberRegistrationViewModel.startAssessment == true) {
            val intent = Intent(requireActivity(), AssessmentToolsActivity::class.java)
            memberRegistrationViewModel.memberRegistrationLiveData.value?.data.let {
                intent.putExtra(MEMBER_ID, it ?: -1)
            }
            intent.putExtra(DOB, memberRegistrationViewModel.memberDob)
            startActivity(intent)
            (activity as HouseholdActivity).finish()
        } else {
            if (!householdRegistrationViewModel.isMemberRegistration) {
                val intent = Intent(
                    requireActivity(),
                    HouseholdSummaryActivity::class.java,
                )
                intent.putExtra(
                    DefinedParams.householdId,
                    memberRegistrationViewModel.selectedHouseholdId,
                )
                intent.putExtra(
                    HouseholdDefinedParams.IS_FROM_HOUSEHOLD_REGISTRATION,
                    memberRegistrationViewModel.memberDetailsLiveData.value
                        ?.data
                        ?.id == null,
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

        formGenerator.getViewByTag(phoneNumber)?.let { view ->
            formGenerator.setValueForView(details.phoneNumber, view)
        }

        formGenerator.getViewByTag(MemberRegistration.idType)?.let { view ->
            formGenerator.setValueForView(details.idType, view)
        }

        formGenerator.getViewByTag(MemberRegistration.nationalId)?.let { view ->
            formGenerator.setValueForView(details.nationalId, view)
            // Enable if idType is set
            view.isEnabled = details.idType.isNotEmpty()
        }

        formGenerator.getViewByTag(MemberRegistration.isHouseholdHead)?.let { view ->
            if (view is CheckBox) {
                view.isChecked = details.isHouseholdHead
            }
        }

        details.gender.let {
            when (it) {
                male -> {
                    singleSelectValueOption(
                        male,
                        gender,
                    )
                }

                female -> {
                    singleSelectValueOption(
                        female,
                        gender,
                    )
                }

                Other -> {
                    singleSelectValueOption(
                        Other,
                        gender,
                    )
                }

                else -> {}
            }
            if (details.gender.isNotBlank()) {
                formGenerator.disableSingleSelection(gender)
            }
        }
        details.dateOfBirth.let { originalDobUtc ->
            val dateOfBirth =
                DateUtils.convertDateFormat(originalDobUtc, DATE_FORMAT_yyyyMMddHHmmssZZZZZ, DATE_ddMMyyyy)
            val dateDob = DateUtils.convertStringToDate(originalDobUtc, DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
            formGenerator.getViewByTag(MemberRegistration.dateOfBirth)?.let { view ->
                if (memberRegistrationViewModel.isPhuWalkInsFlow == false && dateOfBirth.isNotBlank()) {
                    formGenerator.disableView(view)
                }
                // Store original UTC value before setting view value for AgeOrDob edit mode
                formGenerator.setDobValueForAgeOrDob(MemberRegistration.dateOfBirth, originalDobUtc, dateOfBirth, view)
            }

            dateDob?.let { dob ->
                formGenerator.fillDetailsOnDatePickerSet(
                    dob,
                    memberRegistrationViewModel.isPhuWalkInsFlow == true,
                )
            }
            formGenerator.getViewByTag(DateOfBirth + errorSuffix)?.apply {
                visibility = View.GONE
            }
            handleDob(details.dateOfBirth)
        }
        details.maritalStatus?.let {
            singleSelectValueOption(
                it,
                MemberRegistration.ID_MARITAL_STATUS,
            )
        }
        details.disability?.let {
            singleSelectValueOption(
                it,
                MemberRegistration.ID_DISABILITY,
            )
        }
        formGenerator.getViewByTag(MemberRegistration.ID_GUARDIAN)?.let {
            val guardianId = details.guardianId
            if (guardianId != null && guardianId != 0L) {
                // Store guardian ID to set after list is loaded
                pendingGuardianId = guardianId
                formGenerator.setValueForView(guardianId, it)
            }
        }
    }

    private fun singleSelectValueOption(
        value: String,
        key: String,
    ) {
        formGenerator
            .getViewByTag("${value}_$key")
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
            requireContext(),
            binding.llForm,
            this,
            binding.scrollView,
            translate = SecuredPreference.getIsTranslationEnabled(),
        ) { map, id ->
            if (id == DateOfBirth) {
                // This is AgeOrDob component - hide error (validation handled elsewhere)
                formGenerator.getViewByTag(DateOfBirth + errorSuffix)?.apply {
                    visibility = View.GONE
                }
                val dateOfBirth = map[id] as? String
                handleDob(dateOfBirth)
            } else if (id == MemberRegistration.ID_GUARDIAN && formGenerator.isViewVisible(id) && formGenerator.isViewEnabled(id)) {
                val selectedId = CommonUtils.getLongOrNull(map[id]) ?: 0
                if (selectedId == MemberRegistration.ADD_GUARDIAN_ID) {
                    handleAddGuardian()
                }
            }
        }
    }

    private fun handleDob(dateOfBirth: String?) {
        val dateOfBirthDate = DateUtils.parseDate(dateOfBirth) ?: return
        // Flow is for member create or edit then check for guardian otherwise ignore
        if (!householdRegistrationViewModel.isCreateHouseholdForPhu &&
            (householdRegistrationViewModel.isMemberRegistration || householdRegistrationViewModel.memberID != -1L)
        ) {
            formGenerator.getViewByTag(MemberRegistration.ID_GUARDIAN + formGenerator.rootSuffix)?.let { guardianView ->
                val twoYearBeforeDate = LocalDate.now().minusYears(MemberRegistration.MAX_AGE_GUARDIAN)
                // Add guardian if age is <= 2 years of age
                if (dateOfBirthDate >= twoYearBeforeDate) {
                    guardianView.visible()
                } else {
                    guardianView.gone()
                    formGenerator.resetChildViews(guardianView)
                }
            }
        }
        formGenerator.getViewByTag(MemberRegistration.ID_MARITAL_STATUS + formGenerator.rootSuffix)?.let { maritalStatusView ->
            val fourteenYearBeforeDate = LocalDate.now().minusYears(MemberRegistration.MIN_AGE_MARITAL_STATUS)
            // Add marital status if age >= 14 years of age
            if (fourteenYearBeforeDate >= dateOfBirthDate) {
                maritalStatusView.visible()
            } else {
                maritalStatusView.gone()
                formGenerator.resetChildViews(maritalStatusView)
            }
        }
    }

    /**
     * Navigates to add guardian/member with an alert popup
     */
    private fun handleAddGuardian() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true,
        ) { isPositive ->
            if (isPositive) {
                if (householdRegistrationViewModel.householdId != -1L) {
                    val intent =
                        Intent(requireActivity(), HouseholdActivity::class.java)
                    intent.putExtra(isMemberRegistration, true)
                    intent.putExtra(DefinedParams.householdId, householdRegistrationViewModel.householdId)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        }
    }

    private fun handleAddNewMember() {
        memberRegistrationViewModel.addNewMember =
            arguments?.getBoolean(AddNewMember, false) ?: false
        if (memberRegistrationViewModel.addNewMember) {
            UserDetail.startDateTime = AnalyticsUtils.getCurrentDateTimeInLocalTime()
            UserDetail.eventName = AddNewMember
        } else {
            UserDetail.startDateTime = AnalyticsUtils.getCurrentDateTimeInLocalTime()
            UserDetail.eventName = EditNewMember
        }
    }

    override fun loadLocalCache(
        id: String,
        localDataCache: Any,
        selectedParent: Long?,
    ) {
        if (localDataCache is String) {
            householdRegistrationViewModel.loadVillageDataCacheByType(id, localDataCache)
        }
    }

    override fun onPopulate(targetId: String) {
    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        formLayout: FormLayout,
        resultMap: Any?,
    ) {
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?,
    ) {
    }

    private fun showInValidDob(message: String) {
        formGenerator
            .getViewByTag(DateOfBirth + errorSuffix)
            ?.apply {
                visibility = View.VISIBLE
            }.takeIf { it is TextView }
            ?.let { textView ->
                (textView as TextView).text =
                    message
            }
    }

    override fun onFormSubmit(
        resultMap: HashMap<String, Any>?,
        serverData: List<FormLayout>?,
    ) {
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

            // Add member from medical review
            if (memberRegistrationViewModel.medicalReviewFlow) {
                memberRegistrationViewModel.addNewMember(
                    map,
                    formGenerator,
                    location = householdRegistrationViewModel.getCurrentLocation(),
                )
                return
            }

            // Household Member Create or Edit
            val dob = map[dateOfBirth] as String
            if (!householdRegistrationViewModel.isCreateHouseholdForPhu &&
                (householdRegistrationViewModel.isMemberRegistration || householdRegistrationViewModel.memberID != -1L)
            ) {
                if (memberRegistrationViewModel.isPhuWalkInsFlow == true) {
                    val memberLocalId = memberRegistrationViewModel.memberDetailsLiveData.value
                        ?.data
                        ?.id
                    val fhirMemberId = arguments?.getLong(org.medtroniclabs.uhis.common.DefinedParams.FhirMemberID)
                    householdRegistrationViewModel.updateMemberAsAssigned(fhirMemberId, memberLocalId, householdRegistrationViewModel.householdId)
                }

                val location = Location("").apply {
                    latitude = SecuredPreference.getDouble(SecuredPreference.EnvironmentKey.CURRENT_LATITUDE.name)
                    longitude = SecuredPreference.getDouble(SecuredPreference.EnvironmentKey.CURRENT_LONGITUDE.name)
                }

                memberRegistrationViewModel.registerMember(
                    map,
                    householdRegistrationViewModel.householdId,
                    location = location,
                )
                return
            }

            // Household with Member create
            val memberDOB =
                LocalDate.parse(dob, DateTimeFormatter.ofPattern(DATE_FORMAT_yyyyMMddHHmmssZZZZZ))
            if (!isValidMinAge(memberDOB)) {
                showInValidDob(getString(R.string.age_validation_household_head, MemberRegistration.MIN_AGE_HH_HEAD))
                return
            }

            householdRegistrationViewModel.householdEntityDetail?.let { householdEntity ->
                if (memberRegistrationViewModel.isPhuWalkInsFlow == true) {
                    householdRegistrationViewModel.updateMemberAsAssigned(
                        arguments?.getLong(
                            org.medtroniclabs.uhis.common.DefinedParams.FhirMemberID,
                        ),
                    )
                }

                // Remove guardian key for household head
                map.remove(MemberRegistration.ID_GUARDIAN)

                // For Household head
                memberRegistrationViewModel.registerHouseThenMember(
                    householdEntity,
                    map,
                    householdRegistrationViewModel.getCurrentLocation(),
                    householdRegistrationViewModel.initialValue,
                    householdRegistrationViewModel.signatureFilename,
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

    override fun onUpdateInstruction(
        id: String,
        selectedId: Any?,
    ) {
    }

    override fun onInformationHandling(
        id: String,
        noOfDays: Int,
        enteredDays: Int?,
        resultMap: HashMap<String, Any>?,
    ) {
    }

    override fun onAgeCheckForPregnancy() {
        formGenerator.handlePregnancyCardBasedOnAgeAndWeeks()
    }

    override fun handleMandatoryCondition(formLayout: FormLayout?) {
    }

    override fun onAgeUpdateListener(
        age: Int,
        serverData: List<FormLayout>?,
        resultHashMap: HashMap<String, Any>,
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
                    formGenerator.formSubmitAction(v)
                })
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
            arguments?.getBoolean(HouseholdDefinedParams.IS_PHU_WALK_INS_FLOW, false)
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

    fun getEnteredInputs(): Boolean = formGenerator.getResultMap().isNotEmpty()
}
