package com.medtroniclabs.spice.ui.medicalreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.medtroniclabs.spice.appextensions.setSuccess
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.ExaminationModel
import com.medtroniclabs.spice.databinding.FragmentExaminationCardBinding
import com.medtroniclabs.spice.formgeneration.ExaminationGenerator
import com.medtroniclabs.spice.formgeneration.ExaminationListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.formgeneration.utility.CheckBoxDialog
import com.medtroniclabs.spice.ui.BaseFragment
import kotlin.collections.ArrayList

class ExaminationCardFragment : BaseFragment(), ExaminationListener {

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
    }

    private fun initView() {
        examinationGenerator = ExaminationGenerator(binding.root.context, binding.llFamilyRoot,this)
        examinationGenerator.populateExaminationView(mockExaminationData())
    }


    private fun mockExaminationData(): ArrayList<ExaminationModel> {
        val objectList = Gson().fromJson(
            CommonUtils.getStringFromAssets(
                "examination_2_5_years.json",
                requireActivity().assets
            ),
            Array<ExaminationModel>::class.java
        ).asList()
        return ArrayList(objectList)
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

}