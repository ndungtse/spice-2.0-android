package com.medtroniclabs.spice.ui.cbs.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentMemberRegistrationBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.mappingkey.MemberRegistration
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CbsMemberRegistration :
    BaseFragment(),
    View.OnClickListener,
    FormEventListener {
    private lateinit var binding: FragmentMemberRegistrationBinding
    private lateinit var childFormGenerator: FormGenerator
    private val viewModel: AssessmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentMemberRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "CbsMemberRegistration"

        fun newInstance() = CbsMemberRegistration()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    private fun initView() {
        viewModel.setUserJourney("${AnalyticsDefinedParams.CBS} ${AnalyticsDefinedParams.MemberRegistration}")
        binding.btnSubmit.gone()
        binding.btnStartAssessment.safeClickListener(this)
        binding.btnStartAssessment.text = getString(R.string.submit)
        childFormGenerator = FormGenerator(
            requireContext(),
            binding.llForm,
            this,
            binding.scrollView,
            translate = SecuredPreference.getIsTranslationEnabled(),
        )
        viewModel.getFormDataCbs(
            DefinedParams.HOUSEHOLD_MEMBER_REGISTRATION,
        )
    }

    private fun attachObserver() {
        viewModel.formLayoutsCbsLiveData.observe(viewLifecycleOwner) { resources ->
            when (resources.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resources.data?.let { data ->
//                        childFormGenerator.populateViews(data.formLayout.filter { it.id != MemberRegistration.isPregnant })
//                        removeHouseHoldHeadMemberRelationShip()
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            binding.btnStartAssessment.id -> {
                withLocationCheck({
                    viewModel.fetchCurrentLocation(requireContext())
                    childFormGenerator.formSubmitAction(v)
                })
            }
        }
    }

    override fun onFormSubmit(
        resultMap: HashMap<String, Any>?,
        serverData: List<FormLayout>?,
    ) {
        resultMap?.let { map ->
            // Hide Error message
            childFormGenerator
                .getViewByTag(AssessmentDefinedParams.DateOfBirth + AssessmentDefinedParams.errorSuffix)
                ?.apply {
                    visibility = View.GONE
                }
            val month =
                map[com.medtroniclabs.spice.formgeneration.config.DefinedParams.Month] as? Int
            val week = map[com.medtroniclabs.spice.formgeneration.config.DefinedParams.Week] as? Int
            // Month and Week field validation
            if (month !in 0..11 || week !in 0..4) {
                showInValidDob(getString(R.string.please_select_a_valid_value_month))
                return
            }

            val dob = map[MemberRegistration.dateOfBirth] as String
            if (!MemberRegistration.isValidMinAgeForCbsMemberAdd(dob)) {
                showInValidDob(getString(R.string.please_select_a_valid_value_age_cbs))
                return
            }

            viewModel.memberDetailsLiveData.value?.data?.let {
                viewModel.setUserJourney(AnalyticsDefinedParams.SUBMITBUTTONTRIGGERED)
                viewModel.saveMember(
                    serverData,
                    resultMap,
                    householdId = it.householdLocalId,
                    it.id,
                    viewModel.lastLocation,
                )
            }
        }
    }

    private fun showInValidDob(message: String) {
        childFormGenerator
            .getViewByTag(AssessmentDefinedParams.DateOfBirth + AssessmentDefinedParams.errorSuffix)
            ?.apply {
                visibility = View.VISIBLE
            }.takeIf { it is TextView }
            ?.let { textView ->
                (textView as TextView).text =
                    message
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
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?,
    ) {
    }

    override fun onRenderingComplete() {
        autoPopulateGender()
    }

    private fun autoPopulateGender() {
        viewModel.birthLiveData.value?.data?.first?.let {
            when (it.lowercase()) {
                DefinedParams.BOY.lowercase() -> {
                    singleSelectValueOption(
                        DefinedParams.male,
                        MemberRegistration.gender,
                    )
                }

                DefinedParams.GIRL.lowercase() -> {
                    singleSelectValueOption(
                        DefinedParams.female,
                        MemberRegistration.gender,
                    )
                }

                else -> {}
            }
            childFormGenerator.disableSingleSelection(MemberRegistration.gender)
        }
    }

    private fun singleSelectValueOption(
        value: String,
        key: String,
    ) {
        childFormGenerator
            .getViewByTag("${value}_$key")
            ?.let { view ->
                if (view is TextView) {
                    view.isSelected = true
                    view.performClick()
                }
            }
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

    fun getCurrentAnsweredStatus(): Boolean = childFormGenerator.getResultMap().isNotEmpty()
}
