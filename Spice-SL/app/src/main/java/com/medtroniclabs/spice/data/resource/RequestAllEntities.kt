package com.medtroniclabs.spice.data.resource

import com.medtroniclabs.spice.BuildConfig
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.SecuredPreference

data class RequestAllEntities(
    var villageIds: List<Long> = listOf(),
    val lastSyncTime: String? = null,
    val userId: Long = SecuredPreference.getUserId(),
    val appVersionName: String = BuildConfig.VERSION_NAME,
    val appVersionCode: Int = BuildConfig.VERSION_CODE,
    val deviceId: String? = SecuredPreference.getDeviceId(),
    val appType: String = CommonUtils.isCommunityOrNot()
)