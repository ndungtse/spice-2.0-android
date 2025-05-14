package com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel

import androidx.lifecycle.ViewModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.repo.ExaminationComplaintsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class OpportunisticInfectionViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: ExaminationComplaintsRepository
) : ViewModel() {
    val resultHashMap = hashMapOf<String, HashMap<String, String>>()

}