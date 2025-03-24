package com.medtroniclabs.spice.ui.cbs.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.CbsNotifiableCondition
import com.medtroniclabs.spice.common.DefinedParams.NotifiableConditions
import com.medtroniclabs.spice.common.DefinedParams.OtherNotifiableConditions
import com.medtroniclabs.spice.common.DefinedParams.RmnchNotifiableCondition
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentAssessmentIccmSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ChildHoodVisit
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PNCNeonatal
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.medtroniclabs.spice.ui.dialog.SuccessDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CbsSummaryFragment : BaseFragment(),View.OnClickListener {
    private lateinit var binding: FragmentAssessmentIccmSummaryBinding
    private val viewModel: AssessmentViewModel by activityViewModels()
    var type :String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentIccmSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "CbsSummaryFragment"
        fun newInstance() = CbsSummaryFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.resultCardView.gone()
        binding.emptyErrorMessage.gone()
        viewModel.getUserProfile()
        binding.btnDone.safeClickListener(this)
        binding.callSupervisor.safeClickListener(this)
        attachObservers()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnDone -> {
                val existingFragment =
                    childFragmentManager.findFragmentByTag(SuccessDialogFragment.TAG)
                if (existingFragment == null) {
                    SuccessDialogFragment.newInstance(descText = getString(R.string.cbs_register_updated))
                        .show(childFragmentManager, SuccessDialogFragment.TAG)
                }
            }

            R.id.callSupervisor -> {
                callPeerSuperior()
            }
        }
    }

    private fun callPeerSuperior() {
        type = DefinedParams.ps
        val phoneNumber = viewModel.patientHealthFacility.value?.data
            ?.firstOrNull()
            ?.phoneNumber
        val phoneCode = SecuredPreference.getPhoneNumberCode()?.let { if (it.startsWith("+")) it else "+$it" }
        if (!phoneNumber.isNullOrBlank()) {
            navToDial(phoneNumber)
        }
    }

    private fun attachObservers() {
        viewModel.userProfileLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as? BaseActivity)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    binding.resultCardView.visible()
                    (activity as? BaseActivity)?.hideLoading()
                    resourceState.data?.let {
                        getPHUDetails()
                    }
                }

                ResourceState.ERROR -> {
                    (activity as? BaseActivity)?.hideLoading()
                }
            }
        }
        viewModel.patientHealthFacility.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.emptyErrorMessage.gone()
                    binding.parentLayout.visible()
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    binding.resultCardView.visible()
                    hideProgress()
                    viewModel.assessmentStringLiveData.value?.let { result ->
                        updateStatusBar()
                        createSummaryView(createListSummaryData(result))
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun showPHU() {
        if (viewModel.workflowName.equals(ANC, true)
            || viewModel.workflowName.equals(PNCNeonatal, true)
            || viewModel.workflowName.equals(ChildHoodVisit, true)) {
            binding.callSupervisor.visible()
        } else {
            binding.callSupervisor.gone()
            setEmergencyPHUPhoneNumber()
        }
    }

    private fun getPHUDetails() {
        viewModel.memberDetailsLiveData.value?.data?.villageId
            ?.takeIf { it.isNotBlank() }
            ?.toLongOrNull()
            ?.let { id -> viewModel.getHealthFacilityBasedOnVillageId(id) }
    }

    private fun setEmergencyPHUPhoneNumber() {
        val phoneNumber =
            viewModel.patientHealthFacility.value?.data?.firstOrNull()?.phoneNumber?.takeIf { it.isNotBlank() }
                ?: "-"
        val phoneCode =
            SecuredPreference.getPhoneNumberCode()?.let { if (it.startsWith("+")) it else "+$it" }

        bindSummaryView(
            getString(R.string.emergency_contact_at_PHU),
            phoneNumber,
            isCallShown = true,
            countryCode = phoneCode )
    }

    private fun createListSummaryData(data: String): MutableList<AssessmentSummaryModel>? {
        return viewModel.formLayoutsLiveData.value?.data?.formLayout?.filter { it.isSummary == true }
            ?.map { formLayout ->
                AssessmentSummaryModel(
                    title = formLayout.titleSummary ?: formLayout.title,
                    id = formLayout.id,
                    cultureValue = formLayout.titleCulture,
                    value = AssessmentCommonUtils.getValueOfKeyFromMap(
                        StringConverter.stringToMap(data),
                        formLayout.id,
                        DefinedParams.CBS
                    )
                )
            }?.toMutableList()
    }

    private fun createSummaryView(
        listSummaryData: MutableList<AssessmentSummaryModel>?
    ) {
        listSummaryData?.let { summaryData ->
            binding.tvTitle.text = getString(R.string.cbs)
            binding.emptyErrorMessage.visibility = View.GONE
            binding.parentLayout.visibility = View.VISIBLE
            binding.parentLayout.removeAllViews()
            composeSummaryView(summaryData)
        } ?: kotlin.run {
            showErrorInSummary()
        }
    }

    private fun composeSummaryView(listSummaryData: MutableList<AssessmentSummaryModel>) {
        if (viewModel.workflowName.equals(ANC, true)
            || viewModel.workflowName.equals(PNCNeonatal, true)
            || viewModel.workflowName.equals(ChildHoodVisit, true)
        ) {
            listSummaryData.filter { it.value != null && !it.id.equals(OtherNotifiableConditions,true)  }.forEach { item ->
                if (setOf(
                        NotifiableConditions,
                        CbsNotifiableCondition,
                        RmnchNotifiableCondition
                    ).contains(item.id)
                ) {
                    val otherValue =
                        listSummaryData.find {
                            it.id.equals(
                                OtherNotifiableConditions,
                                true
                            )
                        }?.value
                    val value =
                        if (otherValue != null) "${item.value} - $otherValue" else item.value
                    bindSummaryView(item.title, value)
                } else {
                    bindSummaryView(item.title, item.value)
                }
            }
        } else {
            listSummaryData.filter { it.value != null && !it.id.equals(OtherNotifiableConditions,true) }.forEach { item ->
                if (setOf(NotifiableConditions,
                        CbsNotifiableCondition,
                        RmnchNotifiableCondition).contains(item.id)
                ) {
                    val otherValue =
                        listSummaryData.find {
                            it.id.equals(
                                OtherNotifiableConditions,
                                true
                            )
                        }?.value
                    val value =
                        if (otherValue != null) "${item.value} - $otherValue" else item.value
                    bindSummaryView(item.title, value)
                } else {
                    bindSummaryView(item.title, item.value)
                }
            }
            val supervisor = viewModel.userProfileLiveData.value?.data?.supervisor
            val supervisorNumber = supervisor?.phoneNumber.takeIf { !it.isNullOrBlank() }
                ?: getString(R.string.separator_double_hyphen)
            val supervisorName = supervisor?.let {
                if (!supervisor.firstName.isNullOrBlank() && !supervisor.lastName.isNullOrBlank()) {
                    requireContext().getString(
                        R.string.firstname_lastname,
                        supervisor.firstName,
                        supervisor.lastName
                    )
                } else {
                    getString(R.string.separator_double_hyphen)
                }
            } ?: getString(R.string.separator_double_hyphen)

            // Handle the case where phoneNumber might be null or empty

            bindSummaryView(getString(R.string.peer_supervisor_name), supervisorName)
            val phoneCode = SecuredPreference.getPhoneNumberCode()?.let { if (it.startsWith("+")) it else "+$it" }
            bindSummaryView(
                getString(R.string.peer_supervisor_number),
                supervisorNumber,
                isCallShown = true,
                countryCode = phoneCode
            )
            val organizations = viewModel.userProfileLiveData.value?.data?.organizations
            val linkedPHU = organizations?.takeIf { it.isNotEmpty() }
                ?.joinToString(", ") { it.name }
                ?: getString(R.string.hyphen_symbol)
            bindSummaryView(getString(R.string.linked_phu), linkedPHU)
        }
        showPHU()
    }


    private fun bindSummaryView(title: String?, value: String?, valueTextColor: Int? = null,isCallShown :Boolean = false,countryCode:String? = null) {
        value?.let { result ->
            binding.parentLayout.addView(
                AssessmentCommonUtils.addViewSummaryLayout(
                    title,
                    result,
                    valueTextColor,
                    requireContext(),
                    isCallShown = isCallShown,
                    callBtnTag = title,
                    callback = { tag, value ->
                        tag?.let {
                            handleCallBtnClick(tag,value)
                        }
                    }
                    ,forCbs = true,
                    countryCode = countryCode
                )
            )
        }
    }



    private fun handleCallBtnClick(tag: String, value: String?) {
        type = if (tag == getString(R.string.peer_supervisor_number)) {
            DefinedParams.ps
        } else {
            DefinedParams.phu
        }
        value?.let {
            navToDial(it)
        }
    }


    private fun navToDial(phoneNumber: String?) {
        if (hasTelephonyFeature(requireContext())) {
            phoneNumber?.let {
                val dialIntent = Intent(Intent.ACTION_DIAL)
                dialIntent.data = Uri.parse("tel:$it")
                dialerLauncher.launch(dialIntent)
            }
        } else {
            showCallDialError(false)
        }
    }

    private val dialerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK || result.resultCode == Activity.RESULT_CANCELED) {
                CbsCallResultFragment.newInstance(type)
                    .show(childFragmentManager, CbsCallResultFragment.TAG)
            }
        }

    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.parentLayout.visibility = View.GONE
    }


    private fun updateStatusBar() {
        binding.phuReferredGroup.gone()
        binding.riskResultLayout.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.attention_color)
        binding.riskResultLayout.text =
            getString(R.string.urgent_referral)
    }
}
