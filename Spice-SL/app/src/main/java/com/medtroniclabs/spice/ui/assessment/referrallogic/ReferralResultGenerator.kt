package com.medtroniclabs.spice.ui.assessment.referrallogic

import android.content.Context
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams.CBS_Referral
import com.medtroniclabs.spice.common.DefinedParams.Referred_NCD
import com.medtroniclabs.spice.formgeneration.config.DefinedParams
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.NoSymptoms
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails
import com.medtroniclabs.spice.ui.MenuConstants.NCD_MENU_ID
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.AlcoholConsumption
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.DiagnosedWithDiabetes
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Dispensed
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.DryMouthOrTongue
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FB_MAX_BREATHING
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FB_MAX_MONTH
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FB_MIN_BREATHING
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FB_MIN_MONTH
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.HasCoughLastedLonger
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.HasNightSweatsTB
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.HasWeightLoss
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.LittleOrNoUrine
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.NoTearsWhenCrying
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.RegularSmoker
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.RelationshipToIC
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.SkinPinch
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.SleepLocation
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.SunkenEyes
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.SunkenFontanella
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.VeryThirsty
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.otherSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.symptomsDTO
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.ACT
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.Amoxicillin
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.BreathPerMinute
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.ChestInDrawing
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.Diarrhoea
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.DiarrhoeaSigns
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.HasFever
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.IsBloodyDiarrhoea
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.MaxDaysCough
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.MaxDaysOfDiarrhoea
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.MaxDaysOfFever
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.MaxTemperature
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.NA
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.NoOfDaysOfCough
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.NoOfDaysOfDiarrhoea
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.NoOfDaysOfFever
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.OrsDispensedStatus
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.RdtNegative
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.RdtPositive
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.RdtTest
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.Red
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.Temperature
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.Yellow
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.Yes
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.ZincDispensedStatus
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.hasCough
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.hasDiarrhoea
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.hasOedemaOfBothFeet
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.isBreastfeed
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.isConvulsionPastFewDays
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.isUnusualSleepy
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.isVomiting
import com.medtroniclabs.spice.ui.assessment.referrallogic.model.ReferralDefinedParams.muacCode
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralReasons
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.DeathOfMother
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.Miscarriage
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.deathOfBaby
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.deathOfNewborn
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.getDeathStatus
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.lowBirthWeight
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.newbornReferredToSBCU

class ReferralResultGenerator {
    private var patientStatus = HashMap<String, Any>()
    private val referralReason = arrayListOf<String>()

    val MUAC = "muac"

    fun calculateIccmReferralResult(
        map: HashMap<String, Any>,
        memberDetails: AssessmentMemberDetails?,
    ): Pair<String?, ArrayList<String>> {
        calculateDangerSignsResult(map)
        calculateNutritionalStatusResult(map)
        calculateCoughStatus(map, memberDetails)
        calculateFeverStatus(map, ReferralReasons.Fever.name)
        calculateDiarrhoeaStatus(map)
        return Pair(checkStatus(), referralReason)
    }

    fun calculateRMNCHReferralResult(
        map: HashMap<String, Any>,
        isChild: Boolean = false,
    ): Pair<String?, ArrayList<String>> {
        if (map.containsKey(RMNCH.ANC)) {
            val deathOfMother = getDeathStatus(map, RMNCH.ANC, DeathOfMother)
            if (!deathOfMother) {
                if (checkMiscarrageStatus(map, RMNCH.ANC, Miscarriage)) {
                    addResultMap(ReferralReasons.Miscarriage.name, ReferralStatus.Referred.name)
                    addReferralReason(referralReason, ReferralReasons.Miscarriage.name)
                }
                findSignListByWorkflow(
                    RMNCH.ANC,
                    map,
                    RMNCH.ancSigns,
                    ReferralReasons.aliasOf(ReferralReasons.ANCSigns),
                )
                updateVisitCount(map, RMNCH.ANC)
            }
        } else if (map.containsKey(RMNCH.ChildHoodVisit)) {
            val deathOfBaby = getDeathStatus(map, RMNCH.ChildHoodVisit, deathOfBaby)
            if (!deathOfBaby) {
                if (checkMUACReferralStatus(map, RMNCH.ChildHoodVisit, MUAC)) {
                    addResultMap(ReferralReasons.MUAC.name, ReferralStatus.Referred.name)
                    addReferralReason(referralReason, ReferralReasons.MUAC.name)
                }
                findSignListByWorkflow(
                    RMNCH.ChildHoodVisit,
                    map,
                    RMNCH.childhoodVisitSigns,
                    ReferralReasons.aliasOf(ReferralReasons.childhoodVisitSigns),
                )
                updateVisitCount(map, RMNCH.ChildHoodVisit)
            }
        } else {
            val deathOfNewBorn = getDeathStatus(map, RMNCH.PNCNeonatal, deathOfNewborn)
            if (isChild) {
                if (!deathOfNewBorn) {
                    findSignListByWorkflow(
                        RMNCH.PNCNeonatal,
                        map,
                        RMNCH.pncNeonateSigns,
                        ReferralReasons.aliasOf(ReferralReasons.PNCNeonateSigns),
                    )

                    if (checkMiscarrageStatus(map, RMNCH.PNCNeonatal, newbornReferredToSBCU)) {
                        addResultMap("", ReferralStatus.Referred.name)
                        addReferralReason(referralReason, "")
                    }

                    if (checkMiscarrageStatus(map, RMNCH.PNCNeonatal, lowBirthWeight)) {
                        addResultMap("", ReferralStatus.Referred.name)
                        addReferralReason(referralReason, "")
                    }

                    updateVisitCount(map, RMNCH.PNC)
                }
            } else {
                findSignListByWorkflow(
                    RMNCH.PNC,
                    map,
                    RMNCH.pncMotherSigns,
                    ReferralReasons.aliasOf(ReferralReasons.PNCMotherSigns),
                )
                if (!deathOfNewBorn) {
                    findSignListByWorkflow(
                        RMNCH.PNCNeonatal,
                        map,
                        RMNCH.pncNeonateSigns,
                        ReferralReasons.aliasOf(ReferralReasons.PNCNeonateSigns),
                    )
                    if (checkMiscarrageStatus(map, RMNCH.PNCNeonatal, newbornReferredToSBCU)) {
                        addResultMap("", ReferralStatus.Referred.name)
                        addReferralReason(referralReason, "")
                    }

                    if (checkMiscarrageStatus(map, RMNCH.PNCNeonatal, lowBirthWeight)) {
                        addResultMap("", ReferralStatus.Referred.name)
                        addReferralReason(referralReason, "")
                    }
                }
                updateVisitCount(map, RMNCH.PNC)
            }
        }
        return Pair(checkStatus(), referralReason)
    }

    private fun updateVisitCount(
        map: HashMap<String, Any>,
        workFlow: String,
    ) {
        var visitInfo = ""
        val workflowMap = map[workFlow]
        if (workflowMap is Map<*, *> && workflowMap.containsKey(RMNCH.visitNo)) {
            val visitNo = workflowMap[RMNCH.visitNo]
            visitInfo = "${getVisitLabel(workFlow)} $visitNo"
        }

        while (referralReason.isNotEmpty() && referralReason[0].isEmpty()) {
            referralReason.removeAt(0)
        }

        if (referralReason.isEmpty()) {
            referralReason.add(visitInfo)
            return
        }

        for (i in referralReason.size - 1 downTo 0) {
            if (referralReason[i].isNotEmpty()) {
                referralReason[i] = "${referralReason[i]} - $visitInfo"
                return
            }
        }

        referralReason.add(visitInfo)

       /* if (referralReason.isNotEmpty()) {
            var lastReason = referralReason[referralReason.size - 1]
            val workflowMap = map[workFlow]
            if (workflowMap is Map<*, *> && workflowMap.containsKey(RMNCH.visitNo)) {
                val visitNo = workflowMap[RMNCH.visitNo]
                if (visitNo is Long) {
                    if (lastReason.trim().isNotEmpty()) {
                        lastReason = "$lastReason - ${getVisitLabel(workFlow)} $visitNo"
                    } else {
                        lastReason = "${getVisitLabel(workFlow)} $visitNo"
                    }
                    referralReason[referralReason.size - 1] = lastReason
                }
            }
        }*/
    }

    private fun getVisitLabel(workFlow: String): String {
        when (workFlow) {
            RMNCH.ANC -> {
                return RMNCH.ANCVisitNo
            }

            RMNCH.ChildHoodVisit -> {
                return RMNCH.ChildHoodVisitNo
            }

            RMNCH.PNC -> {
                return RMNCH.PNCVisitNo
            }
        }

        return "Visit No: "
    }

    private fun checkMUACReferralStatus(
        map: HashMap<String, Any>,
        workflow: String,
        key: String,
    ): Boolean {
        val workflowMap = map[workflow]
        if (workflowMap is Map<*, *> && workflowMap.containsKey(key)) {
            val value = workflowMap[key]
            if (value is String) {
                if (value.equals(Red, true) || value.equals(Yellow, true)) {
                    return true
                }
            }
        }
        return false
    }

    private fun checkMiscarrageStatus(
        map: HashMap<String, Any>,
        workflow: String,
        key: String,
    ): Boolean {
        val workflowMap = map[workflow]
        if (workflowMap is Map<*, *> && workflowMap.containsKey(key)) {
            val value = workflowMap[key]
            if (value is Boolean) {
                return value
            }
        }
        return false
    }

    private fun findSignListByWorkflow(
        workflow: String,
        map: HashMap<String, Any>,
        signType: String,
        referralReasonName: String,
    ) {
        val workflowMap = map[workflow]
        if (workflowMap is Map<*, *> && workflowMap.containsKey(signType)) {
            val ancList = workflowMap[signType]
            if (ancList is ArrayList<*> && ancList.size > 0) {
                if (!(ancList.any { (it as Map<*, *>)[DefinedParams.NAME] == NoSymptoms })) {
                    addResultMap(referralReasonName, ReferralStatus.Referred.name)
                    addReferralReason(referralReason, referralReasonName)
                }
            }
        }
    }

    private fun formatDiarrhoeaSigns(map: HashMap<String, Any>) {
        if (map.containsKey(DiarrhoeaSigns) && map[DiarrhoeaSigns] is List<*>) {
            val signsList = mutableListOf<String>()
            val list = map[DiarrhoeaSigns] as List<*>
            list.forEach { it ->
                if (it is HashMap<*, *>) {
                    signsList.add(it["name"] as String)
                }
            }
            map[DiarrhoeaSigns] = signsList
        }
    }

    fun calculateOtherSymptomsReferralResult(map: HashMap<String, Any>): Pair<String?, ArrayList<String>> {
        calculateSymptomsStatus(map, otherSymptoms, ReferralReasons.Symptoms.name, ReferralReasons.Symptoms.name.lowercase())
        calculateFeverStatus(map, ReferralReasons.Fever.name)
        return Pair(checkStatus(), referralReason)
    }

    fun calculateTBReferralResult(map: HashMap<String, Any>): Pair<String?, ArrayList<String>> {
        val hasCough = map[hasCough] is Boolean && map[hasCough] == true
        val hasCoughLastLonger = map.containsKey(HasCoughLastedLonger) && map[HasCoughLastedLonger] is Boolean && map[HasCoughLastedLonger] == true
        val isContactTracing = map.containsKey(RelationshipToIC) && map[RelationshipToIC] is String && map[RelationshipToIC] != ""
        if (hasCough && hasCoughLastLonger || isContactTracing) {
            if ((map[HasNightSweatsTB] is Boolean && map[HasNightSweatsTB] == true) ||
                (map[HasFever] is Boolean && map[HasFever] == true) ||
                (map[HasWeightLoss] is Boolean && map[HasWeightLoss] == true) ||
                (map[RelationshipToIC] is String && map[RelationshipToIC] != "") ||
                (map[SleepLocation] is String && map[SleepLocation] != "")
            ) {
                addResultMap("TB Symptoms", ReferralStatus.Referred.name)
                addReferralReason(referralReason, "TB Symptoms")
            }
        }
        return Pair(checkStatus(), referralReason)
    }

    fun calculateCBSReferralResult(map: HashMap<String, Any>): Pair<String?, ArrayList<String>> {
        addResultMap(CBS_Referral, ReferralStatus.Referred.name)
        addReferralReason(referralReason, CBS_Referral)
        return Pair(checkStatus(), referralReason)
    }

    private fun formatSignsAndSymptoms(map: HashMap<String, Any>) {
        if (map.containsKey(otherSymptoms) && map[otherSymptoms] is List<*>) {
            val signsList = mutableListOf<String>()
            val list = map[otherSymptoms] as List<*>
            list.forEach { it ->
                if (it is HashMap<*, *>) {
                    signsList.add(it["name"] as String)
                }
            }
            map[otherSymptoms] = signsList
        }
    }

    private fun addResultMap(
        key: String,
        value: String?,
    ) {
        if (value is String) {
            patientStatus[key] = value
        }
    }

    /**
     * Referral Logic for ICCM General Danger Signs
     * If any of the signs is true, then the patient status is referred
     */
    private fun calculateDangerSignsResult(map: HashMap<String, Any>) {
        val generalDangerSignsList =
            listOf(isUnusualSleepy, isConvulsionPastFewDays, isVomiting, isBreastfeed)
        for (key in generalDangerSignsList) {
            if (map.containsKey(key) && ((map[key] is Boolean && map[key] == true) || map[key] is String && map[key] == Yes)) {
                addResultMap(
                    ReferralReasons.GeneralDangerSigns.name.lowercase(),
                    ReferralStatus.Referred.name,
                )
                addReferralReason(
                    referralReason,
                    ReferralReasons.aliasOf(ReferralReasons.GeneralDangerSigns),
                )
                break
            }
        }
    }

    /**
     * Referral Logic for ICCM Nutritional Status
     * If nutritional code is red or yellow, then patientStatus is referred
     * If nutritional code is green and odema of both feet is true or Yes, then patientStatus is referred
     */
    private fun calculateNutritionalStatusResult(map: HashMap<String, Any>) {
        if (map.containsKey(muacCode)) {
            val muacCodeValue = map[muacCode]
            if (muacCodeValue is String && (muacCodeValue.lowercase() == Red.lowercase() || muacCodeValue.lowercase() == Yellow.lowercase())) {
                addResultMap(ReferralReasons.MUAC.name.lowercase(), ReferralStatus.Referred.name)
                addReferralReason(referralReason, ReferralReasons.MUAC.name)
            }
        }
        if (map.containsKey(hasOedemaOfBothFeet) &&
            (
                (map[hasOedemaOfBothFeet] is String && map[hasOedemaOfBothFeet] == Yes) ||
                    (map[hasOedemaOfBothFeet] is Boolean && map[hasOedemaOfBothFeet] == true)
            )
        ) {
            addResultMap(ReferralReasons.MUAC.name.lowercase(), ReferralStatus.Referred.name)
            addReferralReason(referralReason, ReferralReasons.MUAC.name)
        }
    }

    /**
     * Referral Logic for ICCM Cough
     * If no of days is >= 21, then patientStatus is referred
     * If no of days is < 21,
     *  1. If chest indrawing is yes, then patientStatus is referred
     *  2. If patient has Fast breathing & Amoxicillin is NA, then patientStatus is referred
     *  3. If patient has Fast breathing & Amoxicillin is Dispensed, then patientStatus is on-treatment
     */
    private fun calculateCoughStatus(
        map: HashMap<String, Any>,
        memberDetails: AssessmentMemberDetails?,
    ) {
        if (map.containsKey(hasCough)) {
            if ((map[hasCough] is String && map[hasCough] == Yes) || (map[hasCough] is Boolean && map[hasCough] == true)) {
                // Condition for fast breathing
                if (map.containsKey(BreathPerMinute) && map[BreathPerMinute] is Int) {
                    val bpmValue = map[BreathPerMinute] as Int
                    memberDetails?.let { details ->
                        DateUtils.dateToMonths(details.dateOfBirth).let { month ->
                            month?.let {
                                if ((month in FB_MIN_MONTH..11) && bpmValue >= FB_MAX_BREATHING) {
                                    addResultMap(
                                        ReferralReasons.Pneumonia.name.lowercase(),
                                        getMedicationStatus(map, Amoxicillin),
                                    )
                                    addReferralReason(
                                        referralReason,
                                        ReferralReasons.Pneumonia.name,
                                    )
                                    return
                                } else if (month in FB_MAX_MONTH..60 && bpmValue >= FB_MIN_BREATHING) {
                                    addResultMap(
                                        ReferralReasons.Pneumonia.name.lowercase(),
                                        getMedicationStatus(map, Amoxicillin),
                                    )
                                    addReferralReason(
                                        referralReason,
                                        ReferralReasons.Pneumonia.name,
                                    )
                                    return
                                }
                            }
                        }
                    }
                }

                // Condition for No of days cough
                if (map.containsKey(NoOfDaysOfCough) && map[NoOfDaysOfCough] is Int) {
                    val noOfDays = map[NoOfDaysOfCough] as Int
                    if (noOfDays >= MaxDaysCough) {
                        addResultMap(
                            ReferralReasons.Pneumonia.name.lowercase(),
                            ReferralStatus.Referred.name,
                        )
                        addReferralReason(referralReason, ReferralReasons.Cough.name)
                        return
                    }
                }

                // Condition for Chest In Drawing
                if (map.containsKey(ChestInDrawing) &&
                    ((map[ChestInDrawing] is String && map[ChestInDrawing] == Yes) || (map[ChestInDrawing] is Boolean && map[ChestInDrawing] == true))
                ) {
                    addResultMap(
                        ReferralReasons.Pneumonia.name.lowercase(),
                        ReferralStatus.Referred.name,
                    )
                    addReferralReason(referralReason, ReferralReasons.Cough.name)
                    return
                }
            }
        }
    }

    /**
     * Referral Logic for ICCM Fever
     * If fever is yes no of days is >= 7, then patientStatus is referred
     * If no of days is < 7,
     *  1. If RDT test is -Ve or NA, then patientStatus is referred
     *  2. If RDT test is positive & ACT is NA, then patientStatus is referred
     *  3. If RDT test is positive & ACT is Dispensed, then patientStatus is on-treatment
     * else If temperature is >=37.5, then patientStatus is referred
     */
    private fun calculateFeverStatus(
        map: HashMap<String, Any>,
        referralKey: String,
    ) {
        if (map.containsKey(HasFever)) {
            if ((map[HasFever] is String && map[HasFever] == Yes) || (map[HasFever] is Boolean && map[HasFever] == true)) {
                if (map.containsKey(NoOfDaysOfFever) && map[NoOfDaysOfFever] is Int) {
                    val noOfDays = map[NoOfDaysOfFever] as Int
                    if (noOfDays >= MaxDaysOfFever) {
                        addResultMap(referralKey.lowercase(), ReferralStatus.Referred.name)
                        rdtReferralStatus(map, referralKey)
                    } else if (map.containsKey(Temperature) && map[Temperature] is Double && (map[Temperature] as Double) >= MaxTemperature) {
                        addResultMap(referralKey.lowercase(), ReferralStatus.Referred.name)
                        rdtReferralStatus(map, referralKey)
                    } else {
                        if (map.containsKey(RdtTest) && map[RdtTest] == RdtPositive) {
                            addResultMap(referralKey.lowercase(), getMedicationStatus(map, ACT))
                            addReferralReason(referralReason, ReferralReasons.Malaria.name)
                        } else if (map.containsKey(RdtTest) && (map[RdtTest] == RdtNegative || map[RdtTest] == NA)) {
                            addResultMap(referralKey.lowercase(), ReferralStatus.Referred.name)
                            addReferralReason(referralReason, referralKey)
                        }
                    }
                }
            }
        }
    }

    fun rdtReferralStatus(
        map: HashMap<String, Any>,
        referralKey: String,
    ) {
        if (map.containsKey(RdtTest) && map[RdtTest] == RdtPositive) {
            addReferralReason(referralReason, ReferralReasons.Malaria.name)
        } else {
            addReferralReason(referralReason, referralKey)
        }
    }

    /**
     * Referral Logic for ICCM Diarrhoea
     * If Bloody diarrhoea is yes, then patientStatus is referred
     * If no of days is >= 14, then patientStatus is referred
     * If no of days is < 14,
     *  1. If Any one of the respective dehydration signs is present, then patientStatus is referred
     *  2. If ORS or Zinc is NA, then patientStatus is referred
     *  3. If ORS and Zinc is Dispensed, then patientStatus is On Treatment
     */
    private fun calculateDiarrhoeaStatus(map: HashMap<String, Any>) {
        if (map.containsKey(hasDiarrhoea)) {
            if ((map[hasDiarrhoea] is String && map[hasDiarrhoea] == Yes) || (map[hasDiarrhoea] is Boolean && map[hasDiarrhoea] == true)) {
                if (map.containsKey(IsBloodyDiarrhoea) &&
                    (
                        (map[IsBloodyDiarrhoea] is String && map[IsBloodyDiarrhoea] == Yes) ||
                            (map[IsBloodyDiarrhoea] is Boolean && map[IsBloodyDiarrhoea] == true)
                    )
                ) {
                    addResultMap(
                        ReferralReasons.Diarrhoea.name.lowercase(),
                        ReferralStatus.Referred.name,
                    )
                    addReferralReason(referralReason, ReferralReasons.Diarrhoea.name)
                } else if (map.containsKey(NoOfDaysOfDiarrhoea) && map[NoOfDaysOfDiarrhoea] is Int) {
                    val noOfDays = map[NoOfDaysOfDiarrhoea] as Int
                    if (noOfDays >= MaxDaysOfDiarrhoea) {
                        addResultMap(
                            ReferralReasons.Diarrhoea.name.lowercase(),
                            ReferralStatus.Referred.name,
                        )
                        addReferralReason(referralReason, ReferralReasons.Diarrhoea.name)
                    } else {
                        if ((map.containsKey(DiarrhoeaSigns) && map[DiarrhoeaSigns] is ArrayList<*>) &&
                            (
                                getDiarrhoeaSignsStatus(
                                    map[DiarrhoeaSigns],
                                ) != null
                            )
                        ) {
                            addResultMap(
                                ReferralReasons.Diarrhoea.name.lowercase(),
                                getDiarrhoeaSignsStatus(map[DiarrhoeaSigns]),
                            )
                            if (patientStatus.containsKey(Diarrhoea) && patientStatus[Diarrhoea] == ReferralStatus.Referred.name) {
                                addReferralReason(
                                    referralReason,
                                    ReferralReasons.Diarrhoea.name,
                                )
                            }
                        } else if ((map.containsKey(OrsDispensedStatus) && map[OrsDispensedStatus] == NA) ||
                            (
                                map.containsKey(
                                    ZincDispensedStatus,
                                ) &&
                                    map[ZincDispensedStatus] == NA
                            )
                        ) {
                            addResultMap(
                                ReferralReasons.Diarrhoea.name.lowercase(),
                                ReferralStatus.Referred.name,
                            )
                            addReferralReason(referralReason, ReferralReasons.Diarrhoea.name)
                        } else if ((map.containsKey(OrsDispensedStatus) && map[OrsDispensedStatus] == Dispensed) &&
                            (
                                map.containsKey(
                                    ZincDispensedStatus,
                                ) &&
                                    map[ZincDispensedStatus] == Dispensed
                            )
                        ) {
                            addResultMap(
                                ReferralReasons.Diarrhoea.name.lowercase(),
                                ReferralStatus.OnTreatment.name,
                            )
                            addReferralReason(referralReason, ReferralReasons.Diarrhoea.name)
                        }
                    }
                }
            }
        }
    }

    private fun getDiarrhoeaSignsStatus(value: Any?): String? {
        var status: String? = null
        val signsList = listOf(
            SunkenEyes.lowercase(),
            NoTearsWhenCrying.lowercase(),
            LittleOrNoUrine.lowercase(),
            SkinPinch.lowercase(),
            VeryThirsty.lowercase(),
            DryMouthOrTongue.lowercase(),
            SunkenFontanella.lowercase(),
        )
        val selectedSignsList = ArrayList<String>()
        if (value is ArrayList<*>) {
            value.forEach { map ->
                CommonUtils.getListActual(map)?.let {
                    selectedSignsList.add(it.lowercase())
                }
            }
        }
        for (item in signsList) {
            if (selectedSignsList.contains(item)) {
                status = ReferralStatus.Referred.name
                break
            }
        }
        return status
    }

    private fun addReferralReason(
        referralReason: ArrayList<String>,
        key: String,
    ) {
        if (!referralReason.contains(key)) referralReason.add(key)
    }

    /**
     * This method is to determine the medication status, according to ICCM
     * 1. If Status is NA, then status is under referred
     * 2. If Status is Dispensed, then status is under On Treatment
     * 3. Else it will be null
     */
    private fun getMedicationStatus(
        map: HashMap<String, Any>,
        key: String,
    ): String? {
        val status = if (map.containsKey(key) && map[key] is String) {
            if (map[key] == NA) {
                ReferralStatus.Referred.name
            } else if (map[key] == Dispensed) {
                ReferralStatus.OnTreatment.name
            } else {
                null
            }
        } else {
            null
        }
        return status
    }

    private fun calculateSymptomsStatus(
        map: HashMap<String, Any>,
        symptomType: String,
        referedReason: String,
        referralKey: String,
    ) {
        val selectedSignsList = ArrayList<String>()
        if (map.containsKey(symptomType) && map[symptomType] is ArrayList<*>) {
            (map[symptomType] as ArrayList<*>).forEach { result ->
                CommonUtils.getListActual(result)?.let {
                    selectedSignsList.add(it.lowercase())
                }
            }
        }
        if (!selectedSignsList.contains(NoSymptoms.lowercase())) {
            addResultMap(referralKey, ReferralStatus.Referred.name)
            addReferralReason(referralReason, referedReason)
        }
    }

    private fun checkStatus(): String? {
        if (patientStatus.containsValue(ReferralStatus.Referred.name)) {
            return ReferralStatus.Referred.name
        } else if (patientStatus.containsValue(ReferralStatus.OnTreatment.name)) {
            return ReferralStatus.OnTreatment.name
        }
        return null
    }

    fun calculateFamilyPlanningStatus(map: HashMap<String, Any>): Pair<String?, ArrayList<String>> {
        // Check if referral field is set to "referred"
        val referralValue = map["referral"] as? String
        val isReferred = referralValue == "referred"

//        val memberUsingAnyFamilyPlanning =
//            map[MemberUsingAnyFamilyPlanning] is Boolean && map[MemberUsingAnyFamilyPlanning] == false
//        val isAnySideEffects = map[IsAnySideEffects] is Boolean && map[IsAnySideEffects] == true
//        val needOfOtherFamilyPlanning =
//            map[NeedOfOtherFamilyPlanning] is Boolean && map[NeedOfOtherFamilyPlanning] == true
//         val condomStatus = map[CondomsStatus] is String && map[CondomsStatus] == Not_Available
//         val contraceptiveStatus = map[Contraceptive] is String && map[Contraceptive] == Not_Available
        if (isReferred) {
            addResultMap("Family Planning", ReferralStatus.Referred.name)
            addReferralReason(referralReason, "Family Planning Consult")
        }
        return Pair(checkStatus(), referralReason)
    }

    fun calculateNCDStatus(
        context: Context,
        map: HashMap<String, Any>,
    ): Pair<String?, ArrayList<String>> {
        val diagnosedWithDiabetes =
            map[DiagnosedWithDiabetes] is Boolean && map[DiagnosedWithDiabetes] == true
        val regularSmoker = map[RegularSmoker] is Boolean && map[RegularSmoker] == true
        val alcoholConsumption =
            map[AlcoholConsumption] is Boolean && map[AlcoholConsumption] == true
        val bmiReferral = getBmiReferral(CommonUtils.getBMIInformation(context, map[Screening.BMI] as? Double), context)
        calculateSymptomsStatus(map, symptomsDTO, Referred_NCD, NCD_MENU_ID)
        if ((!patientStatus.containsKey(NCD_MENU_ID)) && (diagnosedWithDiabetes || regularSmoker || alcoholConsumption || bmiReferral)) {
            addResultMap(NCD_MENU_ID, ReferralStatus.Referred.name)
            addReferralReason(referralReason, Referred_NCD)
        }
        return Pair(checkStatus(), referralReason)
    }

    private fun getBmiReferral(
        bmiInformation: Pair<String, Int>?,
        context: Context,
    ): Boolean {
        return if (bmiInformation?.first.equals(
                context.getString(R.string.under_weight),
                true,
            ) ||
            bmiInformation?.first.equals(
                context.getString(R.string.over_weight),
                true,
            ) ||
            bmiInformation?.first.equals(
                context.getString(R.string.obese),
                true,
            ) ||
            bmiInformation?.first.equals(context.getString(R.string.extremely_obese), true)
        ) {
            return true
        } else {
            false
        }
    }
}
