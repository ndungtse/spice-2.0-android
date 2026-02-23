package com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.viewmodel

import androidx.lifecycle.LiveData
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.db.local.RoomHelper
import javax.inject.Inject

class PhysicalExaminationRepository @Inject constructor(
    private var roomHelper: RoomHelper,
) {
    fun getExaminationsComplaintByTypeLiveData(category: String): LiveData<List<MedicalReviewMetaItems>> =
        roomHelper.getExaminationsComplaintByTypeLiveData(category)
}
