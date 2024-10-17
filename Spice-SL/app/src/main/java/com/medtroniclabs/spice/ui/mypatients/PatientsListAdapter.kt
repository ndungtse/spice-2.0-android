package com.medtroniclabs.spice.ui.mypatients

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.databinding.ListItemPatientsBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
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
            val gender = item.gender?.lowercase()?.capitalizeFirstChar()
                ?: context.getString(R.string.separator_hyphen)

            val formattedAge = item.birthDate?.let { DateUtils.getAgeDescription(it,context) }
                ?: context.getString(R.string.separator_hyphen)
            val age = item.age ?: formattedAge
            val patientInfo = context.getString(
                R.string.household_summary_member_info,
                name,
                age,
                gender
            )
            val leftLbl = context.getString(R.string.national_id)
            val rightLbl = context.getString(R.string.patient_id)
            val leftValue = item.identityValue ?: context.getString(R.string.separator_hyphen)
            val rightValue = item.programId ?: context.getString(R.string.separator_hyphen)

            //SL
//            val leftLbl = context.getString(R.string.patient_id)
//            val rightLbl = context.getString(R.string.village)
//            val leftValue = item.patientId ?: context.getString(R.string.separator_hyphen)
//            val rightValue = item.village ?: context.getString(R.string.separator_hyphen)

            with(binding) {
                tvCardPatientName.text = patientInfo
                tvLeftLbl.text = leftLbl
                tvRightLbl.text = rightLbl
                tvLeftValue.text = leftValue
                tvRightValue.text = rightValue
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
            return oldItem.id == newItem.id
        }
    }
}