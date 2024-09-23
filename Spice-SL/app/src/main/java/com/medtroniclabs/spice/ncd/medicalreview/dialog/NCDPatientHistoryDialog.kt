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
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils.getCurrentYearAsDouble
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentNcdPatientHistoryDialogBinding
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDPatientHistoryViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDPatientHistoryDialog : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentNcdPatientHistoryDialogBinding
    private val viewModel: NCDPatientHistoryViewModel by viewModels()
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
        const val TAG = "PatientHistoryDialog"
        const val Diabetes = "Diabetes"
        const val Hypertension = "Hypertension"
        const val Known_patient = "Known Patient"
        fun newInstance(): NCDPatientHistoryDialog {
            return NCDPatientHistoryDialog()
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
            loadSiteDetails(ArrayList(it))
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
                        val selectedValue = it[DefinedParams.NAME] as String?
                        if (selectedId != -1L) {
                            viewModel.value = selectedValue
                        } else {
                            viewModel.value = null
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun loadSiteDetails(data: ArrayList<SignsAndSymptomsEntity>) {
        val list = arrayListOf<Map<String, Any>>(
            hashMapOf(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.ID to DefinedParams.DefaultSelectID
            )
        )

        data.mapNotNullTo(list) { symptoms ->
            hashMapOf<String, Any>().apply {
                symptoms._id?.let { put(DefinedParams.ID, it) }
                symptoms.symptom?.let { put(DefinedParams.NAME, it) }
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
                tvDiabetes.markMandatory()
                tvYearOfDiagnosis.markMandatory()
                tvYearOfDiagnosisHtn.markMandatory()
                tvDiabetesControlledTypeLabel.markMandatory()
                tvHypertension.markMandatory()
                MotherNeonateUtil.initTextWatcherForString(etYearOfDiagnosis) {

                }
                MotherNeonateUtil.initTextWatcherForString(etYearOfDiagnosisHtn) {

                }
            }
            btnLayout.btnCancel.gone()
            btnLayout.btnConfirm.text = getString(R.string.save)
            btnLayout.btnConfirm.isEnabled = true
            tvTitle.text = getString(R.string.patient_history)
            btnLayout.btnConfirm.safeClickListener(this@NCDPatientHistoryDialog)
            ivClose.safeClickListener(this@NCDPatientHistoryDialog)

        }
        viewModel.getSymptoms(Diabetes)

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

        if (isValueValid) {
            binding.ncdDiabetesHypertension.tvDiabetesControlledError.gone()
        } else {
            binding.ncdDiabetesHypertension.tvDiabetesControlledError.visible()
        }

        val isKnownDiabetesPatient =
            (viewModel.resultDiabetesHashMap[Diabetes] as? String)?.equals(
                Known_patient,
                true
            ) == true
        val isKnownHypertensionPatient =
            (viewModel.resultHypertensionHashMap[Hypertension] as? String)?.equals(
                Known_patient,
                true
            ) == true
        val knownPatientValidForDiabetes = (!isKnownDiabetesPatient || isValidDiagnosis())
        val knownPatientValidForHypertension =
            (!isKnownHypertensionPatient || isValidDiagnosisTwo())
        return isDiabetesValid && isHypertensionValid && isValueValid && knownPatientValidForDiabetes
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
                }
            }

            binding.ivClose.id -> {
                dismiss()
            }
        }
    }

}