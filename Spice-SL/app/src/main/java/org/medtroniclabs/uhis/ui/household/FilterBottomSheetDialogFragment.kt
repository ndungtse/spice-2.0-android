package org.medtroniclabs.uhis.ui.household

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams.HOUSEHOLDFILTER
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.databinding.FragmentFilterBottomSheetDialogBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.TagListCustomView
import org.medtroniclabs.uhis.ui.household.viewmodel.HouseholdListViewModel

class FilterBottomSheetDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentFilterBottomSheetDialogBinding
    private lateinit var ssListTagView: TagListCustomView
    private lateinit var subVillageListTagView: TagListCustomView
    private val householdListViewModel: HouseholdListViewModel by activityViewModels()

    companion object {
        const val TAG = "FilterBottomSheetDialogFragment"

        fun newInstance(): FilterBottomSheetDialogFragment = FilterBottomSheetDialogFragment()
    }

    override fun getTheme(): Int = R.style.DialogStyle

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFilterBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initializeListeners()
        attachObservers()
    }

    private fun enableConfirm() {
        val isSsValid = ssListTagView.getSelectedTags().isNotEmpty()
        val isSubVillageValid = subVillageListTagView.getSelectedTags().isNotEmpty()

        binding.btnApply.isEnabled = isSsValid || isSubVillageValid
    }

    private fun initializeListeners() {
        binding.btnApply.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
    }

    private fun attachObservers() {
        householdListViewModel.filterUiData.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let { data ->
                        val ssList = data.ssList.map {
                            ChipViewItemModel(
                                id = it.id,
                                name = it.name,
                            )
                        }
                        ssListTagView.addChipItemList(
                            ssList,
                            householdListViewModel.getFilterLiveData().value?.filterBySs,
                        )
                        val subVillageList = data.subVillages.map {
                            ChipViewItemModel(
                                id = it.id,
                                name = it.name,
                            )
                        }
                        subVillageListTagView.addChipItemList(
                            subVillageList,
                            householdListViewModel.getFilterLiveData().value?.filterBySubVillages,
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
        householdListViewModel.setUserJourney(HOUSEHOLDFILTER)
        // Hide Household location
        binding.tvVillageTitle.gone()
        binding.villageChipGroup.gone()
        // Show SS
        binding.tvSsTitle.visible()
        binding.ssChipGroup.visible()
        // Hide registration status
        binding.tvRegistrationStatus.gone()
        binding.registrationStatusChipGroup.gone()
        // Show village(sub-village)
        binding.tvSubVillage.visible()
        binding.subVillageChipGroup.visible()

        ssListTagView = TagListCustomView(binding.root.context, binding.ssChipGroup) { _, _, _ ->
            enableConfirm()
        }
        subVillageListTagView = TagListCustomView(binding.root.context, binding.subVillageChipGroup) { _, _, _ ->
            enableConfirm()
        }

        householdListViewModel.getFilterUiData()
        binding.etFromDate.safeClickListener(this)
        binding.etToDate.safeClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnApply -> {
                applyFilter()
            }

            R.id.btnCancel -> {
                householdListViewModel.setUserJourney(AnalyticsDefinedParams.HOUSEHOLDFILTERCANCELTRIGGERED)
                householdListViewModel.setFilterLiveData(
                    ssFilter = listOf(),
                    subVillagesFilter = listOf(),
                )
                ssListTagView.clearSelection()
                subVillageListTagView.clearSelection()
                dismiss()
            }
        }
    }

    private fun applyFilter() {
        householdListViewModel.setUserJourney(AnalyticsDefinedParams.HOUSEHOLDFILTERAPPLYTRIGGERED)
        householdListViewModel.setFilterLiveData(
            ssFilter = ssListTagView.getSelectedTags(),
            subVillagesFilter = subVillageListTagView.getSelectedTags(),
        )
        dismiss()
    }

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }
}
