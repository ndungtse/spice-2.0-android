package com.medtroniclabs.spice.ui.cbs.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.common.CommonUtils.getOptionMap
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.PHU
import com.medtroniclabs.spice.common.DefinedParams.PeerSupervisor
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.CbsCallResult
import com.medtroniclabs.spice.data.CbsFollowUp
import com.medtroniclabs.spice.data.offlinesync.model.FollowUpCallStatus
import com.medtroniclabs.spice.databinding.FragmentBottomCallResultDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.medtroniclabs.spice.ui.followup.fragment.CallResultDialogFragment

class CbsCallResultFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentBottomCallResultDialogBinding
    private val viewModel: AssessmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBottomCallResultDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun getTheme(): Int = R.style.DialogStyle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    companion object {
        const val TAG = "CbsCallResultFragment"

        fun newInstance(type: String) =
            CbsCallResultFragment().apply {
                val bundle = Bundle().apply {
                    putString(DefinedParams.type, type)
                }
                arguments = bundle
            }
    }

    private fun attachObservers() {
        viewModel.callResultSaveLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {}
                ResourceState.SUCCESS -> {
                    resourceState?.data?.let {
                        dismiss()
                        viewModel.callResultSaveLiveData.postError()
                    }
                }
                ResourceState.ERROR -> {}
            }
        }
    }

    private fun initView() {
        val type = arguments?.getString(DefinedParams.type)
        viewModel.callResultHashMap[DefinedParams.CallResult] =
            if (!type.isNullOrBlank() &&
                type.equals(
                    DefinedParams.ps,
                    true,
                )
            ) {
                getString(R.string.informed_ps)
            } else {
                getString(R.string.informed_phu)
            }
        getCallResultData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = CallResultDialogFragment.TAG
            view.addViewElements(
                it,
                false,
                viewModel.callResultHashMap,
                Pair(DefinedParams.CallResult, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                callResultSelectionCallback,
            )
            binding.selectionCallResult.addView(view)
        }
        binding.tvPatientStatus.gone()
        binding.selectionPatientStatus.gone()
        binding.btnDone.isEnabled = true
        binding.btnDone.safeClickListener(this)
    }

    private fun getCallResultData(): ArrayList<Map<String, Any>> {
        val type = arguments?.getString(DefinedParams.type)
        val options = if (!type.isNullOrBlank() && type.equals(DefinedParams.ps, true)) {
            listOf(R.string.informed_ps, R.string.ps_not_reachable)
        } else {
            listOf(R.string.informed_phu, R.string.phu_not_reachable)
        }
        return ArrayList(options.map { getOptionMap(getString(it), getString(it)) })
    }

    private var callResultSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, formLayout: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            val newSelection = selectedID as String
            viewModel.callResultHashMap[DefinedParams.CallResult] = newSelection
        }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnDone.id -> {
                saveCallResult()
            }
        }
    }

    private fun saveCallResult() {
        viewModel.assessmentSaveLiveData.value?.data?.let { data ->
            val gson = Gson()
            val type = object : TypeToken<CbsFollowUp>() {}.type

            // Convert callResult JSON to list, handling null safely
            val callResults: CbsFollowUp = data.callResult?.let { json ->
                gson.fromJson(json, type)
            } ?: CbsFollowUp()

            // Determine the type and result status
            val typeValue = arguments?.getString(DefinedParams.type)?.trim()?.lowercase()
            val isPsType = typeValue.equals(DefinedParams.ps, true)

            val selectedResult = viewModel.callResultHashMap[DefinedParams.CallResult] as? String

            val followUpStatus = when {
                !selectedResult.isNullOrBlank() &&
                    selectedResult.equals(
                        getString(if (isPsType) R.string.informed_ps else R.string.informed_phu),
                        true,
                    ) -> FollowUpCallStatus.SUCCESSFUL
                else -> FollowUpCallStatus.UNSUCCESSFUL
            }

            val psAttempts = callResults.followUpDetails.filter { it.reason == PeerSupervisor }.size + 1
            val phuAttempts = callResults.followUpDetails.filter { it.reason == PHU }.size + 1

            val followUpReason = if (isPsType) {
                Pair(PeerSupervisor, psAttempts)
            } else {
                Pair(PHU, phuAttempts)
            }

            val lat = SecuredPreference.getDouble(SecuredPreference.EnvironmentKey.CURRENT_LATITUDE.name)
            val lng = SecuredPreference.getDouble(SecuredPreference.EnvironmentKey.CURRENT_LONGITUDE.name)

            // Add new CbsCallResult entry
            callResults.followUpDetails.add(
                CbsCallResult(
                    duration = 0,
                    attempts = followUpReason.second,
                    status = followUpStatus.name,
                    reason = followUpReason.first,
                    latitude = lat,
                    longitude = lng,
                ),
            )

            // Update callResult JSON and save
            data.apply { callResult = gson.toJson(callResults) }
            viewModel.saveCallResult(data)
        }
    }
}
