package com.medtroniclabs.spice.ui.dashboard.ncd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.isTablet
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.CustomDateModel
import com.medtroniclabs.spice.data.NCDUserDashboardRequest
import com.medtroniclabs.spice.data.NCDUserDashboardResponse
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentDashboardBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.medicalreview.CommonEnums
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.dashboard.ncd.adapter.UserDashboardAdapter
import com.medtroniclabs.spice.ui.dashboard.ncd.viewmodel.NCDDashBoardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentDashboardBinding
    private val viewModel: NCDDashBoardViewModel by viewModels()
    private lateinit var cgCalender: TagListCustomView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListeners()
        attachObservers()
        viewModel.getMenus()
    }

    private fun attachObservers() {
        viewModel.menuListLiveData.observe(viewLifecycleOwner) { menus ->
            if (!menus.isNullOrEmpty())
                initializeChipItem()
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
    }

    private fun initializeChipItem() {
        val chipItemList = getChip()
        cgCalender = TagListCustomView(requireContext(), binding.cgCalender) { _, _, isChecked ->
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
        viewModel.userDashboardDetails.value?.data?.let { showView(true, it) }
    }

    fun getChip(): ArrayList<ChipViewItemModel> {
        val chipItemList = ArrayList<ChipViewItemModel>()
        chipItemList.add(
            ChipViewItemModel(
                id = 1,
                name = CommonEnums.TODAY.name,
                cultureValue = getString(CommonEnums.TODAY.cultureValue),
                value = CommonEnums.TODAY.value
            )
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 2,
                name = CommonEnums.YESTERDAY.name,
                cultureValue = getString(CommonEnums.YESTERDAY.cultureValue),
                value = CommonEnums.YESTERDAY.value
            )
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 3,
                name = CommonEnums.WEEK.name,
                cultureValue = getString(CommonEnums.WEEK.cultureValue),
                value = CommonEnums.WEEK.value
            )
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 4,
                name = CommonEnums.MONTH.name,
                cultureValue = getString(CommonEnums.MONTH.cultureValue),
                value = CommonEnums.MONTH.value
            )
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 5,
                name = CommonEnums.CUSTOMISE.name,
                cultureValue = getString(CommonEnums.CUSTOMISE.cultureValue),
                value = CommonEnums.CUSTOMISE.value
            )
        )
        return chipItemList
    }

    private fun showView(customize: Boolean, entity: NCDUserDashboardResponse) {
        viewModel.menuListLiveData.value?.let { menus ->
            if (menus.isNotEmpty()) {
                val userDashboardList = ArrayList<Triple<String, Int, Int>>()
                entity.let {
                    if (menus.contains(MenuConstants.SCREENING)) {
                        it.screened?.let { screenedCount ->
                            userDashboardList.add(
                                Triple(
                                    getString(R.string.screened),
                                    screenedCount,
                                    R.drawable.ic_screening
                                )
                            )
                        }
                        it.referred?.let { referredCount ->
                            userDashboardList.add(
                                Triple(
                                    getString(R.string.referred),
                                    referredCount,
                                    R.drawable.ic_referred
                                )
                            )
                        }
                    }
                    if (menus.contains(MenuConstants.REGISTRATION)) {
                        it.registered?.let { registeredCount ->
                            userDashboardList.add(
                                Triple(
                                    getString(R.string.registered),
                                    registeredCount,
                                    R.drawable.ic_registration
                                )
                            )
                        }
                    }
                    if (menus.contains(MenuConstants.ASSESSMENT)) {
                        it.assessed?.let { assessedCount ->
                            userDashboardList.add(
                                Triple(
                                    getString(R.string.assessed),
                                    assessedCount,
                                    R.drawable.ic_assessment
                                )
                            )
                        }
                    }
                    if (menus.contains(MenuConstants.DISPENSE)) {
                        it.dispensed?.let { screenedCount ->
                            userDashboardList.add(
                                Triple(
                                    getString(R.string.prescriptions_dispensed),
                                    screenedCount,
                                    R.drawable.ic_dispense
                                )
                            )
                        }
                    }
                    if (menus.contains(MenuConstants.INVESTIGATION)) {
                        it.investigated?.let { assessedCount ->
                            userDashboardList.add(
                                Triple(
                                    getString(R.string.investigations_conducted),
                                    assessedCount,
                                    R.drawable.ic_investigation
                                )
                            )
                        }
                    }
                    if (menus.contains(MenuConstants.LIFESTYLE)) {
                        it.nutritionistLifestyleCount?.let { registeredCount ->
                            userDashboardList.add(
                                Triple(
                                    getString(R.string.reviews_conducted),
                                    registeredCount,
                                    R.drawable.ic_lifestyle
                                )
                            )
                        }
                    }
                    if (menus.contains(MenuConstants.PSYCHOLOGICAL)) {
                        it.psychologicalNotesCount?.let { referredCount ->
                            userDashboardList.add(
                                Triple(
                                    getString(R.string.counsellings_conducted),
                                    referredCount,
                                    R.drawable.ic_psycological_menu
                                )
                            )
                        }
                    }
                }
                binding.rvActivitiesList.apply {
                    layoutManager =
                        GridLayoutManager(
                            requireContext(),
                            if (requireContext().isTablet()) 3 else 1
                        )
                    adapter = UserDashboardAdapter(customize, userDashboardList)
                }
            }
        }
    }

    private fun showDatePickerDialog(isFromDate: Boolean, text: String?) {
        var date: Triple<Int?, Int?, Int?>? = null
        if (!text.isNullOrBlank())
            date = DateUtils.convertedMMMToddMM(text)

        val fromDate = binding.etFromDate.text?.toString()

        val datePickerDialog = ViewUtils.showDatePicker(
            context = requireContext(),
            date = date,
            minDate = if (isFromDate) null else DateUtils.convertDateToLong(
                fromDate,
                DATE_ddMMyyyy
            ),
            maxDate = System.currentTimeMillis()
        ) { _, year, month, dayOfMonth ->
            DateUtils.convertDateTimeToDate(
                "$dayOfMonth-$month-$year",
                DateUtils.DATE_FORMAT_ddMMyyyy,
                DATE_ddMMyyyy
            ).let { stringDate ->
                if (isFromDate) {
                    resetCounts()
                    binding.etFromDate.text = stringDate
                    binding.etToDate.text = getString(R.string.empty)
                } else {
                    binding.etToDate.text = stringDate
                }
            }
            if (!binding.etFromDate.text.isNullOrEmpty() && !binding.etToDate.text.isNullOrEmpty())
                getDashboardList(true)
        }
        datePickerDialog.show()
    }

    private fun getDashboardList(fetchDates: Boolean? = false) {
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
                            userId = SecuredPreference.getUserFhirId()
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
                        inUTC = true
                    ),
                    endDate = DateUtils.getEndDate(
                        endDate,
                        DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        inUTC = true
                    )
                ),
                userId = SecuredPreference.getUserFhirId()
            )
            constructRequest(request)
        }
    }

    private fun constructRequest(request: NCDUserDashboardRequest) {
        withNetworkAvailability(online = {
            viewModel.getUserDashboardDetails(request)
        })
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
                if (binding.etFromDate.text.toString().isNotEmpty()) {
                    showDatePickerDialog(false, binding.etToDate.text.toString())
                }
            }
        }
    }

    companion object {
        const val TAG = "DashboardFragment"
        fun newInstance(): DashboardFragment {
            return DashboardFragment()
        }
    }
}