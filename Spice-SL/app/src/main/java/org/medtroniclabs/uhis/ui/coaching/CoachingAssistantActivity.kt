package org.medtroniclabs.uhis.ui.coaching

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.medtroniclabs.microcoaching.ui.chat.CoachingChatFragment
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.ui.BaseActivity

/**
 * Wrapper Activity that hosts the MicroCoaching SDK's [CoachingChatFragment] inside
 * the standard SPICE toolbar. Launched from the drawer menu (R.id.chwAssistant) in
 * [org.medtroniclabs.uhis.ui.landing.LandingActivity].
 *
 * Mirrors the v1 spice-android pattern. The SDK Fragment manages its own state and
 * on-device LLM invocation; this Activity only provides the toolbar + container.
 */
@AndroidEntryPoint
class CoachingAssistantActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentView = layoutInflater.inflate(R.layout.activity_coaching_assistant, null)
        setMainContentView(
            view = contentView,
            isToolbarVisible = true,
            title = getString(R.string.chw_assistant),
            homeAndBackVisibility = Pair(false, true),
        )
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.coaching_assistant_container, CoachingChatFragment.newInstance())
                .commit()
        }
    }

    companion object {
        fun launch(context: Context) = context.startActivity(Intent(context, CoachingAssistantActivity::class.java))
    }
}
