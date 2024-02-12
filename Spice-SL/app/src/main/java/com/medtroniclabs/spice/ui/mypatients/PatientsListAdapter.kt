package com.medtroniclabs.spice.ui.mypatients

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.databinding.ListItemPatientsBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel

class PatientsListAdapter(
    val listener: PatientSelectionListener,
    private val patientList: ArrayList<PatientListRespModel>
) : RecyclerView.Adapter<PatientsListAdapter.PatientListViewHolder>() {

    inner class PatientListViewHolder(val binding: ListItemPatientsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientListViewHolder {
        return PatientListViewHolder(
            ListItemPatientsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return if (patientList.isEmpty()) {
            0
        } else {
            patientList.size
        }
    }

    override fun onBindViewHolder(holder: PatientListViewHolder, position: Int) {
        val item = patientList[position]
        holder.binding.tvCardPatientName.text = "${item.firstName} ${item.lastName} - ${item.age} - ${item.gender}"
        holder.binding.tvCardNationalID.text = item.nationalID.toString()
        holder.binding.tvCardPatientID.text = item.patientId.toString()
        holder.binding.cardPatient.safeClickListener {
            listener.onSelectedPatient(item)
        }
    }

}