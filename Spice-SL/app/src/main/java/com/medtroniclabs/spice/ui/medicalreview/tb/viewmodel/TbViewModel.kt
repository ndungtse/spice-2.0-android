package com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel

import androidx.lifecycle.ViewModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class TbViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository
) : ViewModel() {
    var patientId: String? = null
    var memberId: String? = null
    fun getTbStaticData() {

    }
}