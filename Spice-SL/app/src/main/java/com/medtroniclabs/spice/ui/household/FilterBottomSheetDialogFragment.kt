package com.medtroniclabs.spice.ui.household

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.safeClickListener
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentFilterBottomSheetDialogBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.household.viewmodel.HouseholdListViewModel

class FilterBottomSheetDialogFragment() : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentFilterBottomSheetDialogBinding
    private lateinit var villageListTagView: TagListCustomView
    private var listener: HouseholdSelectionListener? = null
    private lateinit var statusListTagView: TagListCustomView

    private val householdListViewModel: HouseholdListViewModel by activityViewModels()

    constructor(listener: HouseholdSelectionListener) : this() {
        this.listener = listener
    }

    companion object {
        const val TAG = "FilterBottomSheetDialogFragment"
        fun newInstance(listener: HouseholdSelectionListener): FilterBottomSheetDialogFragment {
            return FilterBottomSheetDialogFragment(listener)
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
        binding.btnApply.isEnabled = villageListTagView.getSelectedTags().isNotEmpty() || statusListTagView.getSelectedTags().isNotEmpty()
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
                        villageListTagView.addChipItemList(chipItemList, householdListViewModel.villageFilterList)
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun initView() {
        villageListTagView = TagListCustomView(binding.root.context, binding.villageChipGroup){ _,_ ->
            enableConfirm()
        }
        statusListTagView = TagListCustomView(binding.root.context, binding.registrationStatusChipGroup){ _,_ ->
            enableConfirm()
        }
        householdListViewModel.getAllVillagesName()
        composeStatusListChipView()
    }

    private fun composeStatusListChipView() {
        val itemList = arrayListOf(HouseholdDefinedParams.Pending, HouseholdDefinedParams.Finished)
        val statusList = ArrayList<ChipViewItemModel>()
        itemList.forEach {
            statusList.add(
                ChipViewItemModel(name = it)
            )
        }
        statusListTagView.addChipItemList(statusList, householdListViewModel.statusFilterList)
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.btnApply -> {
                applyFilter()
            }
            R.id.btnCancel -> {
                householdListViewModel.villageFilterList = null
                householdListViewModel.statusFilterList = null
                villageListTagView.clearSelection()
                statusListTagView.clearSelection()
                householdListViewModel.getHouseHoldList()
                dismiss()
            }
        }
    }

    private fun applyFilter() {
        householdListViewModel.villageFilterList = villageListTagView.getSelectedTags()
        householdListViewModel.statusFilterList = statusListTagView.getSelectedTags()
        listener?.filterHouseholdList()
        dismiss()
    }

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }

}