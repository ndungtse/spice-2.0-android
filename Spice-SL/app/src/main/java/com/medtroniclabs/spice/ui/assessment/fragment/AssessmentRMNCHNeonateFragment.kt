package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setSuccess
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentChildRegistrationBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.MemberRegistration
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentActivity
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentRMNCHNeonateViewModel
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class AssessmentRMNCHNeonateFragment : BaseFragment(), View.OnClickListener,
    FormEventListener {

    private lateinit var binding: FragmentChildRegistrationBinding
    private lateinit var formGenerator: FormGenerator
    private lateinit var childFormGenerator: FormGenerator
    private val viewModel: AssessmentViewModel by activityViewModels()
    private val assessmentRMNCHNeonateViewModel: AssessmentRMNCHNeonateViewModel by activityViewModels()


    companion object {
        const val TAG = "AssessmentRMNCHNeonateFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChildRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        viewModel.getNearestHealthFacility()
        loadJson()
        attachObservers()
        setListener()
    }

    private fun setListener() {
        binding.btnSubmit.setOnClickListener(this)
    }

    private fun initView() {

        formGenerator = FormGenerator(
            requireContext(), binding.llForm, null, this, binding.scrollView,
            translate = SecuredPreference.getIsTranslationEnabled()
        )
        childFormGenerator = FormGenerator(
            requireContext(), binding.llChildForm, null, this, binding.scrollView, translate = SecuredPreference.getIsTranslationEnabled()
        )
    }

    private fun loadJson() {
        viewModel.memberClinicalLiveData.value?.clinicalDate?.let {
            binding.llChildForm.gone()
            binding.bioDataFragmentContainer.visible()
            replaceFragmentInId<BioDataChildFragment>(
                binding.bioDataFragmentContainer.id,
                tag = BioDataChildFragment.TAG
            )

        } ?: kotlin.run {
            binding.llChildForm.visible()
            binding.bioDataFragmentContainer.gone()
            assessmentRMNCHNeonateViewModel.getFormData(
                DefinedParams.HOUSEHOLD_MEMBER_REGISTRATION
            )
        }

        assessmentRMNCHNeonateViewModel.getFormChildData(RMNCH.PNCNeonatal)
    }


    private fun attachObservers() {

        assessmentRMNCHNeonateViewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resourceState ->
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

        assessmentRMNCHNeonateViewModel.memberFormLayoutsLiveData.observe(viewLifecycleOwner) { resources ->
            when (resources.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resources.data?.let { data ->
                        childFormGenerator.populateViews(data.formLayout)
                        disableDateOfBirth()
                        removeHouseHoldHeadMemberRelationShip()
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }

        assessmentRMNCHNeonateViewModel.assessmentSaveLiveData.observe(viewLifecycleOwner) { resources ->
            when (resources.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resources.data?.let { _ ->
                        (requireActivity() as AssessmentActivity).replaceAssessmentRMNCHNeonateSummaryFragment()
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }

    }

    private fun removeHouseHoldHeadMemberRelationShip() {
         childFormGenerator.getViewByTag(MemberRegistration.householdHeadRelationship)?.let {view ->
             if (view is AppCompatSpinner){
                 val adapter = view.adapter
                 if (adapter is CustomSpinnerAdapter){
                     adapter.removeItemById(DefinedParams.HouseholdHead)
                 }
             }
         }
    }

    private fun disableDateOfBirth() {
        val motherMap = viewModel.pncMotherDetailMap
        motherMap?.get(RMNCH.PNC)?.let { map ->
            if (map is Map<*, *>) {
                val dateOfDelivery = map[RMNCH.DateOfDelivery]
                if (dateOfDelivery is String) {
                    val dateOfBirth = DateUtils.convertDateFormat(
                        dateOfDelivery,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DateUtils.DATE_ddMMyyyy
                    )
                    childFormGenerator.getViewByTag(MemberRegistration.dateOfBirth)?.let { view ->
                        if (dateOfBirth.isNotBlank()) {
                            childFormGenerator.disableView(view, requireContext())
                        }
                        childFormGenerator.setValueForView(dateOfBirth, view)
                    }

                    val dateDob = DateUtils.convertStringToDate(dateOfDelivery, DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
                    dateDob?.let { dob ->
                        childFormGenerator.fillDetailsOnDatePickerSet(dob, false)
                    }
                }
            }
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            binding.btnSubmit.id -> {
                if (viewModel.memberClinicalLiveData.value?.clinicalDate == null) {
                    if (childFormGenerator.formSubmitAction(v)) {
                        formGenerator.formSubmitAction(v)
                    }
                } else {
                    formGenerator.formSubmitAction(v)
                }
            }
        }
    }


    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
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
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?
    ) {
    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        resultMap?.let { map ->
            viewModel.pncMotherDetailMap?.let { motherDetailMap ->
                viewModel.memberDetailsLiveData.value?.data?.apply {
                    if (map.containsKey(MemberRegistration.gender)) {
                        assessmentRMNCHNeonateViewModel.memberMap = map
                    } else {
                        viewModel.memberDetailsLiveData.value?.data?.let { memberDetail ->
                            viewModel.handlePregnancy(
                                motherDetailMap,
                                workflowName = RMNCH.PNC,
                                memberDetail,
                                viewModel.memberClinicalLiveData.value,
                                map
                            )
                            assessmentRMNCHNeonateViewModel.savePNCDetail(
                                map,
                                householdLocalId,
                                motherDetailMap,
                                memberDetail,
                                assessmentRMNCHNeonateViewModel.childMemberDetailsLiveData.value?.data,
                                viewModel.followUpId
                            )
                        }
                    }
                }
            }

        }
        viewModel.setAnalyticsData(
            UserDetail.startDateTime,
            AnalyticsDefinedParams.RMNCHNeonateAssessment,
            AnalyticsDefinedParams.AssessmentCreation
        )
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
        childFormGenerator.handlePregnancyCardBasedOnAge()
    }

    override fun handleMandatoryCondition(serverData: FormLayout?) {

    }

    override fun onAgeUpdateListener(
        age: Int,
        serverData: List<FormLayout?>?,
        resultHashMap: HashMap<String, Any>
    ) {
        /*
       Never used
        */
    }

    fun getCurrentAnsweredStatus(): Boolean {
        return formGenerator.getResultMap().isNotEmpty() || childFormGenerator.getResultMap().isNotEmpty()
    }


}