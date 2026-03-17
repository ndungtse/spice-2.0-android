package org.medtroniclabs.uhis.data.offlinesync.model

import org.medtroniclabs.uhis.appextensions.convertToUtcDateTime
import org.medtroniclabs.uhis.common.SecuredPreference

data class ProvanceDto(
    val userId: String = SecuredPreference.getUserFhirId(),
    val organizationId: String = SecuredPreference.getOrganizationFhirId(),
    var modifiedDate: String = System.currentTimeMillis().convertToUtcDateTime(),
    val spiceUserId: Long = SecuredPreference.getOldUserId(),
)
