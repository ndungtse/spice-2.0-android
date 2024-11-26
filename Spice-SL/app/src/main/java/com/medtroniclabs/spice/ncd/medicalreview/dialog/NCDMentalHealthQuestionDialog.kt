package com.medtroniclabs.spice.ncd.medicalreview.dialog

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentNCDMentalHealthQuestionDialogBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.mappingkey.Screening
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
    private val viewModel: AssessmentViewModel by activityViewModels()
    private var assessmentJSON: List<FormLayout>? = null
    private val viewModels: NCDMentalHealthViewModel by activityViewModels()
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
            MenuConstants.SCREENING.lowercase()
        )
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
                        assessmentJSON = it.filter { it.family?.contains("substanceAbuse") == true }
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
        viewModel.mentalHealthQuestions.observe(viewLifecycleOwner) { resourceState ->
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
    }

    companion object {
        const val TAG = "NCDMentalHealthQuestionDialog"
        fun newInstance(type: String): NCDMentalHealthQuestionDialog {
            val fragment = NCDMentalHealthQuestionDialog()
            val args = Bundle()
            args.putString(Screening.type, type)
            fragment.arguments = args
            return fragment
        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        if (localDataCache is String) {
            when (localDataCache) {
                Screening.PHQ4, AssessmentDefinedParams.PHQ9, AssessmentDefinedParams.GAD7 -> {
                    viewModel.fetchMentalHealthQuestions(localDataCache)
                }

                AssessmentDefinedParams.Fetch_MH_Questions -> {
                    formGenerator.fetchMHQuestions(
                        id,
                        viewModel.mentalHealthQuestions.value?.data?.get(id)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.ivClose.id, binding.btnCancel.id -> dismiss()
        }
    }
}