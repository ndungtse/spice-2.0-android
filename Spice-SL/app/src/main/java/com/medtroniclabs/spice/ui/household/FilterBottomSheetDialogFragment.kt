package com.medtroniclabs.spice.ui.household

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentFilterBottomSheetDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.Customize
import com.medtroniclabs.spice.ui.household.viewmodel.HouseholdListViewModel

class FilterBottomSheetDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentFilterBottomSheetDialogBinding
    private lateinit var villageListTagView: TagListCustomView
    private lateinit var statusListTagView: TagListCustomView
    private lateinit var dataRangesListTagView: TagListCustomView
    private var datePickerDialog: DatePickerDialog? = null
    private val householdListViewModel: HouseholdListViewModel by activityViewModels()

    companion object {
        const val TAG = "FilterBottomSheetDialogFragment"
        fun newInstance(): FilterBottomSheetDialogFragment {
            return FilterBottomSheetDialogFragment()
        }

        fun newInstance(origin: String?): FilterBottomSheetDialogFragment {
            val fragment = FilterBottomSheetDialogFragment()
            val bundle = Bundle()
            origin?.let {
                bundle.putString(MenuConstants.MY_PATIENTS_MENU_ID, origin)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getTheme(): Int {
        return R.style.DialogStyle
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilterBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initializeListeners()
        attachObservers()
    }

    private fun enableConfirm() {

        val isCustomizedOptionSelected =
            dataRangesListTagView.getSelectedTags().any { it.name == Customize }

        val isDateRangeValid = if (dataRangesListTagView.getSelectedTags().isNotEmpty()) {
            if (isCustomizedOptionSelected) {
                !(binding.etFromDate.text.isEmpty() || binding.etToDate.text.isEmpty())
            } else {
                true
            }
        } else {
            false
        }

        val isVillageValid = if (isCustomizedOptionSelected) {
            isDateRangeValid && villageListTagView.getSelectedTags().isNotEmpty()
        } else {
            villageListTagView.getSelectedTags().isNotEmpty()
        }

        val isStatusListValid = if (isCustomizedOptionSelected) {
            isDateRangeValid && statusListTagView.getSelectedTags().isNotEmpty()
        } else {
            statusListTagView.getSelectedTags().isNotEmpty()
        }

        binding.btnApply.isEnabled = isVillageValid || isStatusListValid || isDateRangeValid
    }

    private fun initializeListeners() {
        binding.btnApply.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
    }

    private fun attachObservers() {
        householdListViewModel.villageListResponse.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let { listItems ->
                        val chipItemList = ArrayList<ChipViewItemModel>()
                        listItems.forEach {
                            chipItemList.add(
                                ChipViewItemModel(
                                    id = it.id,
                                    name = it.name
                                )
                            )
                        }
                        villageListTagView.addChipItemList(
                            chipItemList,
                            householdListViewModel.getFilterLiveData().value?.filterByVillage
                        )
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun initView() {
        binding.registrationStatusGroup.visibility = View.GONE
        villageListTagView =
            TagListCustomView(binding.root.context, binding.villageChipGroup) { _, _, _ ->
                enableConfirm()
            }
        statusListTagView =
            TagListCustomView(
                binding.root.context,
                binding.registrationStatusChipGroup
            ) { _, _, _ ->
                enableConfirm()
            }
        dataRangesListTagView =
            TagListCustomView(binding.root.context, binding.dataRangesChipGroup) { _, _, _ ->
                if (dataRangesListTagView.getSelectedTags().isEmpty()) {
                    goneDatePicker()
                } else {
                    val isCustomized =
                        dataRangesListTagView.getSelectedTags().any { it.name == Customize }
                    if (isCustomized) {
                        binding.etToDate.text = ""
                        binding.etFromDate.text = ""
                        binding.datePickerGroup.visibility = View.VISIBLE
                        binding.tvApplyError.visibility = View.GONE
                    } else {
                        goneDatePicker()
                    }
                }

                enableConfirm()
            }
        householdListViewModel.getAllVillagesName()
        composeStatusListChipView()
        binding.etFromDate.safeClickListener(this)
        binding.etToDate.safeClickListener(this)
    }

    private fun goneDatePicker() {
        binding.tvApplyError.visibility = View.GONE
        binding.etFromDateError.visibility = View.GONE
        binding.datePickerGroup.visibility = View.GONE
    }


    private fun composeStatusListChipView() {
        val itemList = arrayListOf(
            HouseholdDefinedParams.Pending,
            HouseholdDefinedParams.Finished
        )
        val origin = arguments?.getString(MenuConstants.MY_PATIENTS_MENU_ID)
        if (origin != null && origin == MenuConstants.MY_PATIENTS_MENU_ID) {
            itemList.add(HouseholdDefinedParams.Customize)
        }
        val statusList = ArrayList<ChipViewItemModel>()
        itemList.forEach {
            statusList.add(
                ChipViewItemModel(name = it)
            )
        }
        statusListTagView.addChipItemList(
            statusList,
            householdListViewModel.getFilterLiveData().value?.filterByStatus
        )
        dataRangesListTagView.addChipItemList(
            statusList,
            householdListViewModel.getFilterLiveData().value?.filterByStatus
        )
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnApply -> {
                applyFilter()
            }

            binding.etFromDate.id -> {
                binding.etFromDateError.visibility = View.GONE
                binding.tvApplyError.visibility = View.GONE
                showDatePickerDialog(true, binding.etFromDate.text.toString())
            }

            binding.etToDate.id -> {
                if (binding.etFromDate.text.toString().isNotEmpty()) {
                    showDatePickerDialog(false, binding.etToDate.text.toString())
                } else {
                    binding.etFromDateError.visibility = View.VISIBLE
                    binding.etFromDateError.text = getString(R.string.Select_Date)
                }
                enableConfirm()
            }

            R.id.btnCancel -> {
                householdListViewModel.setFilterLiveData(
                    villageFilter = listOf(),
                    statusFilter = listOf(),
                    dataRangesFilter = listOf()
                )
                villageListTagView.clearSelection()
                statusListTagView.clearSelection()
                dataRangesListTagView.clearSelection()
                dismiss()
            }
        }
    }

    private fun applyFilter() {
        householdListViewModel.setFilterLiveData(
            villageFilter = villageListTagView.getSelectedTags(),
            statusFilter = statusListTagView.getSelectedTags(),
            dataRangesFilter = dataRangesListTagView.getSelectedTags()
        )
        dismiss()
    }

    private fun showDatePickerDialog(isFromDate: Boolean, text: String?) {
        var date: Triple<Int?, Int?, Int?>? = null
        if (!text.isNullOrBlank())
            date = DateUtils.convertedMMMToddMM(text)
        val minMaxDate = getMinDate(isFromDate)
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                disableFutureDate = true,
                date = date,
                minDate = minMaxDate.first,
                cancelCallBack = { datePickerDialog = null }) { _, year, month, dayOfMonth ->
                DateUtils.convertDateTimeToDate(
                    "$dayOfMonth-$month-$year",
                    DateUtils.DATE_FORMAT_ddMMyyyy,
                    DateUtils.DATE_ddMMyyyy
                ).let { stringDate ->
                    if (isFromDate) {
                        binding.etFromDate.text = stringDate
                        binding.etToDate.text = ""
                    } else {
                        binding.etToDate.text = stringDate
                    }
                }
                enableConfirm()
                datePickerDialog = null
            }
        }
    }

    private fun getMinDate(isFromDate: Boolean): Pair<Long?, Long?> {
        val fromDate = binding.etFromDate.text?.toString()
        val toDate = binding.etToDate.text?.toString()
        return if (isFromDate) {
            if (!toDate.isNullOrBlank())
                Pair(null, DateUtils.convertDateToLong(toDate, DateUtils.DATE_ddMMyyyy))
            else Pair(null, System.currentTimeMillis())
        } else {
            if (!fromDate.isNullOrBlank())
                Pair(
                    DateUtils.convertDateToLong(fromDate, DateUtils.DATE_ddMMyyyy),
                    System.currentTimeMillis()
                )
            else Pair(null, System.currentTimeMillis())
        }
    }

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }
}