package com.medtroniclabs.spice.ui.medicalreview.investigation

import com.medtroniclabs.spice.model.medicalreview.InvestigationModel

interface InvestigationListener {
    fun removeInvestigation(investigationGenerator: InvestigationModel)
    fun checkValidation()
}