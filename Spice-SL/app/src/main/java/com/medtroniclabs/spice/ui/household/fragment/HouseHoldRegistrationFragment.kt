package com.medtroniclabs.spice.ui.household.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams.HouseholdCreation
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams.HouseholdEdit
import com.medtroniclabs.spice.common.DefinedParams.VillageId
import com.medtroniclabs.spice.common.EntityMapper.getResultSpinnerMapList
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentHouseHoldRegistrationBinding
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.noOfPeople
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.totalMembers
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.villageId
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.REGISTRATION
import com.medtroniclabs.spice.ui.household.viewmodel.HouseRegistrationViewModel
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HouseHoldRegistrationFragment : BaseFragment(), View.OnClickListener, FormEventListener {

    lateinit var binding: FragmentHouseHoldRegistrationBinding
    private lateinit var formGenerator: FormGenerator
    private var onDismissListener: OnDialogDismissListener? = null
    private val householdRegistrationViewModel: HouseRegistrationViewModel by activityViewModels()
    private var pendingSubVillageId: Long? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onDismissListener = context as OnDialogDismissListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHouseHoldRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        householdRegistrationViewModel.getFormData(REGISTRATION)
        initializeFormGenerator()
        setListeners()
        attachObservers()
        // Generate random 10-digit household number when page loads (only for new household registration)
        if (householdRegistrationViewModel.householdId == -1L) {
            householdRegistrationViewModel.generateHouseholdNumber()
        }
    }

    private fun attachObservers() {
        householdRegistrationViewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resources ->
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
                    }
                }

                ResourceState.ERROR -> {
                    (activity as? BaseActivity)?.hideLoading()
                }
            }
        }

        // Observe generated household number and populate the field
        householdRegistrationViewModel.generatedHouseholdNumberLiveData.observe(viewLifecycleOwner) { householdNumber ->
            if (householdRegistrationViewModel.householdId == -1L) {
                // Only populate for new household registration
                formGenerator.getViewByTag(HouseHoldRegistration.householdNumber)?.let { view ->
                    formGenerator.setValueForView(householdNumber.toString(), view)
                }
            }
        }

        householdRegistrationViewModel.villageListResponse.observe(viewLifecycleOwner) { resourceState ->
            fetchFormDetails()
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { data ->
                        formGenerator.spinnerDataInjection(data, getResultSpinnerMapList(data))

                        // Auto-select if single village
                        if (data.response is List<*> && data.response.size == 1) {
                            val singleItem = data.response[0]
                            if (singleItem is Map<*, *>) {
                                val id = singleItem[DefinedParams.ID]
                                formGenerator.getViewByTag(villageId)?.let { view ->
                                    formGenerator.setValueForView(id, view)
                                }
                            }
                        }

                        arguments?.getLong(VillageId)?.let {
                            if (it != 0L) {
                                formGenerator.getViewByTag(villageId)?.let { view ->
                                    view.isEnabled = false
                                    formGenerator.setValueForView(it, view)
                                }
                            }
                        }
                    }
                }
                else -> {
                    //Invoked if response state is not success
                }
            }
        }

        householdRegistrationViewModel.shasthyaShebikaListResponse.observe(viewLifecycleOwner) { resourceState ->
            fetchFormDetails()
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { data ->
                        formGenerator.spinnerDataInjection(data, getResultSpinnerMapList(data))

                        // Auto-select if single shasthya shebika
                        if (data.response is List<*> && data.response.size == 1) {
                            val singleItem = data.response[0]
                            if (singleItem is Map<*, *>) {
                                val id = singleItem[DefinedParams.ID]
                                formGenerator.getViewByTag(HouseHoldRegistration.shasthyaShebikaId)?.let { view ->
                                    formGenerator.setValueForView(id, view)
                                    // Trigger sub-village loading
                                    val shasthyaShebikaIdLong = com.medtroniclabs.spice.common.CommonUtils.getLongOrNull(id) ?: 0L
                                    if (shasthyaShebikaIdLong != 0L) {
                                        householdRegistrationViewModel.loadSubVillageDataCacheByType(
                                            HouseHoldRegistration.subVillageId,
                                            "",
                                            shasthyaShebikaIdLong
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    //Invoked if response state is not success
                }
            }
        }

        householdRegistrationViewModel.subVillageListResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { data ->
                        formGenerator.spinnerDataInjection(data, getResultSpinnerMapList(data))

                        // Auto-select if single sub village
                        if (data.response is List<*> && data.response.size == 1) {
                            val singleItem = data.response[0]
                            if (singleItem is Map<*, *>) {
                                val id = singleItem[DefinedParams.ID]
                                formGenerator.getViewByTag(HouseHoldRegistration.subVillageId)?.let { view ->
                                    formGenerator.setValueForView(id, view)
                                }
                            }
                        }
                        
                        // Set pending sub village ID if available (for edit mode)
                        pendingSubVillageId?.let { subVillageId ->
                            if (subVillageId != 0L) {
                                formGenerator.getViewByTag(HouseHoldRegistration.subVillageId)?.let { view ->
                                    formGenerator.setValueForView(subVillageId, view)
                                }
                            }
                            pendingSubVillageId = null // Clear after setting
                        }
                    }
                }
                else -> {
                    //Invoked if response state is not success
                }
            }
        }

        householdRegistrationViewModel.houseHoldDetailLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as BaseActivity?)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as BaseActivity?)?.hideLoading()
                    resourceState.data?.let { houseHoldDetail ->
                        autoPopulateFormFields(houseHoldDetail)
                    }
                }

                ResourceState.ERROR -> {
                    (activity as BaseActivity?)?.hideLoading()
                }
            }
        }
    }

    private fun autoPopulateFormFields(details: HouseholdEntity) {
        formGenerator.getViewByTag(villageId)?.let { view ->
            if (details.villageId != 0L) {
                view.isEnabled = false
                formGenerator.setValueForView(details.villageId, view)
            }
        }
        formGenerator.getViewByTag(noOfPeople)?.let { view ->
            formGenerator.setValueForView(details.noOfPeople, view)
        }
        formGenerator.getViewByTag(totalMembers)?.let { view ->
            formGenerator.setValueForView(details.noOfPeople, view)
        }
        formGenerator.getViewByTag(HouseHoldRegistration.shasthyaShebikaId)?.let { view ->
            val shasthyaShebikaId = details.shasthyaShebikaId
            if (shasthyaShebikaId != null && shasthyaShebikaId != 0L) {
                // Store sub village ID to set after list is loaded
                pendingSubVillageId = details.subVillageId
                formGenerator.setValueForView(shasthyaShebikaId, view)
            }
        }
        formGenerator.getViewByTag(HouseHoldRegistration.householdType)?.let { view ->
            details.householdType?.let { type ->
                formGenerator.setValueForView(type, view)
            }
        }
        formGenerator.getViewByTag(HouseHoldRegistration.monthlyIncome)?.let { view ->
            details.monthlyIncome?.let { income ->
                // Convert Double to String for EditText
                formGenerator.setValueForView(income.toString(), view)
            }
        }
        formGenerator.getViewByTag(HouseHoldRegistration.householdNumber)?.let { view ->
            details.householdNo?.let { householdNo ->
                formGenerator.setValueForView(householdNo.toString(), view)
            }
        }
    }

    private fun initializeFormGenerator() {
        if (householdRegistrationViewModel.householdId != -1L) {
            binding.btnNext.text = getString(R.string.submit)
            householdRegistrationViewModel.eventName = HouseholdEdit
            householdRegistrationViewModel.setUserJourney(HouseholdEdit)
        } else {
            householdRegistrationViewModel.eventName = HouseholdCreation
            householdRegistrationViewModel.setUserJourney(HouseholdCreation)
        }
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, this, binding.scrollView,
            translate = SecuredPreference.getIsTranslationEnabled()
        )
    }

    /**
     * This function checks if both village and ss are fetched
     * then trigger fetch form details if it is for edit
     */
    private fun fetchFormDetails() {
        if (householdRegistrationViewModel.householdId != -1L) {
            val villageFetchStatus = householdRegistrationViewModel.villageListResponse.value
            val ssFetchStatus = householdRegistrationViewModel.shasthyaShebikaListResponse.value
            if (villageFetchStatus?.state != ResourceState.LOADING && ssFetchStatus?.state != ResourceState.LOADING) {
                householdRegistrationViewModel.getHouseholdDetailsByID(
                    householdRegistrationViewModel.householdId
                )
            }
        }
    }


    private fun setListeners() {
        binding.btnNext.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnNext -> {
                withLocationCheck({
                    formGenerator.formSubmitAction(v)
                })
                /*if (activity is HouseholdActivity) {
                    (activity as HouseholdActivity).loadFragment(2)
                }*/
            }
            R.id.btnCancel -> {
                householdRegistrationViewModel.setUserJourney(AnalyticsDefinedParams.CANCELBUTTONTRIGGERED)
                householdRegistrationViewModel.setAnalyticsData(
                    UserDetail.startDateTime,
                    eventName = if (householdRegistrationViewModel.householdId != -1L) HouseholdEdit else HouseholdCreation,
                    exitReason = AnalyticsDefinedParams.CancelButtonClicked,
                    isCompleted = false
                )
                onDismissListener?.onDialogDismissListener()
            }
        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        if (localDataCache is String) {
            when (id) {
                HouseHoldRegistration.villageId -> {
                    householdRegistrationViewModel.loadDataCacheByType(id, localDataCache)
                }
                HouseHoldRegistration.shasthyaShebikaId -> {
                    householdRegistrationViewModel.loadShasthyaShebikaDataCacheByType(id, localDataCache)
                }
                HouseHoldRegistration.subVillageId -> {
                    // This will be triggered when shasthya shebika is selected (via dependentID)
                    // selectedParent should contain the shasthya shebika id
                    val shasthyaShebikaIdLong = selectedParent ?: 0L
                    if (shasthyaShebikaIdLong != 0L) {
                        // Clear sub village when parent changes
                        formGenerator.getViewByTag(HouseHoldRegistration.subVillageId)?.let { view ->
                            formGenerator.setValueForView("", view)
                        }
                        householdRegistrationViewModel.loadSubVillageDataCacheByType(id, localDataCache, shasthyaShebikaIdLong)
                    }
                }
                else -> {
                    householdRegistrationViewModel.loadDataCacheByType(id, localDataCache)
                }
            }
        }
    }

    override fun onPopulate(targetId: String) {
    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        formLayout: FormLayout,
        resultMap: Any?
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

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        resultMap?.let { map ->
            if (householdRegistrationViewModel.householdId != -1L) {
                householdRegistrationViewModel.setAnalyticsData(
                    UserDetail.startDateTime,
                    eventName = HouseholdEdit,
                    isCompleted = true
                )
                householdRegistrationViewModel.setUserJourney(AnalyticsDefinedParams.SUBMITBUTTONTRIGGERED)
                householdRegistrationViewModel.updateHousehold(map)
            } else {
                householdRegistrationViewModel.setUserJourney(AnalyticsDefinedParams.NEXTBUTTONTRIGGERED)
                householdRegistrationViewModel.registerHousehold(map)
            }
        }
    }

    override fun onRenderingComplete() {

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

    override fun handleMandatoryCondition(formLayout: FormLayout?) {

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

    fun getHouseHoldEnteredInputs(): Boolean {
        return formGenerator.getResultMap().isNotEmpty()
    }
}