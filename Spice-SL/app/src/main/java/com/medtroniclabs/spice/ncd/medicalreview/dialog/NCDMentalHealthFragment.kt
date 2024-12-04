package com.medtroniclabs.spice.ncd.medicalreview.dialog

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.isVisible
import com.medtroniclabs.spice.appextensions.loadAsGif
import com.medtroniclabs.spice.appextensions.resetImageView
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.model.MultiSelectDropDownModel
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentNCDMentalHealthBinding
import com.medtroniclabs.spice.db.entity.NCDDiagnosisEntity
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.formgeneration.utility.MultiSelectSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.data.MentalHealthStatus
import com.medtroniclabs.spice.ncd.data.NCDMentalHealthStatusRequest
import com.medtroniclabs.spice.ncd.data.NcdPatientStatus
import com.medtroniclabs.spice.ncd.medicalreview.NCDDialogDismissListener
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMentalHealthViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDMentalHealthFragment : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentNCDMentalHealthBinding

    private val viewModel: NCDMentalHealthViewModel by activityViewModels()
    private val medicalReviewViewModel: NCDMedicalReviewViewModel by activityViewModels()

    val adapter by lazy { CustomSpinnerAdapter(requireContext()) }
    var listener: NCDDialogDismissListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNCDMentalHealthBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
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
        val width = when {
            isTablet && isLandscape -> 50
            else -> 100
        }
        val height = when {
            isTablet && isLandscape -> 95
            else -> 100
        }
        setDialogPercent(width, height)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
        initializeMentalHealthSpinner()
        initializeSubstanceSpinner()
        setListeners()
    }

    private fun setListeners() {
        binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?, view: View?, pos: Int, itemId: Long
                ) {
                    adapter.getData(pos)?.let {
                        val selectedId = (it[DefinedParams.id] as? Long) ?: -1L
                        val selectedName = it[DefinedParams.NAME] as String?
                        val value = it[DefinedParams.Value] as String?
                        if (selectedId != -1L) {
                            viewModel.value = value
                        } else {
                            viewModel.value = null
                        }
                        medicalReviewViewModel.statusDiabetesValue = viewModel.value
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun attachObserver() {
        viewModel.getSymptomListByTypeForNCDLiveData.observe(viewLifecycleOwner) {
            medicalReviewViewModel.validationForStatus = it
            loadSiteDetails(ArrayList(it))
        }
        viewModel.createMentalHealthStatus.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    dismiss()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun initializeMentalHealthSpinner() {
        val dropDownList = ArrayList<MultiSelectDropDownModel>()
        dropDownList.add(
            MultiSelectDropDownModel(
                id = 1,
                name = NCDMRUtil.Anxiety,
                value = NCDMRUtil.Anxiety.lowercase()
            )
        )
        dropDownList.add(
            MultiSelectDropDownModel(
                id = 2,
                name = NCDMRUtil.Disorder,
                value = NCDMRUtil.Disorder.lowercase()
            )
        )
        val adapter = MultiSelectSpinnerAdapter(
            requireContext(),
            dropDownList,
            viewModel.selectedMentalHealthListItem
        )
        binding.etMentalHealthDisorder.adapter = adapter
        adapter.setOnItemSelectedListener(object :
            MultiSelectSpinnerAdapter.OnItemSelectedListener {
            override fun onItemSelected(
                selectedItems: List<MultiSelectDropDownModel>,
                pos: Int,
            ) {
                if (selectedItems.isNotEmpty()) {
                    viewModel.selectedMentalHealthListItem = ArrayList(selectedItems)
                }
            }
        }
        )
    }

    private fun initializeSubstanceSpinner() {
        val dropDownList = ArrayList<MultiSelectDropDownModel>()
        dropDownList.add(
            MultiSelectDropDownModel(
                id = 1,
                name = NCDMRUtil.ALCOHOL,
                value = NCDMRUtil.ALCOHOL.lowercase()
            )
        )
        dropDownList.add(
            MultiSelectDropDownModel(
                id = 2,
                name = NCDMRUtil.Tobbaco,
                value = NCDMRUtil.Tobbaco.lowercase()
            )
        )
        val adapter = MultiSelectSpinnerAdapter(
            requireContext(),
            dropDownList,
            viewModel.selectedSubstanceListItem
        )
        binding.etSubstanceDisorder.adapter = adapter
        adapter.setOnItemSelectedListener(object :
            MultiSelectSpinnerAdapter.OnItemSelectedListener {
            override fun onItemSelected(
                selectedItems: List<MultiSelectDropDownModel>,
                pos: Int,
            ) {
                if (selectedItems.isNotEmpty()) {
                    viewModel.selectedSubstanceListItem = ArrayList(selectedItems)
                }
            }
        }
        )
    }

    private fun loadSiteDetails(data: ArrayList<NCDDiagnosisEntity>) {
        val list = arrayListOf<Map<String, Any>>(
            hashMapOf(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.ID to DefinedParams.DefaultSelectID
            )
        )

        data.mapNotNullTo(list) { symptoms ->
            hashMapOf<String, Any>().apply {
                symptoms.id.let { put(DefinedParams.ID, it) }
                symptoms.name.let { put(DefinedParams.NAME, it) }
                symptoms.value?.let { put(DefinedParams.Value, it) }
            }.takeIf { it.isNotEmpty() }
        }
        adapter.setData(list)
        binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.post {
            binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.setSelection(0, false)
        }
        binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.adapter = adapter
    }

    private fun getSingleSelectionOptions(): ArrayList<Map<String, Any>> {
        val yearOfDiagnosis = ArrayList<Map<String, Any>>()
        yearOfDiagnosis.add(
            CommonUtils.getOptionMap(
                NCDPregnancyDialog.N_A, getString(R.string.n_a)
            )
        )
        yearOfDiagnosis.add(
            CommonUtils.getOptionMap(
                NCDPregnancyDialog.NEW_PATIENT, getString(R.string.new_patient)
            )
        )
        yearOfDiagnosis.add(
            CommonUtils.getOptionMap(
                NCDPregnancyDialog.KNOWN_PATIENT, getString(R.string.known_patient)
            )
        )
        return yearOfDiagnosis
    }

    private var singleSelectionCallbackForDiabetes: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultDiabetesHashMap[NCDPregnancyDialog.DIABETES] = selectedID as String
            showViews(
                binding.ncdDiabetesHypertension.groupYearOfDiagnosis,
                selectedID,
                binding.ncdDiabetesHypertension.tvYearOfDiagnosisError,
                binding.ncdDiabetesHypertension.etYearOfDiagnosis
            )
            showSpinnerView(selectedID)
        }

    private var singleSelectionCallbackForSubstanceUse: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultSubstanceUseHashMap[SUBSTANCE_USE_STATUS] =
                selectedID as String
            showViewsMentalHealth(
                binding.groupSubstanceUse,
                selectedID,
                binding.tvDiagnosisError,
                binding.etSubstanceDiagnosis,
                binding.tvSubstanceCommentsError,
                binding.etSubstanceComments
            )
            showSpinnerViewSubstanceUse(
                selectedID,
                binding.groupSubstanceUseSpinner,
                binding.etSubstanceDiagnosis,
                binding.etSubstanceComments,
                binding.etSubstanceDisorder,
                binding.tvSubstanceDisorderError
            )
        }

    private var singleSelectionCallbackForMentalHealth: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultMentalHealthHashMap[MENTAL_HEALTH_STATUS] =
                selectedID as String
            showViewsMentalHealth(
                binding.groupMentalHealth,
                selectedID,
                binding.tvYrOfDiagnosisError,
                binding.etYrOfDiagnosis,
                binding.tvMentalHealthDisorderError,
                binding.etComments
            )
            showSpinnerViewMentalHealth(
                selectedID,
                binding.groupMentalHealthSpinner,
                binding.etYrOfDiagnosis,
                binding.etComments,
                binding.etMentalHealthDisorder,
                binding.tvMentalHealthDisorderError
            )
        }

    private fun showSpinnerView(selectedValue: String) {
        val isKnownPatient =
            selectedValue.equals(Known_patient, ignoreCase = true)
        with(binding.ncdDiabetesHypertension) {
            groupDiabetesSpinner.isVisible = isKnownPatient
            if (!isKnownPatient) {
                etYearOfDiagnosis.setText(getString(R.string.empty))
                viewModel.value = null
                tvDiabetesControlledSpinner.post {
                    tvDiabetesControlledSpinner.setSelection(0, false)
                }
            }
            tvDiabetesControlledError.gone()
        }
    }

    private fun showSpinnerViewMentalHealth(
        selectedValue: String,
        groupSubstanceUseSpinner: Group,
        etSubstanceDiagnosis: AppCompatEditText,
        etSubstanceComments: AppCompatEditText,
        etSubstanceDisorder: AppCompatSpinner,
        tvSubstanceDisorderError: AppCompatTextView
    ) {
        val isKnownPatient =
            selectedValue.equals(Known_patient, ignoreCase = true)
        groupSubstanceUseSpinner.isVisible = isKnownPatient
        if (!isKnownPatient) {
            etSubstanceDiagnosis.setText(getString(R.string.empty))
            etSubstanceComments.setText(getString(R.string.empty))
            viewModel.selectedMentalHealthListItem.clear()
            etSubstanceDisorder.post {
                etSubstanceDisorder.setSelection(0, false)
            }
        }
        tvSubstanceDisorderError.gone()
    }

    private fun showSpinnerViewSubstanceUse(
        selectedValue: String,
        groupSubstanceUseSpinner: Group,
        etSubstanceDiagnosis: AppCompatEditText,
        etSubstanceComments: AppCompatEditText,
        etSubstanceDisorder: AppCompatSpinner,
        tvSubstanceDisorderError: AppCompatTextView
    ) {
        val isKnownPatient =
            selectedValue.equals(Known_patient, ignoreCase = true)
        groupSubstanceUseSpinner.isVisible = isKnownPatient
        if (!isKnownPatient) {
            etSubstanceDiagnosis.setText(getString(R.string.empty))
            etSubstanceComments.setText(getString(R.string.empty))
            viewModel.selectedSubstanceListItem.clear()
            etSubstanceDisorder.post {
                etSubstanceDisorder.setSelection(0, false)
            }
        }
        tvSubstanceDisorderError.gone()
    }

    private fun showViews(
        groupYearOfDiagnosis: Group,
        selectedValue: String,
        tvYearOfDiagnosis: AppCompatTextView,
        etYearOfDiagnosis: AppCompatEditText
    ) {
        if (selectedValue.equals(Known_patient, true)) {
            groupYearOfDiagnosis.isVisible = true
            etYearOfDiagnosis.text = null
        } else {
            groupYearOfDiagnosis.isVisible = false
            etYearOfDiagnosis.text = null
        }
        tvYearOfDiagnosis.gone()
    }

    private fun showViewsMentalHealth(
        groupYearOfDiagnosis: Group,
        selectedValue: String,
        tvYearOfDiagnosis: AppCompatTextView,
        etYearOfDiagnosis: AppCompatEditText,
        tvSubstanceCommentsError: AppCompatTextView,
        etSubstanceComments: AppCompatEditText
    ) {
        if (selectedValue.equals(Known_patient, true)) {
            groupYearOfDiagnosis.isVisible = true
            etYearOfDiagnosis.text = null
            etSubstanceComments.text = null
        } else {
            groupYearOfDiagnosis.isVisible = false
            etYearOfDiagnosis.text = null
            etSubstanceComments.text = null
        }
        tvYearOfDiagnosis.gone()
        tvSubstanceCommentsError.gone()
    }

    private fun initView() {
        ncdVisibility()
        binding.apply {
            tvMentalHealth.markMandatory()
            tvMentalHealthDisorder.markMandatory()
            tvComments.markMandatory()
            tvYrOfDiagnosis.markMandatory()
            tvSubstanceUse.markMandatory()
            tvSubstanceDiagnosis.markMandatory()
            tvSubstanceDisorder.markMandatory()
            tvSubstanceComments.markMandatory()
            btnCancel.safeClickListener(this@NCDMentalHealthFragment)
            btnConfirm.safeClickListener(this@NCDMentalHealthFragment)
            ivClose.safeClickListener(this@NCDMentalHealthFragment)
            MotherNeonateUtil.initTextWatcherForString(ncdDiabetesHypertension.etYearOfDiagnosis) {
                viewModel.yearForDiabetes = it
            }
            MotherNeonateUtil.initTextWatcherForString(ncdDiabetesHypertension.etYearOfDiagnosisHtn) {
                viewModel.yearForHypertension = it
            }
            MotherNeonateUtil.initTextWatcherForString(etComments) {
                viewModel.mentalHealthComments = it
            }
            MotherNeonateUtil.initTextWatcherForString(etSubstanceComments) {
                viewModel.substanceUseComments = it
            }
            MotherNeonateUtil.initTextWatcherForInt(etYrOfDiagnosis) {
                viewModel.yearForMentalHealth = it
            }
            MotherNeonateUtil.initTextWatcherForInt(etSubstanceDiagnosis) {
                viewModel.yearForSubstanceUse = it
            }
        }
        binding.ncdDiabetesHypertension.apply {
            tvDiabetes.markMandatory()
            tvYearOfDiagnosis.markMandatory()
            tvDiabetesControlledTypeLabel.markMandatory()
            MotherNeonateUtil.initTextWatcherForString(etYearOfDiagnosis) {
                viewModel.yearForDiabetes = it
            }

            tvHypertension.markMandatory()
            tvYearOfDiagnosisHtn.markMandatory()
            MotherNeonateUtil.initTextWatcherForString(etYearOfDiagnosisHtn) {
                viewModel.yearForHypertension = it
            }
        }
        viewModel.getSymptoms(
            Diabetes.lowercase(),
            getGender(),
            isPregnant()
        )

        getSingleSelectionOptions().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = NCDPregnancyDialog.DIABETES
            view.addViewElements(
                it,
                false,
                viewModel.resultDiabetesHashMap,
                Pair(NCDPregnancyDialog.DIABETES, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForDiabetes
            )
            binding.ncdDiabetesHypertension.llDiabetes.addView(view)
        }

        getSingleSelectionOptions().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = NCDPregnancyDialog.HYPERTENSION
            view.addViewElements(
                it,
                false,
                viewModel.resultHypertensionHashMap,
                Pair(NCDPregnancyDialog.HYPERTENSION, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForHypertension
            )
            binding.ncdDiabetesHypertension.llHypertension.addView(view)
        }

        getSingleSelectionOptions().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = MENTAL_HEALTH_STATUS
            view.addViewElements(
                it,
                false,
                viewModel.resultMentalHealthHashMap,
                Pair(MENTAL_HEALTH_STATUS, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForMentalHealth
            )
            binding.llMentalHealth.addView(view)
        }

        getSingleSelectionOptions().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = SUBSTANCE_USE_STATUS
            view.addViewElements(
                it,
                false,
                viewModel.resultSubstanceUseHashMap,
                Pair(SUBSTANCE_USE_STATUS, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForSubstanceUse
            )
            binding.llSubstanceUse.addView(view)
        }
        binding.loadingProgress.safeClickListener {

        }
        binding.loadingProgress.bringToFront()
    }

    private fun ncdVisibility() {
        var showNCD = true
        medicalReviewViewModel.ncdPatientDiagnosisStatus.value?.data?.let { responseMap ->
            showNCD =
                !(responseMap.containsKey(NCDMRUtil.NCDPatientStatus) && responseMap[NCDMRUtil.NCDPatientStatus] != null)
        }
        binding.apply {
            tvNCD.setVisible(showNCD)
            ncdDiabetesHypertension.root.setVisible(showNCD)
        }
    }

    private fun getGender(): String {
        return if (arguments?.getBoolean(NCDMRUtil.IS_FEMALE) == true) {
            Screening.Female.lowercase()
        } else {
            Screening.Male.lowercase()
        }
    }

    private fun isPregnant(): Boolean {
        return arguments?.getBoolean(NCDMRUtil.IsPregnant) ?: false
    }

    private var singleSelectionCallbackForHypertension: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultHypertensionHashMap[NCDPregnancyDialog.HYPERTENSION] =
                selectedID as String
            showViews(
                binding.ncdDiabetesHypertension.groupYearOfDiagnosis2,
                selectedID,
                binding.ncdDiabetesHypertension.tvYearOfDiagnosisErrorHtn,
                binding.ncdDiabetesHypertension.etYearOfDiagnosisHtn
            )
        }

    companion object {
        const val TAG = "NCDMentalHealthFragment"
        const val Diabetes = "Diabetes"
        const val Hypertension = "Hypertension"
        const val Known_patient = "Known Patient"

        const val MENTAL_HEALTH_STATUS = "MentalHealthStatus"
        const val SUBSTANCE_USE_STATUS = "SubstanceUseStatus"
        fun newInstance(
            patientReference: String?,
            memberReference: String?,
            isFemale: Boolean,
            isPregnant: Boolean
        ): NCDMentalHealthFragment {
            return NCDMentalHealthFragment().apply {
                arguments = Bundle().apply {
                    putString(NCDMRUtil.PATIENT_REFERENCE, patientReference)
                    putString(NCDMRUtil.MEMBER_REFERENCE, memberReference)
                    putBoolean(NCDMRUtil.IS_FEMALE, isFemale)
                    putBoolean(NCDMRUtil.IsPregnant, isPregnant)
                }
            }
        }
    }

    fun validateInput(): Boolean {
        return if (binding.tvNCD.isVisible()) {
            validateNCDPatientStatus() && validateMentalHealthAndSubstance()
        } else {
            validateMentalHealthAndSubstance()
        }
    }

    private fun validateMentalHealthAndSubstance(): Boolean {
        val isMentalHealthValid = viewModel.resultMentalHealthHashMap.isNotEmpty()
        val isSubstanceUseValid = viewModel.resultSubstanceUseHashMap.isNotEmpty()
        val isMentalHealthValueValid = viewModel.selectedMentalHealthListItem.isNotEmpty()
        val isSubstanceUseValueValid = viewModel.selectedSubstanceListItem.isNotEmpty()
        val isCommentsValidMentalHealth = viewModel.mentalHealthComments?.isNotEmpty()
        val isCommentsValidSubstanceUse = viewModel.substanceUseComments?.isNotEmpty()

        if (isMentalHealthValid) {
            binding.tvMentalHealthError.gone()
        } else {
            binding.tvMentalHealthError.visible()
        }

        if (isSubstanceUseValid) {
            binding.tvSubstanceUseError.gone()
        } else {
            binding.tvSubstanceUseError.visible()
        }

        if (isMentalHealthValid && isCommentsValidMentalHealth == true) {
            binding.tvCommentsError.gone()
        } else {
            binding.tvCommentsError.visible()
        }

        if (isSubstanceUseValid && isCommentsValidSubstanceUse == true) {
            binding.tvDiagnosisError.gone()
        } else {
            binding.tvDiagnosisError.visible()
        }

        val isKnownMentalHealthPatient =
            (viewModel.resultMentalHealthHashMap[MENTAL_HEALTH_STATUS] as? String)?.equals(
                Known_patient,
                true
            ) == true

        val isKnownSubstanceUsePatient =
            (viewModel.resultSubstanceUseHashMap[SUBSTANCE_USE_STATUS] as? String)?.equals(
                Known_patient,
                true
            ) == true

        if (isKnownMentalHealthPatient && isMentalHealthValueValid) {
            binding.tvMentalHealthDisorderError.gone()
        } else {
            if (isKnownMentalHealthPatient) {
                binding.tvMentalHealthDisorderError.visible()
            }
        }

        if (isKnownSubstanceUsePatient && isSubstanceUseValueValid) {
            binding.tvSubstanceDisorderError.gone()
        } else {
            if (isKnownSubstanceUsePatient) {
                binding.tvSubstanceDisorderError.visible()
            }
        }

        val knownPatientValidForMentalHealth =
            (!isKnownMentalHealthPatient || (isValidDiagnosis(
                binding.etYrOfDiagnosis,
                binding.tvYrOfDiagnosisError
            ) && isMentalHealthValueValid))

        val knownPatientValidForSubstanceUse =
            (!isKnownSubstanceUsePatient || (isValidDiagnosis(
                binding.etSubstanceDiagnosis,
                binding.tvDiagnosisError
            ) && isSubstanceUseValueValid))

        return isMentalHealthValid && isSubstanceUseValid && knownPatientValidForMentalHealth && knownPatientValidForSubstanceUse
    }

    private fun validateNCDPatientStatus(): Boolean {
        val isDiabetesValid = viewModel.resultDiabetesHashMap.isNotEmpty()
        val isHypertensionValid = viewModel.resultHypertensionHashMap.isNotEmpty()
        val isValueValid = !viewModel.value.isNullOrBlank()

        binding.ncdDiabetesHypertension.tvDiabetesError.setVisible(!isDiabetesValid)
        binding.ncdDiabetesHypertension.tvHypertensionError.setVisible(!isHypertensionValid)

        val isKnownDiabetesPatient =
            (viewModel.resultDiabetesHashMap[NCDPatientHistoryDialog.Diabetes] as? String)?.equals(
                NCDPatientHistoryDialog.Known_patient,
                true
            ) == true

        if (isKnownDiabetesPatient && isValueValid) {
            binding.ncdDiabetesHypertension.tvDiabetesControlledError.gone()
        } else {
            if (isKnownDiabetesPatient) {
                binding.ncdDiabetesHypertension.tvDiabetesControlledError.visible()
            }
        }

        val isKnownHypertensionPatient =
            (viewModel.resultHypertensionHashMap[NCDPatientHistoryDialog.Hypertension] as? String)?.equals(
                NCDPatientHistoryDialog.Known_patient,
                true
            ) == true

        val knownPatientValidForDiabetes =
            (!isKnownDiabetesPatient || (isValidDiagnosis(
                binding.ncdDiabetesHypertension.etYearOfDiagnosis,
                binding.ncdDiabetesHypertension.tvYearOfDiagnosisError
            ) && isValueValid))
        val knownPatientValidForHypertension =
            (!isKnownHypertensionPatient || isValidDiagnosisTwo())
        return isDiabetesValid && isHypertensionValid && knownPatientValidForDiabetes
                && knownPatientValidForHypertension
    }

    private fun isValidDiagnosis(
        etYearOfDiagnosis: AppCompatEditText,
        tvYearOfDiagnosisError: AppCompatTextView
    ): Boolean {
        return MotherNeonateUtil.isValidInput(
            etYearOfDiagnosis.text.toString(),
            etYearOfDiagnosis,
            tvYearOfDiagnosisError,
            1920.0..DateUtils.getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            requireContext()
        )
    }

    private fun isValidDiagnosisTwo(): Boolean {
        return MotherNeonateUtil.isValidInput(
            binding.ncdDiabetesHypertension.etYearOfDiagnosisHtn.text.toString(),
            binding.ncdDiabetesHypertension.etYearOfDiagnosisHtn,
            binding.ncdDiabetesHypertension.tvYearOfDiagnosisErrorHtn,
            1900.0..DateUtils.getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            requireContext()
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnCancel.id, binding.ivClose.id -> {
                listener?.closePage()
            }
            binding.btnConfirm.id -> {
                if (validateInput()) {
                    val request = NCDMentalHealthStatusRequest(
                        provenance = ProvanceDto(),
                        memberReference = getMemberReference(),
                        patientReference = getPatientReference(),
                        ncdPatientStatus = NcdPatientStatus(
                            diabetesStatus = viewModel.resultDiabetesHashMap[Diabetes] as? String,
                            hypertensionStatus = viewModel.resultHypertensionHashMap[Hypertension] as? String,
                            hypertensionYearOfDiagnosis = viewModel.yearForHypertension.takeIf { !it.isNullOrBlank() },
                            diabetesYearOfDiagnosis = viewModel.yearForDiabetes.takeIf { !it.isNullOrBlank() },
                            diabetesControlledType = null,
                            diabetesDiagnosis = viewModel.value
                        ),
                        mentalHealthStatus = MentalHealthStatus(
                            status = viewModel.resultMentalHealthHashMap[MENTAL_HEALTH_STATUS] as? String,
                            comments = viewModel.mentalHealthComments.takeIf { !it.isNullOrBlank() },
                            yearOfDiagnosis = viewModel.yearForMentalHealth?.takeIf { true },
                            mentalHealthDisorder = viewModel.selectedMentalHealthListItem.takeIf { it.isNotEmpty() }
                                ?.map { it.name.lowercase() } as ArrayList<String>?,
                        ),
                        substanceUseStatus = MentalHealthStatus(
                            status = viewModel.resultSubstanceUseHashMap[SUBSTANCE_USE_STATUS] as? String,
                            comments = viewModel.substanceUseComments.takeIf { !it.isNullOrBlank() },
                            yearOfDiagnosis = viewModel.yearForSubstanceUse?.takeIf { true },
                            mentalHealthDisorder = viewModel.selectedSubstanceListItem.takeIf { it.isNotEmpty() }
                                ?.map { it.name.lowercase() } as ArrayList<String>?,
                        )
                    )
                    viewModel.createMentalHealthStatus(request)
                }
            }
        }
    }

    private fun getPatientReference(): String? {
        return arguments?.getString(NCDMRUtil.PATIENT_REFERENCE)
    }

    private fun getMemberReference(): String? {
        return arguments?.getString(NCDMRUtil.MEMBER_REFERENCE)
    }

    fun showLoading() {
        binding.apply {
            btnConfirm.invisible()
            btnCancel.invisible()
            loadingProgress.visible()
            loaderImage.apply {
                loadAsGif(R.drawable.loader_spice)
            }
        }
    }

    fun hideLoading() {
        binding.apply {
            btnConfirm.visible()
            btnCancel.visible()
            loadingProgress.gone()
            loaderImage.apply {
                resetImageView()
            }
        }
    }
}