package org.medtroniclabs.uhis.ui.boarding

/**
 * Cumulative progress (0–100) for resource loading after login.
 * Rough phases: user-data (~20%), static follow-up APIs (~30%), initial sync (~50%).
 */
object ResourceLoadingSyncProgress {
    /** Shown as soon as metadata fetch starts, before user-data API returns (avoids long 0% wait). */
    const val USER_DATA_REQUEST_PENDING = 5
    const val USER_DATA_COMPLETE = 20
    const val FORMS_COMPLETE = 30
    const val TB_SEGMENT_COMPLETE = 37
    const val FORM_METADATA_COMPLETE = 43
    const val METADATA_PHASE_COMPLETE = 50

    const val SYNC_STATUS_COMPLETE = 55
    const val VILLAGE_CHECK_COMPLETE = 62

    /** After incremental fetch for newly linked villages only. */
    const val PARTIAL_VILLAGE_SYNC_DONE = 64
    const val INITIAL_DOWNLOAD_START = 65
    const val SYNCED_PAYLOAD_RECEIVED = 78
    const val ASSESSMENT_HISTORY_RECEIVED = 88
    const val LOCAL_PERSIST_COMPLETE = 100

    const val NCD_FOLLOW_UP_START = 58
    const val NCD_FOLLOW_UP_RESPONSE = 78
}
