package com.medtroniclabs.spice.ui.mypatients

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.databinding.ListItemPatientsBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel

class PatientsListAdapter(
    val listener: PatientSelectionListener
) : PagingDataAdapter<PatientListRespModel, PatientsListAdapter.PatientListViewHolder>(
    PatientListComparator
) {

    inner class PatientListViewHolder(val binding: ListItemPatientsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context

        fun bind(item: PatientListRespModel) {
            val context = binding.root.context
            val name = item.name ?: context.getString(R.string.separator_hyphen)
            val gender = item.gender ?: context.getString(R.string.separator_hyphen)
            var month = context.getString(R.string.months)
            val age: Int = item.birthDate?.let { birthDate ->
                DateUtils.calculateAge(birthDate).takeIf { it > 5 } ?: DateUtils.dateToMonths(
                    birthDate,
                    DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                )
            } ?: 0

            val formattedAge = if (age <= 5) "$age $month" else age.toString()
            val patientInfo = context.getString(R.string.household_summary_member_info, name, formattedAge, gender)

            with(binding) {
                tvCardPatientName.text = patientInfo
                tvCardPatientId.text =
                    item.patientId ?: context.getString(R.string.separator_hyphen)
                tvCardVillage.text = item.village ?: context.getString(R.string.separator_hyphen)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientListViewHolder {
        return PatientListViewHolder(
            ListItemPatientsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }


    override fun onBindViewHolder(holder: PatientListViewHolder, position: Int) {
        getItem(position)?.let { item ->
            holder.bind(item)
            holder.binding.cardPatient.safeClickListener {
                listener.onSelectedPatient(item)
            }
        }
    }

    object PatientListComparator : DiffUtil.ItemCallback<PatientListRespModel>() {
        override fun areItemsTheSame(
            oldItem: PatientListRespModel,
            newItem: PatientListRespModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PatientListRespModel,
            newItem: PatientListRespModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}