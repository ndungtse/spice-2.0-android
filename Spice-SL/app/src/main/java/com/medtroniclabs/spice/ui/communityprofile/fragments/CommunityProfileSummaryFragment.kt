package com.medtroniclabs.spice.ui.communityprofile.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_ddMMMyyyy
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.community.CommunityPopulationStatistics
import com.medtroniclabs.spice.data.community.CommunitySummaryListItem
import com.medtroniclabs.spice.data.model.SymptomModel
import com.medtroniclabs.spice.databinding.FragmentCommunityProfileSummaryBinding
import com.medtroniclabs.spice.db.entity.CommunityDetailsEntity
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.mappingkey.CommunityDetails.AccessRoadToPhu
import com.medtroniclabs.spice.mappingkey.CommunityDetails.Church
import com.medtroniclabs.spice.mappingkey.CommunityDetails.ChwHouseInCommunity
import com.medtroniclabs.spice.mappingkey.CommunityDetails.CourtBarrie
import com.medtroniclabs.spice.mappingkey.CommunityDetails.EmergencyContactPhu
import com.medtroniclabs.spice.mappingkey.CommunityDetails.EmergencyTransportAvailable
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
import com.medtroniclabs.spice.model.communityprofile.CommunityProfileDetails
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.communityprofile.adapter.CommunitySummaryAdapter
import com.medtroniclabs.spice.ui.communityprofile.viewmodel.CommunityProfileViewModel
import timber.log.Timber


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
        details: CommunityDetailsEntity?
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
        details?.payload?.let {
            communityList.add(
                CommunitySummaryListItem.TitleItem(
                    getString(
                        R.string.water_and_sanitation_facilities
                    )
                )
            )
            val payload = StringConverter.stringToMap(it)
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.no_of_improved_water_sources),
                    payload[NumberOfImprovedWaterSources].toString()
                )
            )
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.no_of_non_improved_water_sources),
                    payload[NumberOfNonImprovedWaterSources].toString()
                )
            )
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.no_of_imporved_toilets),
                    payload[NumberOfImprovedToilets].toString()
                )
            )
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.no_of_non_improved_toilets),
                    payload[NumberOfNonImprovedToilets].toString()
                )
            )
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.no_of_hand_pumps),
                    payload[NumberOfHandPumpsNotFunctional].toString()
                )
            )
            communityList.add(
                CommunitySummaryListItem.TitleItem(
                    getString(R.string.infrastructure_facilities)
                )
            )
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.primary_school),
                    payload[PrimarySchool].toString(),
                    false
                )
            )
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.junior_secondary_school),
                    payload[JuniorSecondarySchool].toString(),
                    false
                )
            )
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.senior_secondary_school),
                    payload[SeniorSecondarySchool].toString(),
                    false
                )
            )
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.market),
                    payload[Market].toString(),
                    false
                )
            )
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.court_barrie),
                    payload[CourtBarrie].toString(),
                    false
                )
            )
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.church),
                    payload[Church].toString(),
                    false
                )
            )
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.mosque),
                    payload[Mosque].toString(),
                    false
                )
            )
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.mobile_network_coverage),
                    payload[SelectedNetwork].toString()
                )
            )
            /*val marketArray = payload[MarketDays] as? ArrayList<SymptomModel>
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.market_days),
                    marketArray?.joinToString(","){ it.value?.take(3)?:""}
                )
            )*/
            communityList.add(
                CommunitySummaryListItem.OtherItem(
                    getString(R.string.chw_house_in_this_community),
                    payload[ChwHouseInCommunity].toString(),
                    false
                )
            )
            communityList.add(
                CommunitySummaryListItem.TitleItem(
                    getString(R.string.emergency_management_plan)
                )
            )
            val emergencyMap = HashMap<String, String>().apply {
                put(
                    getString(R.string.emergency_contact_phu),
                    payload[EmergencyContactPhu].toString()
                )
                put(getString(R.string.access_road_to_phu), payload[AccessRoadToPhu].toString())
                put(
                    getString(R.string.emergency_transport_available),
                    payload[EmergencyTransportAvailable].toString()
                )
                put(
                    getString(R.string.emergency_transport_contact),
                    payload[NameOfEmergencyTransportContact].toString()
                )
                put(
                    getString(R.string.emergency_transport_contact_no),
                    payload[MobileNumberOfEmergencyTransportContact].toString()
                )
                put(getString(R.string.ambulance_driver), payload[NameOfAmbulanceDriver].toString())
                put(
                    getString(R.string.ambulance_driver_contact),
                    payload[MobileNumberOfAmbulanceDriver].toString()
                )
            }

            communityList.add(
                CommunitySummaryListItem.EmergencyItem(
                    payload[NearestPhu].toString(),
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
                viewModel.updateCurrentFragment(1)
            }
        }
    }
}