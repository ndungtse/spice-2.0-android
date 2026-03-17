package org.medtroniclabs.uhis.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.R.styleable
import org.medtroniclabs.uhis.ui.common.FloatingDetectorFrameLayout

class DialogTitleFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : FloatingDetectorFrameLayout(context, attrs) {
    var ivClose: ImageView = ImageView(context)
    var titleView: TextView = TextView(context)

    init {
        initView()
        val a = context.theme.obtainStyledAttributes(
            attrs,
            styleable.DialogTitleFrameLayout,
            0,
            0,
        )
        try {
            a.getString(styleable.DialogTitleFrameLayout_dialogTitle)?.let {
                titleView.text = it
            }
        } finally {
            a.recycle()
        }
    }

    /**
     * init View Here
     */
    private fun initView() {
        val rootView = (
            context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        ).inflate(R.layout.dialog_title, this, true)
        titleView = rootView.findViewById<TextView>(R.id.tvDialogTitle)
        ivClose = rootView.findViewById<ImageView>(R.id.ivClose)
    }
}
