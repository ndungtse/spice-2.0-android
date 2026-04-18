package org.medtroniclabs.uhis.ui.dashboard.ncd

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
import org.medtroniclabs.uhis.ui.dashboard.ncd.viewmodel.NCDDashBoardViewModel

class DashboardFilterBottomSheetDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentFilterBottomSheetDialogBinding
    private lateinit var ssListTagView: TagListCustomView
    private lateinit var subVillageListTagView: TagListCustomView
    private val viewModel: NCDDashBoardViewModel by activityViewModels()

    companion object {
        const val TAG = "DashboardFilterBottomSheetDialogFragment"

        fun newInstance(): DashboardFilterBottomSheetDialogFragment = DashboardFilterBottomSheetDialogFragment()
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
        viewModel.filterUiData.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let { data ->
                        val ssList = data.ssList.map {
                            val name = if (it.ssId.isNullOrBlank()) {
                                it.name
                            } else {
                                "${it.ssId} - ${it.name}"
                            }
                            ChipViewItemModel(
                                id = it.id,
                                name = name,
                            )
                        }
                        ssListTagView.addChipItemList(ssList, viewModel.getFilterLiveData().value?.filterBySs)
                        val subVillageList = data.subVillages.map { ChipViewItemModel(id = it.id, name = it.name) }
                        subVillageListTagView.addChipItemList(
                            subVillageList,
                            viewModel.getFilterLiveData().value?.filterBySubVillages,
                        )
                    }
                }
                ResourceState.ERROR -> hideLoading()
            }
        }
    }

    private fun initView() {
        viewModel.setUserJourney(HOUSEHOLDFILTER)
        binding.tvVillageTitle.gone()
        binding.villageChipGroup.gone()
        binding.tvSsTitle.visible()
        binding.ssChipGroup.visible()
        binding.tvRegistrationStatus.gone()
        binding.registrationStatusChipGroup.gone()
        binding.tvSubVillage.visible()
        binding.subVillageChipGroup.visible()

        ssListTagView = TagListCustomView(binding.root.context, binding.ssChipGroup) { _, _, _ -> enableConfirm() }
        subVillageListTagView =
            TagListCustomView(binding.root.context, binding.subVillageChipGroup) { _, _, _ -> enableConfirm() }

        viewModel.getFilterUiData()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnApply -> {
                viewModel.setUserJourney(AnalyticsDefinedParams.HOUSEHOLDFILTERAPPLYTRIGGERED)
                viewModel.setFilterLiveData(
                    ssFilter = ssListTagView.getSelectedTags(),
                    subVillagesFilter = subVillageListTagView.getSelectedTags(),
                )
                dismiss()
            }
            R.id.btnCancel -> {
                viewModel.setUserJourney(AnalyticsDefinedParams.HOUSEHOLDFILTERCANCELTRIGGERED)
                viewModel.setFilterLiveData(
                    ssFilter = listOf(),
                    subVillagesFilter = listOf(),
                )
                ssListTagView.clearSelection()
                subVillageListTagView.clearSelection()
                dismiss()
            }
        }
    }

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }
}
