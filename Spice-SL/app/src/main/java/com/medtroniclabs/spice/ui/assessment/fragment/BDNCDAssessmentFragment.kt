package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.common.CVDRiskCalculator
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentAssessmentBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.ANY_NEW_OR_WORSENING_SYMPTOMS
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.NAME
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.NEW_WORSENING_SYMPTOMS
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.ncd
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.rootSuffix
import com.medtroniclabs.spice.ui.assessment.referrallogic.ReferralResultGenerator
import com.medtroniclabs.spice.ui.assessment.utils.AssessmentUtil
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.medtroniclabs.spice.ui.common.GeneralInfoDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BDNCDAssessmentFragment() : BaseFragment(), FormEventListener {
    private lateinit var binding: FragmentAssessmentBinding

    private lateinit var formGenerator: FormGenerator
    private val viewModel: AssessmentViewModel by activityViewModels()

    companion object {
        const val TAG: String = "BDNCDAssessmentFragment"

        fun newInstance(): BDNCDAssessmentFragment = BDNCDAssessmentFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        getFormDataForWorkflow()
        setListeners()
        attachObservers()
    }

    private fun getFormDataForWorkflow() {
        viewModel.getFormData(MenuConstants.NCD_MENU_ID)
        viewModel.getRiskEntityList()
        viewModel.getNearestHealthFacility()
    }

    private fun initView() {
        // viewModel.setUserJourney(AnalyticsDefinedParams.NCDASSESSMENT)
        replaceFragmentInId<BioDataFragment>(
            binding.bioDataFragmentContainer.id,
            tag = BioDataFragment.TAG,
        )
        formGenerator = FormGenerator(
            requireContext(),
            binding.llForm,
            this,
            binding.scrollView,
            translate = SecuredPreference.getIsTranslationEnabled(),
        ) { map, id ->
            when (id) {
                Screening.Weight, Screening.Height -> {
                    viewModel.renderBMIValue(requireContext(), formGenerator, map)
                }
            }
        }
    }

    private fun setListeners() {
        binding.btnSubmit.setOnClickListener {
            formGenerator.formSubmitAction(binding.btnSubmit)
        }
    }

    private fun attachObservers() {
        viewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }
                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        formGenerator.populateViews(data.formLayout)
                    }
                }
                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    override fun loadLocalCache(
        id: String,
        localDataCache: Any,
        selectedParent: Long?,
    ) {
    }

    override fun onPopulate(targetId: String) {
    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        formLayout: FormLayout,
        resultMap: Any?,
    ) {
        CheckBoxDialog
            .newInstance(id, resultMap) { resultMap ->
                formGenerator.validateCheckboxDialogue(id, formLayout, resultMap)
                hideOrShowAnyNewWorseningSymptomView(resultMap)
            }.show(childFragmentManager, CheckBoxDialog.TAG)
    }

    private fun hideOrShowAnyNewWorseningSymptomView(resultMap: ArrayList<HashMap<String, Any>>) {
        val shouldShowAnyNew = resultMap.any { map ->
            map[NAME] == ANY_NEW_OR_WORSENING_SYMPTOMS
        }

        formGenerator.getViewByTag(NEW_WORSENING_SYMPTOMS + rootSuffix)?.visibility =
            if (shouldShowAnyNew) View.VISIBLE else View.GONE
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?,
    ) {
        informationList?.let {
            GeneralInfoDialog
                .newInstance(
                    title,
                    description,
                    it,
                ).show(childFragmentManager, GeneralInfoDialog.TAG)
        }
    }

    override fun onFormSubmit(
        resultMap: HashMap<String, Any>?,
        serverData: List<FormLayout>?,
    ) {
        resultMap?.let { details ->
            val result = serverData?.let {
                FormResultComposer().groupValues(
                    serverData = it,
                    details,
                    ncd,
                )
            }

            viewModel.memberDetailsLiveData.value?.data?.let { memberDetail ->
                result?.second?.let {
                    val ncdMap = it[ncd] as HashMap<String, Any>
                    val bpResult = AssessmentUtil.calculateAverageBloodPressure(ncdMap)
                    val bgResult = AssessmentUtil.addDateAndTimeForGlucose(ncdMap)
                    val symptomList = AssessmentUtil.getSymptomsList(ncdMap)

                    // Compute Referral Logic
                    val referralResult = ReferralResultGenerator().computeReferralResultForBDNCD(ncdMap, bpResult, bgResult, symptomList)

                    // Compute CVD Risk
                    CVDRiskCalculator.calculateCVDRiskFactor(ncdMap, viewModel.riskClassificationModels, memberDetail.dateOfBirth, memberDetail.gender)

                    viewModel.saveAssessment(serverData, it, referralResult, viewModel.menuId)
                }
            }
        }
    }

    override fun onRenderingComplete() {
    }

    override fun onUpdateInstruction(
        id: String,
        selectedId: Any?,
    ) {
    }

    override fun onInformationHandling(
        id: String,
        noOfDays: Int,
        enteredDays: Int?,
        resultMap: HashMap<String, Any>?,
    ) {
    }

    override fun onAgeCheckForPregnancy() {
    }

    override fun handleMandatoryCondition(formLayout: FormLayout?) {
    }

    override fun onAgeUpdateListener(
        age: Int,
        serverData: List<FormLayout>?,
        resultHashMap: HashMap<String, Any>,
    ) {
    }
}
