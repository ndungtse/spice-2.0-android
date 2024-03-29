package com.medtroniclabs.spice.ui.mypatients.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentMedicalReviewTreatmentPlanSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.ui.BaseFragment

class MedicalReviewTreatmentPlanSummary : BaseFragment() {

    companion object {
        const val TAG = "MedicalReviewTreatmentPlanSummary"
        fun newInstance(): MedicalReviewTreatmentPlanSummary {
            return MedicalReviewTreatmentPlanSummary()
        }
    }

    private var adapter: CustomSpinnerAdapter? = null
    private var costAdapter: CustomSpinnerAdapter? = null
    private lateinit var binding: FragmentMedicalReviewTreatmentPlanSummaryBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMedicalReviewTreatmentPlanSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        adapter = CustomSpinnerAdapter(requireContext())
        val list = ArrayList<Map<String, Any>>()
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.ID to DefinedParams.DefaultSelectID
            )
        )
        adapter?.setData(list)
        binding.tvPatientText.adapter = adapter

        costAdapter = CustomSpinnerAdapter(requireContext())
        val listCost = ArrayList<Map<String, Any>>()
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.ID to DefinedParams.DefaultSelectID
            )
        )
        costAdapter?.setData(listCost)
        binding.tvCostLabelText.adapter = adapter
        binding.tvMedicalSuppliesText.safeClickListener {
        }
    }
}