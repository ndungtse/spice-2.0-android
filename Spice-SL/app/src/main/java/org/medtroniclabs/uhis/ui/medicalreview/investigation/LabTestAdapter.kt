package com.example.labtest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.model.LabTestResultModel

class LabTestAdapter(private val labTestList: List<LabTestResultModel>) :
    RecyclerView.Adapter<LabTestAdapter.LabTestViewHolder>() {
    class LabTestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val labTestName: TextView = view.findViewById(R.id.labTestName)
        val resultValue: TextView = view.findViewById(R.id.resultValue)
        val labTestUnit: TextView = view.findViewById(R.id.labTestUnit)
        val normalRange: TextView = view.findViewById(R.id.normalRange)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): LabTestViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_lab_test, parent, false)
        return LabTestViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: LabTestViewHolder,
        position: Int,
    ) {
        val labTest = labTestList[position]
        holder.labTestName.text = labTest.labTestName
        holder.resultValue.text = labTest.resultValue
        holder.labTestUnit.text = labTest.labTestUom
        holder.normalRange.text = labTest.normalRange
    }

    override fun getItemCount() = labTestList.size
}
