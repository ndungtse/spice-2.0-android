package com.medtroniclabs.spice.ui.mypatient.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.databinding.LayoutItemMyPatientBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.mypatient.FollowUpCommunicator

class PatientListAdapter(
    private val listOfPatient: ArrayList<FollowUpPatientModel>,
    val assessmentType: String,
    val listener: FollowUpCommunicator
) :
    RecyclerView.Adapter<PatientListAdapter.PatientViewHolder>(), View.OnClickListener {

    inner class PatientViewHolder(val binding: LayoutItemMyPatientBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(followUpPatientModel: FollowUpPatientModel) {
            with(binding) {
                assessmentButton.visibility =
                    if (assessmentType == DefinedParams.HH_VISIT) View.VISIBLE else View.INVISIBLE
                tvCardHouseholdName.text =
                    followUpPatientModel.firstName + " " + followUpPatientModel.lastName
                callButton.safeClickListener {
                    listener.showAssessmentCallDialog()
                }
                assessmentButton.safeClickListener {
                    listener.showAssessmentCallDialog()
                }
            }
        }
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

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }
}