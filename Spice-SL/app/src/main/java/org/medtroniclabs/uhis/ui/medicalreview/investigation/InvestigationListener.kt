package org.medtroniclabs.uhis.ui.medicalreview.investigation

import org.medtroniclabs.uhis.model.medicalreview.InvestigationModel

interface InvestigationListener {
    fun removeInvestigation(investigationGenerator: InvestigationModel)

    fun checkValidation()

    fun markAsReviewed(
        id: String?,
        comments: String?,
    )
}
