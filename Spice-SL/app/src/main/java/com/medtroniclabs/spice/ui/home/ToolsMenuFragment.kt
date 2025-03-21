package com.medtroniclabs.spice.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentToolsMenuBinding
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.EncounterReference
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.MENU_ID
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
import com.medtroniclabs.spice.ncd.medicalreview.NCDMedicalReviewActivity
import com.medtroniclabs.spice.ui.cbs.activity.CbsActivity

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
        viewModel.getMenuForClinicalWorkflows(requireArguments().getString(DefinedParams.Gender))
        attachObservers()
        if (getEncounterReference().isNotBlank()) {
            setTitle(requireContext().getString(R.string.home_title))
        }

        // Deeplink directly goes to Medical review
       if (requireArguments().getBoolean(DefinedParams.IsDeepLink)){
           if (getOrigin().equals(MenuConstants.MY_PATIENTS_MENU_ID, true)) {
               val intent =
                   Intent(requireContext(), NCDMedicalReviewActivity::class.java).apply {
                       putExtra(EncounterReference, getEncounterReference())
                       putExtra(MENU_ID, "ncd")
                       putExtra(DefinedParams.FhirId, getMemberReference())
                       putExtra(DefinedParams.PatientId, getPatientReference())
                       putExtra(DefinedParams.ORIGIN, getOrigin())
                   }
               startActivity(intent)
           } else {
               startAssessmentActivity("ncd", null)
           }
       }
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

    private fun startAssessmentToolsActivity(menuId: String, subModule: String? = null) {
        when (menuId) {
            MenuConstants.RMNCH_MENU_ID -> {
                if (subModule == null) {
                    RMNCHFlowSelectionDialog.newInstance()
                        .show(childFragmentManager, RMNCHFlowSelectionDialog.TAG)
                } else {
                    startAssessmentActivity(MenuConstants.RMNCH_MENU_ID, RMNCH.ChildHoodVisit)
                }
            }

            MenuConstants.CBS_MENU_ID.uppercase() -> {
                startCbsActivity(menuId)
            }

            MenuConstants.CBS_MENU_ID -> {
                startCbsActivity(menuId)
            }
            else -> {
                if (getOrigin().equals(MenuConstants.MY_PATIENTS_MENU_ID, true)) {
                    val intent =
                        Intent(requireContext(), NCDMedicalReviewActivity::class.java).apply {
                            putExtra(EncounterReference, getEncounterReference())
                            putExtra(MENU_ID, menuId)
                            putExtra(DefinedParams.FhirId, getMemberReference())
                            putExtra(DefinedParams.PatientId, getPatientReference())
                            putExtra(DefinedParams.ORIGIN, getOrigin())
                        }
                    startActivity(intent)
                } else {
                    startAssessmentActivity(menuId, null)
                }
            }
        }
    }

    private fun startCbsActivity(menuId: String) {
        val intent = Intent(requireContext(), CbsActivity::class.java)
        intent.putExtra(DefinedParams.MemberID, viewModel.selectedHouseholdMemberID)
        intent.putExtra(DefinedParams.DOB, viewModel.selectedMemberDob)
        intent.putExtra(DefinedParams.FollowUpId, viewModel.followUpId)
        intent.putExtra(DefinedParams.MenuId, menuId)
        intent.putExtra(DefinedParams.FhirId, getMemberReference())
        intent.putExtra(DefinedParams.ORIGIN, getOrigin())
        startActivity(intent)
    }

    private fun startAssessmentActivity(menuId: String, workFlowName: String?) {
        val intent = Intent(requireContext(), AssessmentActivity::class.java)
        intent.putExtra(DefinedParams.HouseholdId,viewModel.selectedHouseholdId)
        intent.putExtra(DefinedParams.MemberID, viewModel.selectedHouseholdMemberID)
        intent.putExtra(DefinedParams.DOB, viewModel.selectedMemberDob)
        intent.putExtra(MenuConstants.FOLLOW_UP, isFollowUp())
        intent.putExtra(DefinedParams.FollowUpId, viewModel.followUpId)
        intent.putExtra(DefinedParams.MenuId, menuId)
        intent.putExtra(DefinedParams.FhirId, getMemberReference())
        intent.putExtra(DefinedParams.ORIGIN, getOrigin())
        workFlowName?.let { name ->
            intent.putExtra(WorkFlowName, name)
        }
        startActivity(intent)
    }

    private fun getPatientReference(): String? {
        return requireArguments().getString(DefinedParams.PatientId)
    }

    private fun getMemberReference(): String? {
        return requireArguments().getString(DefinedParams.FhirId)
    }
    private fun isFollowUp(): Boolean {
        return requireArguments().getBoolean(MenuConstants.FOLLOW_UP)
    }

    private fun getOrigin(): String {
        return requireArguments().getString(DefinedParams.ORIGIN) ?: ""
    }

    private fun getEncounterReference(): String {
        return requireArguments().getString(EncounterReference) ?: ""
    }
}