package com.medtroniclabs.spice.data.offlinesync.utils

import com.medtroniclabs.spice.BuildConfig
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import java.util.UUID

object OfflineUtils {

    fun getRequestObject(): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        map[OfflineConstant.REQUEST_ID] = UUID.randomUUID().toString()
        map[OfflineConstant.APP_VERSION_NAME] = BuildConfig.VERSION_NAME
        map[OfflineConstant.APP_VERSION_CODE] = BuildConfig.VERSION_CODE
        SecuredPreference.getDeviceId()?.let {
            map[OfflineConstant.DEVICE_ID] = it
        }
        if (CommonUtils.isNonCommunity()) {
            map[DefinedParams.appType] = CommonUtils.isCommunityOrNot()
        }
        return map
    }
}