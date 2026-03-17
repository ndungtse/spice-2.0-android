package org.medtroniclabs.uhis.ui.medicalreview.epi.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.getLocalDate
import org.medtroniclabs.uhis.model.medicalreview.VaccinationDetail
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
                getDaysBetween(vaccinationDetail.vaccinatedDate!!, vaccinationDetail.updatedScheduleDate)
            return if (daysDiff > 0) {
                Pair(VaccinatedWithDelay, daysDiff)
            } else {
                Pair(Vaccinated, null)
            }
        } else {
            val dayDiff = vaccinationDetail.updatedScheduleDate
                ?.let { ChronoUnit.DAYS.between(it, LocalDate.now()) }
                ?: return Pair(Upcoming, null)

            return if (dayDiff <= 0) Pair(Upcoming, null) else Pair(Missed, dayDiff)
        }
    }

    private fun getDaysBetween(
        vaccinatedDateStr: String,
        scheduledDate: LocalDate?,
    ): Long {
        val vaccinatedDate = vaccinatedDateStr.getLocalDate()
        return ChronoUnit.DAYS.between(scheduledDate, vaccinatedDate)
    }

    private fun updateStatusIcon(item: VaccinationDetail) {
        val status = getVaccinationStatus(item)
        when (status.first) {
            Upcoming -> {
                background = ContextCompat.getDrawable(context, R.drawable.bg_epi_upcoming)
                flIconEpiStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_ic_epi_upcoming)
                ivIconEpiStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_epi_upcoming))
                tvEpiStatus.setTextColor(context.getColor(R.color.epi_upcoming_primary))
                tvEpiStatus.text = context.getString(R.string.upcoming)
            }

            Vaccinated -> {
                // setBackgroundResource(R.drawable.bg_epi_vaccinated)
                background = ContextCompat.getDrawable(context, R.drawable.bg_epi_vaccinated)
                flIconEpiStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_ic_epi_vaccinated)
                ivIconEpiStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_epi_vaccinated))
                tvEpiStatus.setTextColor(context.getColor(R.color.epi_vaccinated_primary))
                tvEpiStatus.text = context.getString(R.string.vaccinated_on_time)
            }

            VaccinatedWithDelay -> {
                background = ContextCompat.getDrawable(context, R.drawable.bg_epi_delay_vaccinated)
                flIconEpiStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_ic_epi_delay_vaccinated)
                ivIconEpiStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_epi_vaccinated))
                tvEpiStatus.setTextColor(context.getColor(R.color.epi_vaccinated_delay_primary))
                status.second?.toInt()?.let {
                    if (it > 1) {
                        tvEpiStatus.text = context.getString(R.string.vaccinated_with_days_delay, it)
                    } else {
                        tvEpiStatus.text = context.getString(R.string.vaccinated_with_day_delay, it)
                    }
                }
            }

            else -> {
                background = ContextCompat.getDrawable(context, R.drawable.bg_epi_missed)
                flIconEpiStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_ic_epi_missed)
                ivIconEpiStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_epi_missed))
                tvEpiStatus.setTextColor(context.getColor(R.color.epi_missed_primary))
                status.second?.toInt()?.let {
                    if (it > 1) {
                        tvEpiStatus.text = context.getString(R.string.missed_vaccination_by_days, it)
                    } else {
                        tvEpiStatus.text = context.getString(R.string.missed_vaccination_by_day, it)
                    }
                }
            }
        }
    }
}
