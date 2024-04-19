package com.medtroniclabs.spice.ui.mypatients.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.formatListToStringWithOther
import com.medtroniclabs.spice.databinding.FragmentMedicalReviewPatientDiagnosisBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.DiagnosisDialogFragment
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

        fun newInstance(isAnc: Boolean): MedicalReviewPatientDiagnosisFragment {
            val fragment = MedicalReviewPatientDiagnosisFragment()
            val bundle = Bundle()
            bundle.putBoolean(ANC, isAnc)
            fragment.arguments = bundle
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
        val isAnc = arguments?.getBoolean(ANC)
        if (isAnc == true) {
            with(binding) {
                cardAncVisit.visible()
                cardAddWeight.visible()
                cardBloodPressure.visible()
                tvAddWeight.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
                tvAddBp.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
                cardDiagnosis.gone()
                cardPatientStatus.gone()
            }
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