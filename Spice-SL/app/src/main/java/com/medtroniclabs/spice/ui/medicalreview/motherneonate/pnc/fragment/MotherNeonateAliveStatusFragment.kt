package com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentMotherNeonarePncSummaryBinding
import com.medtroniclabs.spice.databinding.MotherNeonateStatusFragmentBinding
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.viewmodel.MotherNeonatePNCViewModel

class MotherNeonateAliveStatusFragment : BaseFragment() {
    private lateinit var binding: MotherNeonateStatusFragmentBinding
    var adapter: CustomSpinnerAdapter? = null
    private var datePickerDialog: DatePickerDialog? = null
    val viewModel: MotherNeonatePNCViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MotherNeonateStatusFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "MotherNeonateSummary"
        fun newInstance(): MotherNeonateAliveStatusFragment {
            return MotherNeonateAliveStatusFragment()
        }

//        fun newInstance(encounterId: String?): MotherNeonarePncSummaryFragment {
//            val fragment = MotherNeonarePncSummaryFragment()
//            val bundle = Bundle()
//            bundle.putString(DefinedParams.EncounterId, encounterId)
//            fragment.arguments = bundle
//            return fragment
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeAliveStatusLayout()
    }
    private fun initializeAliveStatusLayout() {
        getAliveStatusFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.resultFlowHashMap,
                Pair(TAG, null),
                FormLayout(
                    viewType = "",
                    id = "",
                    title = "",
                    visibility = "",
                    optionsList = null
                ),
                singleSelectionCallback
            )
            binding.btnLayout.addView(view)
        }
    }
    private var singleSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->

            viewModel.resultFlowHashMap[TAG] =
                selectedID as String
            val flowValue =
                viewModel.resultFlowHashMap[TAG] as? String
            viewModel.aliveStatus =
                flowValue?.equals(HouseHoldRegistration.yes, ignoreCase = true) ?: false
//            if (viewModel.aliveStatus == true) {
//                binding.blurView.gone()
//            } else {
//                binding.blurView.visible()
//                refreshFragments()
//            }
        }
    private fun getAliveStatusFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(
            CommonUtils.getOptionMap(
                getString(R.string.yes),
                getString(R.string.yes)
            )
        )
        flowList.add(
            CommonUtils.getOptionMap(
                getString(R.string.no),
                getString(R.string.no)
            )
        )
        return flowList
    }

}