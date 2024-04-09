package com.medtroniclabs.spice.data.offlinesync.model

import com.medtroniclabs.spice.common.SecuredPreference

data class RequestGetSyncStatus(
    val requestId: String? = null,
    val dataRequired: Boolean = false,
    val statuses: List<String>? = null,
    val types: List<String>? = null,
    val userId: Long = SecuredPreference.getUserId(),
)
