package org.medtroniclabs.uhis.microcoaching

import android.util.Log
import org.json.JSONObject
import org.medtroniclabs.uhis.db.entity.AssessmentEntity
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams

private const val TAG = "AssessmentEntityExt"

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
 * Additionally surfaces two facility-tier keys out of the `assessmentDetails`
 * JSON column when present:
 *
 *  - `referralFacilityType` (or `childReferralFacilityType` on paeds) — the
 *    *expected* tier (`"Upazila Health Complex"` / `"Community Clinic"`)
 *    written by `ReferralResultGenerator` during assessment processing.
 *  - `picked_facility_type` — the tier of the facility the CHW actually
 *    picked at the referral picker. **Currently not written by SPICE** —
 *    see [docs/gaps/GAPS_TEST.md §F.2] in the SDK repo for the wiring TODO.
 *    The parser here is in place so that once SPICE writes the key into
 *    `assessmentDetails` (or directly into the map), the SDK's
 *    `wrong_facility_tier` rule fires without further SDK changes.
 *
 * The SDK's `wrong_facility_tier` gap evaluator compares these two values.
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

        // System-prescribed facility tier. Lives inside `assessmentDetails`
        // (a JSON-string column SPICE owns) — we parse just the two keys the
        // SDK's `wrong_facility_tier` evaluator needs.
        extractFacilityTierFromDetails(assessmentDetails)?.let { (key, value) ->
            put(key, value)
        }
        // Picked facility tier (Path A). When the referral picker captures
        // the tier and writes `pickedFacilityType` into `assessmentDetails`,
        // forward it under the SDK's expected key `picked_facility_type`.
        extractPickedFacilityTierFromDetails(assessmentDetails)?.let { value ->
            put("picked_facility_type", value)
        }
    }.also { map ->
        // Debug log so the SDK-side test plan can verify what SPICE actually
        // forwards. Keys only — values may carry de-identified clinical data
        // that does not belong in logcat.
        Log.d(TAG, "toSdkAssessmentMap keys=${map.keys}")
    }

/**
 * Parse the `assessmentDetails` JSON column and return the facility-tier
 * pair the SDK reads, or null if not present (e.g. non-NCD assessments,
 * or assessments that didn't trigger referral).
 */
private fun extractFacilityTierFromDetails(detailsJson: String?): Pair<String, String>? {
    if (detailsJson.isNullOrBlank()) return null
    return try {
        val obj = JSONObject(detailsJson)
        when {
            obj.has(AssessmentDefinedParams.REFERRAL_FACILITY_TYPE) ->
                AssessmentDefinedParams.REFERRAL_FACILITY_TYPE to
                    obj.getString(AssessmentDefinedParams.REFERRAL_FACILITY_TYPE)
            obj.has(AssessmentDefinedParams.ID_CHILD_REFERRAL_FACILITY_TYPE) ->
                AssessmentDefinedParams.ID_CHILD_REFERRAL_FACILITY_TYPE to
                    obj.getString(AssessmentDefinedParams.ID_CHILD_REFERRAL_FACILITY_TYPE)
            else -> null
        }
    } catch (e: Exception) {
        Log.w(TAG, "Failed to parse assessmentDetails for facility tier: ${e.message}")
        null
    }
}

/**
 * Parse the `assessmentDetails` JSON column for the *picked* facility tier
 * (Path A). Returns the tier string ("Upazila Health Complex" / "Community
 * Clinic") or null when the key isn't present.
 *
 * **TODO (SPICE-side)**: write `pickedFacilityType` into `assessmentDetails`
 * when the CHW confirms a facility at the referral picker. See
 * [docs/gaps/GAPS_TEST.md §F.2] in the SDK repo for the wiring proposal
 * (capture tier in `ReferPatientViewModel.referToSelectedTier`, propagate
 * through `ReferPatientRepository`, inject into the assessment map before
 * `AssessmentViewModel.getAssessmentDetails` serialises). Until that's
 * done, this returns null and the SDK's `wrong_facility_tier` rule skips
 * with a diagnostic log.
 */
private const val KEY_PICKED_FACILITY_TYPE = "pickedFacilityType"

private fun extractPickedFacilityTierFromDetails(detailsJson: String?): String? {
    if (detailsJson.isNullOrBlank()) return null
    return try {
        val obj = JSONObject(detailsJson)
        if (obj.has(KEY_PICKED_FACILITY_TYPE)) {
            obj.getString(KEY_PICKED_FACILITY_TYPE)
        } else {
            null
        }
    } catch (e: Exception) {
        Log.w(TAG, "Failed to parse assessmentDetails for pickedFacilityType: ${e.message}")
        null
    }
}
