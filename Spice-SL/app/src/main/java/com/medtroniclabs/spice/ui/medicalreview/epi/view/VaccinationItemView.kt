package com.medtroniclabs.spice.ui.medicalreview.epi.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.model.medicalreview.VaccinationDetail
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class VaccinationItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    item: VaccinationDetail? = null,
    callback: (VaccinationDetail) -> Unit?,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val tvImmunisationName: AppCompatTextView
    private val tvImmunisationDate: AppCompatTextView
    private val flEpiStatusNudge: FrameLayout
    private val clRootView: ConstraintLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.view_epi_detail, this, true)
        clRootView = findViewById(R.id.clRootView)
        tvImmunisationName = findViewById(R.id.tvImmunisationName)
        tvImmunisationDate = findViewById(R.id.tvImmunisationDate)
        flEpiStatusNudge = findViewById(R.id.flEpiStatusNudge)

        if (item?.updatedScheduleDate != null) {
            val dayDiff = ChronoUnit.DAYS.between(item.updatedScheduleDate, LocalDate.now())
            if (dayDiff >= 0) {
                enableOrDisableView(true)
            } else {
                enableOrDisableView(false)
            }
            tvImmunisationDate.text =
                item.updatedScheduleDate?.format(DateTimeFormatter.ofPattern(DATE_ddMMyyyy))
        } else {
            tvImmunisationDate.text = item?.scheduledDate?.let {
                DateUtils.convertDateTimeToDate(
                    it,
                    DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DATE_ddMMyyyy,
                )
            } ?: "--"
            enableOrDisableView(false)
        }

        tvImmunisationName.text = item?.vaccineName ?: "--"
        tvImmunisationName.setOnClickListener {
            item?.let {
                callback.invoke(it)
            }
        }

        flEpiStatusNudge.removeAllViews()
        flEpiStatusNudge.addView(VaccinationStatusNudge(context = context, item = item))
    }

    private fun enableOrDisableView(shouldEnable: Boolean) {
        if (shouldEnable) {
            clRootView.alpha = 1.0f
            clRootView.isEnabled = true
            clRootView.isClickable = true
            tvImmunisationName.isEnabled = true
        } else {
            clRootView.alpha = 0.4f
            clRootView.isEnabled = false
            clRootView.isClickable = false
            tvImmunisationName.isEnabled = false
        }
    }
}
