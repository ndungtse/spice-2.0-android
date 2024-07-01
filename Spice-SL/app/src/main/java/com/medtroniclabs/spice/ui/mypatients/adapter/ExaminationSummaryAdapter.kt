package com.medtroniclabs.spice.ui.mypatients.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams.Anaemia
import com.medtroniclabs.spice.common.DefinedParams.Cough
import com.medtroniclabs.spice.common.DefinedParams.CoughOrDifficultBreathing
import com.medtroniclabs.spice.common.DefinedParams.Hiv
import com.medtroniclabs.spice.common.DefinedParams.HivAndAids
import com.medtroniclabs.spice.common.DefinedParams.MalnutritionOrAnaemia
import com.medtroniclabs.spice.data.resource.ExaminationResult

class ExaminationSummaryAdapter() :
    RecyclerView.Adapter<ExaminationSummaryAdapter.DiseaseViewHolder>() {
    private var examinationResult = mutableListOf<ExaminationResult>()

    class DiseaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val linearLayout: LinearLayout = itemView.findViewById(R.id.examination_disease)
        val titleTextView: AppCompatTextView = itemView.findViewById(R.id.titleTextView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiseaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DiseaseViewHolder(
            layoutInflater.inflate(
                R.layout.under_two_months_examination_summary_diease,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DiseaseViewHolder, position: Int) {
        val item = examinationResult[position]
        bindExaminationList(item, holder)
    }

    override fun getItemCount(): Int = examinationResult.size
    fun updateData(newItems: List<ExaminationResult>) {
        this.examinationResult.clear()
        this.examinationResult = newItems.toMutableList()
        notifyDataSetChanged()
    }

    private fun bindExaminationList(diseaseInfo: ExaminationResult, holder: DiseaseViewHolder) {
        var symptoms = diseaseInfo.symptomsTitle

        when (symptoms) {
            Cough -> symptoms = CoughOrDifficultBreathing
            Anaemia -> symptoms = MalnutritionOrAnaemia
            Hiv -> symptoms = HivAndAids
        }

        val formattedSymptoms =
            if (!symptoms.isNullOrEmpty()) {
                "${diseaseInfo.index}. ${symptoms[0].uppercaseChar()}${symptoms.substring(1)}"
            } else {
                "${diseaseInfo.index}. "
            }
        holder.apply {
            titleTextView.text = formattedSymptoms
            linearLayout.removeAllViews()  // Clear previous views
            diseaseInfo.description?.forEach { description ->
                val textView = AppCompatTextView(itemView.context).apply {
                    id = View.generateViewId()
                    layoutParams = ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setTextColor(ContextCompat.getColor(context, R.color.grey_black))
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._18ssp))
                    text = description
                }
                linearLayout.addView(textView)
            }
    }
    }
}

