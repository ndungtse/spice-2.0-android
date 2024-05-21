package com.medtroniclabs.spice.ui.mypatients.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.formatListToStringWithOther
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentMedicalReviewPatientDiagnosisBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.DiagnosisDialogFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.AddBpDialog
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.AddWeightDialog
import com.medtroniclabs.spice.ui.medicalreview.viewmodel.PatientStatusViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MedicalReviewPatientDiagnosisFragment : BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentMedicalReviewPatientDiagnosisBinding
    private val viewModel: PatientStatusViewModel by activityViewModels()

    companion object {
        fun newInstance(): MedicalReviewPatientDiagnosisFragment {
            return MedicalReviewPatientDiagnosisFragment()
        }

        fun newInstance(isAnc: Boolean, patientId: String?): MedicalReviewPatientDiagnosisFragment {
            val fragment = MedicalReviewPatientDiagnosisFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(DefinedParams.PregnancyANC, isAnc)
                putString(DefinedParams.PatientId, patientId)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMedicalReviewPatientDiagnosisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        attachObserver()
        handleFlow()
        initializeListeners()
    }

    private fun handleFlow() {
        val id = arguments?.getString(DefinedParams.PatientId, "")
        with(binding) {
            val isAnc = arguments?.getBoolean(DefinedParams.PregnancyANC, false)
            if (isAnc == true) {
                cardAddWeight.visible()
                cardBloodPressure.visible()
            } else {
                cardAddWeight.gone()
                cardBloodPressure.gone()
            }
            tvAddWeight.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
            tvAddBp.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
            cardDiagnosis.visible()
            cardPatientStatus.visible()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.tvAddWeight.id -> {
                AddWeightDialog.newInstance().show(childFragmentManager, AddWeightDialog.TAG)
            }

            binding.tvAddBp.id -> {
                AddBpDialog.newInstance().show(childFragmentManager, AddBpDialog.TAG)
            }

            binding.tvDiagnosisConfirm.id -> {
                DiagnosisDialogFragment().show(
                    childFragmentManager,
                    DiagnosisDialogFragment.TAG
                )
            }
        }
    }

    private fun initializeListeners() {
        binding.tvDiagnosisConfirm.safeClickListener(this)
    }

    private fun attachObserver() {
        viewModel.patientStatusLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { list ->
                        binding.tvPatientStatusValue.text =
                            formatListToStringWithOther(list.map { it.status })
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun initializeViews() {
        viewModel.patientId?.let {
            viewModel.getPatientStatus(it)
        }
    }

}