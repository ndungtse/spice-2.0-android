package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.google.android.material.button.MaterialButton
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.convertListToString
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.HIV
import com.medtroniclabs.spice.common.DefinedParams.HIV_MEDICAL_REVIEW
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.databinding.FragmentHivMedicalReviewDiagnosesBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.viewmodel.DiagnosisViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.DialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.DialogDismissListenerForTb
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.AddWeightDialog
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.AddHeightDialog
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.BMIListDialog
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.TbConfirmDiagnosisAndSiteOfDiseaseDialog
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.viewmodel.MotherNeonateBpWeightViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel

class HivMedicalReviewDiagnosesFragment : BaseFragment(), View.OnClickListener,
    DialogDismissListenerForTb, DialogDismissListener {

    private lateinit var binding: FragmentHivMedicalReviewDiagnosesBinding
    private val patientViewModel: PatientDetailViewModel by activityViewModels()
    private val diagnosisViewModel: DiagnosisViewModel by activityViewModels()
    private val viewModel: MotherNeonateBpWeightViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHivMedicalReviewDiagnosesBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    private fun initView() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
            details.id?.let { id ->
                diagnosisViewModel.getDiagnosisDetails(
                    CreateUnderTwoMonthsResponse(
                        patientReference = id,
                        type = HIV_MEDICAL_REVIEW
                    )
                )
            }
        }

        diagnosisViewModel.diagnosisType = MedicalReviewTypeEnums.HIV_REVIEW.name
        diagnosisViewModel.getDiagnosisMetaList(diagnosisViewModel.diagnosisType)
        binding.tvDiagnosisConfirm.safeClickListener(this)
        binding.tvBmiHistory.safeClickListener(this@HivMedicalReviewDiagnosesFragment)
        binding.tvAddHeight.safeClickListener(this@HivMedicalReviewDiagnosesFragment)
        binding.retryButtonWeight.safeClickListener(this@HivMedicalReviewDiagnosesFragment)
        binding.tvAddWeight.safeClickListener(this@HivMedicalReviewDiagnosesFragment)
        viewModel.fetchWeight(MotherNeonateAncRequest(memberId = getMemberId()))
        viewModel.fetchHeight(MotherNeonateAncRequest(memberId = getMemberId()))
        viewModel.fetchBmi(MotherNeonateAncRequest(memberId = getMemberId()))
        binding.tvDiagnosisLbl.text = getString(R.string.diagnosis)
        binding.tvHeightLbl.text = getString(R.string.height_hint)

    }

    companion object {
        const val TAG: String = "HivMedicalReviewDiagnosesFragment"
        fun newInstance(): HivMedicalReviewDiagnosesFragment {
            return HivMedicalReviewDiagnosesFragment()
        }

        fun newInstance(isHiv: Boolean = false): HivMedicalReviewDiagnosesFragment {
            val fragment = HivMedicalReviewDiagnosesFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(HIV, isHiv)
            }
            return fragment
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.tvAddWeight.id -> showAddWeightDialog()
            binding.tvDiagnosisConfirm.id -> showDiagnosisDialog()
            binding.tvAddHeight.id -> showAddHeightDialog()
            binding.tvBmiHistory.id -> showBmiDialog()
        }
    }

    private fun showDiagnosisDialog() {
        if (connectivityManager.isNetworkAvailable()) {
            val dialog = TbConfirmDiagnosisAndSiteOfDiseaseDialog().apply {
                arguments = Bundle().apply {
                    putBoolean(HIV, true)
                }
                this.listener = this@HivMedicalReviewDiagnosesFragment
            }
            dialog.show(childFragmentManager, TbConfirmDiagnosisAndSiteOfDiseaseDialog.TAG)
        } else {
            (activity as BaseActivity?)?.showErrorDialogue(
                getString(R.string.error),
                getString(R.string.no_internet_error),
                isNegativeButtonNeed = false
            ) {}
        }
    }


    override fun onDialogDismissedForTb(isPatientType: Boolean) {
        patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
            details.id?.let { id ->
                diagnosisViewModel.getDiagnosisDetails(
                    CreateUnderTwoMonthsResponse(
                        patientReference = id,
                        type = HIV_MEDICAL_REVIEW
                    )
                )
            }
        }
    }

    private fun showAddHeightDialog() {
        withNetworkAvailability(online = {
            showDialogIfNotPresent(AddHeightDialog.TAG) {
                AddHeightDialog.newInstance(
                    getPatientId(),
                    getMemberId(),
                    villageId = patientViewModel.getVillageId(),
                    householdId = patientViewModel.getPatientHouseholdId()
                ).apply {
                    listener = this@HivMedicalReviewDiagnosesFragment
                }
            }
        })
    }


    fun attachObserver() {
        val isHiv = isHiv()
        val progressBar = if (isHiv) binding.pbWeightPageProgress else binding.pbWeightPageProgress
        val addWeightText = if (isHiv) binding.tvWeight else binding.tvAddWeight
        val weightContainer = if (isHiv) binding.clWeight else binding.clWeight
        val retryButton =
            if (isHiv) binding.retryButtonWeight else binding.retryButtonWeight
        val weightTextView = if (isHiv) binding.tvWeight else binding.tvWeight

        viewModel.getWeight.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> handleLoading(progressBar, addWeightText, weightContainer)

                ResourceState.SUCCESS -> {
                    handleSuccess(progressBar, retryButton, weightContainer, addWeightText)
                    resourceState.data?.let {
                        weightTextView.text =
                            MotherNeonateUtil.convertWeight(it.weight, requireContext())
                        if(it.weight == null || it.weight == 0.0){
                            binding.tvAddWeight.text = getString(R.string.edit_weights)
                        }
                    }
                }

                ResourceState.ERROR -> handleError(
                    progressBar,
                    addWeightText,
                    weightContainer,
                    retryButton,
                    weightTextView
                )
            }
        }
        diagnosisViewModel.diagnosisDetailsList.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING ->{
                    binding.diagnosesPageProgress.visible()
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    binding.diagnosesPageProgress.gone()
                    hideProgress()
                    resource.data?.let { list ->
                        val diagnosisItems = list.map { it.diseaseCategory }.distinct()
                        if (isHiv()) {
                            list.forEach { it.type = HIV_MEDICAL_REVIEW }
                            updateDiagnosisUI(
                                diagnosisItems = diagnosisItems,
                                condition = list.any {
                                    it.type.equals(
                                        HIV_MEDICAL_REVIEW,
                                        ignoreCase = true
                                    ) || it.type.isNullOrBlank()
                                },
                                textView = binding.tvDiagnosis,
                                buttonView = binding.tvDiagnosisConfirm,
                                metaList = diagnosisViewModel.diagnosisMetaList
                            )
                        } else {
                            binding.diagnosesPageProgress.visible()
                            updateDiagnosisUI(
                                diagnosisItems = diagnosisItems,
                                condition = list.isNotEmpty(),
                                textView = binding.tvDiagnosis,
                                buttonView = binding.tvDiagnosisConfirm,
                                metaList = diagnosisViewModel.diagnosisMetaList
                            )
                        }
                    }
                }

                ResourceState.ERROR -> hideProgress()
            }
        }

        viewModel.getHeight.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    handleLoading(
                        binding.tbHeightPageProgress,
                        binding.tvAddHeight,
                        binding.clHeight
                    )
                }

                ResourceState.SUCCESS -> {
                    handleSuccess(
                        binding.tbHeightPageProgress,
                        binding.retryButtonTbHeightConfirm,
                        binding.clHeight,
                        binding.tvAddHeight
                    )
                    resourceState.data?.let { data ->
                        binding.tvHeightLbl.text = getString(R.string.height_hint)
                        binding.tvHeight.text =
                            MotherNeonateUtil.convertHeight(
                                data.height,
                                requireContext()
                            ).takeIf { it.isNotEmpty() } ?: "--"

                        if (!binding.tvHeight.text.toString().equals("--",true)){
                            binding.tvAddHeight.text = getString(R.string.edit_height)
                        }
                    }

                }

                ResourceState.ERROR -> {
                    handleError(
                        binding.tbHeightPageProgress,
                        binding.tvAddHeight,
                        binding.clHeight,
                        binding.retryButtonTbHeightConfirm,
                        binding.tvHeight
                    )
                }
            }
        }

        viewModel.getBmi.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    handleLoading(
                        binding.tbBMIPageProgress,
                        binding.tvBmiHistory,
                        binding.clBmi
                    )
                }

                ResourceState.SUCCESS -> {
                    handleSuccess(
                        binding.tbBMIPageProgress,
                        binding.retryButtonTbBMI,
                        binding.clBmi,
                        binding.tvBmiHistory
                    )
                    resourceState.data?.let {
                        binding.tvBmiLbl.text = getString(R.string.bmi)
                        binding.tvBmi.text =
                            MotherNeonateUtil.convertBmi(
                                it.bmi,
                                requireContext()
                            )
                    }
                }

                ResourceState.ERROR -> {
                    handleError(
                        binding.tbBMIPageProgress,
                        binding.tvBmiHistory,
                        binding.clBmi,
                        binding.retryButtonTbBMI,
                        binding.tvBmi
                    )
                }
            }
        }
    }


    private fun updateDiagnosisUI(
        diagnosisItems: List<String>,
        condition: Boolean,
        textView: TextView,
        buttonView: TextView,
        metaList: MutableLiveData<Resource<List<DiseaseCategoryItems>>>,
        isSite: Boolean = false
    ) {
        textView.text = if (condition) {
            metaList.value?.data?.let { diagnosisList ->
                val matchingNames = diagnosisList
                    .filter { item ->
                        diagnosisItems.any {
                            it.equals(
                                item.name,
                                ignoreCase = true
                            )
                        }
                    }
                    .map { it.name }.distinct()

                if (matchingNames.isNotEmpty()) {
                    convertListToString(ArrayList(matchingNames))
                } else {
                    getString(R.string.seperator_hyphen)
                }
            } ?: getString(R.string.seperator_hyphen)
        } else {
            getString(R.string.hyphen_symbol)
        }

    }

    private fun getMemberId(): String {
        return arguments?.getString(DefinedParams.MemberID, "") ?: ""
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

    private fun showAddWeightDialog() {
        withNetworkAvailability(online = {
            showAddBpOrWeightDialog(isBp = false)
        })
    }

    private fun showAddBpOrWeightDialog(isBp: Boolean) {
        val dialog = AddWeightDialog.newInstance(
            getPatientId(),
            villageId = patientViewModel.getVillageId(),
            householdId = patientViewModel.getPatientHouseholdId(),
            memberId = patientViewModel.getPatientMemberId()
        ).apply {
            listener = this@HivMedicalReviewDiagnosesFragment
        }

        showDialogIfNotPresent(AddWeightDialog.TAG) {
            dialog
        }
    }

    override fun onDialogDismissed(isBp: Boolean, isHeight: Boolean) {
        if (connectivityManager.isNetworkAvailable()) {
            if (isHeight) {
                viewModel.fetchHeight(MotherNeonateAncRequest(memberId = getMemberId()))
            } else viewModel.fetchWeight(MotherNeonateAncRequest(memberId = getMemberId()))

            if (isHiv()) {
                viewModel.fetchBmi(MotherNeonateAncRequest(memberId = getMemberId()))
            }

            val dialog =
                childFragmentManager.findFragmentByTag(AddWeightDialog.TAG) as? AddWeightDialog
                    ?: return
            dialog.listener = null
            dialog.dismiss()
        }
    }

    private fun getPatientId(): String {
        return arguments?.getString(DefinedParams.PatientId, "") ?: ""
    }

    private fun showBmiDialog() {
        if (connectivityManager.isNetworkAvailable()) {
            showDialogIfNotPresent(BMIListDialog.TAG) {
                BMIListDialog.newInstance(getMemberId())
            }
        } else {
            (activity as BaseActivity?)?.showErrorDialogue(
                getString(R.string.error),
                getString(R.string.no_internet_error),
                isNegativeButtonNeed = false
            ) {

            }
        }
    }

    private fun isHiv(): Boolean {
        return arguments?.getBoolean(MedicalReviewTypeEnums.HIV.name, false) == true
    }

}