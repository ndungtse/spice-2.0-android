package com.medtroniclabs.spice.ui.mypatients.fragment

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setWidth
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentSelectFlowDialogBinding
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.ui.home.ToolsViewModel
import com.medtroniclabs.spice.ui.mypatients.activity.MotherNeonateANCActivity

class SelectFlowDialog : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentSelectFlowDialogBinding
    private val viewModel: ToolsViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectFlowDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    companion object {
        const val TAG = "SelectFlowDialog"
        fun newInstance(): SelectFlowDialog {
            return SelectFlowDialog()
        }

        fun newInstance(patientId: String?): SelectFlowDialog {
            val fragment = SelectFlowDialog()
            val bundle = Bundle()
            bundle.putString(DefinedParams.PatientId, patientId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        handleDialogSize()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleDialogSize()
    }

    private fun handleDialogSize() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val width = if (CommonUtils.checkIsTablet(requireContext())) {
            if (isLandscape) 65 else 90
        } else {
            if (isLandscape) 65 else 90
        }
        setWidth(width)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListener()
    }

    private var singleSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultANCFlowHashMap[TAG] = selectedID as String
            launchActivity()
            dismiss()
        }

    private fun launchActivity() {
        when (viewModel.resultANCFlowHashMap[TAG]) {
            getString(R.string.anc) -> {
                val patientId = arguments?.getString(DefinedParams.PatientId, "")
                val intent = Intent(requireContext(), MotherNeonateANCActivity::class.java)
                if (patientId?.isNotBlank() == true) {
                    intent.putExtra(DefinedParams.PatientId, patientId)
                }
                startActivity(intent)
            }

            getString(R.string.pnc) -> {

            }

            getString(R.string.child_hood_visit) -> {

            }
        }
    }


    private fun initView() {
        viewModel.resultANCFlowHashMap.clear()
        getRMNCHFlowData().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.resultANCFlowHashMap,
                Pair(TAG, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallback
            )
            binding.selectionGroup.addView(view)
        }

    }

    private fun setListener() {
        binding.ivClose.setOnClickListener(this)
    }

    private fun getRMNCHFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(CommonUtils.getOptionMap(getString(R.string.anc), getString(R.string.anc)))
        flowList.add(CommonUtils.getOptionMap(getString(R.string.pnc), getString(R.string.pnc)))
        flowList.add(
            CommonUtils.getOptionMap(
                getString(R.string.child_hood_visit),
                getString(R.string.child_hood_visit)
            )
        )
        return flowList
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.ivClose.id -> {
                dismiss()
            }
        }
    }
}