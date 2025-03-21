package com.medtroniclabs.spice.ui.mypatients.fragment

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
import com.google.android.material.card.MaterialCardView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.changePatientStatus
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.convertListToString
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.Lactating
import com.medtroniclabs.spice.common.DefinedParams.Postpartum
import com.medtroniclabs.spice.common.DefinedParams.Pregnant
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.databinding.FragmentMedicalReviewPatientDiagnosisBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.DiagnosisDialogFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.DialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.calculateBp
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.viewmodel.DiagnosisViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.AddBpDialog
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.AddWeightDialog
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.AddHeightDialog
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.PatientStatusDialog
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.TbConfirmDiagnosisAndSiteOfDiseaseDialog
import com.medtroniclabs.spice.ui.mypatients.viewmodel.MotherNeonateBpWeightViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.medicalreview.viewmodel.PatientStatusViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PregnancyDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MedicalReviewPatientDiagnosisFragment : BaseFragment(), View.OnClickListener,
    DialogDismissListener {

    private lateinit var binding: FragmentMedicalReviewPatientDiagnosisBinding
    private val viewModel: MotherNeonateBpWeightViewModel by activityViewModels()
    private val statusViewModel: PatientStatusViewModel by activityViewModels()
    private val diagnosisViewModel: DiagnosisViewModel by activityViewModels()
    private val patientViewModel: PatientDetailViewModel by activityViewModels()
    private val pregnancyDetailsViewModel: PregnancyDetailsViewModel by activityViewModels()

    companion object {

        const val TAG: String = "MedicalReviewPatientDiagnosisFragment"
        fun newInstance(): MedicalReviewPatientDiagnosisFragment {
            return MedicalReviewPatientDiagnosisFragment()
        }

        fun newInstance(isAnc: Boolean,isPnc:Boolean=false, patientId: String?,memberID: String?, id: String?,isTB:Boolean = false): MedicalReviewPatientDiagnosisFragment {
            val fragment = MedicalReviewPatientDiagnosisFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(DefinedParams.PregnancyANC, isAnc)
                putBoolean(DefinedParams.PregnancyPNC,isPnc)
                putBoolean(DefinedParams.TB,isTB)
                putString(DefinedParams.PatientId, patientId)
                putString(DefinedParams.MemberID, memberID)
                putString(DefinedParams.ID, id)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMedicalReviewPatientDiagnosisBinding.inflate(inflater, container, false)
        arguments?.let {
            diagnosisViewModel.diagnosisType = getDiagnosisType()
            statusViewModel.patientId = it.getString(DefinedParams.ID)
        }
        return binding.root
    }

    private fun getDiagnosisType(): String {
        return arguments?.let {
            if (it.getBoolean(DefinedParams.PregnancyANC)) {
                MedicalReviewTypeEnums.ANC_REVIEW.name
            } else if (it.getBoolean(DefinedParams.PregnancyPNC)) {
                MedicalReviewTypeEnums.PNC_MOTHER_REVIEW.name
            } else if (isTb()) {
                MedicalReviewTypeEnums.TB.name
            } else {
                it.getString(MedicalReviewTypeEnums.DiagnosisType.name)
            }
        } ?: ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        attachObserver()
        handleFlow()
        initializeListeners()
        attachListeners()
        if (patientViewModel.getAncVisit() == 1 && diagnosisViewModel.diagnosisType!=MedicalReviewTypeEnums.PNC_MOTHER_REVIEW.name ) {
            binding.tvWeightValue.text = MotherNeonateUtil.convertWeight(
                pregnancyDetailsViewModel.pregnancyDetailsModel.weight,
                requireContext()
            )
            binding.tvBpValue.text =
                calculateBp(
                    pregnancyDetailsViewModel.pregnancyDetailsModel.systolic,
                    pregnancyDetailsViewModel.pregnancyDetailsModel.diastolic,
                    requireContext()
                )
            binding.tvAddWeight.invisible()
            binding.tvAddBp.invisible()
        }
    }

    private fun attachListeners() {
        val isTb = isTb()
        val progressBar = if (isTb) binding.tbWeightPageProgress else binding.pageProgress
        val addWeightText = if (isTb) binding.tvTbAddWeight else binding.tvAddWeight
        val weightContainer = if (isTb) binding.clTbWeight else binding.clWeight
        val retryButton =
            if (isTb) binding.retryButtonTbWeight else binding.retryButtonWeight
        val weightTextView = if (isTb) binding.tvTbWeight else binding.tvWeightValue

        viewModel.getWeight.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> handleLoading(progressBar, addWeightText, weightContainer)

                ResourceState.SUCCESS -> {
                    handleSuccess(progressBar, retryButton, weightContainer, addWeightText)
                    resourceState.data?.let {
                        weightTextView.text =
                            MotherNeonateUtil.convertWeight(it.weight, requireContext())
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
            val isTb = isTb()
            val progressBar = if (isTb) binding.tbBpPageProgress else binding.pageProgressBp
            val addBPText = if (isTb) binding.tvTbAddBp else binding.tvAddBp
            val bpContainer = if (isTb) binding.clTbBp else binding.clBp
            val retryButton =
                if (isTb) binding.retryButtonTbBp else binding.retryButtonBp
            val bpTextView = if (isTb) binding.tvTbBp else binding.tvBpValue

            when (resourceState.state) {
                ResourceState.LOADING -> {
                    handleLoading(progressBar, addBPText, bpContainer)
                }

                ResourceState.SUCCESS -> {
                    handleSuccess(
                        progressBar,
                        retryButton,
                        bpContainer,
                        addBPText
                    )
                    resourceState.data?.let {
                        bpTextView.text =
                            calculateBp(it.systolic, it.diastolic, requireContext())
                    }
                }

                ResourceState.ERROR -> {
                    handleError(
                        progressBar,
                        addBPText,
                        bpContainer,
                        retryButton,
                        bpTextView
                    )
                }
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
                    resourceState.data?.let {
                        binding.tvHeightLbl.text = getString(R.string.height_hint)
                        binding.tvHeight.text =
                            MotherNeonateUtil.convertWeight(
                                it.height,
                                requireContext()
                            )
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
    }

    private fun handleLoading(
        pageProgress: ProgressBar,
        textView: AppCompatTextView,
        clView: ConstraintLayout
    ) {
//        pageProgress.visible()
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

    private fun isTb():Boolean {
       return arguments?.getBoolean(DefinedParams.TB, false) ?: false
    }

    private fun handleFlow() {
        with(binding) {
            val isAnc = arguments?.getBoolean(DefinedParams.PregnancyANC, false)
            val isPnc = arguments?.getBoolean(DefinedParams.PregnancyPNC, false)
            if (isAnc == false && isPnc == false) {
                cardAddWeight.gone()
                cardBloodPressure.gone()
            }else {
               ancPncFlow(cardAddWeight,cardBloodPressure)
            }
            tvAddWeight.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
            tvAddBp.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
            retryButtonBp.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
            retryButtonWeight.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
            cardDiagnosis.visible()
            cardPatientStatus.visible()
            val isTb = isTb()
            cardSiteDisease.setVisible(isTb && patientViewModel.getTbMedicalReviewStatus())
            cardPatientType.setVisible(isTb && patientViewModel.getTbMedicalReviewStatus())
            if (isTb) {
                binding.tvHeightLbl.text = getString(R.string.height_hint)
                tvTbAddBp.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
                tvTbAddWeight.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
                tvAddSiteDisease.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
                commonLl.setVisible(patientViewModel.getTbMedicalReviewStatus())
                viewModel.fetchWeight(MotherNeonateAncRequest(memberId = getMemberId()))
                viewModel.fetchBloodPressure(MotherNeonateAncRequest(memberId = getMemberId()))
                viewModel.fetchHeight(MotherNeonateAncRequest(memberId = getMemberId()))
                retryButtonTbBp.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
                retryButtonTbWeight.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
                retryButtonTbHeightConfirm.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
                tvAddHeight.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
                tvAddPatientStatus.visible()
                tvAddPatientStatus.safeClickListener(this@MedicalReviewPatientDiagnosisFragment)
            }
            tbLl.setVisible(isTb)
        }
    }

    private fun ancPncFlow(cardAddWeight: MaterialCardView, cardBloodPressure: MaterialCardView) {
        cardAddWeight.visible()
        cardBloodPressure.visible()
        if (connectivityManager.isNetworkAvailable()) {
            if (patientViewModel.getAncVisit() > 1 || getDiagnosisType() == MedicalReviewTypeEnums.PNC_MOTHER_REVIEW.name) {
                viewModel.fetchWeight(MotherNeonateAncRequest(memberId = getMemberId()))
                viewModel.fetchBloodPressure(MotherNeonateAncRequest(memberId = getMemberId()))
            }
        }
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.tvAddWeight.id, binding.tvTbAddWeight.id -> showAddWeightDialog()
            binding.tvAddBp.id, binding.tvTbAddBp.id -> showAddBpDialog()
            binding.tvDiagnosisConfirm.id -> showDiagnosisDialog()
            binding.tvAddSiteDisease.id -> showDiagnosisDialog()
            binding.retryButtonBp.id, binding.retryButtonTbBp.id -> retryFetchingData(true)
            binding.retryButtonWeight.id, binding.retryButtonTbWeight.id -> retryFetchingData(false)
            binding.tvAddHeight.id -> showAddHeightDialog()
            binding.tvAddPatientStatus.id -> showPatientStatusDialog()
            binding.retryButtonTbHeightConfirm.id -> retryFetchingDataForHeight()
        }
    }

    private fun retryFetchingDataForHeight() {
        if (connectivityManager.isNetworkAvailable()) {
            viewModel.fetchHeight(MotherNeonateAncRequest(memberId = getMemberId()))
        }
    }

    private fun showPatientStatusDialog() {
        PatientStatusDialog.newInstance().apply {
            listener = this@MedicalReviewPatientDiagnosisFragment
        }.show(childFragmentManager, AddHeightDialog.TAG)
    }

    private fun showAddWeightDialog() {
        showAddBpOrWeightDialog(isBp = false)
    }

    private fun showAddBpDialog() {
        showAddBpOrWeightDialog(isBp = true)
    }

    private fun showAddHeightDialog() {
        AddHeightDialog.newInstance(getPatientId(),getMemberId()).apply {
            listener = this@MedicalReviewPatientDiagnosisFragment
        }.show(childFragmentManager, AddHeightDialog.TAG)
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
        if (connectivityManager.isNetworkAvailable()){
            if (isTb()) {
                TbConfirmDiagnosisAndSiteOfDiseaseDialog().show(childFragmentManager, TbConfirmDiagnosisAndSiteOfDiseaseDialog.TAG)
            } else {
                DiagnosisDialogFragment().show(childFragmentManager, DiagnosisDialogFragment.TAG)
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
                            //  binding.tvPatientStatusValue.text = getPatientStatus(patientStatus.status)
                            binding.tvPatientStatusValue.text = patientStatus.status
                        }
                        patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
                            details.id?.let { id ->
                                diagnosisViewModel.getDiagnosisDetails(
                                    CreateUnderTwoMonthsResponse(
                                        patientReference = id,
                                        type = diagnosisViewModel.diagnosisType
                                    )
                                )
                            }
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
                        val diagnosisItems = list.map { it.diseaseCategory }.distinct()
                        if (isTb()) {
                            updateDiagnosisUI(
                                diagnosisItems,
                                list.any { !it.siteOfDisease },
                                binding.tvDiagnosis,
                                binding.tvDiagnosisConfirm,
                                diagnosisViewModel.diagnosisMetaList
                            )
                            updateDiagnosisUI(
                                diagnosisItems,
                                list.any { it.siteOfDisease },
                                binding.tvSiteDisease,
                                binding.tvAddSiteDisease,
                                diagnosisViewModel.siteOfDiseaseMetaList,
                                true
                            )
                        } else {
                            updateDiagnosisUI(
                                diagnosisItems,
                                list.isNotEmpty(),
                                binding.tvDiagnosis,
                                binding.tvDiagnosisConfirm,
                                diagnosisViewModel.diagnosisMetaList
                            )
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
                convertListToString(ArrayList(diagnosisList.filter { it.value in diagnosisItems }
                    .map { it.name }))
            } ?: getString(R.string.seperator_hyphen)
        } else {
            getString(R.string.hyphen_symbol)
        }

        if (!isSite) {
            buttonView.text = if (condition) getString(R.string.edit_diagnoses) else getString(R.string.add_diagnosis)
        } else {
            buttonView.text = if (condition) getString(R.string.edit_disease) else getString(R.string.add_disease)
        }

    }
    private fun getPatientStatus(status: String): String {
        val formattedString = cleanString(status.lowercase())
        return if (diagnosisViewModel.diagnosisType == MedicalReviewTypeEnums.ANC_REVIEW.name) {
            if (formattedString.isEmpty()) {
                Pregnant
            } else if (formattedString.contains(Pregnant, ignoreCase = true)) {
                requireContext().changePatientStatus(formattedString)
            } else {
                "${requireContext().changePatientStatus(formattedString)}, $Pregnant"
            }
        } else {
            requireContext().changePatientStatus(status)
        }
    }


    private fun cleanString(input: String): String {
        val toRemove = listOf(Postpartum.lowercase(), Lactating.lowercase())
        if (toRemove.any { input.contains(it) }) {
            var cleanedString = input
            for (str in toRemove) {
                cleanedString = cleanedString.replace(str, "")
            }
            cleanedString = cleanedString.replace(",", "")
            return cleanedString.trim().capitalizeFirstChar()
        } else {
            return input.capitalizeFirstChar()
        }
    }

    private fun initializeViews() {
        diagnosisViewModel.getDiagnosisMetaList(diagnosisViewModel.diagnosisType)
        if (isTb() && patientViewModel.getTbMedicalReviewStatus()){
            diagnosisViewModel.getSiteOfDiseaseMetaList(MotherNeonateUtil.TB_SITE_OF_DISEASE)
        }
        statusViewModel.patientId?.let {
            binding.tvPatientStatusValue.text = requireContext().getString(R.string.hyphen_symbol)
            if (it.isNotEmpty()){
                diagnosisViewModel.getDiagnosisDetails(
                    CreateUnderTwoMonthsResponse(
                        patientReference = it,
                        type = diagnosisViewModel.diagnosisType
                    )
                )
            }
        }
    }

    private fun getMemberId(): String {
        return arguments?.getString(DefinedParams.MemberID, "") ?: ""
    }

    override fun onDialogDismissed(isBp: Boolean, isHeight: Boolean) {
        if (connectivityManager.isNetworkAvailable()) {
            if (isBp) {
                viewModel.fetchBloodPressure(MotherNeonateAncRequest(memberId = getMemberId()))
            } else {
                viewModel.fetchWeight(MotherNeonateAncRequest(memberId = getMemberId()))
            }
            if (isHeight) {
                viewModel.fetchHeight(MotherNeonateAncRequest(memberId = getMemberId()))
            }
        }
        val dialog =
            childFragmentManager.findFragmentByTag(if (isBp) AddBpDialog.TAG else AddWeightDialog.TAG) as? AddBpDialog
                ?: return
        dialog.listener = null
        dialog.dismiss()
    }

}