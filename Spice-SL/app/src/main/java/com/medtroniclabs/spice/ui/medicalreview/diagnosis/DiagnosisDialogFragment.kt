package com.medtroniclabs.spice.ui.medicalreview.diagnosis

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.appextensions.setError
import com.medtroniclabs.spice.appextensions.setWidth
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.data.DiagnosisDiseaseModel
import com.medtroniclabs.spice.data.DiagnosisSaveUpdateRequest
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentDiagnosisDialogBinding
import com.medtroniclabs.spice.formgeneration.DiagnosisGenerator
import com.medtroniclabs.spice.formgeneration.DiagnosisListener
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.viewmodel.DiagnosisViewModel
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
                                        patientReference = id
                                    )
                                )
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
                                // diagnosisMetaList is to show selected accordions
                                val diagnosisMetaList = ArrayList<DiseaseCategoryItems>()
                                for (diagnosis in diagnosisList) {
                                    if (listItems.any { it.diseaseCategory == diagnosis.name }) {
                                        diagnosisMetaList.add(diagnosis)
                                    }
                                }
                                //diagnosisMetaChipItemList is to render chips in disease category
                                val diagnosisMetaChipItemList = ArrayList<ChipViewItemModel>()
                                diagnosisList.forEach {
                                    diagnosisMetaChipItemList.add(
                                        ChipViewItemModel(
                                            id = it.id, name = it.name
                                        )
                                    )
                                }
                                //selectedDiagnosisMetaChipItemList is to select chips in disease category
                                val selectedDiagnosisMetaChipItemList =
                                    ArrayList<ChipViewItemModel>()
                                diagnosisMetaList.forEach {
                                    selectedDiagnosisMetaChipItemList.add(
                                        ChipViewItemModel(
                                            id = it.id, name = it.name
                                        )
                                    )
                                }
                                //chipItemList is to select chips inside accordion of respective group
                                val chipItemList = ArrayList<ChipViewItemModel>()
                                listItems.forEach {
                                    chipItemList.add(
                                        ChipViewItemModel(
                                            id = it.diseaseConditionId, name = it.diseaseCondition
                                        )
                                    )
                                }

                                diseaseCategoryTagView.addChipItemList(
                                    diagnosisMetaChipItemList,
                                    selectedDiagnosisMetaChipItemList
                                )
                                diagnosisGenerator.populateDiagnosisView(
                                    diagnosisMetaList,
                                    chipItemList
                                )
                            }
                        }
                    } ?: kotlin.run {
                        diagnosisViewModel.diagnosisMetaList.value?.data?.let { listItems ->
                            val chipItemList = ArrayList<ChipViewItemModel>()
                            listItems.forEach {
                                chipItemList.add(
                                    ChipViewItemModel(
                                        id = it.id, name = it.name
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
                    patientViewModel.patientDetailsLiveData.value?.data?.let {
                        diagnosisViewModel.getDiagnosisDetails(
                            CreateUnderTwoMonthsResponse(
                                patientReference = it.id
                            )
                        )
                    }
                    diagnosisViewModel.diagnosisDetailsList.setError()
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

    private fun initView() {
        diagnosisGenerator = DiagnosisGenerator(binding.root.context, binding.llFamilyRoot, this)
        diseaseCategoryTagView = TagListCustomView(
            binding.root.context, binding.diseaseConditionChipGroup
        ) { name, _, isChecked ->
            if (!diagnosisViewModel.viewDiagnosis) {
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
        }
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
                        details.id?.let {
                            val request = DiagnosisSaveUpdateRequest(
                                patientId = patientId,
                                patientReferance = it,
                                diseases = getDiagnosisDiseaseList(),
                                provenance = ProvanceDto(
                                    createdDateTime = DateUtils.getCurrentDateAndTime(
                                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                                    )
                                )
                            )
                            diagnosisViewModel.diagnosisCreate(request)
                        }
                    }
                }
            }
        }
    }

    private fun getDiagnosisDiseaseList(): ArrayList<DiagnosisDiseaseModel> {
        val diagnosisList = ArrayList<DiagnosisDiseaseModel>()
        diagnosisGenerator.getSelectedTagsForAccordions().let {
            for ((diseaseCategoryId, chipViewItemList) in it) {
                for (chipViewItem in chipViewItemList) {
                    val diagnosisDiseaseModel = DiagnosisDiseaseModel(
                        diseaseCategoryId = getDiseaseCategoryId(diseaseCategoryId),
                        diseaseConditionId = chipViewItem.id ?: -1,
                        diseaseCategory = diseaseCategoryId,
                        diseaseCondition = chipViewItem.name
                    )
                    diagnosisList.add(diagnosisDiseaseModel)
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

    override fun onDialogueItemCheckListener(
        id: String,
        formLayout: FormLayout,
        resultMap: Any?,
        diseaseName: String
    ) {

    }

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }


}