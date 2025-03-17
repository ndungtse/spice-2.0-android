package com.medtroniclabs.spice.ui.medicalreview.familyplan.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.databinding.FragmentFamilyPlanTreatmentBinding
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.familyplan.viewmodel.ContraceptivesViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.ClientType
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.PostPartum
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.ProgestinOnlyOrals
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FamilyPlanTreatmentFragment : BaseFragment() {
    private lateinit var binding: FragmentFamilyPlanTreatmentBinding
    val contraceptivesViewModel: ContraceptivesViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFamilyPlanTreatmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.tvClientTypeText.text = contraceptivesViewModel.resultHashMap[ClientType] as? String
        binding.tvPostPartumText.text = contraceptivesViewModel.resultHashMap[PostPartum] as? String
        binding.tvProgestinOnlyOralsText.text = contraceptivesViewModel.resultHashMap[ProgestinOnlyOrals] as? String
        binding.tvQuantityText.text = contraceptivesViewModel.quantity
    }

    companion object {
       const val TAG = "FamilyPlanTreatmentSummaryFragment"
    }
}