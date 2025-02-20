package com.medtroniclabs.spice.ui.communityprofile.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils.getBooleanAsString
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_ddMMMyyyy
import com.medtroniclabs.spice.common.DateUtils.convertDateToStringWithUTC
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.COMMUNITY_ID
import com.medtroniclabs.spice.common.DefinedParams.COMMUNITY_REGISTERED
import com.medtroniclabs.spice.common.DefinedParams.HouseholdHead
import com.medtroniclabs.spice.common.EntityMapper.getResultSpinnerMapList
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.data.community.CommunityPopulation
import com.medtroniclabs.spice.data.model.MultiSelectDropDownModel
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentEditCommunityBinding
import com.medtroniclabs.spice.db.entity.CommunityDetailsEntity
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.formgeneration.utility.MultiSelectSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.CommunityDetails.AccessRoadToPhu
import com.medtroniclabs.spice.mappingkey.CommunityDetails.Church
import com.medtroniclabs.spice.mappingkey.CommunityDetails.ChwHouseInCommunity
import com.medtroniclabs.spice.mappingkey.CommunityDetails.CourtBarrie
import com.medtroniclabs.spice.mappingkey.CommunityDetails.DescribeLocation
import com.medtroniclabs.spice.mappingkey.CommunityDetails.EmergencyContactPhu
import com.medtroniclabs.spice.mappingkey.CommunityDetails.EmergencyTransportAvailable
import com.medtroniclabs.spice.mappingkey.CommunityDetails.False
import com.medtroniclabs.spice.mappingkey.CommunityDetails.JuniorSecondarySchool
import com.medtroniclabs.spice.mappingkey.CommunityDetails.Market
import com.medtroniclabs.spice.mappingkey.CommunityDetails.MarketDays
import com.medtroniclabs.spice.mappingkey.CommunityDetails.MobileNetworkCoverage
import com.medtroniclabs.spice.mappingkey.CommunityDetails.MobileNumberOfAmbulanceDriver
import com.medtroniclabs.spice.mappingkey.CommunityDetails.MobileNumberOfEmergencyTransportContact
import com.medtroniclabs.spice.mappingkey.CommunityDetails.Mosque
import com.medtroniclabs.spice.mappingkey.CommunityDetails.NameOfAmbulanceDriver
import com.medtroniclabs.spice.mappingkey.CommunityDetails.NameOfEmergencyTransportContact
import com.medtroniclabs.spice.mappingkey.CommunityDetails.NearestPhu
import com.medtroniclabs.spice.mappingkey.CommunityDetails.NumberOfHandPumpsNotFunctional
import com.medtroniclabs.spice.mappingkey.CommunityDetails.NumberOfImprovedToilets
import com.medtroniclabs.spice.mappingkey.CommunityDetails.NumberOfImprovedWaterSources
import com.medtroniclabs.spice.mappingkey.CommunityDetails.NumberOfNonImprovedToilets
import com.medtroniclabs.spice.mappingkey.CommunityDetails.NumberOfNonImprovedWaterSources
import com.medtroniclabs.spice.mappingkey.CommunityDetails.PrimarySchool
import com.medtroniclabs.spice.mappingkey.CommunityDetails.SelectedNetwork
import com.medtroniclabs.spice.mappingkey.CommunityDetails.SeniorSecondarySchool
import com.medtroniclabs.spice.mappingkey.CommunityDetails.True
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.householdName
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.isOwnedAnImprovedLatrine
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.no
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.yes
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.rootSuffix
import com.medtroniclabs.spice.ui.communityprofile.adapter.CommunityPopulationAdapter
import com.medtroniclabs.spice.ui.communityprofile.viewmodel.CommunityProfileViewModel
import timber.log.Timber
import kotlin.math.truncate

class EditCommunityProfileFragment : BaseFragment(), FormEventListener, View.OnClickListener {

    private lateinit var binding: FragmentEditCommunityBinding
    private lateinit var formGenerator: FormGenerator
    private val communityProfileViewModel: CommunityProfileViewModel by activityViewModels()
    private lateinit var communityPopulationAdapter: CommunityPopulationAdapter
    private var datePickerDialog: DatePickerDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEditCommunityBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setListener()
        communityProfileViewModel.getFormData(DefinedParams.COMMUNITY_PROFILE)
        addObservers()
    }

    private fun setListener() {
        binding.btnSubmit.safeClickListener(this)
    }

    private fun addObservers() {
        communityProfileViewModel.saveCommunityDetailsLiveDataLocal.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.community_details_added_successfully),
                        positiveButtonName = getString(R.string.ok),
                    ) {
                        if (it) {
                            communityProfileViewModel.updateCurrentFragment(1)
                            communityProfileViewModel.saveCommunityDetailsLiveDataLocal.postValue(
                                Resource(ResourceState.ERROR)
                            )
                        }
                    }

                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }

        communityProfileViewModel.updateCommunityDetailsLiveDataLocal.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.community_details_updated_successfully),
                        positiveButtonName = getString(R.string.ok),
                    ) {
                        if (it) {
                            communityProfileViewModel.updateCurrentFragment(1)
                            communityProfileViewModel.updateCommunityDetailsLiveDataLocal.postValue(
                                Resource(ResourceState.ERROR)
                            )
                            //communityProfileViewModel.updateUnSynStatus(arguments?.getLong(DefinedParams.COMMUNITY_ID)!!)
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }

        }

        communityProfileViewModel.getCommunityDetailsLiveDataLocal.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let {
                        if(it.villageId == arguments?.getLong(COMMUNITY_ID))
                            autoPopulateFormField(it)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }

        }

        communityProfileViewModel.nearestHealthFacilityLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        val view =
                            formGenerator.getViewByTag(DefinedParams.NEAREST_PHU) as? AppCompatSpinner
                        if (view != null) {
                            (view.adapter as CustomSpinnerAdapter).setData(data)
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }

        }

        communityProfileViewModel.communityStatistics.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let {
                        val list = mutableListOf<CommunityPopulation>()
                        list.add(
                            CommunityPopulation(
                                getString(R.string.total_population),
                                it.populationCount
                            )
                        )
                        list.add(
                            CommunityPopulation(
                                getString(R.string.no_of_household_families),
                                it.householdCount
                            )
                        )
                        list.add(
                            CommunityPopulation(
                                getString(R.string.no_of_women_of_child_bearing_age),
                                it.populationCount
                            )
                        )
                        list.add(
                            CommunityPopulation(
                                getString(R.string.no_of_pregnant_women),
                                it.pregnantCount
                            )
                        )
                        list.add(
                            CommunityPopulation(
                                getString(R.string.no_of_child_under_one_year),
                                it.belowOneYearCount
                            )
                        )
                        list.add(
                            CommunityPopulation(
                                getString(R.string.no_of_children_under_five_years),
                                it.belowFiveYearCount
                            )
                        )
                        communityPopulationAdapter.updateList(list)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }

        communityProfileViewModel.formLayoutLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let {
                        formGenerator.populateViews(it.formLayout)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }

        }
    }

    private fun initViews() {
        hideHomeIcon()
        arguments?.getLong(DefinedParams.COMMUNITY_ID)?.let { villageId ->
            communityProfileViewModel.getPopulationStatistics(villageId = villageId)
        }
        arguments?.getString(DefinedParams.COMMUNITY_NAME).let { villageName ->
            binding.etCommunityName.setText(villageName)
        }
        communityPopulationAdapter = CommunityPopulationAdapter()
        binding.rvCommunitiesStatistics.adapter = communityPopulationAdapter
        formGenerator = FormGenerator(
            requireContext(), binding.llForm,
            null, this, binding.nestedScrollView, translate = false
        )
        binding.etRegisteredDate.safeClickListener(this)
    }

    companion object {
        const val TAG = "EditCommunityFragment"
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.etRegisteredDate -> {
                showDatePickerDialog()
            }

            R.id.btnSubmit -> {
                if (validateCommunityDetails()) {
                    formGenerator.formSubmitAction(view)
                }
            }
        }
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
        CheckBoxDialog.newInstance(
            id,
            resultMap,
            title = getString(R.string.market_days)
        ) { resultMap ->
            formGenerator.validateCheckboxDialogue(id, serverViewModel, resultMap)
        }.show(childFragmentManager, CheckBoxDialog.TAG)
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
        //Calling Local DB
        val regDateUtc = DateUtils.convertStringToDate(
            binding.etRegisteredDate.text.toString(),
            DateUtils.DATE_ddMMyyyy
        )?.let { regDate ->
            convertDateToStringWithUTC(regDate, DateUtils.DATE_ddMMyyyy)
        }
        val payload = StringConverter.convertGivenMapToString(resultMap)
        val villageId = arguments?.getLong(DefinedParams.COMMUNITY_ID)
        if (regDateUtc != null && payload != null && villageId != null) {
            arguments?.getBoolean(COMMUNITY_REGISTERED)?.let {
                if (it) {
                    communityProfileViewModel.updateCommunityDetailsLocal(
                        villageId = villageId,
                        description = binding.etCommunityBoundaryDesc.text.toString(),
                        regDate = regDateUtc,
                        payload = payload
                    )
                } else {
                    communityProfileViewModel.insertCommunityDetailsLocal(
                        villageId = villageId,
                        description = binding.etCommunityBoundaryDesc.text.toString(),
                        regDate = regDateUtc,
                        payload = payload
                    )
                }
            }
        }

        //Calling API
        /*resultMap.apply {
            this?.put(DefinedParams.VILLAGE_ID,arguments?.getLong(DefinedParams.COMMUNITY_ID).toString())
            this?.put(DefinedParams.COMMUNITY_NAME,binding.etCommunityName.text.toString())
            this?.put(DefinedParams.COMMUNITY_DESC,binding.etCommunityBoundaryDesc.text.toString())
            regDateUtc?.let { utcDate ->
                this?.put(DefinedParams.COMMUNITY_REGISTERED_DATE,utcDate)
            }
        }
        resultMap?.let {
            communityProfileViewModel.updateCommunityDetails(it)
        }*/
    }

    override fun onRenderingComplete() {
        communityProfileViewModel.getNearestHealthFacility()
        arguments?.getLong(DefinedParams.COMMUNITY_ID)?.let { villageId ->
            communityProfileViewModel.getCommunityDetailsLocal(villageId)
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

    }

    override fun handleMandatoryCondition(serverData: FormLayout?) {

    }

    override fun onAgeUpdateListener(
        age: Int,
        serverData: List<FormLayout?>?,
        resultHashMap: HashMap<String, Any>
    ) {

    }

    fun getCurrentAnswerStatus(): Boolean {
        return formGenerator.getResultMap().isNotEmpty()
    }

    private fun validateCommunityDetails(): Boolean {
        var isValid = true

        binding.apply {
            isValid = checkField(etCommunityName, tvErrorMessage)
            isValid = checkField(etCommunityBoundaryDesc, tvErrorMessageForCommunityDesc) && isValid
        }

        if (binding.etRegisteredDate.text.toString().isEmpty()) {
            binding.tvErrorRegisteredDate.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.tvErrorRegisteredDate.visibility = View.GONE
        }

        return isValid
    }

    private fun checkField(
        editText: AppCompatEditText,
        errorView: AppCompatTextView
    ): Boolean {
        return if (editText.text.isNullOrEmpty()) {
            errorView.visibility = View.VISIBLE
            false
        } else {
            errorView.visibility = View.GONE
            true
        }
    }

    private fun showDatePickerDialog() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.etRegisteredDate.text.isNullOrBlank())
            yearMonthDate =
                DateUtils.convertedMMMToddMM(binding.etRegisteredDate.text.toString())
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.etRegisteredDate.setText(
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DateUtils.DATE_FORMAT_ddMMyyyy,
                        DateUtils.DATE_ddMMyyyy
                    )
                )

                datePickerDialog = null
            }
        }
    }

    private fun autoPopulateFormField(details: CommunityDetailsEntity) {
        binding.etCommunityBoundaryDesc.setText(details.communityDescription)
        binding.etRegisteredDate.text = DateUtils.convertUTCString(
            details.registeredDate,
            DateUtils.DATE_ddMMyyyy
        )
        details.payload?.let {
            val payload = StringConverter.stringToMap(it)
            formGenerator.getViewByTag(NumberOfImprovedWaterSources)?.let { view ->
                formGenerator.setValueForView(payload[NumberOfImprovedWaterSources], view)
            }
            formGenerator.getViewByTag(NumberOfNonImprovedWaterSources)?.let { view ->
                formGenerator.setValueForView(payload[NumberOfNonImprovedWaterSources], view)
            }
            formGenerator.getViewByTag(NumberOfImprovedToilets)?.let { view ->
                formGenerator.setValueForView(payload[NumberOfImprovedToilets], view)
            }
            formGenerator.getViewByTag(NumberOfNonImprovedToilets)?.let { view ->
                formGenerator.setValueForView(payload[NumberOfNonImprovedToilets], view)
            }
            formGenerator.getViewByTag(NumberOfHandPumpsNotFunctional)?.let { view ->
                formGenerator.setValueForView(payload[NumberOfHandPumpsNotFunctional], view)
            }
            setSingleSelection(payload[PrimarySchool], PrimarySchool)
            setSingleSelection(payload[JuniorSecondarySchool], JuniorSecondarySchool)
            setSingleSelection(payload[SeniorSecondarySchool], SeniorSecondarySchool)
            setSingleSelection(payload[Market], Market)
            setSingleSelection(payload[CourtBarrie], CourtBarrie)
            setSingleSelection(payload[Church], Church)
            setSingleSelection(payload[Mosque], Mosque)
            setSingleSelection(payload[MobileNetworkCoverage], MobileNetworkCoverage)
            formGenerator.getViewByTag(SelectedNetwork)?.let { view ->
                formGenerator.setValueForView(payload[SelectedNetwork], view)
            }
            /*formGenerator.getViewByTag(MarketDays)?.let { view ->
                payload[MarketDays] as? ArrayList<String>
                formGenerator.setValueForView(,view)
            }*/
            setSingleSelection(payload[ChwHouseInCommunity], ChwHouseInCommunity)
            formGenerator.getViewByTag(DescribeLocation)?.let { view ->
                formGenerator.setValueForView(payload[DescribeLocation], view)
            }
            formGenerator.getViewByTag(NearestPhu)?.let { view ->
                formGenerator.setValueForView(payload[NearestPhu], view)
            }
            formGenerator.getViewByTag(EmergencyContactPhu)?.let { view ->
                formGenerator.setValueForView(payload[EmergencyContactPhu], view)
            }
            setSingleSelection(payload[AccessRoadToPhu], AccessRoadToPhu)
            formGenerator.getViewByTag(EmergencyTransportAvailable)?.let { view ->
                formGenerator.setValueForView(payload[EmergencyTransportAvailable], view)
            }
            formGenerator.getViewByTag(NameOfEmergencyTransportContact)?.let { view ->
                formGenerator.setValueForView(payload[NameOfEmergencyTransportContact], view)
            }
            formGenerator.getViewByTag(MobileNumberOfEmergencyTransportContact)?.let { view ->
                formGenerator.setValueForView(
                    payload[MobileNumberOfEmergencyTransportContact],
                    view
                )
            }
            formGenerator.getViewByTag(NameOfAmbulanceDriver)?.let { view ->
                formGenerator.setValueForView(payload[NameOfAmbulanceDriver], view)
            }
            formGenerator.getViewByTag(MobileNumberOfAmbulanceDriver)?.let { view ->
                formGenerator.setValueForView(payload[MobileNumberOfAmbulanceDriver], view)
            }
        }
    }

    private fun setSingleSelection(value: Any?, key: String) {
        value.let {
            when (value.toString()) {
                True -> {
                    singleSelectValueOption(
                        True,
                        key
                    )
                }

                False -> {
                    singleSelectValueOption(
                        False,
                        key
                    )
                }

                else -> {}
            }
        }
    }

    private fun singleSelectValueOption(value: String, key: String) {
        formGenerator.getViewByTag("${value}_${key}")
            ?.let { view ->
                if (view is TextView) {
                    view.performClick()
                }
            }
    }

}