package com.medtroniclabs.spice.ui.medicalreview.examinations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.ExaminationModel
import com.medtroniclabs.spice.databinding.FragmentExaminationCardBinding
import com.medtroniclabs.spice.formgeneration.ExaminationGenerator
import com.medtroniclabs.spice.formgeneration.ExaminationListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ExaminationCardFragment : BaseFragment(), ExaminationListener {

    private val viewModel: ExaminationCardViewModel by activityViewModels()
    private lateinit var binding: FragmentExaminationCardBinding
    private lateinit var examinationGenerator: ExaminationGenerator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExaminationCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
        viewModel.getExaminationQuestionsByWorkFlow(viewModel.workFlowType)
    }

    private fun attachObservers() {
        viewModel.examinationQuestionsLiveData.observe(viewLifecycleOwner) {
            examinationGenerator.populateExaminationView(it)
        }
    }

    private fun initView() {
        examinationGenerator = ExaminationGenerator(binding.root.context, binding.llFamilyRoot,this)
    }

    override fun onDialogueCheckboxListener(
        id: String,
        formLayout: FormLayout,
        resultMap: Any?,
        diseaseName: String
    ) {
        CheckBoxDialog.newInstance(id, resultMap) { map ->
            examinationGenerator.validateCheckboxDialogue(id, map,diseaseName)
        }.show(childFragmentManager, CheckBoxDialog.TAG)
    }

    override fun setResultHashMap(resultMap: HashMap<String, Any>) {
        viewModel.examinationResultHashMap = resultMap
    }

}