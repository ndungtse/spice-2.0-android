package org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.viewmodel

import androidx.lifecycle.LiveData
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.db.local.RoomHelper
import javax.inject.Inject

class PhysicalExaminationRepository @Inject constructor(
    private var roomHelper: RoomHelper,
) {
    fun getExaminationsComplaintByTypeLiveData(category: String): LiveData<List<MedicalReviewMetaItems>> =
        roomHelper.getExaminationsComplaintByTypeLiveData(category)
}
