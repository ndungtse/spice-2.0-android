package com.medtroniclabs.spice.ui.medicalreview.epi.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.getLocalDate
import com.medtroniclabs.spice.model.medicalreview.VaccinationGroupItem
import com.medtroniclabs.spice.ui.medicalreview.epi.view.VaccinationStatusNudge.Companion.Missed
import com.medtroniclabs.spice.ui.medicalreview.epi.view.VaccinationStatusNudge.Companion.Upcoming
import com.medtroniclabs.spice.ui.medicalreview.epi.view.VaccinationStatusNudge.Companion.Vaccinated
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class EpiProgressIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private var viewWidth: Float = 0f
    private var viewHeight: Float = 0f
    private var margin: Int = dpToPx(24)

    private var totalEpiPeriod = 0L
    private val vaccinationList = mutableListOf<Pair<Int, String>>()
    private val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w.toFloat()
        viewHeight = h.toFloat()
    }

    fun setVaccinationList(list: List<VaccinationGroupItem>) {
        totalEpiPeriod = list.size.toLong()
        vaccinationList.clear()
        val ldToday = LocalDate.now()

        // #####################
        for (item in list) {
            val scheduleDate = item.scheduleDate.getLocalDate()
            val anyUpdatedScheduleDate =
                item.vaccinationItems.filter { it.updatedScheduleDate != null }.minOfOrNull { it.updatedScheduleDate!! }

            if (anyUpdatedScheduleDate != null && !anyUpdatedScheduleDate.isAfter(ldToday)) { // There is some vaccination case

                if (anyUpdatedScheduleDate.isBefore(ldToday)) {
                    val anyMissed = item.vaccinationItems.any { it.vaccinatedDate == null }
                    if (anyMissed) {
                        vaccinationList.add(Pair(Missed, formatter.format(scheduleDate)))
                    } else {
                        vaccinationList.add(Pair(Vaccinated, formatter.format(scheduleDate)))
                    }
                } else { // Same Day - Upcoming or Vaccinated
                    val anyMissed = item.vaccinationItems.any { it.vaccinatedDate == null }
                    if (anyMissed) {
                        vaccinationList.add(Pair(Upcoming, formatter.format(scheduleDate)))
                    } else {
                        vaccinationList.add(Pair(Vaccinated, formatter.format(scheduleDate)))
                    }
                }
            } else { // No pending vaccination
                break
            }
        }

        // #####################

        /*for (item in list) {
            val scheduleDate = item.scheduleDate.getLocalDate()
            if (scheduleDate.isAfter(ldToday)) { // After - Upcoming
                //vaccinationList.add(Pair(Upcoming, formatter.format(scheduleDate)))
                break
            } else if (scheduleDate.isBefore(ldToday)) { // Before - Missed or Vaccinated
                val anyMissed = item.vaccinationItems.any { it.vaccinatedDate == null }
                if (anyMissed) {
                    vaccinationList.add(Pair(Missed, formatter.format(scheduleDate)))
                } else {
                    vaccinationList.add(Pair(Vaccinated, formatter.format(scheduleDate)))
                }
            } else { // Same Day - Upcoming or Vaccinated
                val anyMissed = item.vaccinationItems.any { it.vaccinatedDate == null }
                if (anyMissed) {
                    vaccinationList.add(Pair(Upcoming, formatter.format(scheduleDate)))
                } else {
                    vaccinationList.add(Pair(Vaccinated, formatter.format(scheduleDate)))
                }
            }
        }*/

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
            style = Paint.Style.FILL
        }

        // bar width = full width + margin
        // bar height = 16% of Height
        val startX = 0f + margin
        val startY = (viewHeight * 42) / 100

        val endX = viewWidth - margin
        val endY = (viewHeight * 58) / 100
        val rectF = RectF(startX, startY, endX, endY)
        canvas.drawRoundRect(rectF, 30f, 30f, paint)

        val progressBarWidth = viewWidth - (2 * margin)

        val oneDayDisplacement = progressBarWidth / totalEpiPeriod
        val centerY = viewHeight / 2
        var centerX = 0f

        val txtSize = (progressBarWidth * 1) / 100
        val txtPointY = (viewHeight * 80) / 100
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = txtSize
            textAlign = Paint.Align.CENTER
        }

        val statusBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        var barStartX = 0f
        var barEndX = 0f
        vaccinationList.forEachIndexed { index, item ->
            val txt = item.second

            statusBarPaint.color = getStatusColor(item.first)
            barStartX = margin + (index * oneDayDisplacement)
            barEndX = margin + ((index + 1) * oneDayDisplacement)

            val statusBarRectF = RectF(barStartX, startY, barEndX, endY)
            canvas.drawRoundRect(statusBarRectF, 30f, 30f, statusBarPaint)

            val textBounds = Rect()
            textPaint.getTextBounds(txt, 0, txt.length, textBounds)
            val textHeight = textBounds.height()
            canvas.drawText(txt, barEndX, txtPointY + textHeight / 2f, textPaint)
        }

        val periodPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val borderColorRadius = (viewHeight * 17) / 100
        val statusColorRadius = (viewHeight * 12) / 100

        vaccinationList.forEachIndexed { index, item ->
            statusBarPaint.color = getStatusColor(item.first)
            centerX = margin + ((index + 1) * oneDayDisplacement)

            canvas.drawCircle(centerX, centerY, borderColorRadius, periodPaint)
            canvas.drawCircle(centerX, centerY, borderColorRadius, borderPaint)

            canvas.drawCircle(centerX, centerY, statusColorRadius, statusBarPaint)

            getBitmap(item.first)?.let {
                val bmpX = centerX - (it.width / 2f)
                val bmpY = centerY - (it.height / 2f)
                canvas.drawBitmap(it, bmpX, bmpY, null)
            }
        }
    }

    private fun getStatusColor(status: Int): Int =
        when (status) {
            Upcoming -> ContextCompat.getColor(context, R.color.epi_upcoming_primary)
            Vaccinated -> ContextCompat.getColor(context, R.color.epi_vaccinated_primary)
            else -> ContextCompat.getColor(context, R.color.epi_missed_primary)
        }

    private fun getBitmap(status: Int): Bitmap? =
        when (status) {
            Upcoming -> getBitmapFromVectorDrawable(R.drawable.ic_epi_upcoming)
            Vaccinated -> getBitmapFromVectorDrawable(R.drawable.ic_epi_vaccinated)
            else -> getBitmapFromVectorDrawable(R.drawable.ic_epi_missed)
        }

    private fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
        val drawable: Drawable = context.getDrawable(drawableId)!!
        if (drawable is VectorDrawable) {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888,
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
        return null
    }
}
