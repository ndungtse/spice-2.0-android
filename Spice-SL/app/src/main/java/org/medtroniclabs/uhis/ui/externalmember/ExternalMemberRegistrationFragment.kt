package org.medtroniclabs.uhis.ui.externalmember

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsUtils
import org.medtroniclabs.uhis.appextensions.startBackgroundOfflineSync
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import org.medtroniclabs.uhis.common.DateUtils.DATE_ddMMyyyy
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.common.DefinedParams.DOB
import org.medtroniclabs.uhis.common.DefinedParams.EXTERNAL_MEMBER_REGISTRATION
import org.medtroniclabs.uhis.common.DefinedParams.MEMBER_ID
import org.medtroniclabs.uhis.common.EntityMapper.getResultSpinnerMapList
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.data.model.RecommendedDosageListModel
import org.medtroniclabs.uhis.databinding.FragmentExternalMemberRegistrationBinding
import org.medtroniclabs.uhis.db.entity.HouseholdMemberEntity
import org.medtroniclabs.uhis.db.entity.VillageEntity
import org.medtroniclabs.uhis.formgeneration.FormGenerator
import org.medtroniclabs.uhis.formgeneration.listener.FormEventListener
import org.medtroniclabs.uhis.formgeneration.model.FormLayout
import org.medtroniclabs.uhis.formgeneration.model.FormResponse
import org.medtroniclabs.uhis.formgeneration.utility.CustomSpinnerAdapter
import org.medtroniclabs.uhis.mappingkey.HouseHoldRegistration.SHASTHYA_SHEBIKA_ID
import org.medtroniclabs.uhis.mappingkey.HouseHoldRegistration.SUB_VILLAGE_ID
import org.medtroniclabs.uhis.mappingkey.HouseHoldRegistration.VILLAGE_ID
import org.medtroniclabs.uhis.mappingkey.MemberRegistration
import org.medtroniclabs.uhis.mappingkey.MemberRegistration.NAME
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.dialog.SuccessDialogFragment
import org.medtroniclabs.uhis.ui.home.AssessmentToolsActivity
import org.medtroniclabs.uhis.ui.household.viewmodel.HouseRegistrationViewModel
import org.medtroniclabs.uhis.ui.member.MemberRegistrationViewModel
import org.medtroniclabs.uhis.formgeneration.config.DefinedParams as FormDefinedParams

@AndroidEntryPoint
class ExternalMemberRegistrationFragment : BaseFragment(), FormEventListener, View.OnClickListener {
    private lateinit var binding: FragmentExternalMemberRegistrationBinding
    private lateinit var formGenerator: FormGenerator
    private val memberRegistrationViewModel: MemberRegistrationViewModel by activityViewModels()
    private val householdRegistrationViewModel: HouseRegistrationViewModel by activityViewModels()
    private var editMemberId: Long = -1L
    private var pendingVillageId: Long? = null
    private var pendingSsId: Long? = null
    private var pendingSubVillageId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentExternalMemberRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        memberRegistrationViewModel.prefetchNationalIds()
        initializeView()
        setListener()
        initializeFlow()
        attachObserver()
        memberRegistrationViewModel.setUserJourney(AnalyticsDefinedParams.AddNewMember)
        UserDetail.startDateTime = AnalyticsUtils.getCurrentDateTimeInLocalTime()
        UserDetail.eventName = AnalyticsDefinedParams.AddNewMember
        withLocationCheck({}, shouldHideProgress = true)
    }

    private fun setListener() {
        binding.btnSubmit.setOnClickListener(this)
        binding.btnStartAssessment.setOnClickListener(this)
    }

    private fun initializeView() {
        formGenerator = FormGenerator(
            requireContext(),
            binding.llForm,
            this,
            binding.scrollView,
            translate = SecuredPreference.getIsTranslationEnabled(),
        ) { map, id ->
            if (id == MemberRegistration.DATE_OF_BIRTH) {
                // This is AgeOrDob component - hide error (validation handled elsewhere)
                formGenerator.hideError(id)
            } else if (id == MemberRegistration.ID_TYPE) {
                val selectedId = map[id] as? String
                val nationalIdView = formGenerator.getViewByTag(MemberRegistration.NATIONAL_ID) as? EditText
                nationalIdView?.let {
                    if (MemberRegistration.IdType.NATIONAL_ID.value == selectedId) {
                        nationalIdView.inputType = InputType.TYPE_CLASS_NUMBER
                        val filters = nationalIdView.filters.toMutableList()
                        filters.add(InputFilter.LengthFilter(MemberRegistration.MAX_LENGTH_NATIONAL_ID))
                        nationalIdView.filters = filters.toTypedArray()
                    } else {
                        nationalIdView.inputType = InputType.TYPE_CLASS_TEXT
                        val filters = nationalIdView.filters.toMutableList()
                        filters.removeIf {
                            it is InputFilter.LengthFilter
                        }
                        nationalIdView.filters = filters.toTypedArray()
                    }
                }
            } else if (id == MemberRegistration.NATIONAL_ID) {
                // This is national id component - hide error (validation handled elsewhere)
                formGenerator.hideError(id)
            }
        }
    }

    private fun initializeFlow() {
        editMemberId = requireActivity().intent.getLongExtra(MEMBER_ID, -1L)
        memberRegistrationViewModel.getFormData(EXTERNAL_MEMBER_REGISTRATION)
    }

    private fun attachObserver() {
        // Observers for external member dropdowns (Union, SS, Village)
        householdRegistrationViewModel.villageListResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { data ->
                        val mapList = getResultSpinnerMapList(data)

                        // Ensure mapList is not empty before injecting
                        if (mapList.isNotEmpty()) {
                            formGenerator.spinnerDataInjection(data, mapList)

                            // Verify data was set - if not, set it directly
                            formGenerator.getViewByTag(VILLAGE_ID)?.let { view ->
                                if (view is AppCompatSpinner) {
                                    val adapter = view.adapter
                                    if (adapter is CustomSpinnerAdapter) {
                                        // Check if adapter has data (more than just default option)
                                        if (adapter.count <= 1) {
                                            // Data wasn't set, set it directly
                                            val finalMapList = ArrayList(mapList)
                                            val mandatory = formGenerator.getServerData()?.find { it.id == VILLAGE_ID }?.isMandatory ?: false
                                            if (!mandatory || finalMapList.size != 1) {
                                                finalMapList.add(
                                                    0,
                                                    hashMapOf(
                                                        DefinedParams.NAME to getString(R.string.please_select),
                                                        DefinedParams.ID to DefinedParams.DefaultID,
                                                    ),
                                                )
                                            }
                                            adapter.setData(finalMapList)
                                        }
                                    }
                                }
                            }

                            // If we have a pending village ID from edit-mode, apply it now
                            applyPendingSelectionIfReady(VILLAGE_ID, pendingVillageId) {
                                pendingVillageId = null
                            }

                            // Auto-select if single village - use ID from mapList (same as household registration)
                            if (data.response is List<*> && data.response.size == 1) {
                                val singleItem = data.response[0]
                                if (singleItem is VillageEntity) {
                                    // Get the ID from the mapList that was created (matches household registration pattern)
                                    val mapItem = mapList.firstOrNull()
                                    mapItem?.let { map ->
                                        val id = map[DefinedParams.ID]
                                        formGenerator.getViewByTag(VILLAGE_ID)?.let { spinnerView ->
                                            // Post to ensure adapter data is set
                                            spinnerView.post {
                                                formGenerator.setValueForView(id, spinnerView)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Show village/union field if we have more than 1
                        if (data.response is List<*> && data.response.size > 1) {
                            formGenerator.getViewByTag(VILLAGE_ID + formGenerator.rootSuffix)?.visible()
                        }
                    }
                }
                else -> {
                    // Invoked if response state is not success
                }
            }
        }

        householdRegistrationViewModel.shasthyaShebikaListResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { data ->
                        formGenerator.spinnerDataInjection(data, getResultSpinnerMapList(data))
                        // Apply pending SS selection after data injection
                        applyPendingSelectionIfReady(SHASTHYA_SHEBIKA_ID, pendingSsId) {
                            pendingSsId = null
                        }
                    }
                }
                else -> {
                    // Invoked if response state is not success
                }
            }
        }

        householdRegistrationViewModel.subVillageListResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { data ->
                        formGenerator.spinnerDataInjection(data, getResultSpinnerMapList(data))
                        // Apply pending Sub-village selection after data injection
                        applyPendingSelectionIfReady(SUB_VILLAGE_ID, pendingSubVillageId) {
                            pendingSubVillageId = null
                        }
                    }
                }
                else -> {
                    // Invoked if response state is not success
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
                    memberRegistrationViewModel.setAnalyticsData(
                        AnalyticsUtils.getCurrentDateTimeInLocalTime(),
                        eventName = AnalyticsDefinedParams.AddNewMember,
                        isCompleted = true,
                    )

                    (activity as BaseActivity?)?.hideLoading()
                    resourceState.data?.let {
                        launchAssessmentOrSuccessPage()
                    }
                }
            }
        }

        memberRegistrationViewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resources ->
            when (resources.state) {
                ResourceState.SUCCESS -> {
                    resources.data?.let { jsonString ->
                        val formLayout: FormResponse = Gson().fromJson(
                            jsonString,
                            object : TypeToken<FormResponse>() {}.type,
                        )
                        formGenerator.populateViews(formLayout.formLayout)
                        if (editMemberId != -1L) {
                            memberRegistrationViewModel.getMemberDetailsByID(editMemberId)
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

        memberRegistrationViewModel.memberDetailsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { data ->
                        autoPopulateDetails(data)
                    }
                }
                else -> {
                    // no-op
                }
            }
        }
    }

    private fun autoPopulateDetails(details: HouseholdMemberEntity) {
        // Simple fields
        formGenerator.getViewByTag(NAME)?.let { view ->
            formGenerator.setValueForView(details.name, view)
        }
        formGenerator.getViewByTag(MemberRegistration.PHONE_NUMBER)?.let { view ->
            formGenerator.setValueForView(details.phoneNumber, view)
        }
        formGenerator.getViewByTag(MemberRegistration.PHONE_NUMBER_CATEGORY)?.let { view ->
            formGenerator.setValueForView(details.phoneNumberCategory, view)
        }
        formGenerator.getViewByTag(MemberRegistration.ID_TYPE)?.let { view ->
            formGenerator.setValueForView(details.idType, view)
        }
        formGenerator.getViewByTag(MemberRegistration.NATIONAL_ID)?.let { view ->
            formGenerator.setValueForView(details.nationalId, view)
            view.isEnabled = details.idType.isNotEmpty()
        }

        // Gender: select then disable like normal edit
        details.gender.let { gender ->
            formGenerator.getViewByTag("${gender}_${MemberRegistration.GENDER}")?.performClick()
            if (gender.isNotBlank()) {
                formGenerator.disableSingleSelection(MemberRegistration.GENDER)
            }
        }

        // DOB handling like member edit: set formatted value and disable field
        val originalDobUtc = details.dateOfBirth
        val dateOfBirth =
            DateUtils.convertDateFormat(
                originalDobUtc,
                DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                DATE_ddMMyyyy,
            )
        val dateDob =
            DateUtils.convertStringToDate(
                originalDobUtc,
                DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            )

        formGenerator.getViewByTag(MemberRegistration.DATE_OF_BIRTH)?.let { view ->
            if (dateOfBirth.isNotBlank()) {
                formGenerator.disableView(view)
            }
            formGenerator.setDobValueForAgeOrDob(
                MemberRegistration.DATE_OF_BIRTH,
                originalDobUtc,
                dateOfBirth,
                view,
            )
        }
        dateDob?.let { dob ->
            formGenerator.fillDetailsOnDatePickerSet(dob, false)
        }
        formGenerator.hideError(MemberRegistration.DATE_OF_BIRTH)
        memberRegistrationViewModel.memberDob = originalDobUtc

        // Marital status & disability
        details.maritalStatus?.let { m ->
            singleSelectValueOption(m, MemberRegistration.ID_MARITAL_STATUS)
        }
        details.disability?.let { d ->
            singleSelectValueOption(d, MemberRegistration.ID_DISABILITY)
        }

        // Defer spinner cascade selections until data injection observers
        pendingVillageId = details.villageId
        pendingSsId = details.shasthyaShebikaId
        pendingSubVillageId = details.subVillageId

        // If dropdown data already loaded before member-details response, apply immediately
        applyPendingSelectionIfReady(VILLAGE_ID, pendingVillageId) {
            pendingVillageId = null
        }
        applyPendingSelectionIfReady(SHASTHYA_SHEBIKA_ID, pendingSsId) {
            pendingSsId = null
        }
        applyPendingSelectionIfReady(SUB_VILLAGE_ID, pendingSubVillageId) {
            pendingSubVillageId = null
        }

        // Lock location selections for external-member edit mode.
        disableLocationFieldsInEditMode()
    }

    private fun singleSelectValueOption(
        value: String,
        key: String,
    ) {
        formGenerator.getViewByTag("${value}_$key")?.let { view ->
            view.isSelected = true
            view.performClick()
        }
    }

    private fun applyPendingSelectionIfReady(
        fieldId: String,
        pendingId: Long?,
        onApplied: () -> Unit,
    ) {
        if (pendingId == null) return
        formGenerator.getViewByTag(fieldId)?.let { view ->
            if (view is AppCompatSpinner) {
                val adapter = view.adapter as? CustomSpinnerAdapter
                adapter?.let {
                    val adapterCount = it.count
                    if (adapterCount > 0) {
                        // Ignore the case where spinner only has the placeholder item.
                        val hasOnlyDefaultOption =
                            adapterCount == 1 &&
                                (
                                    it.getData(0)?.get(FormDefinedParams.ID) == DefinedParams.DefaultID ||
                                        it.getData(0)?.get(FormDefinedParams.ID) == "-1"
                                )
                        if (hasOnlyDefaultOption) return
                        view.post {
                            formGenerator.setValueForView(pendingId, view)
                            onApplied()
                        }
                    }
                }
            }
        }
    }

    private fun disableLocationFieldsInEditMode() {
        if (editMemberId == -1L) return
        formGenerator.getViewByTag(VILLAGE_ID)?.isEnabled = false
        formGenerator.getViewByTag(SHASTHYA_SHEBIKA_ID)?.isEnabled = false
        formGenerator.getViewByTag(SUB_VILLAGE_ID)?.isEnabled = false
    }

    override fun onRenderingComplete() {
        // Trigger initial load for Union dropdown after form is rendered
        householdRegistrationViewModel.loadDataCacheByType(
            VILLAGE_ID,
            "",
        )
    }

    override fun onUpdateInstruction(
        id: String,
        selectedId: Any?,
    ) {
        // Handle dropdown dependencies for external members
        when (id) {
            VILLAGE_ID -> {
                // Union selected - load SS list
                val villageIdLong = CommonUtils.getLongOrNull(selectedId) ?: 0L
                if (villageIdLong != 0L) {
                    householdRegistrationViewModel.loadShasthyaShebikaDataCacheByType(
                        SHASTHYA_SHEBIKA_ID,
                        "",
                    )
                    // During edit prefill, preserve pending child values and avoid clearing.
                    if (pendingSsId == null && pendingSubVillageId == null) {
                        formGenerator.getViewByTag(SHASTHYA_SHEBIKA_ID)?.let { view ->
                            formGenerator.setValueForView(null, view)
                        }
                        formGenerator.getViewByTag(SUB_VILLAGE_ID)?.let { view ->
                            formGenerator.setValueForView(null, view)
                        }
                    }
                }
            }
            SHASTHYA_SHEBIKA_ID -> {
                // SS selected - load Village list
                val shasthyaShebikaIdLong = CommonUtils.getLongOrNull(selectedId) ?: 0L
                if (shasthyaShebikaIdLong != 0L) {
                    householdRegistrationViewModel.loadSubVillageDataCacheByType(
                        SUB_VILLAGE_ID,
                        "",
                        shasthyaShebikaIdLong,
                    )
                    // During edit prefill, preserve pending sub-village and avoid clearing.
                    if (pendingSubVillageId == null) {
                        formGenerator.getViewByTag(SUB_VILLAGE_ID)?.let { view ->
                            formGenerator.setValueForView(null, view)
                        }
                    }
                }
            }
        }
    }

    override fun loadLocalCache(
        id: String,
        localDataCache: Any,
        selectedParent: Long?,
    ) {
        if (localDataCache is String) {
            when (id) {
                VILLAGE_ID -> {
                    householdRegistrationViewModel.loadDataCacheByType(id, localDataCache)
                }
                SHASTHYA_SHEBIKA_ID -> {
                    householdRegistrationViewModel.loadShasthyaShebikaDataCacheByType(id, localDataCache)
                }
                SUB_VILLAGE_ID -> {
                    selectedParent?.let {
                        householdRegistrationViewModel.loadSubVillageDataCacheByType(id, localDataCache, it)
                    }
                }
            }
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

            formGenerator.hideError(MemberRegistration.DATE_OF_BIRTH)
            if (formGenerator.isViewVisible(MemberRegistration.NATIONAL_ID)) {
                val nationalId = map[MemberRegistration.NATIONAL_ID] as? String
                val idType = map[MemberRegistration.ID_TYPE] as? String
                if (MemberRegistration.IdType.NATIONAL_ID.value == idType &&
                    (nationalId == null || !nationalId.isDigitsOnly() || !MemberRegistration.NATIONAL_ID_LENGTH.contains(nationalId.length))
                ) {
                    formGenerator.showError(MemberRegistration.NATIONAL_ID, getString(R.string.national_id_validation))
                    return
                }

                // Unique validation for National ID
                val originalNationalId = memberRegistrationViewModel.memberDetailsLiveData.value
                    ?.data
                    ?.nationalId
                if (MemberRegistration.IdType.NATIONAL_ID.value == idType &&
                    nationalId != originalNationalId &&
                    memberRegistrationViewModel.nationalIdsSet.contains(nationalId)
                ) {
                    formGenerator.showError(MemberRegistration.NATIONAL_ID, getString(R.string.national_id_already_exists))
                    return
                }
            }

            // Register external member (householdId = null)
            val location = Location("").apply {
                latitude = SecuredPreference.getDouble(SecuredPreference.EnvironmentKey.CURRENT_LATITUDE.name)
                longitude = SecuredPreference.getDouble(SecuredPreference.EnvironmentKey.CURRENT_LONGITUDE.name)
            }

            memberRegistrationViewModel.registerMember(
                map,
                householdId = null, // External members have no household
                location = location,
            )
        }
    }

    private fun launchAssessmentOrSuccessPage() {
        requireActivity().startBackgroundOfflineSync()
        if (memberRegistrationViewModel.startAssessment == true) {
            val intent = Intent(requireActivity(), AssessmentToolsActivity::class.java)
            memberRegistrationViewModel.memberRegistrationLiveData.value?.data.let {
                intent.putExtra(MEMBER_ID, it ?: -1)
            }
            intent.putExtra(DOB, memberRegistrationViewModel.memberDob)
            startActivity(intent)
            requireActivity().finish()
        } else {
            val existingFragment =
                childFragmentManager.findFragmentByTag(
                    SuccessDialogFragment.TAG,
                )
            if (existingFragment == null) {
                SuccessDialogFragment
                    .newInstance(isMember = true)
                    .show(childFragmentManager, SuccessDialogFragment.TAG)
            }
        }
    }

    fun getEnteredInputs(): Boolean = formGenerator.getResultMap().isNotEmpty()

    companion object {
        const val ARG_IS_EDIT_MODE = "isEditMode"
    }
}
