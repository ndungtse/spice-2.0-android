package com.medtroniclabs.spice.ui.medicalreview.abovefiveyears

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ClinicalNotesViewModel @Inject constructor() : ViewModel() {
    var enteredClinicalNotes = ""
}