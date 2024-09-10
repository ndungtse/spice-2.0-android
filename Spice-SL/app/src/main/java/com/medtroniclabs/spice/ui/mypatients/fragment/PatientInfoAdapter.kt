package com.medtroniclabs.spice.ui.mypatients.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setExpandableText
import com.medtroniclabs.spice.appextensions.takeIfNotNull
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.canShowToggle
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.PatientInfoItemBinding
import com.medtroniclabs.spice.ui.BaseActivity

class PatientInfoAdapter(
    private val data: List<Map<String, Any?>?> = emptyList(),
    private val fragmentBg: Int,
    private val activity: BaseActivity
) :
    RecyclerView.Adapter<PatientInfoAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: PatientInfoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(label: Map<String, Any?>) {
            with(binding) {
                val empty = binding.root.context.getString(R.string.hyphen_symbol)
                tvLabel.text = (label[DefinedParams.label] as? String).takeIfNotNull(empty)
                tvValue.setExpandableText(
                    (label[DefinedParams.value] as? String).takeIfNotNull(empty),
                    title = (label[DefinedParams.label] as? String).takeIfNotNull(empty),
                    maxLength = 35,
                    activity = activity
                )
                if (label.containsKey(DefinedParams.color)) {
                    label[DefinedParams.color]?.let {
                        tvValue.setTextColor(label[DefinedParams.color] as Int)
                    }
                }
                if (label[DefinedParams.label]?.equals(binding.root.context.getString(R.string.high_risk)) == true) {
                    // Your logic here
                    tvValue.gone()
                    if (canShowToggle(
                            label[DefinedParams.Gender]?.toString(),
                            label[DefinedParams.value] as Boolean
                        )
                    ) {
                        smHighRisk.visible()
                        viewToggle.visible()
                        tvHighRiskPregnancyCriteria.visible()
                    } else {
                        tvValue.visible()
                        smHighRisk.gone()
                        viewToggle.gone()
                        tvHighRiskPregnancyCriteria.gone()
                    }
                    smHighRisk.isOn = label[DefinedParams.value] as Boolean == true
                } else {
                    tvValue.visible()
                    smHighRisk.gone()
                    viewToggle.gone()
                    tvHighRiskPregnancyCriteria.gone()
                }
                root.background = ContextCompat.getDrawable(binding.root.context, fragmentBg)
            }
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

}