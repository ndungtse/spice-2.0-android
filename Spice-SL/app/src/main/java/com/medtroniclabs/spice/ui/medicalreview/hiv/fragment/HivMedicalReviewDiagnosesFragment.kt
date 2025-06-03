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
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.databinding.FragmentHivMedicalReviewDiagnosesBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.HivVitalsRequest
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.DiagnosisDialogFragment
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.viewmodel.DiagnosisViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.WhoClinicalStageViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.DialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.DialogDismissListenerForTb
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.AddWeightDialog
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.AddBMIDialog
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.AddHeightDialog
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.BMIListDialog
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.EMTCCT_VISIT_STATUS
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.viewmodel.MotherNeonateBpWeightViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel

class HivMedicalReviewDiagnosesFragment : BaseFragment(), View.OnClickListener,
    DialogDismissListenerForTb, DialogDismissListener {

    private lateinit var binding: FragmentHivMedicalReviewDiagnosesBinding
    private val patientViewModel: PatientDetailViewModel by activityViewModels()
    private val diagnosisViewModel: DiagnosisViewModel by activityViewModels()
    private val viewModel: MotherNeonateBpWeightViewModel by activityViewModels()
    private val hivViewModel: HivViewModel by activityViewModels()
    private val whoStageViewModel: WhoClinicalStageViewModel by activityViewModels()


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
        hivViewModel.isEMTCTMR = arguments?.getBoolean(DefinedParams.EMTCTMR, false) == true
        diagnosisViewModel.diagnosisType = MedicalReviewTypeEnums.HIV_REVIEW.name
        diagnosisViewModel.getDiagnosisMetaList(diagnosisViewModel.diagnosisType)
        patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
            details.id?.let { id ->
                diagnosisViewModel.getDiagnosisDetails(
                    CreateUnderTwoMonthsResponse(
                        patientReference = id,
                        type = MedicalReviewTypeEnums.HIV_REVIEW.name
                    )
                )
            }
        }

        binding.tvDiagnosisConfirm.safeClickListener(this)
        binding.tvBmiHistory.safeClickListener(this@HivMedicalReviewDiagnosesFragment)
        binding.tvEmtctVisitStatusUpdate.safeClickListener(this@HivMedicalReviewDiagnosesFragment)
        binding.tvAddHeight.safeClickListener(this@HivMedicalReviewDiagnosesFragment)
        binding.retryButtonWeight.safeClickListener(this@HivMedicalReviewDiagnosesFragment)
        binding.tvAddWeight.safeClickListener(this@HivMedicalReviewDiagnosesFragment)
        binding.tvWho.safeClickListener(this)
        binding.tvCd4.safeClickListener(this)
        viewModel.fetchWeight(MotherNeonateAncRequest(memberId = getMemberId()))
        viewModel.fetchHeight(MotherNeonateAncRequest(memberId = getMemberId()))
        viewModel.fetchBmi(MotherNeonateAncRequest(memberId = getMemberId()))
        binding.tvDiagnosisLbl.text = getString(R.string.diagnosis)
        binding.tvHeightLbl.text = getString(R.string.height_hint)
        patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
            details.id?.let { id ->
                diagnosisViewModel.getDiagnosisDetails(
                    CreateUnderTwoMonthsResponse(
                        patientReference = id,
                        type = MedicalReviewTypeEnums.HIV_REVIEW.name
                    )
                )
            }
        }
        if (hivViewModel.isEMTCTMR) {
            with(binding) {
                cardWhoClinicalStage.visible()
                cardCd4.visible()
                cardEmtct.visible()
                cardHeight.gone()
                cardBMI.gone()
            }

            patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
                details.id?.let { id ->
                    diagnosisViewModel.getHivVitalsDetails(
                        patientReference = id,
                        memberId = getMemberId()
                    )
                }
            }
        } else {
            with(binding) {
                cardWhoClinicalStage.gone()
                cardCd4.gone()
                cardEmtct.gone()
                cardHeight.visible()
                cardBMI.visible()
            }
        }

    }

    companion object {
        const val TAG: String = "HivMedicalReviewDiagnosesFragment"
        fun newInstance(): HivMedicalReviewDiagnosesFragment {
            return HivMedicalReviewDiagnosesFragment()
        }

        fun newInstance(isHiv: Boolean = false,isEmtctMR:Boolean = false): HivMedicalReviewDiagnosesFragment {
            val fragment = HivMedicalReviewDiagnosesFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(HIV, isHiv)
                putBoolean(DefinedParams.EMTCTMR,isEmtctMR)
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
            binding.tvEmtctVisitStatusUpdate.id -> showEmtctVisitStatusDialog()
            binding.tvWho.id -> showWhoStage()
            binding.tvCd4.id -> showCD4Dialog(true, false)
        }
    }
    private fun showCD4Dialog(isCd4: Boolean, isCd4Percentage: Boolean) {
        withNetworkAvailability(online = {
            showDialogIfNotPresent(CD4DialogFragment.TAG) {
                CD4DialogFragment.newInstance(
                    isCD4 = isCd4,
                    isCD4Percentage = isCd4Percentage
                )
            }
        })
    }

    private fun showWhoStage() {
        withNetworkAvailability(online = {
            showDialogIfNotPresent(WhoClinicalStageFragment.TAG) {
                WhoClinicalStageFragment.newInstance().apply {
                    listener = this@HivMedicalReviewDiagnosesFragment
                }
            }
        })
    }

    private fun showDiagnosisDialog() {
        if (connectivityManager.isNetworkAvailable()) {
            DiagnosisDialogFragment().show(childFragmentManager, DiagnosisDialogFragment.TAG)
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
                        type = MedicalReviewTypeEnums.HIV_REVIEW.name
                    )
                )
            }
        }
    }

    private fun showAddHeightDialog() {
        withNetworkAvailability(online = {
            showDialogIfNotPresent(AddBMIDialog.TAG) {
                AddBMIDialog.newInstance(
                    patientId = patientViewModel.getPatientId(),
                    villageId = patientViewModel.getVillageId(),
                    householdId = patientViewModel.getPatientHouseholdId(),
                    memberId = getMemberId(),
                ).apply {
                    listener = this@HivMedicalReviewDiagnosesFragment
                }
            }
        })
    }


    fun attachObserver() {
        val progressBar = binding.pbWeightPageProgress
        val addWeightText = binding.tvAddWeight
        val weightContainer = binding.clWeight
        val retryButton = binding.retryButtonWeight
        val weightTextView = binding.tvWeight

        /*whoStageViewModel.getWhoStageLiveData.observe(viewLifecycleOwner){
            diagnosisViewModel.hivVitalsDetailLiveData.value?.data?.let {list ->
                binding.tvWhoValue.text = getClinicalStageText(list.whoClinicalStage)
            }
        }*/

        viewModel.getWeight.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> handleLoading(progressBar, addWeightText, weightContainer)

                ResourceState.SUCCESS -> {
                    handleSuccess(progressBar, retryButton, weightContainer, addWeightText)
                    resourceState.data?.let {
                        weightTextView.text =
                            MotherNeonateUtil.convertWeight(it.weight, requireContext())
                        if (it.weight == null || it.weight == 0.0) {
                            binding.tvAddWeight.text = getString(R.string.add_weight)
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
                ResourceState.LOADING -> {
                    binding.diagnosesPageProgress.visible()
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    binding.diagnosesPageProgress.gone()
                    hideProgress()
                    resource.data?.let { list ->
                        val diagnosisItems = list.map { it.diseaseCategory }.distinct()
                        if (isHiv()) {
                            list.forEach { it.type = MedicalReviewTypeEnums.HIV_REVIEW.name }
                            updateDiagnosisUI(
                                diagnosisItems = diagnosisItems,
                                condition = list.any {
                                    it.type.equals(
                                        MedicalReviewTypeEnums.HIV_REVIEW.name,
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

                        if (!binding.tvHeight.text.toString().equals("--", true)) {
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

        hivViewModel.hivVitalsLiveData.observe(viewLifecycleOwner) { response ->
            when (response.state) {
                ResourceState.ERROR -> {
                    handleError(
                        binding.emtctPageProgress,
                        binding.tvEmtctVisitStatusUpdate,
                        binding.clEmtct,
                        binding.retryButtonEmtct,
                        binding.tvEmtct
                    )
                }

                ResourceState.LOADING -> {
                    handleLoading(
                        binding.emtctPageProgress,
                        binding.tvEmtctVisitStatusUpdate,
                        binding.clEmtct,
                    )
                }

                ResourceState.SUCCESS -> {
                    handleSuccess(
                        binding.emtctPageProgress,
                        binding.retryButtonEmtct,
                        binding.clEmtct,
                        binding.tvEmtctVisitStatusUpdate
                    )
                    response.data?.let { data ->
                        binding.tvEmtct.text =
                            data.emtctVisitStatus.takeIf { !it.isNullOrEmpty() } ?: "-"
                        hivViewModel.emtctVisitStatus = data.emtctVisitStatus
                    }
                }
            }
        }
        diagnosisViewModel.hivVitalsDetailLiveData.observe(viewLifecycleOwner) { resources ->
            when (resources.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resources.data?.let { list ->
                        binding.tvCd4Value.text = list.cd4 ?: getString(R.string.seperator_hyphen)
                        binding.tvWhoValue.text = list.whoClinicalStage ?: getString(R.string.seperator_hyphen)
                            //getClinicalStageText(list.whoClinicalStage)
                        binding.tvWho.text = if (list.whoClinicalStage != null) {
                            getString(R.string.edit_who_clinical_stage)
                        } else {
                            getString(R.string.add_who_clinical_stage)
                        }
                        hivViewModel.cd4Value = list.cd4
                        hivViewModel.whovalue = getClinicalStageText(list.whoClinicalStage)
                            .takeUnless { it.equals(getString(R.string.hyphen_symbol), true) }
                        binding.tvEmtct.text = getEmtctVisitText(list.emtctVisitStatus)
                        hivViewModel.emtctVisitStatus = list.emtctVisitStatus
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }


        whoStageViewModel.whoStageCreateLiveData.observe(viewLifecycleOwner) { resourcesState ->
            when (resourcesState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
                        diagnosisViewModel.getHivVitalsDetails(
                            patientReference = details.id,
                            memberId = details.memberId
                        )
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
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
                                item.value,
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
        return patientViewModel.getPatientMemberId() ?: ""
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
            showAddBpOrWeightDialog()
        })
    }

    private fun showAddBpOrWeightDialog() {
        val dialog = AddBMIDialog.newInstance(
            patientId = patientViewModel.getPatientId(),
            villageId = patientViewModel.getVillageId(),
            householdId = patientViewModel.getPatientHouseholdId(),
            memberId = patientViewModel.getPatientMemberId()
        ).apply {
            listener = this@HivMedicalReviewDiagnosesFragment
        }

        showDialogIfNotPresent(AddBMIDialog.TAG) {
            dialog
        }
    }

    override fun onDialogDismissed(isBp: Boolean, isHeight: Boolean) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModel.fetchHeight(MotherNeonateAncRequest(memberId = getMemberId()))
            viewModel.fetchWeight(MotherNeonateAncRequest(memberId = getMemberId()))
            viewModel.fetchBmi(MotherNeonateAncRequest(memberId = getMemberId()))
            hivViewModel.getHivVitalsDetails(
                HivVitalsRequest(
                    patientReference = hivViewModel.id,
                    memberId = hivViewModel.memberId,
                    types = listOf(EMTCCT_VISIT_STATUS)
                )
            )

            val dialog =
                childFragmentManager.findFragmentByTag(AddBMIDialog.TAG) as? AddBMIDialog
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
            ) {}
        }
    }

    private fun showEmtctVisitStatusDialog() {
        if (connectivityManager.isNetworkAvailable()) {
            showDialogIfNotPresent(EmctVisitStatusDialogFragment.TAG) {
                EmctVisitStatusDialogFragment.newInstance().apply {
                    listener = this@HivMedicalReviewDiagnosesFragment
                }
            }
        } else {
            (activity as BaseActivity?)?.showErrorDialogue(
                getString(R.string.error),
                getString(R.string.no_internet_error),
                isNegativeButtonNeed = false
            ) {}
        }
    }

    private fun isHiv(): Boolean {
        return arguments?.getBoolean(MedicalReviewTypeEnums.HIV.name) == true
    }

    private fun getClinicalStageText(whoClinicalStage: String?): String {
        whoStageViewModel.getWhoStageLiveData.value?.let { whoStageList ->
            return if (whoClinicalStage.isNullOrEmpty()) {
                getString(R.string.seperator_hyphen)
            } else {
                whoStageList.find { it.value == whoClinicalStage }?.name?.capitalizeFirstChar()
                    ?: getString(R.string.seperator_hyphen)
            }
        } ?: run {
            return getString(R.string.seperator_hyphen)
        }
    }
    private fun getEmtctVisitText(whoClinicalStage: String?): String {
        hivViewModel.hivEmtctStatusLiveData.value?.let { whoStageList ->
            return if (whoClinicalStage.isNullOrEmpty()) {
                getString(R.string.seperator_hyphen)
            } else {
                whoStageList.find { it.value == whoClinicalStage }?.name?.capitalizeFirstChar()
                    ?: getString(R.string.seperator_hyphen)
            }
        } ?: run {
            return getString(R.string.seperator_hyphen)
        }
    }


}