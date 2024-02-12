package com.medtroniclabs.spice.ui.mypatients.viewmodel

import androidx.lifecycle.ViewModel
import com.medtroniclabs.spice.common.DefinedParams
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PatientListViewModel @Inject constructor(): ViewModel() {

    //Patient list - Grid count
    var spanCount: Int = DefinedParams.span_count_1
}