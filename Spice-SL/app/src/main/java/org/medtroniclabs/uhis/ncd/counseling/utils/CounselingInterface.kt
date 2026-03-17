package org.medtroniclabs.uhis.ncd.counseling.utils

import org.medtroniclabs.uhis.ncd.data.NCDCounselingModel

interface CounselingInterface {
    fun removeElement(model: NCDCounselingModel)
}
