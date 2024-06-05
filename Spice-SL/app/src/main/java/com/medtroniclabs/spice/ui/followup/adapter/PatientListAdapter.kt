package com.medtroniclabs.spice.ui.followup.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.convertToLocalDateTime
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_TIME_CALL_DISPLAY_FORMAT
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.databinding.LayoutItemMyPatientBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.followup.FollowUpDefinedParams.FU_TYPE_HH_VISIT
import com.medtroniclabs.spice.ui.followup.FollowUpDefinedParams.FU_TYPE_REFERRED
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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

                if (data.type == FU_TYPE_REFERRED) {
                    setOverDueInfo(data.encounterDate, tvDueInformation)
                } else {
                    setOverDueInfo(data.nextVisitDate, tvDueInformation)
                }

                tvReason.text = data.reason
                tvPatientStatus.text = data.patientStatus
                tvLastCallAtLabel.text = calledAtLabel(context, data.type)
                tvLastCallAtValue.text = data.calledAt?.convertToLocalDateTime(format = DATE_TIME_CALL_DISPLAY_FORMAT) ?: "--"

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

    private fun calledAtLabel(context: Context, type: String?): String {
        when(type) {
            FU_TYPE_REFERRED -> return context.getString(R.string.referred_at)
            else -> return context.getString(R.string.called_at)
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
            CommonUtils.getAgeFromDOB(
                dob,
                context
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

    private fun setOverDueInfo(dateString: String?, tv: TextView) {
        if (dateString != null) {
            val currentDate = LocalDate.now()
            val dateTime = OffsetDateTime.parse(dateString,  DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            val date = dateTime.toLocalDate()
            val daysBetween = ChronoUnit.DAYS.between(currentDate, date)

            when {
                daysBetween < 0L -> {
                    tv.text = "${-daysBetween} days, Overdue"
                    tv.setTextColor(Color.parseColor("#994242"))
                }
                daysBetween == 0L -> {
                    tv.text = "Today"
                    tv.setTextColor(Color.parseColor("#EB956A"))
                }
                daysBetween == 1L -> {
                    tv.text = "Tomorrow"
                    tv.setTextColor(Color.parseColor("#54CC90"))
                }
                daysBetween > 1L -> {
                    tv.text = "Upcoming in $daysBetween days"
                    tv.setTextColor(Color.parseColor("#54CC90"))
                }
                else -> {
                    tv.text = "--"
                    tv.setTextColor(Color.parseColor("#54CC90"))
                }
            }
        } else {
            tv.text = "--"
            tv.setTextColor(Color.parseColor("#54CC90"))
        }
    }
}