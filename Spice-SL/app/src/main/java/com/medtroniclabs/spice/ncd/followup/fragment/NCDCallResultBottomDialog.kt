package com.medtroniclabs.spice.ncd.followup.fragment

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
import com.medtroniclabs.spice.appextensions.convertToUtcDateTime
import com.medtroniclabs.spice.common.CommonUtils.getOptionMap
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.offlinesync.model.FollowUpCallReason
import com.medtroniclabs.spice.data.offlinesync.model.FollowUpCallStatus
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentNcdCallResultBottomDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.ncd.data.CallDetails
import com.medtroniclabs.spice.ncd.data.FollowUpUpdateRequest
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.visited_facility
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.will_visit_facility
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.wont_visit_facility
import com.medtroniclabs.spice.ncd.followup.viewmodel.NCDFollowUpViewModel
import com.medtroniclabs.spice.ui.followup.fragment.CallResultDialogFragment

class NCDCallResultBottomDialog : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentNcdCallResultBottomDialogBinding
    private val viewModel: NCDFollowUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNcdCallResultBottomDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.DialogStyle
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false

        viewModel.callResultHashMap.clear()
        viewModel.patientStatusHashMap.clear()
        viewModel.unSuccessfulHashMap.clear()
    }

    companion object {
        const val TAG = "NCDCallResultBottomDialog"
        fun newInstance() =
            NCDCallResultBottomDialog().apply {
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        viewModel.callResultHashMap[DefinedParams.CallResult] = FollowUpCallStatus.SUCCESSFUL.name
        getCallResultData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = CallResultDialogFragment.TAG
            view.addViewElements(
                it,
                false,
                viewModel.callResultHashMap,
                Pair(DefinedParams.CallResult, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                callResultSelectionCallback
            )
            binding.selectionCallResult.addView(view)
        }
        enableForSuccessFul()
        showPatientStatusForSuccess()
        binding.btnDone.safeClickListener(this)
    }

    private fun enableForSuccessFul() {
        val isCallResultEnabled = viewModel.callResultHashMap.isNotEmpty()
        val isReasonEnabled = viewModel.patientStatusHashMap.isNotEmpty()
        binding.btnDone.isEnabled = isCallResultEnabled && isReasonEnabled
    }

    private var callResultSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            val newSelection = selectedID as String
            viewModel.callResultHashMap[DefinedParams.CallResult] = newSelection
            viewModel.unSuccessfulHashMap.clear()
            viewModel.patientStatusHashMap.clear()
            if (newSelection == FollowUpCallStatus.UNSUCCESSFUL.name) {
                showUnsuccessfulReason()
                enableForUnSuccessful()
            } else {
                showPatientStatusForSuccess()
                enableForSuccessFul()
            }
        }

    private fun showUnsuccessfulReason() {
        binding.selectionPatientStatus.removeAllViews()
        binding.tvPatientStatus.text = getString(R.string.reason)
        getUnsuccessfulData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = CallResultDialogFragment.TAG
            view.addViewElements(
                it,
                false,
                viewModel.unSuccessfulHashMap,
                Pair(DefinedParams.UnSuccessful, null),
                FormLayout(
                    viewType = "",
                    id = "",
                    title = "",
                    visibility = "",
                    optionsList = null
                ),
                unsuccessfulSelectionCallback
            )
            binding.selectionPatientStatus.addView(view)
        }
    }

    private var unsuccessfulSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.unSuccessfulHashMap[DefinedParams.UnSuccessful] = selectedID as String
            enableForUnSuccessful()
        }


    private fun enableForUnSuccessful() {
        val isCallResultEnabled = viewModel.callResultHashMap.isNotEmpty()
        val isReasonEnabled = viewModel.unSuccessfulHashMap.isNotEmpty()

        binding.btnDone.isEnabled = isCallResultEnabled && isReasonEnabled
    }

    private fun getUnsuccessfulData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(
            getOptionMap(
                FollowUpCallReason.UNREACHABLE.name,
                getString(R.string.un_reachable)
            )
        )
        flowList.add(
            getOptionMap(
                FollowUpCallReason.WRONG_NUMBER.name,
                getString(R.string.wrong_number)
            )
        )
        return flowList
    }

    private fun getCallResultData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(
            getOptionMap(
                FollowUpCallStatus.SUCCESSFUL.name,
                getString(R.string.successful)
            )
        )
        flowList.add(
            getOptionMap(
                FollowUpCallStatus.UNSUCCESSFUL.name,
                getString(R.string.un_successful)
            )
        )
        return flowList
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnDone.id -> {
                viewModel.getPatientRegisterResponse.value?.data?.let {
                    val request = FollowUpUpdateRequest(
                        id = it.id,
                        patientId = it.patientId,
                        memberId = it.memberId,
                        type = viewModel.type,
                        villageId = it.villageId,
                        isInitiated = false,
                        provenance = ProvanceDto(),
                        followUpDetails = listOf(
                            CallDetails(
                                callDate = System.currentTimeMillis().convertToUtcDateTime(),
                                status = (viewModel.callResultHashMap[DefinedParams.CallResult] as? String)
                                    ?: null,
                                reason = (viewModel.unSuccessfulHashMap[DefinedParams.UnSuccessful] as? String)
                                    ?: null,
                                patientStatus = (viewModel.patientStatusHashMap[DefinedParams.PatientStatus] as? String)
                                    ?: null
                            )
                        )
                    )
                    viewModel.updatePatientCallRegister(request)
                }
            }
        }
    }

    private fun showPatientStatusForSuccess() {
        binding.selectionPatientStatus.removeAllViews()
        binding.tvPatientStatus.text = "Current Status"
        getPatientStatusForSuccessData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = CallResultDialogFragment.TAG
            view.addViewElements(
                it,
                false,
                viewModel.patientStatusHashMap,
                Pair(DefinedParams.PatientStatus, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                patientStatusForSuccessSelectionCallback
            )
            binding.selectionPatientStatus.addView(view)
        }
    }

    private var patientStatusForSuccessSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.patientStatusHashMap[DefinedParams.PatientStatus] = selectedID as String
            enableForSuccessFul()
        }


    private fun getPatientStatusForSuccessData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(visited_facility, visited_facility))
        flowList.add(
            getOptionMap(
                will_visit_facility,
                will_visit_facility
            )
        )
        flowList.add(getOptionMap(wont_visit_facility, wont_visit_facility))
        return flowList
    }
}