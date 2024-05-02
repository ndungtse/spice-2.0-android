package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setSuccess
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.EntityMapper
import com.medtroniclabs.spice.databinding.FragmentAssessmentRmnchBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentActivity
import com.medtroniclabs.spice.ui.assessment.referrallogic.ReferralResultGenerator
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.Miscarriage
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PlaceOfDelivery
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class AssessmentRMNCHFragment : BaseFragment(), View.OnClickListener,
    FormEventListener {

    private lateinit var binding: FragmentAssessmentRmnchBinding
    private val viewModel: AssessmentViewModel by activityViewModels()
    private lateinit var formGenerator: FormGenerator

    companion object {
        const val TAG = "AssessmentRMNCHFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentRmnchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        getFormDataForWorkflow()
        attachObservers()
        setListener()
    }

    private fun setListener() {
        binding.btnSubmit.safeClickListener(this)
    }

    private fun attachObservers() {
        viewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        formGenerator.populateViews(data.formLayout)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }

                ResourceState.LOADING -> {
                    showProgress()
                }
            }
        }
        viewModel.facilitySpinnerLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        formGenerator.spinnerDataInjection(
                            data,
                            EntityMapper.getResultSpinnerMapList(data)
                        )
                    }
                }

                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }

        viewModel.memberClinicalLiveData.observe(viewLifecycleOwner) { data ->
            data?.clinicalDate?.let { date ->
                if (date.isNotEmpty()) {
                    formGenerator.getViewByTag(RMNCH.lastMenstrualPeriod + formGenerator.rootSuffix)
                        ?.gone()
                    formGenerator.getViewByTag(RMNCH.DateOfDelivery + formGenerator.rootSuffix)
                        ?.gone()
                    formGenerator.getViewByTag(RMNCH.NoOfNeonate + formGenerator.rootSuffix)?.gone()
                }
            }
        }
    }

    private fun getFormDataForWorkflow() {
        viewModel.getFormData(RMNCH.RMNCHChildHoodVisit)
    }

    private fun initView() {
        replaceFragmentInId<BioDataFragment>(
            binding.bioDataFragmentContainer.id,
            tag = BioDataFragment.TAG
        )
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, null, this, binding.scrollView,
            translate = false
        )
        showRespectiveWorkflow()
    }

    private fun showRespectiveWorkflow() {
        var resultJsonFileName: String? = null
        when (viewModel.workflowName) {
            RMNCH.ANC -> {
                resultJsonFileName = "rmnch_anc_visit.json"
                binding.btnSubmit.text = getString(R.string.submit)
            }

            RMNCH.ChildHoodVisit -> {
                resultJsonFileName = "rmnch_childhood_visit.json"
                binding.btnSubmit.text = getString(R.string.submit)
            }

            RMNCH.PNC -> {
                resultJsonFileName = "rmnch_pnc_phu_delivery_mother.json"
                binding.btnSubmit.text = getString(R.string.next)

            }
        }
        resultJsonFileName?.let { name ->
            loadJson(name)
        }
    }

    private fun loadJson(resultJsonFileName: String) {
        val objectList = Gson().fromJson(
            CommonUtils.getStringFromAssets(
                resultJsonFileName,
                requireActivity().assets
            ),
            Array<FormLayout>::class.java
        ).asList()

        viewModel.formLayoutsLiveData.setSuccess(FormResponse(objectList, time = 123231231))
    }


    override fun onClick(v: View) {
        when (v.id) {
            binding.btnSubmit.id -> {
                formGenerator.formSubmitAction(v)
            }
        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        when (id) {
            PlaceOfDelivery -> {
                if (localDataCache is String) {
                    viewModel.loadDataCacheByType(id, localDataCache)
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
        CheckBoxDialog.newInstance(id, resultMap) { map ->
            formGenerator.validateCheckboxDialogue(id, serverViewModel, map)
        }.show(childFragmentManager, CheckBoxDialog.TAG)
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?
    ) {
    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        resultMap?.let { details ->
            val result = serverData?.let {
                FormResultComposer().groupValues(
                    context = requireContext(),
                    serverData = it,
                    details
                )
            }
            result?.second?.let { second ->
                handleNextPregnancyFlow(second)
            }
        }
    }

    private fun handleNextPregnancyFlow(second: HashMap<String, Any>) {
        viewModel.workflowName?.let { name ->
            when (name) {
                RMNCH.PNC -> {
                    viewModel.pncMotherDetailMap = second
                    (requireActivity() as AssessmentActivity).replaceAssessmentRMNCHNeonateFragment()
                }

                else -> {
                    viewModel.memberDetailsLiveData.value?.data?.let { memberDetail ->
                        viewModel.handlePregnancy(
                            second,
                            workflowName = name,
                            memberDetail,
                            viewModel.memberClinicalLiveData.value
                        )
                    }
                    calculateGestationalAge(second, name)
                    checkForOtherMetrics(second, name)
                    val resultGenerator = ReferralResultGenerator()
                    val referralResult = resultGenerator.calculateRMNCHReferralResult(second)
                    viewModel.saveAssessment(second, referralResult, getMenuName(viewModel.workflowName))
                }
            }
        }
    }

    private fun checkForOtherMetrics(details: HashMap<String, Any>, name: String) {
        if (details.containsKey(name) && details[name] is Map<*, *>) {
            val second = details[name] as HashMap<String, Any>
            if (second.containsKey(Miscarriage)) {
                val miscarriage = second[Miscarriage]
                if (miscarriage is Boolean && miscarriage) {
                    viewModel.memberDetailsLiveData.value?.data?.let {
                        viewModel.updateMemberClinicalData(it.patientId,RMNCH.ANC,0L,null)
                    }
                }
            }
        }
    }

    private fun getMenuName(workflowName: String?): String {
        when (workflowName) {
            RMNCH.ANC -> return RMNCH.ANC_MENU
            RMNCH.ChildHoodVisit -> return RMNCH.CHILD_MENU
            RMNCH.PNC -> return RMNCH.PNC_MENU
        }
        return MenuConstants.RMNCH_MENU_ID
    }

    private fun calculateGestationalAge(details: HashMap<String, Any>, name: String) {
        if (details.containsKey(name) && details[name] is Map<*, *>) {
            val second = details[name] as HashMap<String, Any>
            if (second.containsKey(RMNCH.lastMenstrualPeriod)) {
                val lastMenstrualDate = second[RMNCH.lastMenstrualPeriod]
                if (lastMenstrualDate is String) {
                    val calendar = getLastMenstrualDate(lastMenstrualDate)
                    second[RMNCH.gestationalAge] = "${
                        DateUtils.calculateGestationalAge(
                            calendar
                        ).first
                    } ${getString(R.string.weeks)}"
                }
            }
        }
    }

    private fun getLastMenstrualDate(clinicalDate: String): Calendar {
        // Define the format of the input date string
        val lastMenstrualDateString = DateUtils.convertDateFormat(
            clinicalDate,
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_ddMMyyyy
        )
        return Calendar.getInstance().apply {
            time = getDateFormat().parse(lastMenstrualDateString)
        }
    }

    private fun getDateFormat(): SimpleDateFormat {
        return SimpleDateFormat(
            DateUtils.DATE_ddMMyyyy,
            Locale.getDefault()
        )
    }


    override fun onRenderingComplete() {
        viewModel.memberClinicalLiveData.value?.clinicalDate?.let {
            formGenerator.getViewByTag(RMNCH.lastMenstrualPeriod + formGenerator.rootSuffix)?.gone()
        }
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

}
