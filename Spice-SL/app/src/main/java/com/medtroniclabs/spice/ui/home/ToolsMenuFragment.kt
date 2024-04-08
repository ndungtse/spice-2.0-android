package com.medtroniclabs.spice.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentToolsMenuBinding
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.MenuConstants.DialogResult
import com.medtroniclabs.spice.ui.MenuConstants.WorkFlowName
import com.medtroniclabs.spice.ui.assessment.AssessmentActivity
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.dialog.RMNCHFlowSelectionDialog
import com.medtroniclabs.spice.ui.home.adapter.DashboardMenuItemsAdapter

class ToolsMenuFragment : BaseFragment(), MenuSelectionListener {

    private lateinit var binding: FragmentToolsMenuBinding
    private val viewModel: ToolsViewModel by activityViewModels()

    companion object {
        const val TAG = "ToolsMenuFragment"
        fun newInstance(): ToolsMenuFragment {
            return ToolsMenuFragment()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentToolsMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.setFragmentResultListener(
            DialogResult,
            viewLifecycleOwner
        ) { _, bundle ->
            val result = bundle.getString(WorkFlowName)
            startAssessmentActivity(MenuConstants.RMNCH_MENU_ID, result)
        }
        viewModel.getMenuForClinicalWorkflows()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.menuListLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as BaseActivity).showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as BaseActivity).hideLoading()
                    resourceState.data?.let {
                        setAdapterViews(it)
                    }
                }

                ResourceState.ERROR -> {
                    (activity as BaseActivity).hideLoading()
                }
            }
        }
    }

    private fun setAdapterViews(menus: List<MenuEntity>) {
        if (CommonUtils.checkIsTablet(requireContext())) {
            val layoutManager = FlexboxLayoutManager(context)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.CENTER
            binding.rvActivitiesList.layoutManager = layoutManager
        } else {
            val layoutManager = GridLayoutManager(context, 2)
            binding.rvActivitiesList.layoutManager = layoutManager
        }
        binding.rvActivitiesList.adapter = DashboardMenuItemsAdapter(menus, this)
    }

    override fun onMenuSelected(menuId: String, subModule: String?) {
        startAssessmentToolsActivity(menuId, subModule)
    }

    private fun startAssessmentToolsActivity(menuId: String, subModule: String?) {
        when (menuId) {
            MenuConstants.RMNCH_MENU_ID -> {
                if (subModule == null) {
                    RMNCHFlowSelectionDialog.newInstance()
                        .show(childFragmentManager, RMNCHFlowSelectionDialog.TAG)
                } else {
                    startAssessmentActivity(MenuConstants.RMNCH_MENU_ID, RMNCH.ChildHoodVisit)
                }
            }

            else -> {
                startAssessmentActivity(menuId, null)
            }
        }
    }

    private fun startAssessmentActivity(menuId: String, workFlowName: String?) {
        val intent = Intent(requireContext(), AssessmentActivity::class.java)
        intent.putExtra(DefinedParams.MemberID, viewModel.selectedHouseholdMemberID)
        intent.putExtra(DefinedParams.MenuId, menuId)
        workFlowName?.let { name ->
            intent.putExtra(WorkFlowName, name)
        }
        startActivity(intent)
    }
}