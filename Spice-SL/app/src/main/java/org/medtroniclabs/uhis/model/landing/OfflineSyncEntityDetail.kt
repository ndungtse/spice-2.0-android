package org.medtroniclabs.uhis.model.landing

import androidx.annotation.StringRes

data class OfflineSyncEntityDetail(
    @StringRes val labelRes: Int,
    var unSyncedCount: Int,
)
