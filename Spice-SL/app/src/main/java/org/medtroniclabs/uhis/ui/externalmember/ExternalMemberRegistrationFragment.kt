package org.medtroniclabs.uhis.ui.externalmember

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsUtils
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.startBackgroundOfflineSync
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.common.DefinedParams.DOB
import org.medtroniclabs.uhis.common.DefinedParams.EXTERNAL_MEMBER_REGISTRATION
import org.medtroniclabs.uhis.common.DefinedParams.MEMBER_ID
import org.medtroniclabs.uhis.common.EntityMapper.getResultSpinnerMapList
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.databinding.FragmentExternalMemberRegistrationBinding
import org.medtroniclabs.uhis.db.entity.VillageEntity
import org.medtroniclabs.uhis.formgeneration.FormGenerator
import org.medtroniclabs.uhis.formgeneration.listener.FormEventListener
import org.medtroniclabs.uhis.formgeneration.model.FormLayout
import org.medtroniclabs.uhis.formgeneration.model.FormResponse
import org.medtroniclabs.uhis.formgeneration.utility.CustomSpinnerAdapter
import org.medtroniclabs.uhis.mappingkey.HouseHoldRegistration
import org.medtroniclabs.uhis.mappingkey.HouseHoldRegistration.shasthyaShebikaId
import org.medtroniclabs.uhis.mappingkey.HouseHoldRegistration.subVillageId
import org.medtroniclabs.uhis.mappingkey.HouseHoldRegistration.villageId
import org.medtroniclabs.uhis.mappingkey.MemberRegistration
import org.medtroniclabs.uhis.mappingkey.MemberRegistration.dateOfBirth
import org.medtroniclabs.uhis.mappingkey.MemberRegistration.gender
import org.medtroniclabs.uhis.mappingkey.MemberRegistration.name
import org.medtroniclabs.uhis.mappingkey.MemberRegistration.phoneNumber
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.DateOfBirth
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.errorSuffix
import org.medtroniclabs.uhis.ui.dialog.SuccessDialogFragment
import org.medtroniclabs.uhis.ui.home.AssessmentToolsActivity
import org.medtroniclabs.uhis.ui.household.viewmodel.HouseRegistrationViewModel
import org.medtroniclabs.uhis.ui.member.MemberRegistrationViewModel

@AndroidEntryPoint
class ExternalMemberRegistrationFragment : BaseFragment(), FormEventListener, View.OnClickListener {
    private lateinit var binding: FragmentExternalMemberRegistrationBinding
    private lateinit var formGenerator: FormGenerator
    private val memberRegistrationViewModel: MemberRegistrationViewModel by activityViewModels()
    private val householdRegistrationViewModel: HouseRegistrationViewModel by activityViewModels()

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
            if (id == DateOfBirth) {
                // This is AgeOrDob component - hide error (validation handled elsewhere)
                formGenerator.getViewByTag(DateOfBirth + errorSuffix)?.apply {
                    visibility = View.GONE
                }
            }
        }
    }

    private fun initializeFlow() {
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
                            formGenerator.getViewByTag(villageId)?.let { view ->
                                if (view is AppCompatSpinner) {
                                    val adapter = view.adapter
                                    if (adapter is CustomSpinnerAdapter) {
                                        // Check if adapter has data (more than just default option)
                                        if (adapter.count <= 1) {
                                            // Data wasn't set, set it directly
                                            val finalMapList = ArrayList(mapList)
                                            val mandatory = formGenerator.getServerData()?.find { it.id == villageId }?.isMandatory ?: false
                                            if (!mandatory || finalMapList.size != 1) {
                                                finalMapList.add(0, hashMapOf(
                                                    DefinedParams.NAME to getString(R.string.please_select),
                                                    DefinedParams.ID to DefinedParams.DefaultID
                                                ))
                                            }
                                            adapter.setData(finalMapList)
                                        }
                                    }
                                }
                            }
                            
                            // Auto-select if single village - use ID from mapList (same as household registration)
                            if (data.response is List<*> && data.response.size == 1) {
                                val singleItem = data.response[0]
                                if (singleItem is VillageEntity) {
                                    // Get the ID from the mapList that was created (matches household registration pattern)
                                    val mapItem = mapList.firstOrNull()
                                    mapItem?.let { map ->
                                        val id = map[DefinedParams.ID]
                                        formGenerator.getViewByTag(villageId)?.let { spinnerView ->
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
                            formGenerator.getViewByTag(villageId)?.visible()
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

    override fun onRenderingComplete() {
        // Trigger initial load for Union dropdown after form is rendered
        householdRegistrationViewModel.loadDataCacheByType(
            villageId,
            "",
        )
    }

    override fun onUpdateInstruction(
        id: String,
        selectedId: Any?,
    ) {
        // Handle dropdown dependencies for external members
        when (id) {
            villageId -> {
                // Union selected - load SS list
                val villageIdLong = CommonUtils.getLongOrNull(selectedId) ?: 0L
                if (villageIdLong != 0L) {
                    householdRegistrationViewModel.loadShasthyaShebikaDataCacheByType(
                        shasthyaShebikaId,
                        "",
                    )
                    // Clear SS and Village selections
                    formGenerator.getViewByTag(shasthyaShebikaId)?.let { view ->
                        formGenerator.setValueForView(null, view)
                    }
                    formGenerator.getViewByTag(subVillageId)?.let { view ->
                        formGenerator.setValueForView(null, view)
                    }
                }
            }
            shasthyaShebikaId -> {
                // SS selected - load Village list
                val shasthyaShebikaIdLong = CommonUtils.getLongOrNull(selectedId) ?: 0L
                if (shasthyaShebikaIdLong != 0L) {
                    householdRegistrationViewModel.loadSubVillageDataCacheByType(
                        subVillageId,
                        "",
                        shasthyaShebikaIdLong,
                    )
                    // Clear Village selection
                    formGenerator.getViewByTag(subVillageId)?.let { view ->
                        formGenerator.setValueForView(null, view)
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
                villageId -> {
                    householdRegistrationViewModel.loadDataCacheByType(id, localDataCache)
                }
                shasthyaShebikaId -> {
                    householdRegistrationViewModel.loadShasthyaShebikaDataCacheByType(id, localDataCache)
                }
                subVillageId -> {
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
        dosageListModel: ArrayList<org.medtroniclabs.uhis.data.model.RecommendedDosageListModel>?,
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

            // Hide Error message
            formGenerator.getViewByTag(DateOfBirth + errorSuffix)?.apply {
                visibility = View.GONE
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
}
