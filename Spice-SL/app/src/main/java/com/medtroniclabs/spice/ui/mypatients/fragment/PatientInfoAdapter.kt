package com.medtroniclabs.spice.ui.mypatients.fragment

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setExpandableText
import com.medtroniclabs.spice.appextensions.takeIfNotNull
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.Married
import com.medtroniclabs.spice.common.DefinedParams.Single
import com.medtroniclabs.spice.databinding.PatientInfoItemBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.toggle.OnToggledListener
import com.medtroniclabs.spice.toggle.ToggleableView
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.initTextWatcherForString
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel

class PatientInfoAdapter(
    private val data: List<Map<String, Any?>?> = emptyList(),
    private val fragmentBg: Int,
    private val activity: BaseActivity,
    private val mentalHealthAssessment: (mhPair: Pair<String?, Boolean>) -> Unit,
    private val onItemPregnantDialog: () -> Unit,
    private val onItemToggle: (isChecked: Boolean) -> Unit,
    private val occupation: (String?) -> Unit,
    private val maritalStatus: (String?) -> Unit,
    private val resultValues: HashMap<String, Any>,
    private val presumptiveTbNo: (String?) -> Unit,
    private val artCode: (String?) -> Unit,
    private val isHiv: Boolean = false
) :
    RecyclerView.Adapter<PatientInfoAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: PatientInfoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val context: Context = binding.root.context
        fun bind(label: Map<String, Any?>) {
            with(binding) {
                val empty = context.getString(R.string.hyphen_symbol)
                tvLabel.text = (label[DefinedParams.label] as? String).takeIfNotNull(empty)
                tvValue.setExpandableText(
                    (label[DefinedParams.Value] as? String).takeIfNotNull(empty),
                    title = (label[DefinedParams.label] as? String).takeIfNotNull(empty),
                    maxLength = 25,
                    activity = activity
                )
                if (label.containsKey(DefinedParams.color)) {
                    label[DefinedParams.color]?.let {
                        tvValue.setTextColor(label[DefinedParams.color] as Int)
                    }
                }
                val mentalHealthLabels = listOf(
                    context.getString(R.string.phq4_score),
                    context.getString(R.string.suicidal_ideation),
                    context.getString(R.string.cage_aid)
                )

                if (label[DefinedParams.label] != null && mentalHealthLabels.contains(label[DefinedParams.label])) {
                    tvMentalHealth.visible()
                    tvMentalHealth.text = (label[Screening.type] as? String).takeIfNotNull()
                } else {
                    tvMentalHealth.text = context.getString(R.string.empty)
                    tvMentalHealth.gone()
                }
                tvMentalHealth.safeClickListener {
                    val isEdit = tvMentalHealth.text.toString()
                        .equals(context.getString(R.string.edit_assessment), true)
                    mentalHealthAssessment.invoke(
                        Pair(
                            label[DefinedParams.label] as? String?,
                            isEdit
                        )
                    )
                }
                if (label[DefinedParams.label]?.equals(context.getString(R.string.high_risk)) == true) {
                    tvValue.gone()
                    viewToggle.visible()
                    smHighRisk.visible()
                    tvHighRiskPregnancyCriteria.visible()
                    smHighRisk.isOn = label[DefinedParams.Value] as Boolean == true
                    viewToggle.safeClickListener {
                        if (!binding.smHighRisk.isOn) {
                            userConfirms()
                            smHighRisk.setOnToggledListener(toggledListener)
                        }
                    }
                    tvHighRiskPregnancyCriteria.safeClickListener {
                        onItemPregnantDialog.invoke()
                    }
                } else {
                    tvValue.visible()
                    smHighRisk.gone()
                    viewToggle.gone()
                    tvHighRiskPregnancyCriteria.gone()
                }

                if (label[DefinedParams.label]?.equals(context.getString(R.string.occupation)) == true && !isHiv) {
                    tvValue.gone()
                    tvSeparator.gone()
                    etOccupation.visible()
                    setupEditText(etOccupation,occupation)
                }

                if (label[DefinedParams.label]?.equals(context.getString(R.string.marital_status)) == true && !isHiv) {
                    tvValue.gone()
                    tvSeparator.gone()
                    spinnerMaritalStatus.visible()
                    val spinnerFormAdapter = CustomSpinnerAdapter(context)
                    spinnerFormAdapter.setData(getMaterialStatusData())
                    var defaultPosition = 0
                    val selectedMaritalStatus= resultValues[DefinedParams.MaritalStatus] as String?
                    selectedMaritalStatus?.let{status ->
                        for ((index, patientStatus) in getMaterialStatusData().withIndex()) {
                            if ((patientStatus[DefinedParams.ID] as? String).equals(
                                    status,
                                    true
                                )
                            ) {
                                defaultPosition = index
                            }
                        }
                        binding.spinnerMaritalStatus.post {
                            binding.spinnerMaritalStatus.setSelection(defaultPosition, false)
                            if (defaultPosition != 0) {
                                binding.spinnerMaritalStatus.isEnabled = false
                            }
                        }
                    }
                    binding.spinnerMaritalStatus.adapter = spinnerFormAdapter
                    binding.spinnerMaritalStatus.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                p2: Int,
                                p3: Long
                            ) {
                                val selectedItem = spinnerFormAdapter.getData(position = p2)
                                maritalStatus.invoke(selectedItem?.get(DefinedParams.ID) as String)
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {
                                /**
                                 * this method is not used
                                 */
                            }
                        }
                }
                if (label[DefinedParams.label]?.equals(context.getString(R.string.occupation_summary)) == true) {
                    tvSeparator.visible()
                    etOccupation.gone()
                    tvLabel.text = context.getString(R.string.occupation)
                }
                if (label[DefinedParams.label]?.equals(context.getString(R.string.marital_status_summary)) == true) {
                    tvValue.visible()
                    tvSeparator.visible()
                    spinnerMaritalStatus.gone()
                    tvLabel.text = context.getString(R.string.marital_status)
                }
                handleCustomField(
                    label,
                    context.getString(R.string.presumptive_tb_no),
                    etValue,
                    tvValue,
                    ivEdit,
                    context
                ) {
                    presumptiveTbNo.invoke(it)
                }
                handleCustomField(
                    label,
                    context.getString(R.string.art_code),
                    etValue,
                    tvValue,
                    ivEdit,
                    context
                ) {
                    artCode.invoke(it)
                }
                root.background = ContextCompat.getDrawable(context, fragmentBg)
            }
        }

        private fun handleCustomField(
            label: Map<String, Any?>,
            expectedLabel: String,
            etValue: AppCompatEditText,
            tvValue: AppCompatTextView,
            ivEdit: AppCompatImageView,
            context: Context,
            onTextChanged: (String) -> Unit
        ) {
            val labelText = label[DefinedParams.label] as? String
            val isSummary = label[DefinedParams.IsSummary] == "true"
            val value = label[DefinedParams.Value] as? String
            etValue.isEnabled = false

            if (labelText == expectedLabel) {
                if (!isSummary) {
                    etValue.visible()
                    ivEdit.visible()
                    tvValue.gone()
                    value?.takeIf { it.isNotBlank() }?.let { etValue.setText(it) }
                    ivEdit.safeClickListener {
                        onTextChanged.invoke(value?.takeIf { it.isNotBlank() } ?: "")
                    }
                } else {
                    etValue.gone()
                    ivEdit.gone()
                    tvValue.visible()
                    tvValue.text = context.getString(R.string.hyphen_symbol) // Default hyphen
                    value?.takeIf { it.isNotBlank() && it != context.getString(R.string.hyphen_symbol) }
                        ?.let {
                            tvValue.text = it
                        }
                }
            }
        }


        private fun userConfirms() {
            (activity as? BaseActivity?)?.showErrorDialogue(
                context.getString(R.string.alert),
                context.getString(R.string.high_risk_confirmation),
                positiveButtonName = context.getString(R.string.yes),
                isNegativeButtonNeed = true,
                cancelBtnName = context.getString(R.string.no),
                okayBtnEnable = true
            ) {
                if (it)
                    binding.smHighRisk.performClick()
            }
        }
    }

    private val toggledListener = object : OnToggledListener {
        override fun onSwitched(toggleableView: ToggleableView?, isOn: Boolean) {
            onItemToggle.invoke(isOn)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            PatientInfoItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        data[position]?.let { holder.bind(it) }
    }

    private fun getMaterialStatusData(): ArrayList<Map<String, Any>> {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.ID to DefinedParams.DefaultID,
            )
        )
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to Married,
                DefinedParams.ID to Married,
            )
        )
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to Single,
                DefinedParams.ID to Single,
            )
        )

        return dropDownList
    }

    fun setupEditText(editText: AppCompatEditText, callback: (String) -> Unit) {
        val occupationValue = resultValues[DefinedParams.Occupation]
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                callback.invoke(s?.toString().orEmpty())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        if (occupationValue is String && occupationValue.isNotBlank()) {
            editText.setText(occupationValue)
            editText.isEnabled = false
            editText.isFocusable = false
        }
        editText.addTextChangedListener(watcher)
    }


    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}