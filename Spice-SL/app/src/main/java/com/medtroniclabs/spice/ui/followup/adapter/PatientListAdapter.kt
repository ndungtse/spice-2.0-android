package com.medtroniclabs.spice.ui.followup.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.databinding.LayoutItemMyPatientBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.followup.FollowUpDefinedParams.FU_TYPE_HH_VISIT

class PatientListAdapter(private val callback: (Int, FollowUpPatientModel) -> Unit) :
    RecyclerView.Adapter<PatientListAdapter.PatientViewHolder>() {

    object ConstantPatientListAdapter {
        const val PATIENT_DETAIL = 0
        const val CALL = 1
        const val ASSESSMENT = 2
    }

    private val listOfPatient = mutableListOf<FollowUpPatientModel>()

    fun updateList(list: List<FollowUpPatientModel>) {
        listOfPatient.clear()
        listOfPatient.addAll(list)
        notifyDataSetChanged()
    }

    inner class PatientViewHolder(val binding: LayoutItemMyPatientBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: FollowUpPatientModel) {
            val context = binding.root.context

            with(binding) {
                tvPatientName.text =
                    getPatientName(context, data.name, data.dateOfBirth, data.gender)

                if (data.type == FU_TYPE_HH_VISIT) {
                    assessmentButton.visible()
                } else {
                    assessmentButton.invisible()
                }

                tvReason.text = data.reason
                tvPatientStatus.text = data.patientStatus

                root.safeClickListener {
                    callback(ConstantPatientListAdapter.PATIENT_DETAIL, data)
                }

                callButton.safeClickListener {
                    callback(ConstantPatientListAdapter.CALL, data)
                }
                assessmentButton.safeClickListener {
                    callback(ConstantPatientListAdapter.ASSESSMENT, data)
                }
            }
        }
    }

    private fun getPatientName(
        context: Context,
        name: String?,
        dob: String?,
        gender: String?
    ): String {
        return context.getString(
            R.string.household_summary_member_info,
            name,
            CommonUtils.getAgeFromDob(
                dob,
                context.getString(R.string.months)
            ),
            CommonUtils.getGenderText(gender, context)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        return PatientViewHolder(
            LayoutItemMyPatientBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return listOfPatient.size
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(listOfPatient[position])
    }
}