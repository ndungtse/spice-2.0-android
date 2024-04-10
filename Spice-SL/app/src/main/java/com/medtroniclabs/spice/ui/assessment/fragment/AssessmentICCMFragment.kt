package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.getYearMonthAndWeek
import com.medtroniclabs.spice.common.DefinedParams.DefaultID
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentAssessmentBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Information
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.formgeneration.utility.InformationLayoutFragment
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants.ICCM_MENU_ID
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getNutritionStatus
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Amoxicillin
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.BreathPerMinute
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FB_MAX_BREATHING
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FB_MAX_MONTH
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FB_MAX_YEAR
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FB_MIN_BREATHING
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FB_MIN_MONTH
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FB_MIN_YEAR
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.MUAC
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.chestInDrawing
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.hasOedemaOfBothFeet
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.muacCode
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.muacStatus
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.rootSuffix
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.summaryKey
import com.medtroniclabs.spice.ui.assessment.referrallogic.ReferralResultGenerator
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentICCMFragment : BaseFragment(), FormEventListener, View.OnClickListener {

    private lateinit var binding: FragmentAssessmentBinding
    private lateinit var formGenerator: FormGenerator
    private val viewModel: AssessmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        getFormDataForWorkflow()
        setListeners()
        attachObservers()
    }

    private fun setListeners() {
        binding.btnSubmit.safeClickListener(this)
    }

    private fun getFormDataForWorkflow() {
        viewModel.getFormData(ICCM_MENU_ID)
        viewModel.getNearestHealthFacility()
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

    private fun initView() {
        replaceFragmentInId<BioDataFragment>(
            binding.bioDataFragmentContainer.id,
            tag = BioDataFragment.TAG
        )
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, null, this, binding.scrollView,
            translate = false
        )
    }

    companion object {
        const val TAG = "AssessmentICCMFragment"
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
        description: String?
    ) {
        val titleById = getTitleById(id)
        InformationLayoutFragment.newInstance(id, titleById)
            .show(childFragmentManager, InformationLayoutFragment.TAG)
    }

    private fun getTitleById(id: String): String {
        return when (id) {
            muacCode -> getString(R.string.measuring_muac)
            hasOedemaOfBothFeet -> getString(R.string.checking_for_oedema)
            chestInDrawing -> getString(R.string.chest_in_drawing)
            else -> {id}
        }
    }


    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        resultMap?.let { details ->
            val referralResult = ReferralResultGenerator().calculateIccmReferralResult(details, viewModel.memberDetailsLiveData.value?.data)
            val result = serverData?.let {
                FormResultComposer().groupValues(
                    context = requireContext(),
                    serverData = it,
                    details,
                    ICCM_MENU_ID
                )
            }

            result?.second?.let {
                viewModel.saveAssessment(it, referralResult,viewModel.menuId)
            }
        }
    }

    override fun onRenderingComplete() {

    }

    override fun onUpdateInstruction(id: String, selectedId: Any?) {
        when(id) {
            muacCode -> {
                if (selectedId is String && selectedId != DefaultID)
                {
                    formGenerator.getViewByTag(muacStatus + rootSuffix )?.apply {
                        visibility = View.VISIBLE
                    }
                    formGenerator.getViewByTag(muacStatus + summaryKey)?.let {
                        if (it is TextView){
                            it.text = requireContext().getString(R.string.firstname_lastname, MUAC.uppercase(), selectedId)
                        }
                    }
                    formGenerator.getViewByTag(muacStatus )?.let {
                        if (it is TextView){
                            it.text = getNutritionStatus(selectedId, requireContext())
                        }
                    }
                } else {
                    formGenerator.getViewByTag(muacStatus + rootSuffix )?.apply {
                        visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onInformationHandling(
        id: String,
        noOfDays: Int,
        enteredDays: Int?,
        resultMap: HashMap<String, Any>?
    ) {
        when (id) {
            BreathPerMinute -> {
                enteredDays?.let {
                    viewModel.memberDetailsLiveData.value?.data?.let { data ->
                        getYearMonthAndWeek(data.dateOfBirth, DATE_FORMAT_yyyyMMddHHmmssZZZZZ).let { result ->
                            val year = result.first ?: 0
                            val month = result.second ?: 0
                            if ((year == 0 && month > FB_MIN_MONTH && month < FB_MAX_MONTH) && enteredDays >= FB_MAX_BREATHING) {
                                displayDaysInformation(id, View.VISIBLE)
                                getAmoxicillinStatus()
                            } else if ((month == FB_MAX_MONTH || year in FB_MIN_YEAR..FB_MAX_YEAR) && enteredDays >= FB_MIN_BREATHING) {
                                displayDaysInformation(id, View.VISIBLE)
                                getAmoxicillinStatus()
                            } else {
                                displayDaysInformation(id, View.INVISIBLE)
                                dismissAmoxicillinStatus(resultMap)
                            }
                        }
                    }
                }
            }
            else -> {
                if (enteredDays!=null && enteredDays > noOfDays) {
                    updateColorCode(id, ContextCompat.getColor(requireContext(), R.color.medium_high_risk_color))
                } else {
                    updateColorCode(id, ContextCompat.getColor(requireContext(), R.color.secondary_black))
                }
            }
        }
    }

    override fun onAgeCheckForPregnancy() {
        
    }

    private fun dismissAmoxicillinStatus(resultMap: HashMap<String, Any>?) {
        formGenerator.getViewByTag((Amoxicillin.lowercase()) + rootSuffix )?.apply {
            visibility = View.GONE
            resultMap?.let {map ->
                if (map.containsKey(Amoxicillin.lowercase())){
                    map.remove(Amoxicillin.lowercase())
                    formGenerator.resetSingleSelection(Amoxicillin.lowercase())
                }
            }
        }
    }

    private fun getAmoxicillinStatus() {
        formGenerator.getViewByTag((Amoxicillin.lowercase()) + rootSuffix )?.apply {
            visibility = View.VISIBLE
        }
    }

    private fun updateColorCode(id: String, colorCode: Int) {
        formGenerator.getViewByTag(id + Information)?.let { view ->
            if (view is TextView){
                view.setTextColor(colorCode)
            }
        }
    }

    private fun displayDaysInformation(id: String, viewVisibility: Int) {
        formGenerator.getViewByTag(id + Information)?.apply { visibility = viewVisibility }
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnSubmit.id -> {
                formGenerator.formSubmitAction(view)
            }
        }
    }

}