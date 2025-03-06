package com.medtroniclabs.spice.ui.medicalreview.epi.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.getLocalDate
import com.medtroniclabs.spice.model.medicalreview.VaccinationDetail
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class VaccinationStatusNudge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    item: VaccinationDetail? = null,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        const val Vaccinated = 1
        const val VaccinatedWithDelay = 2
        const val Upcoming = 3
        const val Missed = 4
    }

    private val flIconEpiStatus: FrameLayout
    private val ivIconEpiStatus: AppCompatImageView
    private val tvEpiStatus: AppCompatTextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_vaccination_status_nudge, this, true)

        flIconEpiStatus = findViewById(R.id.flIconEpiStatus)
        ivIconEpiStatus = findViewById(R.id.ivIconEpiStatus)
        tvEpiStatus = findViewById(R.id.tvEpiStatus)

        item?.let {
            updateStatusIcon(it)
        }
    }


    private fun getVaccinationStatus(vaccinationDetail: VaccinationDetail): Pair<Int, Long?> {
        if (vaccinationDetail.vaccinatedDate != null) { // Vaccination Completed
            val daysDiff =
                getDaysBetween(vaccinationDetail.vaccinatedDate!!, vaccinationDetail.scheduledDate)
            return if (daysDiff > 0)
                Pair(VaccinatedWithDelay, daysDiff)
            else
                Pair(Vaccinated, null)

        } else {
            val dayDiff = ChronoUnit.DAYS.between(
                vaccinationDetail.scheduledDate.getLocalDate(),
                LocalDate.now()
            )

            return if (dayDiff <= 0)
                Pair(Upcoming, null)
            else
                Pair(Missed, dayDiff)
        }
    }

    private fun getDaysBetween(vaccinatedDateStr: String, scheduledDateStr: String): Long {
        val vaccinatedDate = vaccinatedDateStr.getLocalDate()
        val scheduledDate = scheduledDateStr.getLocalDate()
        return ChronoUnit.DAYS.between(scheduledDate, vaccinatedDate)
    }

    private fun updateStatusIcon(item: VaccinationDetail) {
        val status = getVaccinationStatus(item)
        when(status.first) {
            Upcoming -> {
                background =  ContextCompat.getDrawable(context, R.drawable.bg_epi_upcoming)
                flIconEpiStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_ic_epi_upcoming)
                ivIconEpiStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_epi_upcoming))
                tvEpiStatus.setTextColor(context.getColor(R.color.epi_upcoming_primary))
                tvEpiStatus.text = context.getString(R.string.upcoming)
            }

            Vaccinated -> {
                //setBackgroundResource(R.drawable.bg_epi_vaccinated)
                background =  ContextCompat.getDrawable(context, R.drawable.bg_epi_vaccinated)
                flIconEpiStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_ic_epi_vaccinated)
                ivIconEpiStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_epi_vaccinated))
                tvEpiStatus.setTextColor(context.getColor(R.color.epi_vaccinated_primary))
                tvEpiStatus.text = context.getString(R.string.vaccinated_on_time)
            }

            VaccinatedWithDelay -> {
                background =  ContextCompat.getDrawable(context, R.drawable.bg_epi_delay_vaccinated)
                flIconEpiStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_ic_epi_delay_vaccinated)
                ivIconEpiStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_epi_vaccinated))
                tvEpiStatus.setTextColor(context.getColor(R.color.epi_vaccinated_delay_primary))
                tvEpiStatus.text = context.getString(R.string.vaccinated_with_delay, status.second?.toInt())
            }

            else -> {
                background =  ContextCompat.getDrawable(context, R.drawable.bg_epi_missed)
                flIconEpiStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_ic_epi_missed)
                ivIconEpiStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_epi_missed))
                tvEpiStatus.setTextColor(context.getColor(R.color.epi_missed_primary))
                tvEpiStatus.text = context.getString(R.string.missed_vaccination, status.second?.toInt())
            }
        }
    }
}