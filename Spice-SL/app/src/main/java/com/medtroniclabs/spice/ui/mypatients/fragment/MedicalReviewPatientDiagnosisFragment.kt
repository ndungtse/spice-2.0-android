package com.medtroniclabs.spice.ui.mypatients.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.convertListToString
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.databinding.FragmentMedicalReviewPatientDiagnosisBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.DiagnosisDialogFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.DialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.calculateBp
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.viewmodel.DiagnosisViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.AddBpDialog
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.AddWeightDialog
import com.medtroniclabs.spice.ui.mypatients.viewmodel.MotherNeonateBpWeightViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.medicalreview.viewmodel.PatientStatusViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MedicalReviewPatientDiagnosisFragment : BaseFragment(), View.OnClickListener,
    DialogDismissListener {

    private lateinit var binding: FragmentMedicalReviewPatientDiagnosisBinding
    private val viewModel: MotherNeonateBpWeightViewModel by activityViewModels()
    private val statusViewModel: PatientStatusViewModel by activityViewModels()
    private val diagnosisViewModel: DiagnosisViewModel by activityViewModels()
    private val patientViewModel: PatientDetailViewModel by activityViewModels()

    companion object {
        fun newInstance(): MedicalReviewPatientDiagnosisFragment {
            return MedicalReviewPatientDiagnosisFragment()
        }

        fun newInstance(isAnc: Boolean, patientId: String?,memberID: String?): MedicalReviewPatientDiagnosisFragment {
            val fragment = MedicalReviewPatientDiagnosisFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(DefinedParams.PregnancyANC, isAnc)
                putString(DefinedParams.PatientId, patientId)
                putString(DefinedParams.MemberID, memberID)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMedicalReviewPatientDiagnosisBinding.inflate(inflater, container, false)
        arguments?.let {
            diagnosisViewModel.diagnosisType =
                it.getString(MedicalReviewTypeEnums.DiagnosisType.name) ?: ""
            statusViewModel.patientId = it.getString(DefinedParams.ID)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        attachObserver()
        handleFlow()
        initializeListeners()
        attachListeners()
    }

    private fun attachListeners() {
        viewModel.getWeight.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    handleLoading(binding.pageProgress, binding.tvAddWeight, binding.clWeight)
                }

                ResourceState.SUCCESS -> {
                    handleSuccess(
                        binding.pageProgress,
                        binding.retryButtonWeight,
                        binding.clWeight,
                        binding.tvAddWeight
                    )
                    resourceState.data?.let {
                        binding.tvWeightValue.text =
                            MotherNeonateUtil.convertWeight(
                                it.Weight,
                                requireContext()
                            )
                    }
                }

                ResourceState.ERROR -> {
                    handleError(
                        binding.pageProgress,
                        binding.tvAddWeight,
                        binding.clWeight,
                        binding.retryButtonWeight,
                        binding.tvWeightValue
                    )
                }
            }
        }

        patientViewModel.patientDetailsLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.SUCCESS -> {
                    resource.data?.let {
                        statusViewModel.getPatientStatusDetails(
                            it,
                            diagnosisViewModel.diagnosisType
                        )
                    }
                }
                else -> {

                }
            }
        }

        viewModel.getBloodPressure.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    handleLoading(binding.pageProgressBp, binding.tvAddBp, binding.clBp)
                }

                ResourceState.SUCCESS -> {
                    handleSuccess(
                        binding.pageProgressBp,
                        binding.retryButtonBp,
                        binding.clBp,
                        binding.tvAddBp
                    )
                    resourceState.data?.let {
                        binding.tvBpValue.text =
                            calculateBp(it.systolic, it.diastolic, requireContext())
                    }
                }

                ResourceState.ERROR -> {
                    handleError(
                        binding.pageProgressBp,
                        binding.tvAddBp,
                        binding.clBp,
                        binding.retryButtonBp,
                        binding.tvBpValue
                    )
                }
            }
        }
    }

    private fun handleLoading(
        pageProgress: ProgressBar,
        textView: AppCompatTextView,
        clView: ConstraintLayout
    ) {
        pageProgress.visible()
        textView.isEnabled = false
        clView.setBackgroundResource(R.color.grey_bg)
    }

    private fun handleSuccess(
        pageProgress: ProgressBar,
        retryButton: MaterialButton,
        clView: ConstraintLayout,
        textView: AppCompatTextView
    ) {
        pageProgress.gone()
        retryButton.gone()
        clView.setBackgroundResource(R.color.white)
        textView.isEnabled = true
    }

    private fun handleError(
        pageProgress: ProgressBar,
        textView: AppCompatTextView,
        clView: ConstraintLayout,
        retryButton: MaterialButton,
        textViewError: AppCompatTextView
    ) {
        pageProgress.gone()
        textView.isEnabled = true
        clView.setBackgroundResource(R.color.white)
        retryButton.visible()
        textViewError.text =
            requireContext().getString(R.string.something_went_wrong)
    }

    private fun handleFlow() {
        with(binding) {
            val isAnc = arguments?.getBoolean(DefinedParams.PregnancyANC, false)
            if (isAnc == true) {
                cardAddWeight.visible()
                cardBloodPressure.visible()
                if (connectivityManager.isNetworkAvailable()) {
                    viewModel.fetchWeight(MotherNeonateAncRequest(memberId = getMemberId()))
                    viewModel.fetchBloodPressure(MotherNeonateAncRequest(memberId = getMemberId()))
                }
            } else {
                cardAddWeight.gone()
                cardBloodPressure.gone()
            }
            tvAddWeight.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
            tvAddBp.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
            retryButtonBp.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
            retryButtonWeight.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
            cardDiagnosis.visible()
            cardPatientStatus.visible()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.tvAddWeight.id -> showAddWeightDialog()
            binding.tvAddBp.id -> showAddBpDialog()
            binding.tvDiagnosisConfirm.id -> showDiagnosisDialog()
            binding.retryButtonBp.id -> retryFetchingData(true)
            binding.retryButtonWeight.id -> retryFetchingData(false)
        }
    }

    private fun showAddWeightDialog() {
        showAddBpOrWeightDialog(isBp = false)
    }

    private fun showAddBpDialog() {
        showAddBpOrWeightDialog(isBp = true)
    }

    private fun showAddBpOrWeightDialog(isBp: Boolean) {
        val dialog = if (isBp) {
            AddBpDialog.newInstance(getPatientId()).apply {
                listener = this@MedicalReviewPatientDiagnosisFragment
            }
        } else {
            AddWeightDialog.newInstance(getPatientId()).apply {
                listener = this@MedicalReviewPatientDiagnosisFragment
            }
        }
        dialog.show(childFragmentManager, if (isBp) AddBpDialog.TAG else AddWeightDialog.TAG)
    }


    private fun showDiagnosisDialog() {
        DiagnosisDialogFragment().show(childFragmentManager, DiagnosisDialogFragment.TAG)
    }

    private fun retryFetchingData(isBp: Boolean) {
        if (connectivityManager.isNetworkAvailable()) {
            if (isBp) {
                viewModel.fetchBloodPressure(MotherNeonateAncRequest(memberId = getMemberId()))
            } else {
                viewModel.fetchWeight(MotherNeonateAncRequest(memberId = getMemberId()))
            }
        }
    }

    private fun initializeListeners() {
        binding.tvDiagnosisConfirm.safeClickListener(this)
    }

    private fun getPatientId(): String {
        return arguments?.getString(DefinedParams.PatientId, "") ?: ""
    }

    private fun attachObserver() {
        statusViewModel.patientStatusLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { patientStatus ->
                        if (patientStatus.status.isNullOrEmpty()) {
                            binding.tvPatientStatusValue.text = getString(R.string.seperator_hyphen)
                        } else {
                            binding.tvPatientStatusValue.text = patientStatus.status
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }

        diagnosisViewModel.diagnosisDetailsList.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resource.data?.let { list ->
                        if (list.isNotEmpty()) {
                            val diagnosisItems = list.map { it.diseaseCategory }.distinct()
                            binding.tvDiagnosis.text =
                                diagnosisViewModel.diagnosisMetaList.value?.data?.let { diagnosisList ->
                                    convertListToString(ArrayList(diagnosisList.filter { it.value in diagnosisItems }
                                        .map { it.name }))
                                } ?: getString(R.string.seperator_hyphen)
                            binding.tvDiagnosisConfirm.text = getString(R.string.edit_diagnoses)
                        } else {
                            binding.tvDiagnosis.text =
                                requireContext().getString(R.string.hyphen_symbol)
                            binding.tvDiagnosisConfirm.text = getString(R.string.add_diagnosis)
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }

        diagnosisViewModel.diagnosisSaveUpdateResponse.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resource.data?.let { list ->
                        if (list.isNotEmpty()) {
                            val diagnosisItems = list.map { it.diseaseCategory }.distinct()
                            binding.tvDiagnosis.text =
                                convertListToString(ArrayList(diagnosisItems))
                            binding.tvDiagnosisConfirm.text = getString(R.string.edit_diagnoses)
                        } else {
                            binding.tvDiagnosis.text =
                                requireContext().getString(R.string.hyphen_symbol)
                            binding.tvDiagnosisConfirm.text = getString(R.string.add_diagnosis)
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun initializeViews() {
        statusViewModel.patientId?.let {
            binding.tvPatientStatusValue.text = requireContext().getString(R.string.hyphen_symbol)
            diagnosisViewModel.getDiagnosisMetaList(diagnosisViewModel.diagnosisType)
            diagnosisViewModel.getDiagnosisDetails(
                CreateUnderTwoMonthsResponse(
                    patientReference = it
                )
            )
        }
    }

    private fun getMemberId(): String {
        return arguments?.getString(DefinedParams.MemberID, "") ?: ""
    }

    override fun onDialogDismissed(isBp: Boolean) {
        if (connectivityManager.isNetworkAvailable()) {
            if (isBp) {
                viewModel.fetchBloodPressure(MotherNeonateAncRequest(memberId = getMemberId()))
            } else {
                viewModel.fetchWeight(MotherNeonateAncRequest(memberId = getMemberId()))
            }
        }
        val dialog =
            childFragmentManager.findFragmentByTag(if (isBp) AddBpDialog.TAG else AddWeightDialog.TAG) as? AddBpDialog
                ?: return
        dialog.listener = null
        dialog.dismiss()
    }

}