package org.medtroniclabs.uhis.ui.dashboard.ncd

import android.content.Intent
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.isTablet
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import org.medtroniclabs.uhis.common.DateUtils.DATE_ddMMyyyy
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.common.ViewUtils
import org.medtroniclabs.uhis.data.CustomDateModel
import org.medtroniclabs.uhis.data.NCDUserDashboardRequest
import org.medtroniclabs.uhis.data.NCDUserDashboardResponse
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.databinding.FragmentDashboardBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ncd.medicalreview.CommonEnums
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.MenuConstants
import org.medtroniclabs.uhis.ui.TagListCustomView
import org.medtroniclabs.uhis.ui.dashboard.ncd.adapter.DashboardCardItem
import org.medtroniclabs.uhis.ui.dashboard.ncd.adapter.UserDashboardAdapter
import org.medtroniclabs.uhis.ui.dashboard.ncd.viewmodel.NCDDashBoardViewModel
import org.medtroniclabs.uhis.ui.household.HouseholdSearchActivity
import org.medtroniclabs.uhis.model.services.ServiceStaticFilter
import org.medtroniclabs.uhis.ui.services.ServicesActivity
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_ANC
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_ANC_3_PLUS
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_ASSESSED
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_CATARACT
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_CHILD_VISIT
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_DISPENSED
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_EYE_CARE
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_FAMILY_PLANNING
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_HIGH_RISK_PREGNANT_WOMEN
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_HOUSEHOLD_REGISTERED
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_INVESTIGATED
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_LIFESTYLE
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_PNC
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_PREGNANCY_OUTCOME
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_PREGNANT_WOMEN_REGISTRATION
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_PSYCHOLOGICAL
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_PW_IDENTIFIED_4_MONTHS_ANC
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_REFERRED
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_REGISTERED
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_SCREENED
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_TB_ASSESSMENT
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.CARD_TB_CONTACT_TRACING
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.EXTRA_DASHBOARD_SS_IDS
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.EXTRA_DASHBOARD_STATIC_FILTER
import org.medtroniclabs.uhis.ui.dashboard.ncd.DashboardConstants.EXTRA_DASHBOARD_SUB_VILLAGE_IDS

@AndroidEntryPoint
class DashboardFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentDashboardBinding
    private val viewModel: NCDDashBoardViewModel by activityViewModels()
    private lateinit var cgCalender: TagListCustomView
    private var dashboardFilterCount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        clickListeners()
        attachObservers()
        viewModel.getMenus()
    }

    private fun attachObservers() {
        viewModel.menuListLiveData.observe(viewLifecycleOwner) { menus ->
            if (!menus.isNullOrEmpty()) {
                initializeChipItem()
            }
        }
        viewModel.getFilterLiveData().observe(viewLifecycleOwner) { filter ->
            var count = 0
            if (filter.filterBySs.isNotEmpty()) count++
            if (filter.filterBySubVillages.isNotEmpty()) count++
            dashboardFilterCount = count
            updateFilterButtonLabel(dashboardFilterCount)
            if (::cgCalender.isInitialized) {
                getDashboardList()
            }
        }
        viewModel.userDashboardDetails.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { entity ->
                        showView(false, entity)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                    showErrorDialog(getString(R.string.error), resourceState.message.toString())
                }
            }
        }
    }

    private fun clickListeners() {
        binding.etFromDate.safeClickListener(this)
        binding.etToDate.safeClickListener(this)
        updateFilterButtonLabel(dashboardFilterCount)
        // Dashboard filter button (after date range row)
        binding.llFilter?.btnFilter?.safeClickListener {
            DashboardFilterBottomSheetDialogFragment
                .newInstance()
                .show(childFragmentManager, DashboardFilterBottomSheetDialogFragment.TAG)
        }
    }

    private fun updateFilterButtonLabel(count: Int) {
        binding.llFilter?.btnFilter?.text =
            if (count > 0) {
                getString(R.string.filter_count, count)
            } else {
                getString(R.string.filter)
            }
    }

    private fun initializeChipItem() {
        val chipItemList = getChip()
        cgCalender = TagListCustomView(
            requireContext(),
            binding.cgCalender,
            isSelectionRequired = true,
        ) { _, _, isChecked ->
            if (isChecked) {
                val isVisible =
                    cgCalender.getSelectedTags().any { it.value == CommonEnums.CUSTOMISE.value }
                if (isVisible) {
                    resetCounts()
                    binding.clDateRange.visible()
                } else {
                    binding.clDateRange.gone()
                    binding.etFromDate.text = getString(R.string.empty)
                    binding.etToDate.text = getString(R.string.empty)

                    getDashboardList()
                }
            }
        }
        val selectedList = ArrayList<ChipViewItemModel>()
        selectedList.add(chipItemList[0])
        cgCalender.addChipItemList(chipItemList, selectedList)
    }

    private fun resetCounts() {
        viewModel.userDashboardDetails.value
            ?.data
            ?.let { showView(true, it) }
    }

    fun getChip(): ArrayList<ChipViewItemModel> {
        val chipItemList = ArrayList<ChipViewItemModel>()
        chipItemList.add(
            ChipViewItemModel(
                id = 1,
                name = CommonEnums.TODAY.title,
                cultureValue = getString(CommonEnums.TODAY.cultureValue),
                value = CommonEnums.TODAY.value,
            ),
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 3,
                name = CommonEnums.WEEK.title,
                cultureValue = getString(CommonEnums.WEEK.cultureValue),
                value = CommonEnums.WEEK.value,
            ),
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 4,
                name = CommonEnums.MONTH.title,
                cultureValue = getString(CommonEnums.MONTH.cultureValue),
                value = CommonEnums.MONTH.value,
            ),
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 5,
                name = CommonEnums.CUSTOMISE.title,
                cultureValue = getString(CommonEnums.CUSTOMISE.cultureValue),
                value = CommonEnums.CUSTOMISE.value,
            ),
        )
        return chipItemList
    }

    private fun showView(
        customize: Boolean,
        entity: NCDUserDashboardResponse,
    ) {
        val userDashboardList = ArrayList<DashboardCardItem>()
        entity.let {
            // Dedicated flow indicators
            userDashboardList.add(DashboardCardItem(CARD_PREGNANT_WOMEN_REGISTRATION, getString(R.string.pregnant_women_registration), it.pregnantWomenRegistrationCount ?: 0, R.drawable.ic_rmnch_tool))
            userDashboardList.add(DashboardCardItem(CARD_ANC, getString(R.string.anc), it.ancCount ?: 0, R.drawable.ic_rmnch_tool))
            userDashboardList.add(
                DashboardCardItem(
                    CARD_PW_IDENTIFIED_4_MONTHS_ANC,
                    getString(R.string.pw_identified_first_4_months_received_anc),
                    it.pwIdentifiedFirst4MonthsWithAncCount ?: 0,
                    R.drawable.ic_rmnch_tool,
                ),
            )
            userDashboardList.add(
                DashboardCardItem(
                    CARD_ANC_3_PLUS,
                    getString(R.string.anc_3_plus_services),
                    it.anc3PlusCount ?: 0,
                    R.drawable.ic_rmnch_tool,
                ),
            )
            userDashboardList.add(DashboardCardItem(CARD_PREGNANCY_OUTCOME, getString(R.string.pregnancy_outcome), it.pregnancyOutcomeCount ?: 0, R.drawable.ic_rmnch_tool))
            userDashboardList.add(DashboardCardItem(CARD_PNC, getString(R.string.pnc), it.pncCount ?: 0, R.drawable.ic_rmnch_tool))
            userDashboardList.add(
                DashboardCardItem(
                    CARD_HIGH_RISK_PREGNANT_WOMEN,
                    getString(R.string.highrisk_pregnant_women),
                    it.highRiskPregnantWomenCount ?: 0,
                    R.drawable.ic_rmnch_tool,
                ),
            )
            userDashboardList.add(DashboardCardItem(CARD_CHILD_VISIT, getString(R.string.child_visit), it.childVisitCount ?: 0, R.drawable.ic_rmnch_tool))
            userDashboardList.add(DashboardCardItem(CARD_HOUSEHOLD_REGISTERED, getString(R.string.household_registered), it.householdRegisteredCount ?: 0, R.drawable.ic_registration))
            userDashboardList.add(
                DashboardCardItem(
                    CARD_FAMILY_PLANNING,
                    getString(R.string.family_planning),
                    it.familyPlanningCount ?: 0,
                    R.drawable.ic_family_planning,
                ),
            )
            // userDashboardList.add(DashboardCardItem(CARD_TB_ASSESSMENT, getString(R.string.tb_assessment), it.tbAssessmentCount ?: 0, R.drawable.ic_tb_tool))
            // userDashboardList.add(DashboardCardItem(CARD_TB_CONTACT_TRACING, getString(R.string.tb_contact_tracing), it.tbContactTracingCount ?: 0, R.drawable.ic_tb_tool))
            // userDashboardList.add(DashboardCardItem(CARD_EYE_CARE, getString(R.string.eye_care), it.eyeCareCount ?: 0, R.drawable.ic_eye_care))
            // userDashboardList.add(DashboardCardItem(CARD_CATARACT, getString(R.string.cataract), it.cataractCount ?: 0, R.drawable.ic_cataract))
            // userDashboardList.add(DashboardCardItem(CARD_SCREENED, getString(R.string.screened), it.screened ?: 0, R.drawable.ic_screening))
            // userDashboardList.add(DashboardCardItem(CARD_REFERRED, getString(R.string.referred), it.referred ?: 0, R.drawable.ic_referred))
            // userDashboardList.add(DashboardCardItem(CARD_REGISTERED, getString(R.string.registered), it.registered ?: 0, R.drawable.ic_registration))
            // userDashboardList.add(DashboardCardItem(CARD_ASSESSED, getString(R.string.assessed), it.assessed ?: 0, R.drawable.ic_assessment))
            // userDashboardList.add(DashboardCardItem(CARD_DISPENSED, getString(R.string.prescriptions_dispensed), it.dispensed ?: 0, R.drawable.ic_dispense))
            // userDashboardList.add(
            //     DashboardCardItem(
            //         CARD_INVESTIGATED,
            //         getString(R.string.investigations_conducted),
            //         it.investigated ?: 0,
            //         R.drawable.ic_investigation,
            //     ),
            // )
            // userDashboardList.add(DashboardCardItem(CARD_LIFESTYLE, getString(R.string.reviews_conducted), it.nutritionistLifestyleCount ?: 0, R.drawable.ic_lifestyle))
            // userDashboardList.add(
            //     DashboardCardItem(
            //         CARD_PSYCHOLOGICAL,
            //         getString(R.string.counsellings_conducted),
            //         it.psychologicalNotesCount ?: 0,
            //         R.drawable.ic_psycological_menu,
            //     ),
            // )
        }
        binding.rvActivitiesList.apply {
            layoutManager =
                GridLayoutManager(
                    requireContext(),
                    if (requireContext().isTablet()) 3 else 1,
                )
            adapter = UserDashboardAdapter(customize, userDashboardList) { card ->
                navigateFromDashboardCard(card.key)
            }
        }
    }

    private fun navigateFromDashboardCard(cardKey: String) {
        val ssIds = viewModel.getFilterLiveData().value?.filterBySs?.mapNotNull { it.id } ?: emptyList()
        val subVillageIds = viewModel.getFilterLiveData().value?.filterBySubVillages?.mapNotNull { it.id } ?: emptyList()
        val intent = if (cardKey == CARD_HOUSEHOLD_REGISTERED) {
            Intent(requireActivity(), HouseholdSearchActivity::class.java).apply {
                putExtra(EXTRA_DASHBOARD_SS_IDS, ssIds.toLongArray())
                putExtra(EXTRA_DASHBOARD_SUB_VILLAGE_IDS, subVillageIds.toLongArray())
            }
        } else {
            Intent(requireActivity(), ServicesActivity::class.java).apply {
                putExtra(EXTRA_DASHBOARD_SS_IDS, ssIds.toLongArray())
                putExtra(EXTRA_DASHBOARD_SUB_VILLAGE_IDS, subVillageIds.toLongArray())
                putExtra(EXTRA_DASHBOARD_STATIC_FILTER, mapCardKeyToStaticFilter(cardKey).name)
            }
        }
        startActivity(intent)
    }

    private fun mapCardKeyToStaticFilter(cardKey: String): ServiceStaticFilter {
        return when (cardKey) {
            CARD_FAMILY_PLANNING -> ServiceStaticFilter.FAMILY_PLANNING_COUNSELLING_ELIGIBLE
            CARD_PREGNANT_WOMEN_REGISTRATION -> ServiceStaticFilter.PREGNANT_WOMEN
            CARD_ANC, CARD_PW_IDENTIFIED_4_MONTHS_ANC, CARD_ANC_3_PLUS -> ServiceStaticFilter.PREGNANT_WOMEN
            CARD_HIGH_RISK_PREGNANT_WOMEN -> ServiceStaticFilter.HIGH_RISK_PREGNANT_WOMEN
            CARD_PREGNANCY_OUTCOME -> ServiceStaticFilter.PENDING_DELIVERIES
            CARD_PNC -> ServiceStaticFilter.POSTNATAL_CARE_MOTHERS
            CARD_CHILD_VISIT -> ServiceStaticFilter.CHILDREN_UNDER_TWO_YEARS
            CARD_TB_ASSESSMENT, CARD_TB_CONTACT_TRACING, CARD_EYE_CARE, CARD_CATARACT,
            CARD_SCREENED, CARD_REFERRED, CARD_REGISTERED, CARD_ASSESSED, CARD_DISPENSED,
            CARD_INVESTIGATED, CARD_LIFESTYLE, CARD_PSYCHOLOGICAL,
            -> ServiceStaticFilter.ALL_MEMBERS
            else -> ServiceStaticFilter.ALL_MEMBERS
        }
    }

    private fun showDatePickerDialog(
        isFromDate: Boolean,
        text: String?,
    ) {
        var date: Triple<Int?, Int?, Int?>? = null
        if (!text.isNullOrBlank()) {
            date = DateUtils.convertedMMMToddMM(text)
        }

        val fromDate = binding.etFromDate.text?.toString()

        val datePickerDialog = ViewUtils.showDatePicker(
            context = requireContext(),
            date = date,
            minDate = if (isFromDate) {
                null
            } else {
                DateUtils.convertDateToLong(
                    fromDate,
                    DATE_ddMMyyyy,
                )
            },
            maxDate = System.currentTimeMillis(),
        ) { _, year, month, dayOfMonth ->
            DateUtils
                .convertDateTimeToDate(
                    "$dayOfMonth-$month-$year",
                    DateUtils.DATE_FORMAT_ddMMyyyy,
                    DATE_ddMMyyyy,
                ).let { stringDate ->
                    if (isFromDate) {
                        resetCounts()
                        binding.etFromDate.text = stringDate
                        binding.etToDate.text = getString(R.string.empty)
                    } else {
                        binding.etToDate.text = stringDate
                    }
                }
            if (!binding.etFromDate.text.isNullOrEmpty() && !binding.etToDate.text.isNullOrEmpty()) {
                getDashboardList(true)
            }
        }
        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.okay)) { dg, _ ->
            datePickerDialog.onClick(dg, DialogInterface.BUTTON_POSITIVE)
        }

        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel)) { dg, _ ->
            datePickerDialog.onClick(dg, DialogInterface.BUTTON_NEGATIVE)
        }
        datePickerDialog.show()
    }

    private fun getDashboardList(fetchDates: Boolean? = false) {
        if (!::cgCalender.isInitialized) return
        if (fetchDates == false) {
            val selectedItem = cgCalender.getSelectedTags()
            if (selectedItem.isNotEmpty()) {
                (selectedItem[0] as? ChipViewItemModel)?.let { model ->
                    if (model.name == getString(R.string.customize)) {
                        showDatePickers()
                    } else {
                        hideDatePicker()
                        val request = NCDUserDashboardRequest(
                            sortField = model.value,
                            userId = SecuredPreference.getUserFhirId(),
                            filterBySs = viewModel.getFilterLiveData().value?.filterBySs?.mapNotNull { it.id },
                            filterBySubVillages = viewModel.getFilterLiveData().value?.filterBySubVillages?.mapNotNull { it.id },
                        )
                        constructRequest(request)
                    }
                }
            }
        } else {
            val endDate =
                DateUtils.convertStringToDate(binding.etToDate.text.toString(), DATE_ddMMyyyy)
            val request = NCDUserDashboardRequest(
                customDate = CustomDateModel(
                    startDate = DateUtils.convertDateTimeToDate(
                        binding.etFromDate.text.toString(),
                        DATE_ddMMyyyy,
                        DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        inUTC = true,
                    ),
                    endDate = DateUtils.getEndDate(
                        endDate,
                        DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        inUTC = true,
                    ),
                ),
                userId = SecuredPreference.getUserFhirId(),
                filterBySs = viewModel.getFilterLiveData().value?.filterBySs?.mapNotNull { it.id },
                filterBySubVillages = viewModel.getFilterLiveData().value?.filterBySubVillages?.mapNotNull { it.id },
            )
            constructRequest(request)
        }
    }

    private fun constructRequest(request: NCDUserDashboardRequest) {
        // Dashboard is fully offline. Do not gate with network availability.
        viewModel.getUserDashboardDetails(request)
    }

    private fun showDatePickers() {
        binding.clDateRange.visible()
    }

    private fun hideDatePicker() {
        binding.clDateRange.gone()
        binding.etFromDate.text = getString(R.string.empty)
        binding.etToDate.text = getString(R.string.empty)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.etFromDate.id -> {
                showDatePickerDialog(true, binding.etFromDate.toString())
            }

            binding.etToDate.id -> {
                if (binding.etFromDate.text
                        .toString()
                        .isNotEmpty()
                ) {
                    showDatePickerDialog(false, binding.etToDate.text.toString())
                }
            }
        }
    }

    companion object {
        const val TAG = "DashboardFragment"

        fun newInstance(): DashboardFragment = DashboardFragment()
    }
}
