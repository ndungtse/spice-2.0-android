package com.medtroniclabs.spice.ui.medicalreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.medtroniclabs.spice.data.ExaminationModel
import com.medtroniclabs.spice.databinding.FragmentExaminationCardBinding
import com.medtroniclabs.spice.formgeneration.ExaminationGenerator
import com.medtroniclabs.spice.formgeneration.ExaminationListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
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

    private fun getOptionList(): ArrayList<Map<String, Any>>? {
        val list = ArrayList<Map<String,Any>>()
        val map = HashMap<String,Any>()
        map["id"] = "Yes"
        map["name"] = "Yes"
        list.add(map)
        val mapone  = HashMap<String,Any>()
        mapone["id"] = "No"
        mapone["name"] = "No"
        list.add(mapone)
        return list
    }

    private fun mockExaminationData(): ArrayList<ExaminationModel> {
        val list = ArrayList<ExaminationModel>()
        val questionnairesList = ArrayList<FormLayout>()
        questionnairesList.add(FormLayout(viewType = "SingleSelectionView", id= "yellowSkinOrFace",title = "Yellow skin or face less than 24 hrs", family = "Jaundice", visibility = null, optionsList = getOptionList()))
        questionnairesList.add(FormLayout(viewType = "SingleSelectionView", id= "yellowPalmOrSoles",title = "Yellow palms and soles at any age", family = "Jaundice", visibility = null, optionsList = getOptionList()))
        questionnairesList.add(FormLayout(viewType = "SingleSelectionView", id= "jaundiceAfter24",title = "Jaundice appearing after 24 hrs of age and palms ", family = "Jaundice", visibility = null, optionsList = getOptionList()))
        questionnairesList.add(FormLayout(viewType = "EditText", id= "NoOfDays",title = "No of Days", family = "Jaundice", visibility = null, optionsList = null, hint = "Number"))
        questionnairesList.add(FormLayout(viewType = "DialogCheckbox", id= "diarrhoeaSigns",title = "Signs", family = "Jaundice", visibility = null, optionsList = null, hint = "Select Signs"))
        val model = ExaminationModel("Jaundice",questionnairesList)
        list.add(model)
        val questionnairesList1 = ArrayList<FormLayout>()
        questionnairesList1.add(FormLayout(viewType = "SingleSelectionView", id= "yellowSkinOrFace1",title = "Yellow skin or face less than 24 hrs", family = "Jaundice", visibility = null, optionsList = getOptionList()))
        questionnairesList1.add(FormLayout(viewType = "SingleSelectionView", id= "yellowPalmOrSoles1",title = "Yellow palms and soles at any age", family = "Jaundice", visibility = null, optionsList = getOptionList()))
        questionnairesList1.add(FormLayout(viewType = "SingleSelectionView", id= "jaundiceAfter241",title = "Jaundice appearing after 24 hrs of age and palms ", family = "Jaundice", visibility = null, optionsList = getOptionList()))
        questionnairesList1.add(FormLayout(viewType = "EditText", id= "NoOfDays1",title = "No of Days1", family = "Jaundice", visibility = null, optionsList = null, hint = "Number"))
        questionnairesList1.add(FormLayout(viewType = "DialogCheckbox", id= "diarrhoeaSigns1",title = "Signs", family = "Jaundice", visibility = null, optionsList = null, hint = "Select Signs"))
        val model1 = ExaminationModel("Jaundice1",questionnairesList1)
        list.add(model1)
        return list
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