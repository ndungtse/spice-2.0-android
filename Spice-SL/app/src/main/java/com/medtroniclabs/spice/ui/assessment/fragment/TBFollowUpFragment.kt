package com.medtroniclabs.spice.ui.assessment.fragment

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.getOptionMap
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentTBFollowUpBinding
import com.medtroniclabs.spice.databinding.FragmentTBTreatmentBinding
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.ClientType
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.EmergencyContraceptive
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TBFollowUpFragment : BaseFragment() {

    private lateinit var binding: FragmentTBFollowUpBinding
    private val viewModel: AssessmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTBFollowUpBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

        binding.etNextFollowUpDate.background=null
        binding.etNextFollowUpDate.background= ContextCompat.getDrawable(requireContext(),R.drawable.edittext_background)
        val background = binding.etNextFollowUpDate.background as? GradientDrawable
        background?.setStroke(resources.getDimensionPixelSize(R.dimen._1sdp), ContextCompat.getColor(requireContext(), R.color.edittext_stroke))

        addCustomView(
            getOptionsData(),
            DefinedParams.SymptomsFollowUp,
            viewModel.rxBuddyFollowUpResultHashMap,
            symptomsSelectionCallBack,
            binding.SymptomsWorseRoot
        )

        addCustomView(
            getOptionsData(),
            DefinedParams.MedicationFollowUp,
            viewModel.rxBuddyFollowUpResultHashMap,
            medicationSelectionCallBack,
            binding.MedicationRoot
        )
    }

    companion object {
      const val TAG = "TBFollowUpFragment"
    }

    private fun addCustomView(
        data: ArrayList<Map<String, Any>>,
        tag: String,
        hashMap: HashMap<String, Any>,
        callback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)?,
        container: ViewGroup?
    ) {
        SingleSelectionCustomView(binding.root.context).apply {
            this.tag = tag
            addViewElements(
                data,
                false,
                hashMap,
                Pair(tag, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                callback
            )
            container?.addView(this)
        }
    }

    private fun getOptionsData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.yes), getString(R.string.yes)))
        flowList.add(getOptionMap(getString(R.string.no), getString(R.string.no)))
        return flowList
    }

    private var symptomsSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.rxBuddyFollowUpResultHashMap[DefinedParams.SymptomsFollowUp] = selectedID as String
            resultMapChanged()
        }

    private var medicationSelectionCallBack: (selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit =
        { selectedID, _, _, _ ->
            viewModel.rxBuddyFollowUpResultHashMap[DefinedParams.MedicationFollowUp] = selectedID as String
            resultMapChanged()
        }

    private fun resultMapChanged(){
        setFragmentResult(
            DefinedParams.RX_BUDDY_FOLLOW_UP, bundleOf(
                DefinedParams.RX_BUDDY_FOLLOW_UP_VALUES to true)
        )
    }

     fun validInput():Boolean{
        var isValid = true
        if(!checkAndToggleError(DefinedParams.SymptomsFollowUp,binding.tvSymptomsWorseErrorMessage)){
            isValid = false
        }
        if(!checkAndToggleError(DefinedParams.MedicationFollowUp,binding.tvMedicationErrorMessage)){
            isValid = false
        }
        return isValid
    }

    private fun checkAndToggleError(key: String, errorView: View) :Boolean{
        if (viewModel.rxBuddyFollowUpResultHashMap.containsKey(key)) {
            errorView.gone()
            return true
        } else {
            errorView.visible()
            return false
        }
    }
}