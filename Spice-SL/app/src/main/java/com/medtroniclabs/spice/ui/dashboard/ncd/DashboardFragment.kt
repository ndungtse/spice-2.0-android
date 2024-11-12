package com.medtroniclabs.spice.ui.dashboard.ncd

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.CustomDateModel
import com.medtroniclabs.spice.data.NCDUserDashboardRequest
import com.medtroniclabs.spice.data.NCDUserDashboardResponse
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.CardViewLayoutBinding
import com.medtroniclabs.spice.databinding.FragmentDashboardBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.common.ActivityEnum
import com.medtroniclabs.spice.ui.dashboard.ncd.viewmodel.NCDDashBoardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var cgCalender: TagListCustomView
    private var datePickerDialog: DatePickerDialog? = null
    private val viewModel: NCDDashBoardViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        initializeChipItem()
        initializeCardView()
    }

    private fun initializeCardView() {
        viewModel.userDashboardDetails.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {showProgress()}
                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { entity ->
                        showView(entity)
                    }
                }
                ResourceState.ERROR -> {
                    showErrorDialog(getString(R.string.error),resourceState.message.toString())
                }
            }
        }
    }

    private fun showView(entity: NCDUserDashboardResponse) {
        val cardList = arrayListOf(
            mapOf(
                DefinedParams.Title to DefinedParams.SCREENED,
                DefinedParams.Count to entity.screened.toString()
            ),
            mapOf(
                DefinedParams.Title to DefinedParams.ASSESSED,
                DefinedParams.Count to entity.assessed.toString()
            ),
            mapOf(
                DefinedParams.Title to DefinedParams.REGISTERED,
                DefinedParams.Count to entity.registered.toString()
            ),
            mapOf(
                DefinedParams.Title to DefinedParams.REFERREDD,
                DefinedParams.Count to entity.referred.toString()
            )
        )
        binding.dashboard.removeAllViews()
        cardList.forEach { cardData ->
            val bindingCard =
                CardViewLayoutBinding.inflate(LayoutInflater.from(context))
            bindingCard.root.tag = cardData[DefinedParams.Title]
            bindingCard.txTitle.text = cardData[DefinedParams.Title]
            bindingCard.txCount.text = cardData[DefinedParams.Count]
            binding.dashboard.addView(bindingCard.root)
        }
    }

    private fun initializeChipItem() {
        val chipItemList = getChip()
        cgCalender = TagListCustomView(requireContext(), binding.cgCalender) { _, _, _ ->
            val isVisible =
                cgCalender.getSelectedTags().any { it.name == getString(R.string.customize) }
            if (isVisible) {
                binding.clDateRange.visible()
            } else {
                getDashboardList()
                binding.etFromDate.text = ""
                binding.etToDate.text = ""
                binding.clDateRange.gone()
            }
        }
        val selectedList = ArrayList<ChipViewItemModel>()
        selectedList.add( ChipViewItemModel(name = getString(R.string.today)))
        cgCalender.addChipItemList(chipItemList,selectedList)
    }

    private fun initializeView() {
        binding.etFromDate.safeClickListener(this)
        binding.etToDate.safeClickListener(this)
    }

    companion object {
        const val TAG = "DashboardFragment"
        fun newInstance(): DashboardFragment {
            return DashboardFragment()
        }
    }

    private fun showDatePickerDialog(isFromDate: Boolean, text: String?) {
        var date: Triple<Int?, Int?, Int?>? = null
        if (!text.isNullOrBlank())
            date = DateUtils.convertedMMMToddMM(text)

        val fromDate = binding.etFromDate.text?.toString()

        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                date = date,
                minDate = if(isFromDate) null else DateUtils.convertDateToLong(fromDate, DATE_ddMMyyyy),
                maxDate = System.currentTimeMillis(),
                cancelCallBack = { datePickerDialog = null }) { _, year, month, dayOfMonth ->
                DateUtils.convertDateTimeToDate(
                    "$dayOfMonth-$month-$year",
                    DateUtils.DATE_FORMAT_ddMMyyyy,
                    DATE_ddMMyyyy
                ).let { stringDate ->
                    if (isFromDate) {
                        binding.etFromDate.text = stringDate
                        binding.etToDate.text = ""
                    } else {
                        binding.etToDate.text = stringDate
                    }
                }
                if (!binding.etFromDate.text.isNullOrEmpty() && !binding.etToDate.text.isNullOrEmpty())
                    getDashboardList(true)
                datePickerDialog = null
            }
        }
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
            val endDate = DateUtils.convertStringToDate(binding.etToDate.text.toString(), DATE_ddMMyyyy)
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

    fun getChip(): ArrayList<ChipViewItemModel> {
        val chipItemList = ArrayList<ChipViewItemModel>()
        chipItemList.add(
            ChipViewItemModel(
                id = 1,
                name = getString(R.string.today),
                value = ActivityEnum.TODAY.fieldName
            )
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 2,
                name = getString(R.string.yesterday),
                value = ActivityEnum.YESTERDAY.fieldName
            )
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 3,
                name = getString(R.string.this_week),
                value = ActivityEnum.WEEK.fieldName
            )
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 4,
                name = getString(R.string.this_month),
                value = ActivityEnum.MONTH.fieldName
            )
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 5,
                name = getString(R.string.customize),
                value = ActivityEnum.CUSTOMISE.fieldName
            )
        )
        return chipItemList
    }

    private fun showDatePickers() {
        binding.clDateRange.visible()
    }

    private fun hideDatePicker() {
        binding.clDateRange.gone()
        binding.etFromDate.text = ""
        binding.etToDate.text = ""
    }

}