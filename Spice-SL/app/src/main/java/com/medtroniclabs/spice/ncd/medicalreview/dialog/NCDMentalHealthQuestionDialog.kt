package com.medtroniclabs.spice.ncd.medicalreview.dialog

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
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
import com.medtroniclabs.spice.ncd.data.NCDMentalHealthDetails
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDFormViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMentalHealthViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class NCDMentalHealthQuestionDialog : DialogFragment(), FormEventListener, View.OnClickListener {

    private lateinit var binding: FragmentNCDMentalHealthQuestionDialogBinding
    private lateinit var formGenerator: FormGenerator
    private val ncdFormViewModel: NCDFormViewModel by activityViewModels()
    private val assessmentViewModel: AssessmentViewModel by activityViewModels()
    private var assessmentJSON: List<FormLayout>? = null
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
        val percent = when {
            isTablet && isLandscape -> 70
            isTablet && !isLandscape -> 90
            else -> 100
        }
        setDialogPercent(percent)
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
        ncdFormViewModel.getNCDForm(
            DefinedParams.Assessment,
            workFlow = NCDMRUtil.mentalHealth
        )
        val request = NCDMentalHealthDetails(
            memberReference = requireArguments().getString(NCDMRUtil.MEMBER_REFERENCE) as String,
            type = AssessmentDefinedParams.PHQ9
        )
        if (isEditAssessment) {
            viewModel.ncdMentalHealthDetails(request)
        }
        binding.tvMentalHealthLabel.text = requireArguments().getString(Screening.type)
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
                        assessmentJSON = it.filter {
                            requireArguments().getString(Screening.type)
                                ?.let { type -> it.family?.contains(type) } == true
                        }
                        assessmentJSON?.let { json ->
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
    }

    companion object {
        const val TAG = "NCDMentalHealthQuestionDialog"
        fun newInstance(
            type: String,
            patientFHIRId: String?,
            isEditAssessment: Boolean
        ): NCDMentalHealthQuestionDialog {
            val fragment = NCDMentalHealthQuestionDialog()
            val args = Bundle()
            args.putString(Screening.type, type)
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

    private fun showLoading() {
        binding.loadingProgress.visible()
    }

    private fun hideLoading() {
        binding.loadingProgress.gone()
    }

    private fun processValuesAndProceed(
        resultHashMap: HashMap<String, Any>,
        serverData: List<FormLayout?>?
    ) {
        val map = HashMap<String, Any>()
        map.putAll(resultHashMap)
        map[AssessmentDefinedParams.memberReference] =
            requireArguments().getString(NCDMRUtil.MEMBER_REFERENCE) as String
        val provenance = HashMap<String, Any>()
        provenance[DefinedParams.Provenance] = ProvanceDto()
        viewModel.encounterId?.let {
            provenance[DefinedParams.id] = it
        }
        map[AssessmentDefinedParams.encounter] = provenance
        if (map.containsKey(Screening.PHQ4_Mental_Health))
            CommonUtils.calculatePHQScore(map)

        if (map.containsKey(AssessmentDefinedParams.PHQ9_Mental_Health))
            CommonUtils.calculatePHQScore(map, type = AssessmentDefinedParams.PHQ9)

        if (map.containsKey(AssessmentDefinedParams.GAD7_Mental_Health))
            CommonUtils.calculatePHQScore(map, type = AssessmentDefinedParams.GAD7)

        try {
            var result = serverData?.let {
                FormResultComposer().groupValues(
                    context = requireContext(),
                    serverData = it,
                    map
                )
            }
            result?.second
            if (result?.second?.containsKey(AssessmentDefinedParams.PHQ9.lowercase()) == true) {
                val gad7 =
                    result?.second?.get(AssessmentDefinedParams.PHQ9.lowercase()) as HashMap<String, Any>
                if (gad7.isEmpty()) {
                    result?.second?.apply {
                        remove(AssessmentDefinedParams.PHQ9.lowercase())
                    }
                } else {
                    viewModel.questionarieId?.let {
                        gad7[NCDMRUtil.questionnaireId] = it
                    }
                    val score = gad7[AssessmentDefinedParams.PHQ9_Score] as Int
                    gad7[Screening.PHQ4_Score] = score
                    gad7.remove(AssessmentDefinedParams.PHQ9_Score)

                    val riskLevel = gad7[AssessmentDefinedParams.PHQ9_Risk_Level] as String
                    gad7[Screening.PHQ4_Risk_Level] = riskLevel
                    gad7.remove(AssessmentDefinedParams.PHQ9_Risk_Level)

                    val mentalHealth =
                        gad7[AssessmentDefinedParams.PHQ9_Mental_Health] as ArrayList<*>
                    gad7[Screening.PHQ4_Mental_Health] = mentalHealth
                    gad7.remove(AssessmentDefinedParams.PHQ9_Mental_Health)
                }
            }
            if (result != null) {
                result = Pair(StringConverter.convertGivenMapToString(result.second), result.second)
                result.first?.let {
                    val reqMap = StringConverter.convertStringToMap(it)
                    val request = StringConverter.getJsonObject(
                        Gson().toJson(reqMap)
                    )
                    viewModel.createMentalHealthAssessment(request)
                }
            }
        } catch (_: Exception) {
            //Exception - Catch block
        }
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