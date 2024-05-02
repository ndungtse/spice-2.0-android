package com.medtroniclabs.spice.ui.followup.fragment

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
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.CallResult
import com.medtroniclabs.spice.common.DefinedParams.PatientStatus
import com.medtroniclabs.spice.common.DefinedParams.UnSuccessful
import com.medtroniclabs.spice.databinding.FragmentBottomCallResultDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.ui.followup.viewmodel.FollowUpViewModel

class CallResultDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentBottomCallResultDialogBinding
    private val viewModel: FollowUpViewModel by activityViewModels()

    companion object {
        const val TAG = "CallResultDialogFragment"
        fun newInstance(): CallResultDialogFragment {
            return CallResultDialogFragment()
        }
    }

    override fun getTheme(): Int {
        return R.style.DialogStyle
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomCallResultDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        getCallResultData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.callResultHashMap,
                Pair(CallResult,null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                callResultSelectionCallback
            )
            binding.selectionCallResult.addView(view)
        }

        getPatientStatusData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.patientStatusHashMap,
                Pair(PatientStatus,null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                patientStatusSelectionCallback
            )
            binding.selectionPatientStatus.addView(view)
        }
        binding.btnDone.safeClickListener(this)

        getUnsuccessfulData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.unSuccessfulHashMap,
                Pair(UnSuccessful,null),
                FormLayout(
                    viewType = "",
                    id = "",
                    title = "",
                    visibility = "",
                    optionsList = null
                ),
                unsuccessfulSelectionCallback
            )
            binding.selectionReason.addView(view)
        }
    }

    private var callResultSelectionCallback: ((selectedID: Any?, elementId: Pair<String,String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.callResultHashMap[CallResult] = selectedID as String
            if (selectedID == "Unsuccessful") {
                viewPatientStatusGone()
            } else {
                viewPatientStatus()
            }
            enableConfirmed()
        }

    private var unsuccessfulSelectionCallback: ((selectedID: Any?, elementId: Pair<String,String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.unSuccessfulHashMap[UnSuccessful] = selectedID as String
            enableConfirmed()
        }

    private fun viewPatientStatusGone() {
        binding.tvPatientStatus.visibility = View.GONE
        binding.selectionPatientStatus.visibility = View.GONE
        binding.tvReason.visibility = View.VISIBLE
        binding.selectionReason.visibility = View.VISIBLE
    }

    private fun viewPatientStatus() {
        binding.tvReason.visibility = View.GONE
        binding.selectionReason.visibility = View.GONE
        binding.tvPatientStatus.visibility = View.VISIBLE
        binding.selectionPatientStatus.visibility = View.VISIBLE
    }


    private var patientStatusSelectionCallback: ((selectedID: Any?, elementId: Pair<String,String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.patientStatusHashMap[PatientStatus] = selectedID as String
            enableConfirmed()
        }


    private fun enableConfirmed() {
        binding.btnDone.isEnabled =
            viewModel.callResultHashMap.containsKey(CallResult) || viewModel.patientStatusHashMap.containsKey(
                PatientStatus
            )
    }

    private fun getCallResultData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.successful)))
        flowList.add(getOptionMap(getString(R.string.un_successful)))
        return flowList
    }

    private fun getPatientStatusData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.recovered)))
        flowList.add(getOptionMap(getString(R.string.on_treatment)))
        flowList.add(getOptionMap(getString(R.string.referred)))
        return flowList
    }

    private fun getUnsuccessfulData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.un_reachable)))
        flowList.add(getOptionMap(getString(R.string.wrong_number)))
        return flowList
    }

    private fun getOptionMap(value: String): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[DefinedParams.ID] = value
        map[DefinedParams.NAME] = value
        return map
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnDone.id -> {
                dismiss()
            }
        }
    }
}