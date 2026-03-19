package org.medtroniclabs.uhis.ui.household.summary

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/**
 * A custom [RecyclerView] that disables scrolling and flinging gestures.
 *
 * This is useful in scenarios where the [RecyclerView] is placed inside another scrollable
 * container (like a ScrollView or NestedScrollView) and you want the parent to handle the scrolling
 * while the [RecyclerView] remains static but still handles clicks on its items.
 */
class NoScrollRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : RecyclerView(context, attrs) {
    override fun fling(
        velocityX: Int,
        velocityY: Int,
    ): Boolean = false

    /**
     * Intercepts touch events to block MOVE actions (scrolling).
     *
     * @param e The motion event being dispatched.
     * @return True if the action is [MotionEvent.ACTION_MOVE] to intercept the scroll,
     * otherwise delegates to the superclass implementation.
     */
    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        // Block only MOVE (scroll gesture)
        return if (e.action == MotionEvent.ACTION_MOVE) {
            true
        } else {
            super.onInterceptTouchEvent(e)
        }
    }

    /**
     * Prevents handling of MOVE actions to avoid scrolling.
     *
     * @param e The motion event.
     * @return False if the action is [MotionEvent.ACTION_MOVE], otherwise delegates to
     * the superclass implementation.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        // Prevent handling scroll
        return e.action != MotionEvent.ACTION_MOVE && super.onTouchEvent(e)
    }
}
