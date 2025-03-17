package com.medtroniclabs.spice.ui.communityprofile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.gson.internal.LinkedTreeMap
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_ddMMMyyyy
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.community.CommunityPopulationStatistics
import com.medtroniclabs.spice.data.community.CommunitySummaryListItem
import com.medtroniclabs.spice.databinding.FragmentCommunityProfileSummaryBinding
import com.medtroniclabs.spice.db.entity.CommunityProfile
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.CommunityDetails.AccessRoadToPhu
import com.medtroniclabs.spice.mappingkey.CommunityDetails.ChwHouseInCommunity
import com.medtroniclabs.spice.mappingkey.CommunityDetails.DescribeLocation
import com.medtroniclabs.spice.mappingkey.CommunityDetails.EmergencyManagementPlan
import com.medtroniclabs.spice.mappingkey.CommunityDetails.False
import com.medtroniclabs.spice.mappingkey.CommunityDetails.Infrastructure
import com.medtroniclabs.spice.mappingkey.CommunityDetails.MarketDays
import com.medtroniclabs.spice.mappingkey.CommunityDetails.MobileNetworkCoverage
import com.medtroniclabs.spice.mappingkey.CommunityDetails.NearestPhu
import com.medtroniclabs.spice.mappingkey.CommunityDetails.SelectedNetwork
import com.medtroniclabs.spice.mappingkey.CommunityDetails.True
import com.medtroniclabs.spice.mappingkey.CommunityDetails.WaterAndSanitationFacilities
import com.medtroniclabs.spice.mappingkey.CommunityDetails.market
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.communityprofile.adapter.CommunitySummaryAdapter
import com.medtroniclabs.spice.ui.communityprofile.viewmodel.CommunityProfileViewModel


class CommunityProfileSummaryFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentCommunityProfileSummaryBinding
    private lateinit var communitySummaryAdapter: CommunitySummaryAdapter
    private val viewModel: CommunityProfileViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCommunityProfileSummaryBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        addObservers()
    }

    private fun addObservers() {
        viewModel.combinedLiveData.observe(viewLifecycleOwner) { (stats, details) ->
            if (stats?.data != null && details?.data != null) {
                setData(stats.data, details.data)
            }
        }
    }

    private fun initViews() {
        showHomeIcon()
        communitySummaryAdapter = CommunitySummaryAdapter()
        binding.rvCommunityProfileSummary.apply {
            adapter = communitySummaryAdapter
        }
        arguments?.getLong(DefinedParams.COMMUNITY_ID)?.let { villageId ->
            viewModel.getCommunityDetailsLocal(villageId = villageId)
            viewModel.getPopulationStatistics(villageId = villageId)
        }
        binding.btnDone.safeClickListener(this)
    }

    private fun setData(
        statistics: CommunityPopulationStatistics?,
        details: CommunityProfile?
    ) {
        val communityList = mutableListOf<CommunitySummaryListItem>()
        communityList.add(CommunitySummaryListItem.TitleItem(getString(R.string.profile_details)))
        communityList.add(
            CommunitySummaryListItem.ProfileItem(
                arguments?.getString(DefinedParams.COMMUNITY_NAME),
                details?.communityDescription,
                DateUtils.convertUTCString(details?.registeredDate, DATE_FORMAT_ddMMMyyyy)
            )
        )
        communityList.add(
            CommunitySummaryListItem.TitleItem(
                getString(
                    R.string.community_population_statistics
                )
            )
        )
        communityList.add(
            CommunitySummaryListItem.OtherItem(
                getString(R.string.total_population),
                statistics?.populationCount.toString()
            )
        )
        communityList.add(
            CommunitySummaryListItem.OtherItem(
                getString(R.string.no_of_household_families),
                statistics?.householdCount.toString()
            )
        )
        communityList.add(
            CommunitySummaryListItem.OtherItem(
                getString(R.string.no_of_women_of_child_bearing_age),
                statistics?.childBearingAgeOfWomen.toString()
            )
        )
        communityList.add(
            CommunitySummaryListItem.OtherItem(
                getString(R.string.no_of_pregnant_women),
                statistics?.pregnantCount.toString()
            )
        )
        communityList.add(
            CommunitySummaryListItem.OtherItem(
                getString(R.string.no_of_child_under_one_year),
                statistics?.belowOneYearCount.toString()
            )
        )
        communityList.add(
            CommunitySummaryListItem.OtherItem(
                getString(R.string.no_of_children_under_five_years),
                statistics?.belowFiveYearCount.toString()
            )
        )

        details?.payload?.let { payloadString ->
            val payload = StringConverter.stringToMap(payloadString)

        }
        
        details?.payload?.let { payload ->
            val forms = viewModel.formLayoutLiveData.value?.data?.formLayout?.filter { it.isSummary == true }
            val dataMap = StringConverter.stringToMap(payload)
            
            //Water and sanitation
            val waterAndSanitation = dataMap[WaterAndSanitationFacilities] as LinkedTreeMap<*, *>
            val waterAndSanitationForms = forms?.filter { it.family == WaterAndSanitationFacilities }
            communityList.add(
                CommunitySummaryListItem.TitleItem(
                    getString(
                        R.string.water_and_sanitation_facilities
                    )
                )
            )
            waterAndSanitationForms?.forEach { form ->
                val value = waterAndSanitation[form.id]
                communityList.add(
                    CommunitySummaryListItem.OtherItem(
                        form.title,
                        value.toString()
                    )
                )
            }


            //Infrastructure
            val infrastructure = dataMap[Infrastructure] as LinkedTreeMap<*, *>
            val infrastructureForms = forms?.filter { it.family == Infrastructure }
            communityList.add(
                CommunitySummaryListItem.TitleItem(
                    getString(R.string.infrastructure_facilities)
                )
            )

            infrastructureForms?.forEach { form ->
                when(form.id){
                    MarketDays -> {
                        val marketDays = infrastructure[form.id] as? ArrayList<String>
                        val size = marketDays?.size?:0
                        if(size > 0) {
                            communityList.add(
                                CommunitySummaryListItem.OtherItem(
                                    getString(R.string.market_days),
                                    marketDays?.joinToString(",") { it.take(3) ?: "" }
                                )
                            )
                        }
                    }
                    MobileNetworkCoverage -> {
                        val value = infrastructure[form.id]
                        when(value.toString()){
                            True -> {
                                viewModel.isNetworkCoverage = true
                                viewModel.networkName = form.title
                            }
                            False -> {
                                communityList.add(
                                    CommunitySummaryListItem.OtherItem(
                                        form.title,
                                        value.toString(),
                                        isText = false
                                    )
                                )
                            }
                        }
                    }
                    SelectedNetwork -> {
                        val value = infrastructure[form.id]
                        if(viewModel.isNetworkCoverage) {
                            communityList.add(
                                CommunitySummaryListItem.OtherItem(
                                    viewModel.networkName,
                                    value.toString()
                                )
                            )
                            viewModel.isNetworkCoverage = false
                            viewModel.networkName = ""
                        }
                    }
                    ChwHouseInCommunity -> {
                        val value = infrastructure[form.id]
                        when(value.toString()){
                            True -> {
                                viewModel.isChwHome = true
                                viewModel.chwHome = form.title
                            }
                            False -> {
                                communityList.add(
                                    CommunitySummaryListItem.OtherItem(
                                        form.title,
                                        value.toString(),
                                        isText = false
                                    )
                                )
                            }
                        }
                    }
                    DescribeLocation -> {
                        val value = infrastructure[form.id]
                        if(viewModel.isChwHome) {
                            communityList.add(
                                CommunitySummaryListItem.OtherItem(
                                    viewModel.chwHome,
                                    value.toString()
                                )
                            )
                            viewModel.isChwHome = false
                            viewModel.chwHome = ""
                        }
                    }
                    else -> {
                        val value = infrastructure[form.id]
                        communityList.add(
                            CommunitySummaryListItem.OtherItem(
                                form.title,
                                value.toString(),
                                isText = (value is String)
                            )
                        )
                    }
                }
            }

            //EmergencyManagementPlan
            val emergency = dataMap[EmergencyManagementPlan] as LinkedTreeMap<*, *>
            val emergencyForms = forms?.filter { it.family == EmergencyManagementPlan }
            communityList.add(
                CommunitySummaryListItem.TitleItem(
                    getString(R.string.emergency_management_plan)
                )
            )
            val emergencyMap = mutableMapOf<String,String>()
            emergencyForms?.forEach { form ->
                val value = emergency[form.id]
                when(form.id){
                    NearestPhu -> {
                        viewModel.nearestPhu = value.toString()
                    }
                    AccessRoadToPhu -> {
                        when(value.toString()){
                            True ->  emergencyMap[form.title] = getString(R.string.yes)
                            False ->  emergencyMap[form.title] = getString(R.string.no)
                        }
                    }
                    else -> {
                        emergencyMap[form.title] = value.toString()
                    }
                }
            }

            communityList.add(
                CommunitySummaryListItem.EmergencyItem(
                     viewModel.nearestPhu,
                    emergencyMap
                )
            )
            
        }


        communitySummaryAdapter.updateList(communityList)
    }



    fun navigateToEditScreen() {
        arguments?.getLong(DefinedParams.COMMUNITY_ID)?.let {
            val bundle = Bundle().apply {
                putLong(DefinedParams.COMMUNITY_ID, it)
                putString(
                    DefinedParams.COMMUNITY_NAME,
                    arguments?.getString(DefinedParams.COMMUNITY_NAME)
                )
                arguments?.getBoolean(DefinedParams.COMMUNITY_REGISTERED)?.let {
                    putBoolean(
                        DefinedParams.COMMUNITY_REGISTERED,
                        it
                    )
                }
            }
            viewModel.updateCurrentFragment(2, bundle)
        }
    }

    companion object {
        const val TAG = "CommunityProfileSummaryFragment"
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnDone -> {
                //viewModel.updateCurrentFragment(1)
                requireActivity().finish()
            }
        }
    }
}