package org.medtroniclabs.uhis.microcoaching

import org.medtroniclabs.uhis.db.entity.AssessmentEntity

/**
 * Build the `Map<String, Any>` the MicroCoaching SDK's
 * [com.medtroniclabs.microcoaching.MicroCoachingSDK.onAssessmentSubmitted]
 * expects for the `assessmentData` argument.
 *
 * Always-present keys derived from the entity:
 *  - `patient_id`        — for telemetry tracing (hashed by SDK before write)
 *  - `village_id`        — geo on the `coaching_event` row
 *  - `assessment_type`   — useful for cardType routing once UC-2 render lands
 *  - `is_referred`       — CHW's actual referral action
 *  - `referral_status`   — full referral classification, CHW's outcome
 *                          (`Referred` / `OnTreatment` / `Recovered` / `Died`)
 *  - `referred_reason`   — comma-joined list of reasons / facility types,
 *                          when present
 *
 * Optional context the caller can pass in for richer telemetry:
 *  - [systemReferralStatus]  — what `ReferralResultGenerator` prescribed
 *                              (e.g. `"Referred"` or null). Fetched from
 *                              `AssessmentViewModel.referralStatus` after
 *                              `assessmentSaveLiveData` posts SUCCESS.
 *  - [systemReferralReasons] — system-prescribed reasons / facility types
 *                              (from `AssessmentViewModel.referralReason`).
 *  - [upazilaId]             — Bangladesh-localised admin area. SPICE's
 *                              equivalent is `VillageEntity.chiefdomId`;
 *                              caller resolves via
 *                              `MetaDataDAO.getVillageByID(villageId).chiefdomId`.
 *
 * When the optional arguments are supplied, the SDK's `ReferralRulesStub`
 * switches from its risk-level fallback to a real three-axis comparison
 * (`correctReferral`, `correctReferralLocation`, `correctReferralType`).
 * Missing keys are tolerated — the SDK simply falls back to the stub path.
 *
 * Everything remains optional on the SDK side; missing keys land as null
 * on the wire. New fields can be added by extending this map without
 * touching either Activity callsite.
 */
fun AssessmentEntity.toSdkAssessmentMap(
    systemReferralStatus: String? = null,
    systemReferralReasons: List<String>? = null,
    upazilaId: String? = null,
): Map<String, Any> =
    buildMap {
        patientId?.let { put("patient_id", it) }
        put("village_id", villageId)
        upazilaId?.let { put("upazila_id", it) }
        put("assessment_type", assessmentType)
        put("is_referred", isReferred)
        put("referral_status", referralStatus.name)
        referredReason?.let { put("referred_reason", it.joinToString(",")) }
        systemReferralStatus?.let { put("system_referral_status", it) }
        systemReferralReasons?.let { put("system_referral_reasons", it.joinToString(",")) }
    }
