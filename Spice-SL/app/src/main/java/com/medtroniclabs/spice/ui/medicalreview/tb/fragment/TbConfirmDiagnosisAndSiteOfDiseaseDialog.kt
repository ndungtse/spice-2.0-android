package com.medtroniclabs.spice.ui.medicalreview.tb.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setError
import com.medtroniclabs.spice.appextensions.setWidth
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.DiagnosisDiseaseModel
import com.medtroniclabs.spice.data.DiagnosisSaveUpdateRequest
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentPatientStatusDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.viewmodel.DiagnosisViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.DialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.TB_SITE_OF_DISEASE
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TbConfirmDiagnosisAndSiteOfDiseaseDialog : DialogFragment(), View.OnClickListener {

    var listener: DialogDismissListener? = null
    private lateinit var binding: FragmentPatientStatusDialogBinding
    private val diagnosisViewModel: DiagnosisViewModel by activityViewModels()
    private val patientViewModel: PatientDetailViewModel by activityViewModels()
    private lateinit var diseaseConfirmCategoryTagView: TagListCustomView
    private lateinit var siteDiseaseCategoryTagView: TagListCustomView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPatientStatusDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    companion object {
        const val TAG = "TbConfirmDiagnosisAndSiteOfDiseaseDialog"
        fun newInstance() =
            TbConfirmDiagnosisAndSiteOfDiseaseDialog()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
        initTag()
    }

    private fun initTag() {
        diseaseConfirmCategoryTagView = TagListCustomView(
            binding.root.context, binding.diagnosisChip
        ) { name, _, isChecked ->
            enableBtn()
        }

        siteDiseaseCategoryTagView = TagListCustomView(
            binding.root.context, binding.siteChip
        ) { name, _, isChecked ->
            enableBtn()
        }
    }

    private fun enableBtn() {
        binding.btnOkay.isEnabled = siteDiseaseCategoryTagView.getSelectedTags()
            .isNotEmpty() && diseaseConfirmCategoryTagView.getSelectedTags().isNotEmpty()
    }

    private fun getDiagnosisDetails(id: String) {
        diagnosisViewModel.getDiagnosisDetails(
            CreateUnderTwoMonthsResponse(
                patientReference = id,
                type = MenuConstants.TB_MENU_ID
            )
        )
    }

    private fun attachObservers() {
        diagnosisViewModel.diagnosisSaveUpdateResponse.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                }

                ResourceState.SUCCESS -> {
                    diagnosisViewModel.viewDiagnosis = true
                    patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
                        details.id?.let { id ->
                            getDiagnosisDetails(id)
                        } ?: kotlin.run {
                            details.patientId?.let { patientViewModel.getPatients(it) }
                        }
                    }
                    diagnosisViewModel.diagnosisSaveUpdateResponse.setError()
                    dismiss()
                }

                ResourceState.ERROR -> {
                }
            }
        }
        diagnosisViewModel.diagnosisMetaList.observe(viewLifecycleOwner) { resource ->
            if (resource.state == ResourceState.SUCCESS) {
                diagnosisViewModel.getSiteOfDiseaseMetaList(TB_SITE_OF_DISEASE)
            }
        }

        diagnosisViewModel.siteOfDiseaseMetaList.observe(viewLifecycleOwner) { resource ->
            if (resource.state == ResourceState.SUCCESS) {
                val listItems = resource.data ?: return@observe

                val patientId = patientViewModel.patientDetailsLiveData.value?.data?.id
                if (patientId != null) {
                    diagnosisViewModel.getDiagnosisDetails(
                        CreateUnderTwoMonthsResponse(
                            patientReference = patientId,
                            type = MenuConstants.TB_MENU_ID
                        )
                    )
                } else {
                    val siteMetaChipItems = listItems.map {
                        ChipViewItemModel(it.id, it.name, value = it.value)
                    }

                    val diagnosisMetaChipItems =
                        diagnosisViewModel.diagnosisMetaList.value?.data?.map {
                            ChipViewItemModel(it.id, it.name, value = it.value)
                        } ?: emptyList()

                    siteDiseaseCategoryTagView.addChipItemList(siteMetaChipItems, null)
                    diseaseConfirmCategoryTagView.addChipItemList(diagnosisMetaChipItems, null)
                }
            }
        }

        diagnosisViewModel.diagnosisDetailsList.observe(viewLifecycleOwner) { resource ->
            if (resource.state == ResourceState.SUCCESS) {
                val listItems = resource.data ?: return@observe

                val confirmMetaList = diagnosisViewModel.diagnosisMetaList.value?.data?.map {
                    ChipViewItemModel(it.id, it.name, value = it.value)
                } ?: emptyList()

                val siteMetaList = diagnosisViewModel.siteOfDiseaseMetaList.value?.data?.map {
                    ChipViewItemModel(it.id, it.name, value = it.value)
                } ?: emptyList()

                val (siteGetItem, confirmGetItem) = listItems.partition { it.siteOfDisease }

                val filteredConfirmList = confirmMetaList.filter { diagnosis ->
                    confirmGetItem.any { it.diseaseCategory == diagnosis.value }
                }

                val filteredSiteList = siteMetaList.filter { diagnosis ->
                    siteGetItem.any { it.diseaseCategory == diagnosis.value }
                }

                siteDiseaseCategoryTagView.addChipItemList(siteMetaList, filteredSiteList)
                diseaseConfirmCategoryTagView.addChipItemList(confirmMetaList, filteredConfirmList)
            } else {
                val siteMetaChipItems = diagnosisViewModel.siteOfDiseaseMetaList.value?.data?.map {
                    ChipViewItemModel(it.id, it.name, value = it.value)
                } ?: emptyList()

                val diagnosisMetaChipItems = diagnosisViewModel.diagnosisMetaList.value?.data?.map {
                    ChipViewItemModel(it.id, it.name, value =  it.value)
                } ?: emptyList()

                siteDiseaseCategoryTagView.addChipItemList(siteMetaChipItems, null)
                diseaseConfirmCategoryTagView.addChipItemList(diagnosisMetaChipItems, null)
            }
        }
    }

    private fun initView() {
        diagnosisViewModel.getDiagnosisMetaList(diagnosisViewModel.diagnosisType)
        with(binding) {
            tvDiagnosisLbl.visible()
            tvSiteError.gone()
            tvSiteLbl.text = getString(R.string.site_of_disease)
            tvSiteLbl.visible()
            siteChip.visible()
            tvDiagnosisError.gone()
            tvTitle.text = getString(R.string.confirm_diagnoses)
            tvDiagnosisLbl.text = getString(R.string.diagnosis)
            btnOkay.text = getString(R.string.save).uppercase()
            btnCancel.safeClickListener(this@TbConfirmDiagnosisAndSiteOfDiseaseDialog)
            btnOkay.safeClickListener(this@TbConfirmDiagnosisAndSiteOfDiseaseDialog)
            ivClose.safeClickListener(this@TbConfirmDiagnosisAndSiteOfDiseaseDialog)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnOkay.id -> handleOkayClick()
            binding.btnCancel.id, binding.ivClose.id -> dismiss()
        }
    }

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    private fun handleOkayClick() {
        if (connectivityManager.isNetworkAvailable()) {
            patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
                details.patientId?.let { patientId ->
                    val request = DiagnosisSaveUpdateRequest(
                        patientId = patientId,
                        patientReference = details.id,
                        diseases = getDiagnosisDiseaseList().toCollection(arrayListOf()),
                        provenance = ProvanceDto(),
                        type = MenuConstants.TB_MENU_ID.uppercase()
                    )
                    diagnosisViewModel.diagnosisCreate(request)
                }
            }
        } else {
            (activity as? BaseActivity)?.showErrorDialogue(
                getString(R.string.error),
                getString(R.string.no_internet_error),
                isNegativeButtonNeed = false
            ) {

            }
        }
    }

    private fun getDiagnosisDiseaseList(): List<DiagnosisDiseaseModel> {
        return (siteDiseaseCategoryTagView.getSelectedTags().map {
            DiagnosisDiseaseModel(
                diseaseCategoryId = it.id ?: -1L,
                diseaseCategory = it.value ?: "",
                diseaseConditionId = null,
                diseaseCondition = null,
                siteOfDisease = true // Set true for siteDiseaseCategoryTagView
            )
        } + diseaseConfirmCategoryTagView.getSelectedTags().map {
            DiagnosisDiseaseModel(
                diseaseCategoryId = it.id ?: -1L,
                diseaseCategory = it.value ?: "",
                diseaseConditionId = null,
                diseaseCondition = null,
                siteOfDisease = false // Set false for diseaseConfirmCategoryTagView
            )
        })
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

    override fun onStart() {
        super.onStart()
        handleDialogSize()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleDialogSize()
    }
}