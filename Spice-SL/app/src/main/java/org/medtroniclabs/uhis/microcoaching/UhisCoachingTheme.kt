package org.medtroniclabs.uhis.microcoaching

import androidx.compose.ui.graphics.Color
import com.medtroniclabs.microcoaching.ui.theme.CoachingColors

/**
 * UHIS brand palette for the MicroCoaching SDK.
 *
 * The SDK ships a SPICE-blue default; this replaces the brand family with the UHIS
 * magenta from the app icon. Registered once at init via
 * `MicroCoachingSDK.Builder.theme(UhisCoachingColors)` — the SDK owns its own Compose
 * roots, so a theme cannot be supplied by wrapping its content.
 *
 * Only the brand tokens are overridden. Everything else — the neutral text ramp,
 * success/warning/error/reward families, locked states — stays at the SDK's defaults,
 * which is the point of `copy`: a host states what it cares about and inherits a
 * coherent design for the rest.
 *
 * Colours the SDK *derives* from [CoachingColors.primary] follow automatically: the chat
 * bubbles, the SK-detail header gradient, progress-bar tracks and the status bar all
 * become magenta without being named here.
 */

/** UHIS magenta, taken from the app icon. */
private val UhisMagenta = Color(0xFFD6218C)

/** Deep magenta for header text and on-container content. */
private val UhisMagentaDark = Color(0xFF8E1259)

/** Soft magenta wash for chips, banners and selected states. */
private val UhisMagentaContainer = Color(0xFFFBE3F0)

/**
 * The token set handed to the SDK.
 *
 * Note on contrast: white on [UhisMagenta] is 4.7:1, which clears WCAG AA. But the
 * derived outgoing chat bubble is `primary` at 70% over white — a much paler pink where
 * white text would only reach ~3.2:1. The SDK picks bubble and badge label colours by
 * measured contrast rather than assuming white, so those labels come out dark
 * automatically. Nothing here has to compensate for it.
 */
val UhisCoachingColors: CoachingColors = CoachingColors.Spice.copy(
    primary = UhisMagenta,
    primaryContainer = UhisMagentaContainer,
    onPrimaryContainer = UhisMagentaDark,
    secondary = UhisMagentaDark,
)
