package org.medtroniclabs.uhis.common

import android.view.View

/**
 * Listener which intercepts MultiClick
 */
class MultiClickListener(
    private val targetClicks: Int,
    private val interval: Long = 500L,
    private val onMultiClick: () -> Unit,
) : View.OnClickListener {
    private var clickCount = 0
    private var firstClickTime = 0L

    override fun onClick(v: View?) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - firstClickTime > interval) {
            clickCount = 0
            firstClickTime = currentTime
        }

        clickCount++

        if (clickCount == targetClicks) {
            onMultiClick()
            clickCount = 0
        }
    }
}
