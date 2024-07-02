package com.medtroniclabs.spice.ui.medicalreview.diagnosis

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setError
import com.medtroniclabs.spice.appextensions.setWidth
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams.Other
import com.medtroniclabs.spice.data.DiagnosisDiseaseModel
import com.medtroniclabs.spice.data.DiagnosisSaveUpdateRequest
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentDiagnosisDialogBinding
import com.medtroniclabs.spice.formgeneration.DiagnosisGenerator
import com.medtroniclabs.spice.formgeneration.DiagnosisListener
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.viewmodel.DiagnosisViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiagnosisDialogFragment : DialogFragment(), View.OnClickListener, DiagnosisListener {

    private lateinit var binding: FragmentDiagnosisDialogBinding
    private lateinit var diagnosisGenerator: DiagnosisGenerator
    private lateinit var diseaseCategoryTagView: TagListCustomView
    private val patientViewModel: PatientDetailViewModel by activityViewModels()
    private val diagnosisViewModel: DiagnosisViewModel by activityViewModels()

    companion object {
        const val TAG: String = "DiagnosisDialogueFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiagnosisDialogBinding.inflate(layoutInflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListener()
        attachObserver()
    }

    private fun setListener() {
        binding.btnCancel.safeClickListener(this)
        binding.ivClose.safeClickListener(this)
        binding.btnOkay.safeClickListener(this)
        binding.loadingProgress.safeClickListener(this)
    }

    private fun attachObserver() {
        diagnosisViewModel.diagnosisMetaList.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.SUCCESS -> {
                    resource.data?.let { listItems ->
                        patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
                            details.id?.let { id ->
                                diagnosisViewModel.getDiagnosisDetails(
                                    CreateUnderTwoMonthsResponse(
                                        patientReference = id,
                                        type = diagnosisViewModel.diagnosisType
                                    )
                                )
                            } ?: kotlin.run {
                                val diagnosisMetaChipItemList = ArrayList<ChipViewItemModel>()
                                listItems.forEach {
                                    diagnosisMetaChipItemList.add(
                                        ChipViewItemModel(
                                            id = it.id, name = it.name, value = it.value
                                        )
                                    )
                                }
                                diseaseCategoryTagView.addChipItemList(
                                    diagnosisMetaChipItemList,
                                    null
                                )
                                diagnosisViewModel.viewDiagnosis = false
                            }
                        }
                    }
                }

                else -> {

                }
            }
        }

        diagnosisViewModel.diagnosisDetailsList.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let { listItems ->
                        if (diagnosisViewModel.viewDiagnosis) {
                            binding.llFamilyRoot.removeAllViews()
                            diagnosisViewModel.diagnosisMetaList.value?.data?.let { diagnosisList ->
                                /**
                                 * From backend diagnosis items respective ID and its FHIR value only be given,
                                 * We need to compare it with our meta items and filter respective items to render
                                 * and auto-populate the respective chip Items
                                 *
                                 * diagnosisAccordionList - Collecting list of accordions which has been already selected
                                 *                          by comparing diseaseCategory and it value
                                 * diagnosisMetaChipItemList - It is to render the Parent Chip item Disease Category
                                 * selectedDiagnosisMetaChipItemList - It is to collect overall selected parent chip item list
                                 * selectedDiseaseConditionItemList - It is to select chips of disease conditions which is inside accordion of respective group
                                 */

                                val diagnosisAccordionList = ArrayList<DiseaseCategoryItems>()
                                for (diagnosis in diagnosisList) {
                                    if (listItems.any { it.diseaseCategory == diagnosis.value }) {
                                        diagnosisAccordionList.add(diagnosis)
                                    }
                                }

                                val diagnosisMetaChipItemList = ArrayList<ChipViewItemModel>()
                                diagnosisList.forEach {
                                    diagnosisMetaChipItemList.add(
                                        ChipViewItemModel(
                                            id = it.id, name = it.name, value = it.value
                                        )
                                    )
                                }

                                val selectedDiagnosisMetaChipItemList =
                                    ArrayList<ChipViewItemModel>()
                                diagnosisAccordionList.forEach {
                                    selectedDiagnosisMetaChipItemList.add(
                                            ChipViewItemModel(
                                                id = it.id, name = it.name, value = it.value
                                            )
                                        )
                                }

                                val selectedDiseaseConditionItemList = ArrayList<ChipViewItemModel>()
                                listItems.forEach {
                                    val nameItemValue =
                                        diagnosisList.flatMap { item -> item.diseaseCondition }
                                            .find { conditionItem -> conditionItem.value == it.diseaseCondition }?.name
                                    nameItemValue?.let { value ->
                                        if (value.lowercase() != Other.lowercase()) {
                                            selectedDiseaseConditionItemList.add(
                                                ChipViewItemModel(
                                                    id = it.diseaseConditionId, name = value
                                                )
                                            )
                                        }
                                    }
                                }

                                diseaseCategoryTagView.addChipItemList(
                                    diagnosisMetaChipItemList,
                                    selectedDiagnosisMetaChipItemList
                                )
                                diagnosisAccordionList.removeAll{it.name.lowercase() == Other.lowercase()}
                                selectedDiseaseConditionItemList.removeAll{it.name.lowercase() == Other.lowercase()}
                                diagnosisGenerator.populateDiagnosisView(
                                    diagnosisAccordionList,
                                    selectedDiseaseConditionItemList
                                )
                            }
                        }
                    } ?: kotlin.run {
                        diagnosisViewModel.diagnosisMetaList.value?.data?.let { listItems ->
                            val chipItemList = ArrayList<ChipViewItemModel>()
                            listItems.forEach {
                                chipItemList.add(
                                    ChipViewItemModel(
                                        id = it.id, name = it.name, value = it.value
                                    )
                                )
                            }
                            diseaseCategoryTagView.addChipItemList(chipItemList, null)
                        }
                    }
                    diagnosisViewModel.viewDiagnosis = false
                    hideLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }

        diagnosisViewModel.diagnosisSaveUpdateResponse.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    diagnosisViewModel.viewDiagnosis = true
                    patientViewModel.patientDetailsLiveData.value?.data?.let {details ->
                        details.id?.let {id ->
                            getDiagnosisDetails(id)
                        } ?: kotlin.run {
                            details.patientId?.let { patientViewModel.getPatients(it) }
                        }
                    }
                    diagnosisViewModel.diagnosisSaveUpdateResponse.setError()
                    hideLoading()
                    dismiss()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun getDiagnosisDetails(id: String) {
        diagnosisViewModel.getDiagnosisDetails(
            CreateUnderTwoMonthsResponse(
                patientReference = id,
                type = diagnosisViewModel.diagnosisType
            )
        )
    }

    private fun initView() {
        handleVisibility()
        diagnosisGenerator = DiagnosisGenerator(binding.root.context, binding.llFamilyRoot, this)
        diseaseCategoryTagView = TagListCustomView(
            binding.root.context, binding.diseaseConditionChipGroup
        ) { name, _, isChecked ->
            if (!diagnosisViewModel.viewDiagnosis && isShowAccordion() && name?.lowercase() != Other.lowercase() ) {
                diagnosisViewModel.diagnosisMetaList.value?.data?.let { listItems ->
                    if (isChecked) {
                        val filteredList =
                            listItems.filter { it.name.lowercase() == name?.lowercase() }
                        diagnosisGenerator.populateDiagnosisView(filteredList, null)
                    } else {
                        name?.let {
                            diagnosisGenerator.removeViewByTag(it)
                        }
                    }
                }
            }
            saveBtnStateHandler()
        }
        diagnosisGenerator.setDiagnosisCallback(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnCancel.id, binding.ivClose.id -> {
                diagnosisViewModel.viewDiagnosis = true
                dismiss()
            }

            binding.btnOkay.id -> {
                patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
                    details.patientId?.let { patientId ->
                        val request = DiagnosisSaveUpdateRequest(
                            patientId = patientId,
                            patientReference = details.id,
                            diseases = getDiagnosisDiseaseList(),
                            provenance = ProvanceDto(
                                createdDateTime = DateUtils.getCurrentDateAndTime(
                                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                                )
                            ),
                            otherNotes = binding.etOtherDiagnosisNotes.text?.trim().toString(),
                            type = diagnosisViewModel.diagnosisType
                        )
                        diagnosisViewModel.diagnosisCreate(request)
                    }
                }
            }

            binding.loadingProgress.id -> {}
        }
    }

    private fun getDiagnosisDiseaseList(): ArrayList<DiagnosisDiseaseModel> {
        /**
         * This method is to compose request for save and update diagnosis
         */
        val diagnosisList = ArrayList<DiagnosisDiseaseModel>()
        if (isShowAccordion()) {
            diagnosisGenerator.getSelectedTagsForAccordions().let { selectedAccordionMap ->
                for ((diseaseCategoryValue, chipViewItemList) in selectedAccordionMap) {
                    for (chipViewItem in chipViewItemList) {
                        chipViewItem.value?.let { value ->
                            diagnosisViewModel.diagnosisMetaList.value?.data?.filter { item -> item.name == diseaseCategoryValue }
                                ?.let { categoryItem ->
                                    val diagnosisDiseaseModel = DiagnosisDiseaseModel(
                                        diseaseCategoryId = getDiseaseCategoryId(
                                            diseaseCategoryValue
                                        ),
                                        diseaseConditionId = chipViewItem.id ?: -1,
                                        diseaseCategory = categoryItem[0].value,
                                        diseaseCondition = value
                                    )
                                    diagnosisList.add(diagnosisDiseaseModel)
                                }
                        }
                    }
                }
            }
        } else {
            diseaseCategoryTagView.getSelectedTags().let { list ->
                list.forEach { item ->
                    item.id?.let {
                        item.value?.let {
                            diagnosisList.add(
                                DiagnosisDiseaseModel(
                                    diseaseCategoryId = item.id,
                                    diseaseConditionId = null,
                                    diseaseCategory = item.value,
                                    diseaseCondition = null
                                )
                            )
                        }
                    }
                }
            }
        }
        diseaseCategoryTagView.getSelectedTags().let { list ->
            val otherItem = list.filter { item -> item.value?.lowercase() == Other.lowercase() }
            if (otherItem.isNotEmpty() && diagnosisViewModel.diagnosisType == MedicalReviewTypeEnums.AboveFiveYears.name) {
                otherItem[0].id?.let { otherId ->
                    diagnosisList.add(
                        DiagnosisDiseaseModel(
                            diseaseCategoryId = otherId,
                            diseaseConditionId = null,
                            diseaseCategory = otherItem[0].value ?: otherItem[0].name,
                            diseaseCondition = null
                        )
                    )
                }
            }
        }
        return diagnosisList
    }

    private fun getDiseaseCategoryId(diseaseCategoryName: String): Long {
        return diagnosisViewModel.diagnosisMetaList.value?.data?.let { list ->
            list.filter { it.name == diseaseCategoryName }[0].id
        } ?: -1L
    }

    override fun onStart() {
        super.onStart()
        handleDialogSize()
    }

    private fun handleDialogSize() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val width = if (CommonUtils.checkIsTablet(requireContext())) {
            if (isLandscape) 65 else 90
        } else {
            if (isLandscape) 65 else 90
        }
        setWidth(width)
    }

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }

    private fun isShowAccordion(): Boolean {
        return when (diagnosisViewModel.diagnosisType) {
            MedicalReviewTypeEnums.ANC.name, MedicalReviewTypeEnums.PNC.name -> {
                false
            }

            else -> {
                true
            }
        }
    }

    private fun handleVisibility() {
        when(diagnosisViewModel.diagnosisType) {
            MedicalReviewTypeEnums.AboveFiveYears.name -> {
                binding.tvSelectedDiseaseConditionLbl.gone()
            }
            MedicalReviewTypeEnums.ANC.name -> {
                binding.tvSelectedDiseaseCategoryLbl.text = requireContext().getString(R.string.select_diagnosis_found_on_the_patient)
                binding.tvSelectedDiseaseConditionLbl.gone()
                binding.llFamilyRoot.gone()
            }
        }
    }

    override fun onDiagnosisSelection(isEmptyOrNot : Boolean) {
        val previousSelectedItems = diagnosisViewModel.diagnosisDetailsList.value?.data?.let {
            it.isNotEmpty()
        }
        val otherSelection = diseaseCategoryTagView.getSelectedTags()
            .any { it.name.lowercase() == Other.lowercase() }
        binding.btnOkay.isEnabled = isEmptyOrNot|| (previousSelectedItems == true && diagnosisGenerator.isEmptyAccordion()) || otherSelection
    }

    private fun saveBtnStateHandler() {
        val otherSelection = diseaseCategoryTagView.getSelectedTags()
            .any { it.name.lowercase() == Other.lowercase() }
        when (diagnosisViewModel.diagnosisType) {
            MedicalReviewTypeEnums.AboveFiveYears.name -> {
                if (diseaseCategoryTagView.getSelectedTags().isNotEmpty() && (!otherSelection)){
                    binding.tvSelectedDiseaseConditionLbl.visible()
                } else {
                    binding.tvSelectedDiseaseConditionLbl.gone()
                }
                diagnosisViewModel.diagnosisDetailsList.value?.data?.let {
                    if ((diseaseCategoryTagView.getSelectedTags().size <= 1) || otherSelection) {
                        binding.btnOkay.isEnabled =
                            (it.isNotEmpty() && diseaseCategoryTagView.getSelectedTags()
                                .isEmpty()) || otherSelection || diagnosisGenerator.isAccordionNotEmpty()
                    } else {
                        if (diagnosisViewModel.diagnosisType == MedicalReviewTypeEnums.AboveFiveYears.name) {
                            binding.btnOkay.isEnabled = diagnosisGenerator.isAccordionNotEmpty()
                        }
                    }
                } ?: kotlin.run {
                    binding.btnOkay.isEnabled = otherSelection
                }
            }

            MedicalReviewTypeEnums.ANC.name -> {
                if (diseaseCategoryTagView.getSelectedTags().isNotEmpty() || otherSelection) {
                    binding.btnOkay.isEnabled = true
                } else {
                    binding.btnOkay.isEnabled =
                        diagnosisViewModel.diagnosisDetailsList.value?.data?.let { list ->
                            list.size > 0
                        } ?: false
                }
            }
        }
    }
}