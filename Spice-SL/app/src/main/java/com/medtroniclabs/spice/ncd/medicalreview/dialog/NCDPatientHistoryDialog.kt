package com.medtroniclabs.spice.ncd.medicalreview.dialog

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils.getCurrentYearAsDouble
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentNcdPatientHistoryDialogBinding
import com.medtroniclabs.spice.db.entity.NCDDiagnosisEntity
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.Screening.Female
import com.medtroniclabs.spice.mappingkey.Screening.Male
import com.medtroniclabs.spice.ncd.data.NCDPatientStatusRequest
import com.medtroniclabs.spice.ncd.data.NcdPatientStatus
import com.medtroniclabs.spice.ncd.medicalreview.NCDDialogDismissListener
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDPatientHistoryViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDPatientHistoryDialog : DialogFragment(), View.OnClickListener {
    var listener: NCDDialogDismissListener? = null
    private lateinit var binding: FragmentNcdPatientHistoryDialogBinding
    private val viewModel: NCDPatientHistoryViewModel by viewModels()
    private val medicalReviewViewModel : NCDMedicalReviewViewModel by activityViewModels()
    val adapter by lazy { CustomSpinnerAdapter(requireContext()) }

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
            isTablet && !isLandscape -> 85
            else -> 95
        }
        setDialogPercent(percent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNcdPatientHistoryDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    companion object {
        const val TAG = "NCDPatientHistoryDialog"
        const val Diabetes = "Diabetes"
        const val Hypertension = "Hypertension"
        const val Known_patient = "Known Patient"
        const val IS_CLOSED = "Is Closed"
        fun newInstance(
            patientReference: String?,
            memberReference: String?,
            isCloseNeeded: Boolean = false,
            isFemale: Boolean
        ): NCDPatientHistoryDialog {
            return NCDPatientHistoryDialog().apply {
                arguments = Bundle().apply {
                    putString(NCDMRUtil.PATIENT_REFERENCE, patientReference)
                    putString(NCDMRUtil.MEMBER_REFERENCE, memberReference)
                    putBoolean(IS_CLOSED, isCloseNeeded)
                    putBoolean(NCDMRUtil.IS_FEMALE, isFemale)
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

    private fun getCloseNeeded(): Boolean {
        return arguments?.getBoolean(IS_CLOSED) ?: false
    }

    private fun getGender(): String {
        return if (arguments?.getBoolean(NCDMRUtil.IS_FEMALE) == true) {
            Female.lowercase()
        } else {
            Male.lowercase()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
        setListeners()
    }

    private fun attachObserver() {
        viewModel.getSymptomListByTypeForNCDLiveData.observe(viewLifecycleOwner) {
            medicalReviewViewModel.validationForStatus = it
            loadSiteDetails(ArrayList(it))
        }
        viewModel.createNCDPatientStatus.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                }

                ResourceState.SUCCESS -> {
                    listener?.onDialogDismissed(true)
                }

                ResourceState.ERROR -> {

                }
            }
        }
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
                        val value = it[DefinedParams.value] as String?
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

    private fun loadSiteDetails(data: ArrayList<NCDDiagnosisEntity>) {
        val list = arrayListOf<Map<String, Any>>(
            hashMapOf(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.ID to DefinedParams.DefaultSelectID
            )
        )

        data.mapNotNullTo(list) { symptoms ->
            hashMapOf<String, Any>().apply {
                symptoms.id?.let { put(DefinedParams.ID, it) }
                symptoms.name?.let { put(DefinedParams.NAME, it) }
                symptoms.value?.let { put(DefinedParams.value, it) }
            }.takeIf { it.isNotEmpty() }
        }
        adapter.setData(list)
        binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.post {
            binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.setSelection(0, false)
        }
        binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.adapter = adapter
    }

    private fun initView() {
        binding.apply {
            ncdDiabetesHypertension.apply {
                if (getCloseNeeded()) {
                    ivClose.gone()
                }
                tvDiabetes.markMandatory()
                tvYearOfDiagnosis.markMandatory()
                tvYearOfDiagnosisHtn.markMandatory()
                tvDiabetesControlledTypeLabel.markMandatory()
                tvHypertension.markMandatory()
                MotherNeonateUtil.initTextWatcherForString(etYearOfDiagnosis) {
                    viewModel.yearForDiabetes = it
                }
                MotherNeonateUtil.initTextWatcherForString(etYearOfDiagnosisHtn) {
                    viewModel.yearForHypertension = it
                }
            }
            btnLayout.btnCancel.gone()
            btnLayout.btnConfirm.text = getString(R.string.save)
            btnLayout.btnConfirm.isEnabled = true
            tvTitle.text = getString(R.string.patient_history)
            btnLayout.btnConfirm.safeClickListener(this@NCDPatientHistoryDialog)
            ivClose.safeClickListener(this@NCDPatientHistoryDialog)

        }
        viewModel.getSymptoms(Diabetes.lowercase(), getGender())

        getSingleSelectionOptions().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = Diabetes
            view.addViewElements(
                it,
                false,
                viewModel.resultDiabetesHashMap,
                Pair(Diabetes, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForDiabetes
            )
            binding.ncdDiabetesHypertension.llDiabetes.addView(view)
        }

        getSingleSelectionOptions().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = Hypertension
            view.addViewElements(
                it,
                false,
                viewModel.resultHypertensionHashMap,
                Pair(Hypertension, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForHypertension
            )
            binding.ncdDiabetesHypertension.llHypertension.addView(view)
        }
    }

    private var singleSelectionCallbackForDiabetes: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultDiabetesHashMap[Diabetes] = selectedID as String
            showViews(
                binding.ncdDiabetesHypertension.groupYearOfDiagnosis,
                selectedID,
                binding.ncdDiabetesHypertension.tvYearOfDiagnosisError,
                binding.ncdDiabetesHypertension.etYearOfDiagnosis
            )
            showSpinnerView(selectedID)
        }

    private var singleSelectionCallbackForHypertension: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultHypertensionHashMap[Hypertension] =
                selectedID as String
            showViews(
                binding.ncdDiabetesHypertension.groupYearOfDiagnosis2,
                selectedID,
                binding.ncdDiabetesHypertension.tvYearOfDiagnosisErrorHtn,
                binding.ncdDiabetesHypertension.etYearOfDiagnosisHtn
            )
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

    private fun showSpinnerView(selectedValue: String) {
        val isKnownPatient = selectedValue.equals(Known_patient, ignoreCase = true)
        with(binding.ncdDiabetesHypertension) {
            groupDiabetesSpinner.isVisible = isKnownPatient
            if (!isKnownPatient) {
                etYearOfDiagnosis.setText("")
                viewModel.value = null
                tvDiabetesControlledSpinner.post {
                    tvDiabetesControlledSpinner.setSelection(0, false)
                }
            }
            tvDiabetesControlledError.gone()
        }
    }

    private fun getSingleSelectionOptions(): ArrayList<Map<String, Any>> {
        val yearOfDiagnosis = ArrayList<Map<String, Any>>()
        yearOfDiagnosis.add(
            CommonUtils.getOptionMap(
                getString(R.string.na),
                getString(R.string.na)
            )
        )
        yearOfDiagnosis.add(
            CommonUtils.getOptionMap(
                getString(R.string.new_patient),
                getString(R.string.new_patient)
            )
        )
        yearOfDiagnosis.add(
            CommonUtils.getOptionMap(
                getString(R.string.known_patient),
                getString(R.string.known_patient)
            )
        )
        return yearOfDiagnosis
    }

    fun validateInput(): Boolean {
        val isDiabetesValid = !viewModel.resultDiabetesHashMap.isNullOrEmpty()
        val isHypertensionValid = !viewModel.resultHypertensionHashMap.isNullOrEmpty()
        val isValueValid = !viewModel.value.isNullOrBlank()

        if (isDiabetesValid) {
            binding.ncdDiabetesHypertension.tvDiabetesError.gone()
        } else {
            binding.ncdDiabetesHypertension.tvDiabetesError.visible()
        }

        if (isHypertensionValid) {
            binding.ncdDiabetesHypertension.tvHypertensionError.gone()
        } else {
            binding.ncdDiabetesHypertension.tvHypertensionError.visible()
        }

        val isKnownDiabetesPatient =
            (viewModel.resultDiabetesHashMap[Diabetes] as? String)?.equals(
                Known_patient,
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
            (viewModel.resultHypertensionHashMap[Hypertension] as? String)?.equals(
                Known_patient,
                true
            ) == true

        val knownPatientValidForDiabetes =
            (!isKnownDiabetesPatient || (isValidDiagnosis() && isValueValid))
        val knownPatientValidForHypertension =
            (!isKnownHypertensionPatient || isValidDiagnosisTwo())
        return isDiabetesValid && isHypertensionValid && knownPatientValidForDiabetes
                && knownPatientValidForHypertension

    }

    private fun isValidDiagnosis(): Boolean {
        return MotherNeonateUtil.isValidInput(
            binding.ncdDiabetesHypertension.etYearOfDiagnosis.text.toString(),
            binding.ncdDiabetesHypertension.etYearOfDiagnosis,
            binding.ncdDiabetesHypertension.tvYearOfDiagnosisError,
            1920.0..getCurrentYearAsDouble(),
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
            1900.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            requireContext()
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnLayout.btnConfirm.id -> {
                if (validateInput()) {
                    // Do the API call
                    val request = NCDPatientStatusRequest(
                        provenance = ProvanceDto(),
                        patientReference = getPatientReference(),
                        memberReference = getMemberReference(),
                        ncdPatientStatus = NcdPatientStatus(
                            diabetesStatus = viewModel.resultDiabetesHashMap[Diabetes] as? String,
                            hypertensionStatus = viewModel.resultHypertensionHashMap[Hypertension] as? String,
                            hypertensionYearOfDiagnosis = viewModel.yearForHypertension.takeIf { !it.isNullOrBlank() },
                            diabetesYearOfDiagnosis = viewModel.yearForDiabetes.takeIf { !it.isNullOrBlank() },
                            diabetesControlledType = null,
                            diabetesDiagnosis = viewModel.value
                        )
                    )
                    viewModel.createNCDPatientStatus(request)
                }
            }

            binding.ivClose.id -> {
                dismiss()
            }
        }
    }

}