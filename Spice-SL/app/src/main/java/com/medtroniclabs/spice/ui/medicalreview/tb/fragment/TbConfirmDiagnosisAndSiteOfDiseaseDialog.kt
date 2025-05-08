package com.medtroniclabs.spice.ui.medicalreview.tb.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.hideView
import com.medtroniclabs.spice.appextensions.isGone
import com.medtroniclabs.spice.appextensions.isVisible
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.setError
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.DrugSensitiveTB
import com.medtroniclabs.spice.common.DefinedParams.ExtraPulmonary
import com.medtroniclabs.spice.common.DefinedParams.HIV_MEDICAL_REVIEW
import com.medtroniclabs.spice.common.DefinedParams.OrganAffected
import com.medtroniclabs.spice.common.DefinedParams.SiteOfDisease
import com.medtroniclabs.spice.common.DefinedParams.TB
import com.medtroniclabs.spice.data.DiagnosisDiseaseModel
import com.medtroniclabs.spice.data.DiagnosisSaveUpdateRequest
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentPatientStatusDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.DialogDismissListenerForTb
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.TB_ORGAN_AFFECTED
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.TB_SITE_OF_DISEASE
import com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel.TbConfirmDiagnosisAndSiteOfDiseaseViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TbConfirmDiagnosisAndSiteOfDiseaseDialog : DialogFragment(), View.OnClickListener {

    var listener: DialogDismissListenerForTb? = null
    private lateinit var binding: FragmentPatientStatusDialogBinding
    private val diagnosisViewModel: TbConfirmDiagnosisAndSiteOfDiseaseViewModel by activityViewModels()
    private val patientViewModel: PatientDetailViewModel by activityViewModels()
    private val hivViewModel: HivViewModel by activityViewModels()
    private var diseaseConfirmCategoryTagView: TagListCustomView? = null
    private var siteDiseaseCategoryTagView: TagListCustomView? = null
    private var organAffectedTagView: TagListCustomView? = null
    private var hivDiseaseConfirmCategoryTagView: TagListCustomView? = null

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        if (diagnosisViewModel.diagnosisDetailsList.value?.data?.isNotEmpty() == true) {
            patientViewModel.setUserJourney(AnalyticsDefinedParams.EDITDIAGNOSISDIALOGUEFRAGMENT)
        }else{
            patientViewModel.setUserJourney(AnalyticsDefinedParams.ADDDIAGNOSISDIALOGUEFRAGMENT)

        }
    }

    private fun initTag() {
        if (isHiv()) {
            hivDiseaseConfirmCategoryTagView = TagListCustomView(
                binding.root.context, binding.diagnosisChip
            ) { _, _, _ ->
                enableBtnHivFlow()
            }
            binding.tvSiteLbl.gone()
            binding.siteChip.gone()
        } else {
            diseaseConfirmCategoryTagView = TagListCustomView(
                binding.root.context, binding.diagnosisChip
            ) { _, _, _ ->
                enableBtn()
            }

            siteDiseaseCategoryTagView = TagListCustomView(
                binding.root.context, binding.siteChip
            ) { _, _, _ ->
                enableBtn()
            }
        }
        organAffectedTagView = TagListCustomView(
            binding.root.context,
            binding.organChip
        ) { _, _, _ ->
            val selectedTags = organAffectedTagView?.getSelectedTags()
            val hasOther = selectedTags?.any { it.value.equals(DefinedParams.Other, ignoreCase = true) }

            enableBtn()
            if (hasOther != null) {
                showOtherNotes(hasOther)}
            if (!hasOther!! && binding.etOtherDiagnosisNotes.isGone()) {
                setOtherDiagnosisTextSafely("")
            }
        }
    }

    private fun showOtherNotes(isShow: Boolean) {
        binding.etOtherDiagnosisNotes.setVisible(isShow)
    }

    private fun enableBtn() {
        val siteTags = siteDiseaseCategoryTagView?.getSelectedTags()
        val confirmTags = diseaseConfirmCategoryTagView?.getSelectedTags()
        val organTags = organAffectedTagView?.getSelectedTags()

        val hasSiteTags = siteTags?.isNotEmpty()
        val hasConfirmTags = confirmTags?.isNotEmpty()
        val hasOrganTags = organTags?.isNotEmpty()

        val hasDrugSensitiveTB = confirmTags?.any { it.value == DrugSensitiveTB }
        val hasExtraPulmonary = siteTags?.any { it.value == ExtraPulmonary }
        val hasOtherOrgan = organTags?.any { it.value.equals(DefinedParams.Other, true) }
        val otherNotesFilled =
            binding.etOtherDiagnosisNotes.text?.toString()?.trim()?.isNotBlank() == true


        val validOrganSelection = hasOrganTags == true &&
                (!hasOtherOrgan!! || (hasOtherOrgan == true && otherNotesFilled))

        val validDrugSensitiveTB = !hasDrugSensitiveTB!! ||
                (!hasExtraPulmonary!! || (hasExtraPulmonary == true && validOrganSelection))

        if (hasSiteTags == true && hasConfirmTags == true && hasDrugSensitiveTB == true && hasExtraPulmonary == true) {
            if (binding.organChip.isGone()) {
                binding.organChip.visible()
                binding.tvOrganLbl.visible()
                binding.tvOrganError.gone()
            }
        } else {
            if (binding.organChip.isVisible()) {
                binding.organChip.gone()
                binding.tvOrganLbl.gone()
                binding.tvOrganError.gone()
                setOtherDiagnosisTextSafely("")

                binding.etOtherDiagnosisNotes.gone()
                organAffectedTagView?.clearOtherChip()
                organAffectedTagView?.clearSelection()
            }
        }
        binding.btnOkay.isEnabled = hasSiteTags == true && hasConfirmTags == true && validDrugSensitiveTB
    }

    private fun enableBtnHivFlow() {
        val hivDiagnoses = hivDiseaseConfirmCategoryTagView?.getSelectedTags()
        val hasHivDiagnoses = hivDiagnoses?.isNotEmpty()
        if (hasHivDiagnoses == true) {
            binding.tvDiagnosisError.gone()
            binding.btnOkay.isEnabled = true
        } else {
            binding.tvDiagnosisError.visible()
        }
    }

    private fun setOtherDiagnosisTextSafely(text: String) {
        with(binding.etOtherDiagnosisNotes) {
            removeTextChangedListener(defaultOtherDiagnosisWatcher)
            setText(text)
            addTextChangedListener(defaultOtherDiagnosisWatcher)
        }
    }

    private val defaultOtherDiagnosisWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            enableBtn()
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    private fun attachObservers() {
        hivViewModel.hivMetaListItems.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.loader.visible()
                }

                ResourceState.ERROR -> {
                    binding.loader.hideView()
                }

                ResourceState.SUCCESS -> {
                    binding.loader.hideView()
                    resourceState.data?.let { list ->
                        val chipItemList =
                            list.filter { it.type == MedicalReviewTypeEnums.HIV_REVIEW.name }.map {
                                ChipViewItemModel(
                                    id = it.id,
                                    name = it.name,
                                    value = it.value
                                )
                            }
                        hivDiseaseConfirmCategoryTagView?.addChipItemList(chipItemList, null)
                    }
                }
            }
        }

        diagnosisViewModel.diagnosisSaveUpdateResponse.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    binding.loader.visible()
                }

                ResourceState.SUCCESS -> {
                    binding.loader.gone()
                    diagnosisViewModel.diagnosisSaveUpdateResponse.setError()
                    listener?.onDialogDismissedForTb(isPatientType = false)
                    dismiss()
                }

                ResourceState.ERROR -> {
                    binding.loader.gone()
                }
            }
        }
        diagnosisViewModel.diagnosisMetaList.observe(viewLifecycleOwner) { resource ->
            if (resource.state == ResourceState.SUCCESS) {
                if(!isHiv())diagnosisViewModel.getSiteOfDiseaseMetaList(TB_SITE_OF_DISEASE) else diagnosisViewModel.getSiteOfDiseaseMetaList(
                    MedicalReviewTypeEnums.HIV_REVIEW.name
                )
            }
        }
        diagnosisViewModel.organAffectedMetaList.observe(viewLifecycleOwner) { resource ->
            if (resource.state == ResourceState.SUCCESS) {
                val listItems = resource.data ?: return@observe
                val patientId = patientViewModel.patientDetailsLiveData.value?.data?.id
                if (patientId != null) {
                    if (!isHiv()) {
                        diagnosisViewModel.getDiagnosisDetails(
                            CreateUnderTwoMonthsResponse(
                                patientReference = patientId,
                                type = MenuConstants.TB_MENU_ID
                            )
                        )
                    }
                } else {
                    val organMetaChipItems = listItems.map {
                        ChipViewItemModel(it.id, it.name, value = it.value)
                    }

                    val diagnosisMetaChipItems =
                        diagnosisViewModel.diagnosisMetaList.value?.data?.map {
                            ChipViewItemModel(it.id, it.name, value = it.value)
                        } ?: emptyList()
                    val siteMetaChipItems =
                        diagnosisViewModel.siteOfDiseaseMetaList.value?.data?.map {
                            ChipViewItemModel(it.id, it.name, value = it.value)
                        } ?: emptyList()
                    siteDiseaseCategoryTagView?.addChipItemList(siteMetaChipItems, null)
                    organAffectedTagView?.addChipItemList(organMetaChipItems, null)
                    diseaseConfirmCategoryTagView?.addChipItemList(diagnosisMetaChipItems, null)
                }
            }
        }

        diagnosisViewModel.siteOfDiseaseMetaList.observe(viewLifecycleOwner) { resource ->
            if (resource.state == ResourceState.SUCCESS) {
                val patientId = patientViewModel.patientDetailsLiveData.value?.data?.id
                if (patientId != null) {
                  val type = if (isHiv()) HIV_MEDICAL_REVIEW else  MenuConstants.TB_MENU_ID
                    diagnosisViewModel.getDiagnosisDetails(
                        CreateUnderTwoMonthsResponse(
                            patientReference = patientId,
                            type = type
                        )
                    )
                }
                diagnosisViewModel.getOrganAffectedMetaList(TB_ORGAN_AFFECTED)
            }
        }

        diagnosisViewModel.diagnosisDetailsList.observe(viewLifecycleOwner) { resource ->
            binding.loader.visible()
            if (resource.state == ResourceState.SUCCESS) {
                binding.loader.gone()
                val listItems = resource.data ?: return@observe

                val confirmMetaList = diagnosisViewModel.diagnosisMetaList.value?.data?.map {
                    ChipViewItemModel(it.id, it.name, value = it.value)
                } ?: emptyList()

                val siteMetaList = diagnosisViewModel.siteOfDiseaseMetaList.value?.data?.map {
                    ChipViewItemModel(it.id, it.name, value = it.value)
                } ?: emptyList()

                val organMetaList = diagnosisViewModel.organAffectedMetaList.value?.data?.map {
                    ChipViewItemModel(it.id, it.name, value = it.value)
                } ?: emptyList()

                val hivConfirmMetaList = diagnosisViewModel.diagnosisMetaList.value?.data?.map {
                    ChipViewItemModel(it.id, it.name, value = it.type)
                } ?: emptyList()
                val siteGetItem = listItems.filter { it.type == SiteOfDisease }
                val confirmGetItem = listItems.filter {  it.type.isNullOrBlank() || it.type == TB.uppercase() }
                val organGetItem = listItems.filter { it.type == OrganAffected }
                val hivConfirmGetItem = listItems.filter { it.type == HIV_MEDICAL_REVIEW }

                val filteredConfirmList = confirmMetaList.filter { diagnosis ->
                    confirmGetItem.any { it.diseaseCategory == diagnosis.value }
                }

                val filteredSiteList = siteMetaList.filter { diagnosis ->
                    siteGetItem.any { it.diseaseCategory == diagnosis.value }
                }

                val filteredOrganList = organMetaList.filter { diagnosis ->
                    organGetItem.any { it.diseaseCategory == diagnosis.value }
                }

                val filteredHivConfirmGetItemList = hivConfirmMetaList.filter { diagnosis ->
                    hivConfirmGetItem.any { it.diseaseCategory == diagnosis.name}
                }
                siteDiseaseCategoryTagView?.addChipItemList(siteMetaList, filteredSiteList)
                organAffectedTagView?.addChipItemList(organMetaList, filteredOrganList)
                diseaseConfirmCategoryTagView?.addChipItemList(confirmMetaList, filteredConfirmList)
                hivDiseaseConfirmCategoryTagView?.addChipItemList(confirmMetaList, filteredHivConfirmGetItemList)

                if (filteredOrganList.isNotEmpty()) {
                    if (binding.organChip.isGone()) {
                        binding.apply {
                            organChip.visible()
                            tvOrganLbl.visible()
                            tvOrganError.gone()
                        }
                    }
                    val hasOther = filteredOrganList.any { it.value.equals(DefinedParams.Other, ignoreCase = true) }
                    val otherNotesItem = listItems.firstOrNull { it.diseaseCategory.equals(DefinedParams.OtherNotes, ignoreCase = true) }

                    if (hasOther && otherNotesItem?.diseaseCondition?.isNotBlank() == true) {
                        binding.etOtherDiagnosisNotes.setText(otherNotesItem.diseaseCondition)
                    }

                } else {
                    if (binding.organChip.isVisible()) {
                        binding.apply {
                            organChip.gone()
                            tvOrganLbl.gone()
                            tvOrganError.gone()
                            etOtherDiagnosisNotes.setText("")
                            etOtherDiagnosisNotes.gone()
                        }
                    }
                }
            } else {
                binding.loader.gone()
                val siteMetaChipItems = diagnosisViewModel.siteOfDiseaseMetaList.value?.data?.map {
                    ChipViewItemModel(it.id, it.name, value = it.value)
                } ?: emptyList()

                val diagnosisMetaChipItems = diagnosisViewModel.diagnosisMetaList.value?.data?.map {
                    ChipViewItemModel(it.id, it.name, value =  it.value)
                } ?: emptyList()

                val organMetaChipItems = diagnosisViewModel.organAffectedMetaList.value?.data?.map {
                    ChipViewItemModel(it.id, it.name, value = it.value)
                } ?: emptyList()

                siteDiseaseCategoryTagView?.addChipItemList(siteMetaChipItems, null)
                organAffectedTagView?.addChipItemList(organMetaChipItems, null)
                diseaseConfirmCategoryTagView?.addChipItemList(diagnosisMetaChipItems, null)
                hivDiseaseConfirmCategoryTagView?.addChipItemList(diagnosisMetaChipItems, null)
            }
        }
    }

    private fun initView() {

        if (!isHiv()) {diagnosisViewModel.getDiagnosisMetaList(MedicalReviewTypeEnums.TB.name) } else
            diagnosisViewModel.getDiagnosisMetaList(MedicalReviewTypeEnums.HIV_REVIEW.name)

        with(binding) {
            if (isHiv()) {
                tvDiagnosisLbl.visible()
                tvDiagnosisError.gone()
                tvTitle.text = getString(R.string.confirm_diagnoses)
                tvDiagnosisLbl.text = getString(R.string.diagnosis_tb)
                btnOkay.text = getString(R.string.save).uppercase()
                btnCancel.safeClickListener(this@TbConfirmDiagnosisAndSiteOfDiseaseDialog)
                btnOkay.safeClickListener(this@TbConfirmDiagnosisAndSiteOfDiseaseDialog)
                ivClose.safeClickListener(this@TbConfirmDiagnosisAndSiteOfDiseaseDialog)
            } else {
                tvDiagnosisLbl.visible()
                tvSiteError.gone()
                tvSiteLbl.text = getString(R.string.site_of_disease)
                tvSiteLbl.markMandatory()
                tvOrganLbl.markMandatory()
                tvSiteLbl.visible()
                siteChip.visible()
                tvDiagnosisError.gone()
                tvTitle.text = getString(R.string.confirm_diagnoses)
                tvDiagnosisLbl.text = getString(R.string.diagnosis_tb)
                tvDiagnosisLbl.markMandatory()
                btnOkay.text = getString(R.string.save).uppercase()
                btnCancel.safeClickListener(this@TbConfirmDiagnosisAndSiteOfDiseaseDialog)
                btnOkay.safeClickListener(this@TbConfirmDiagnosisAndSiteOfDiseaseDialog)
                ivClose.safeClickListener(this@TbConfirmDiagnosisAndSiteOfDiseaseDialog)
            }

        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnOkay.id -> handleOkayClick()
            binding.btnCancel.id, binding.ivClose.id -> {
                patientViewModel.setUserJourney(AnalyticsDefinedParams.CANCELBUTTONTRIGGERED)
                dismiss()
            }
        }
    }


    private fun handleOkayClick() {
        if (connectivityManager.isNetworkAvailable()) {
            patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
                val type = if (isHiv()) { HIV_MEDICAL_REVIEW } else { MenuConstants.TB_MENU_ID.uppercase() }
                details.patientId?.let { patientId ->
                    val request = DiagnosisSaveUpdateRequest(
                        patientId = patientId,
                        patientReference = details.id,
                        diseases = getDiagnosisDiseaseList().toCollection(arrayListOf()),
                        provenance = ProvanceDto(),
                        type = type,
                        otherNotes = binding.etOtherDiagnosisNotes.text.toString().ifBlank { null }
                    )
                    patientViewModel.setUserJourney(AnalyticsDefinedParams.SAVEBUTTONTRIGGERED)
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
        return if (!isHiv()) {
            val siteDiseases = siteDiseaseCategoryTagView?.getSelectedTags().orEmpty().map {
                DiagnosisDiseaseModel(
                    diseaseCategoryId = it.id ?: -1L,
                    diseaseCategory = it.value ?: "",
                    diseaseConditionId = null,
                    diseaseCondition = null,
                    type = SiteOfDisease
                )
            }

            val confirmDiseases = diseaseConfirmCategoryTagView?.getSelectedTags().orEmpty().map {
                DiagnosisDiseaseModel(
                    diseaseCategoryId = it.id ?: -1L,
                    diseaseCategory = it.value ?: "",
                    diseaseConditionId = null,
                    diseaseCondition = null,
                    type = null
                )
            }

            val organDiseases = organAffectedTagView?.getSelectedTags().orEmpty().map {
                DiagnosisDiseaseModel(
                    diseaseCategoryId = it.id ?: -1L,
                    diseaseCategory = it.value ?: "",
                    diseaseConditionId = null,
                    diseaseCondition = null,
                    type = OrganAffected
                )
            }
            siteDiseases + confirmDiseases + organDiseases
        } else {
            hivDiseaseConfirmCategoryTagView?.getSelectedTags().orEmpty().map {
                DiagnosisDiseaseModel(
                    diseaseCategoryId = it.id ?: -1L,
                    diseaseCategory = it.name,
                    diseaseConditionId = it.id ?: -1L,
                    diseaseCondition = it.name,
                    type = HIV_MEDICAL_REVIEW
                )
            }
        }
    }

    private fun handleDialogSize() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val width = if (CommonUtils.checkIsTablet(requireContext())) {
            if (isLandscape) 65 else 90
        } else {
            if (isLandscape) 65 else 90
        }
        setDialogPercent(width)
    }

    override fun onStart() {
        super.onStart()
        handleDialogSize()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleDialogSize()
    }

   private fun isHiv(): Boolean {
        return arguments?.getBoolean(MedicalReviewTypeEnums.HIV.name, false) == true
    }
}