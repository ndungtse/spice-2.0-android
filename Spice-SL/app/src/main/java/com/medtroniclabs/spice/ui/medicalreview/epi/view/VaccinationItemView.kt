package com.medtroniclabs.spice.ui.medicalreview.epi.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.model.medicalreview.VaccinationDetail

class VaccinationItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    item: VaccinationDetail? = null,
    callback: (VaccinationDetail) -> Unit?
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val tvImmunisationName: AppCompatTextView
    private val tvImmunisationDate: AppCompatTextView
    private val flEpiStatusNudge: FrameLayout



    init {
        LayoutInflater.from(context).inflate(R.layout.view_epi_detail, this, true)
        tvImmunisationName = findViewById(R.id.tvImmunisationName)
        tvImmunisationDate = findViewById(R.id.tvImmunisationDate)
        flEpiStatusNudge = findViewById(R.id.flEpiStatusNudge)

        tvImmunisationName.text = item?.vaccineName ?: "--"
        tvImmunisationName.setOnClickListener {
            item?.let {
                callback.invoke(it)
            }
        }

        tvImmunisationDate.text = item?.scheduledDate?.let {
            DateUtils.convertDateTimeToDate(
                it,
                DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                DATE_ddMMyyyy
            )
        } ?: "--"

        flEpiStatusNudge.removeAllViews()
        flEpiStatusNudge.addView(VaccinationStatusNudge(context = context, item = item))
    }


}