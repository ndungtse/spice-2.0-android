package com.medtroniclabs.spice.ui.assessment.referrallogic.utils

import com.medtroniclabs.spice.mappingkey.UnderFiveYearExaminationKeyMapping.DiseaseName.generalDangerSigns
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH

enum class ReferralReasons {
    GeneralDangerSigns,
    Fever,
    Pneumonia,
    Malaria,
    Symptoms,
    Diarrhoea,
    MUAC,
    ANCSigns,
    PNCMotherSigns,
    childhoodVisitSigns,
    PNCNeonateSigns,
    Cough,
    Miscarriage,
    NCD,
    NCDSymptoms,
    ;

    companion object {
        fun aliasOf(value: ReferralReasons): String =
            when (value) {
                ANCSigns -> RMNCH.ancSignsLabel
                PNCMotherSigns -> RMNCH.pncMotherSignsLabel
                childhoodVisitSigns -> RMNCH.childhoodVisitSignsLabel
                PNCNeonateSigns -> RMNCH.pncNeonateSignsLabel
                GeneralDangerSigns -> generalDangerSigns
                else -> value.name
            }
    }
}
