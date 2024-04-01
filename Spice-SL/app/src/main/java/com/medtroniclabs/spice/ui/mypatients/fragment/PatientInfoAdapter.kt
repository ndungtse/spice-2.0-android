package com.medtroniclabs.spice.ui.mypatients.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.PatientInfoItemBinding

class PatientInfoAdapter(
    private val data: List<Map<String, String?>?> = emptyList(),
    private val fragmentBg: Int
) :
    RecyclerView.Adapter<PatientInfoAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: PatientInfoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(label: Map<String, String?>) {
            with(binding) {
                tvLabel.text = label[DefinedParams.label]
                    ?: binding.root.context.getString(R.string.hyphen_symbol)
                tvValue.text = label[DefinedParams.value]
                    ?: binding.root.context.getString(R.string.hyphen_symbol)
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