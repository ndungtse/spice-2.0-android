package com.medtroniclabs.spice.ui.mypatients.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setExpandableText
import com.medtroniclabs.spice.appextensions.takeIfNotNull
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.PatientInfoItemBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.toggle.OnToggledListener
import com.medtroniclabs.spice.toggle.ToggleableView
import com.medtroniclabs.spice.ui.BaseActivity
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

                if (label[DefinedParams.label]?.equals(context.getString(R.string.occupation)) == true) {
                    tvValue.gone()
                    tvSeparator.gone()
                    etOccupation.visible()
                    setupEditText(etOccupation,occupation)
                }

                if (label[DefinedParams.label]?.equals(context.getString(R.string.marital_status)) == true) {
                    tvValue.gone()
                    tvSeparator.gone()
                    spinnerMaritalStatus.visible()
                    val spinnerFormAdapter = CustomSpinnerAdapter(context)
                    spinnerFormAdapter.setData(getMaterialStatusData())
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
                root.background = ContextCompat.getDrawable(context, fragmentBg)
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
                DefinedParams.NAME to "Married",
                DefinedParams.ID to "Married",
            )
        )
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to "Single",
                DefinedParams.ID to "Single",
            )
        )

        return dropDownList
    }

    fun setupEditText(editText: AppCompatEditText, callback: (String) -> Unit) {
        editText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                callback(v.text.toString().trim())
                hideKeyboard(editText)
                true
            } else {
                false
            }
        }
    }

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}