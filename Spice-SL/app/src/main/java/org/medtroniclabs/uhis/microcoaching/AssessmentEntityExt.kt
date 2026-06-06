package org.medtroniclabs.uhis.microcoaching

import android.util.Log
import org.json.JSONArray
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
 * Additionally surfaces the facility-tier keys the SDK's `wrong_facility_tier`
 * gap compares:
 *
 *  - `referralFacilityType` (or `childReferralFacilityType` on paeds) — the
 *    *expected* tier (`"Upazila Health Complex"` / `"Community Clinic"`)
 *    written by `ReferralResultGenerator`. Lives nested under the workflow key
 *    in `assessmentDetails`, so it's resolved recursively.
 *  - `picked_facility_type` + `referred_site_id` — the *actual* facility the
 *    CHW picked on the BD NCD summary screen (`etPhuChange`), captured into the
 *    `otherDetails` JSON column. The tier comes from `HealthFacilityEntity.type`
 *    (synced from the metadata API).
 *
 * The expected tier is read from `assessmentDetails`; the picked tier/site id
 * from `otherDetails`. The SDK's `wrong_facility_tier` evaluator compares the
 * expected vs picked tier (normalising both via its `TierNormalizer`).
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

        // System-prescribed facility tier (the *expected* tier). Written by
        // ReferralResultGenerator into `assessmentDetails`, nested under the
        // workflow key (e.g. "ncd") — resolve it recursively, not top-level.
        extractFacilityTierFromDetails(assessmentDetails)?.let { (key, value) ->
            put(key, value)
        }
        // Picked facility (the CHW's *actual* choice) lives in `otherDetails` —
        // the PHU selected on the summary screen. Forward its tier
        // (`picked_facility_type`) and site id (`referred_site_id`).
        extractPickedReferralFromOtherDetails(otherDetails).forEach { (key, value) ->
            put(key, value)
        }
    }.also { map ->
        // Debug log so the SDK-side test plan can verify what SPICE actually
        // forwards. Keys only — values may carry de-identified clinical data
        // that does not belong in logcat.
        Log.d(TAG, "toSdkAssessmentMap keys=${map.keys}")
    }

/**
 * Resolve the system-prescribed (expected) facility tier from the
 * `assessmentDetails` JSON, or null if absent. `ReferralResultGenerator` writes
 * `referralFacilityType` (paeds: `childReferralFacilityType`) into the workflow
 * sub-map (e.g. `{"ncd": { "referralFacilityType": "Upazila Health Complex" }}`),
 * so we search **recursively** — a top-level lookup would miss it.
 */
private fun extractFacilityTierFromDetails(detailsJson: String?): Pair<String, String>? {
    if (detailsJson.isNullOrBlank()) return null
    return try {
        val root = JSONObject(detailsJson)
        (findValueByKey(root, AssessmentDefinedParams.REFERRAL_FACILITY_TYPE) as? String)
            ?.let { return AssessmentDefinedParams.REFERRAL_FACILITY_TYPE to it }
        (findValueByKey(root, AssessmentDefinedParams.ID_CHILD_REFERRAL_FACILITY_TYPE) as? String)
            ?.let { return AssessmentDefinedParams.ID_CHILD_REFERRAL_FACILITY_TYPE to it }
        null
    } catch (e: Exception) {
        Log.w(TAG, "Failed to parse assessmentDetails for facility tier: ${e.message}")
        null
    }
}

/**
 * Parse the *picked* referral out of the `otherDetails` JSON column (the
 * serialised `otherAssessmentDetails` map). The BD NCD summary screen captures
 * the CHW's chosen PHU there:
 *  - `pickedFacilityType` → forwarded as `picked_facility_type` (the actual tier)
 *  - `referredSiteId`     → forwarded as `referred_site_id` (the facility id)
 *
 * Returns an empty map when neither is present (non-referring pathways, or the
 * tier not yet synced). Both keys are top-level in `otherDetails`.
 */
private fun extractPickedReferralFromOtherDetails(otherDetailsJson: String?): Map<String, String> {
    if (otherDetailsJson.isNullOrBlank()) return emptyMap()
    return try {
        val obj = JSONObject(otherDetailsJson)
        buildMap {
            obj
                .optString(KEY_PICKED_FACILITY_TYPE)
                .takeIf { it.isNotBlank() }
                ?.let { put("picked_facility_type", it) }
            obj
                .optString(AssessmentDefinedParams.ReferredPHUSiteID)
                .takeIf { it.isNotBlank() }
                ?.let { put("referred_site_id", it) }
        }
    } catch (e: Exception) {
        Log.w(TAG, "Failed to parse otherDetails for picked referral: ${e.message}")
        emptyMap()
    }
}

private const val KEY_PICKED_FACILITY_TYPE = AssessmentDefinedParams.PICKED_FACILITY_TYPE

/**
 * Depth-first search for [targetKey] anywhere in a parsed JSON tree. Mirrors
 * `AssessmentCommonUtils.findValueByKey` — kept local so this SDK-facing ext
 * has no UI-layer dependency.
 */
private fun findValueByKey(
    node: Any?,
    targetKey: String,
): Any? {
    when (node) {
        is JSONObject -> {
            val keys = node.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = node.get(key)
                if (key == targetKey) return value
                findValueByKey(value, targetKey)?.let { return it }
            }
        }
        is JSONArray -> {
            for (i in 0 until node.length()) {
                findValueByKey(node.get(i), targetKey)?.let { return it }
            }
        }
    }
    return null
}
