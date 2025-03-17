package com.medtroniclabs.spice.ui.medicalreview.familyplan.viewmodel

import androidx.lifecycle.ViewModel
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class FamilyPlanViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
): ViewModel(){
    var patientId: String? = null
    var memberId: String? = null
    var isFamilyPlanSummary:Boolean = false
    fun getFamilyPlanStaticData(){

    }
}