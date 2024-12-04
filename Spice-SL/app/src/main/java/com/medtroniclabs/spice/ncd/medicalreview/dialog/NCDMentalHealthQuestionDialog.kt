package com.medtroniclabs.spice.ncd.medicalreview.dialog

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.loadAsGif
import com.medtroniclabs.spice.appextensions.resetImageView
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.calculateCAGEAIDSCore
import com.medtroniclabs.spice.common.CommonUtils.calculateSuicidalIdeation
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.FormAutofill
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentNCDMentalHealthQuestionDialogBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.data.NCDMentalHealthMedicalReviewDetails
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDFormViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMentalHealthViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.medtroniclabs.spice.ui.dialog.GeneralSuccessDialog

class NCDMentalHealthQuestionDialog : DialogFragment(), FormEventListener, View.OnClickListener {
    private lateinit var binding: FragmentNCDMentalHealthQuestionDialogBinding
    private lateinit var formGenerator: FormGenerator

    private val ncdFormViewModel: NCDFormViewModel by activityViewModels()
    private val assessmentViewModel: AssessmentViewModel by activityViewModels()
    private val viewModel: NCDMentalHealthViewModel by activityViewModels()

    private var isEditAssessment: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNCDMentalHealthQuestionDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeFormGenerator()
        attachObserver()
    }

    override fun onStart() {
        super.onStart()
        handleOrientation()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleOrientation()
    }

    private fun handleOrientation() {
        val isTablet = CommonUtils.checkIsTablet(requireContext())
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val width = when {
            isTablet && isLandscape -> 75
            else -> 100
        }
        val height = when {
            isTablet && isLandscape -> 95
            else -> 100
        }
        setDialogPercent(width, height)
    }

    private fun initializeFormGenerator() {
        isEditAssessment = requireArguments().getBoolean(NCDMRUtil.isEditAssessment)
        binding.btnCancel.safeClickListener(this@NCDMentalHealthQuestionDialog)
        binding.btnConfirm.safeClickListener(this@NCDMentalHealthQuestionDialog)
        binding.ivClose.safeClickListener(this@NCDMentalHealthQuestionDialog)
        binding.loadingProgress.safeClickListener(this@NCDMentalHealthQuestionDialog)
        formGenerator = FormGenerator(
            requireContext(),
            binding.llForm,
            listener = this,
            scrollView = binding.mentalHealthScrollView
        ) { map, id ->

        }
        if (screeningCards().contains(myFormType()))
            ncdFormViewModel.getNCDForm(DefinedParams.Screening)
        else
            ncdFormViewModel.getNCDForm(
                DefinedParams.Assessment,
                workFlow = NCDMRUtil.mentalHealth
            )
        val request = NCDMentalHealthMedicalReviewDetails(
            memberReference = requireArguments().getString(NCDMRUtil.MEMBER_REFERENCE) as String,
            type = myFormType()
        )
        if (isEditAssessment) {
            viewModel.ncdMentalHealthMedicalReviewDetails(request, isAssessment())
        }
        binding.tvMentalHealthLabel.text = getTitle(myFormType())
    }

    private fun myFormType(): String? {
        return requireArguments().getString(Screening.type)
    }

    private fun assessmentCards(): List<String> {
        return listOf(
            AssessmentDefinedParams.phq4,
            AssessmentDefinedParams.phq9,
            AssessmentDefinedParams.gad7
        )
    }

    private fun screeningCards(): List<String> {
        return listOf(AssessmentDefinedParams.suicidcalIdeation, AssessmentDefinedParams.cageAid)
    }

    private fun getTitle(type: String?): String {
        return type?.let { t ->
            when (t.lowercase()) {
                AssessmentDefinedParams.phq4 -> getString(R.string.phq4_assessment)
                AssessmentDefinedParams.phq9 -> getString(R.string.phq9_assessment)
                AssessmentDefinedParams.gad7 -> getString(R.string.gad7_assessment)
                AssessmentDefinedParams.suicidcalIdeation -> getString(R.string.gad7_assessment)
                AssessmentDefinedParams.cageAid -> getString(R.string.cage_aid)
                else -> getString(R.string.assessment)
            }
        } ?: run {
            getString(R.string.assessment)
        }
    }

    private fun attachObserver() {
        ncdFormViewModel.ncdFormResponse.observe(viewLifecycleOwner) { resources ->
            when (resources.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resources.data?.let { it ->
                        it.filter {
                            myFormType()?.let { type -> it.family?.contains(type) } == true
                        }.let { json ->
                            if (json.isNotEmpty())
                                formGenerator.populateViews(json)
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
        assessmentViewModel.mentalHealthQuestions.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { localMHResponse ->
                        localMHResponse.forEach {
                            formGenerator.loadMentalHealthQuestions(it.value)
                        }
                    }
                }
            }
        }
        viewModel.mentalHealthDetails.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { data ->
                        viewModel.questionarieId = data[NCDMRUtil.questionnaireId] as? String
                        viewModel.encounterId = data[DefinedParams.EncounterId] as? String
                        FormAutofill.start(formGenerator, data)
                    }
                }
            }
        }
        viewModel.assessmentMentalHealth.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    (activity as? BaseActivity?)?.showErrorDialogue(
                        title = getString(R.string.error),
                        message = resource.message
                            ?: getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok)
                    ) {}
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let {
                        val fragment = childFragmentManager.findFragmentByTag(GeneralSuccessDialog.TAG)
                        if (fragment == null) {
                            GeneralSuccessDialog.newInstance(
                                title = getString(R.string.tab_medical_review),
                                message = it,
                                okayButton = getString(R.string.done)
                            ) {
                                dismiss()
                            }.show(childFragmentManager, GeneralSuccessDialog.TAG)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "NCDMentalHealthQuestionDialog"
        fun newInstance(
            type: String,
            patientId: String?,
            patientFHIRId: String?,
            isEditAssessment: Boolean
        ): NCDMentalHealthQuestionDialog {
            val fragment = NCDMentalHealthQuestionDialog()
            val args = Bundle()
            args.putString(Screening.type, type)
            args.putString(NCDMRUtil.PATIENT_REFERENCE, patientId)
            args.putString(NCDMRUtil.MEMBER_REFERENCE, patientFHIRId)
            args.putBoolean(NCDMRUtil.isEditAssessment, isEditAssessment)
            fragment.arguments = args
            return fragment
        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        if (localDataCache is String) {
            when (localDataCache) {
                Screening.PHQ4, AssessmentDefinedParams.PHQ9, AssessmentDefinedParams.GAD7 -> {
                    assessmentViewModel.fetchMentalHealthQuestions(localDataCache)
                }

                AssessmentDefinedParams.Fetch_MH_Questions -> {
                    formGenerator.fetchMHQuestions(
                        id,
                        assessmentViewModel.mentalHealthQuestions.value?.data?.get(id)
                    )
                }
            }
        }
    }

    override fun onPopulate(targetId: String) {

    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        serverViewModel: FormLayout,
        resultMap: Any?
    ) {

    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?
    ) {

    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {

    }

    override fun onRenderingComplete() {

    }

    override fun onUpdateInstruction(id: String, selectedId: Any?) {

    }

    override fun onInformationHandling(
        id: String,
        noOfDays: Int,
        enteredDays: Int?,
        resultMap: HashMap<String, Any>?
    ) {

    }

    override fun onAgeCheckForPregnancy() {

    }

    override fun handleMandatoryCondition(serverData: FormLayout?) {

    }

    override fun onAgeUpdateListener(
        age: String?,
        serverData: List<FormLayout?>?,
        resultHashMap: HashMap<String, Any>
    ) {

    }

    fun showLoading() {
        binding.apply {
            btnConfirm.invisible()
            btnCancel.invisible()
            loadingProgress.visible()
            loaderImage.apply {
                loadAsGif(R.drawable.loader_spice)
            }
        }
    }

    fun hideLoading() {
        binding.apply {
            btnConfirm.visible()
            btnCancel.visible()
            loadingProgress.gone()
            loaderImage.apply {
                resetImageView()
            }
        }
    }

    private fun processValuesAndProceed(
        resultHashMap: HashMap<String, Any>,
        serverData: List<FormLayout?>?
    ) {
        val map = HashMap<String, Any>()
        map.putAll(resultHashMap)

        //Patient ID
        getPatientReference()?.let { patientId ->
            map[Screening.Patient_Id] = patientId
        }

        //Member or FHIR ID
        getMemberReference()?.let { memberId ->
            map[AssessmentDefinedParams.memberReference] = memberId
        }

        //Encounter object [Encounter ID and Provance]
        val provenance = HashMap<String, Any>()
        provenance[DefinedParams.Provenance] = ProvanceDto()
        viewModel.encounterId?.let {
            provenance[DefinedParams.id] = it
        }
        map[AssessmentDefinedParams.encounter] = provenance

        when (myFormType()) {
            AssessmentDefinedParams.phq4 -> CommonUtils.calculatePHQScore(map)
            AssessmentDefinedParams.phq9 -> CommonUtils.calculatePHQScore(
                map,
                type = AssessmentDefinedParams.PHQ9
            )

            AssessmentDefinedParams.gad7 -> CommonUtils.calculatePHQScore(
                map,
                type = AssessmentDefinedParams.GAD7
            )

            AssessmentDefinedParams.cageAid -> calculateCAGEAIDSCore(map, serverData)
            AssessmentDefinedParams.suicidcalIdeation -> calculateSuicidalIdeation(map)
        }

        try {
            var result = serverData?.let {
                FormResultComposer().groupValues(
                    context = requireContext(),
                    serverData = it,
                    map
                )
            }
            if (result != null) {
                CommonUtils.assessmentPHQ4(result)
                result = Pair(StringConverter.convertGivenMapToString(result.second), result.second)
                result.first?.let {
                    val reqMap = StringConverter.convertStringToMap(it)
                    val request = StringConverter.getJsonObject(
                        Gson().toJson(reqMap)
                    )
                    viewModel.ncdMentalHealthMedicalReviewCreate(request, isAssessment())
                }
            }
        } catch (_: Exception) {
            //Exception - Catch block
        }
    }

    private fun getPatientReference(): String? {
        return requireArguments().getString(NCDMRUtil.PATIENT_REFERENCE)
    }

    private fun getMemberReference(): String? {
        return requireArguments().getString(NCDMRUtil.MEMBER_REFERENCE)
    }

    private fun isAssessment(): Boolean {
        return assessmentCards().contains(myFormType())
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.ivClose.id, binding.btnCancel.id -> dismiss()
            binding.btnConfirm.id -> {
                val assessment = formGenerator.formSubmitAction(v)
                if (assessment) {
                    formGenerator.let {
                        processValuesAndProceed(
                            it.getResultMap(),
                            it.getServerData()
                        )
                    }
                }
            }
        }
    }
}