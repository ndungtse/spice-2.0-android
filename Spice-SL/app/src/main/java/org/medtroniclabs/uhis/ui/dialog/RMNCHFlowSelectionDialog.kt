package org.medtroniclabs.uhis.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.common.CommonUtils.getOptionMap
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.databinding.FragmentRmnchSelectionDialogBinding
import org.medtroniclabs.uhis.formgeneration.model.FormLayout
import org.medtroniclabs.uhis.formgeneration.ui.SingleSelectionCustomView
import org.medtroniclabs.uhis.ui.MenuConstants.DIALOG_RESULT
import org.medtroniclabs.uhis.ui.MenuConstants.WORKFLOW_NAME
import org.medtroniclabs.uhis.ui.assessment.rmnch.RMNCH
import org.medtroniclabs.uhis.ui.home.ToolsViewModel

class RMNCHFlowSelectionDialog : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentRmnchSelectionDialogBinding

    private val viewModel: ToolsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    companion object {
        const val TAG = "RMNCHFlowSelectionDialog"

        fun newInstance(): RMNCHFlowSelectionDialog = RMNCHFlowSelectionDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRmnchSelectionDialogBinding.inflate(layoutInflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListener()
    }

    private fun initView() {
        viewModel.setUserJourney(AnalyticsDefinedParams.RMNCHSELECTFLOWDIALOUGE)
        getRMNCHFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                SecuredPreference.getIsTranslationEnabled(),
                viewModel.resultRMNCHFlowHashMap,
                Pair(TAG, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallback,
            )
            binding.selectionGroup.addView(view)
        }
    }

    private fun setListener() {
        binding.ivClose.setOnClickListener(this)
    }

    private var singleSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, formLayout: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            saveSelectedOptionValue(selectedID)
        }

    private fun saveSelectedOptionValue(selectedId: Any?) {
        selectedId?.let {
            setFragmentResult(DIALOG_RESULT, bundleOf(WORKFLOW_NAME to it))
            dismiss()
        }
    }

    private fun getRMNCHFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(RMNCH.ANC, getString(R.string.anc)))
        flowList.add(getOptionMap(RMNCH.PNC, getString(R.string.pnc)))
        return flowList
    }

    override fun onClick(v: View) {
        when (v.id) {
            binding.ivClose.id -> {
                viewModel.setUserJourney(AnalyticsDefinedParams.RMNCHSELECTFLOWDIALOUGECLOSETRIGGERED)
                dismiss()
            }
        }
    }
}
