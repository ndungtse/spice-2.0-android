package org.medtroniclabs.uhis.db.dao

import org.medtroniclabs.uhis.db.response.HouseHoldEntityWithLastActivity

/**
 * Controls how [HouseholdDAO.getHouseholdsWithLastActivity] orders its results.
 *
 * All directions are **descending** (most recent / largest value first).
 */
enum class HouseholdSortOrder {
    /** Sort by [HouseHoldEntityWithLastActivity.householdNo] DESC */
    HOUSEHOLD_NO,

    /** Sort by [HouseHoldEntityWithLastActivity.lastActivityAt] DESC
     *  (latest member registration OR assessment, whichever is newer) */
    LAST_VISIT_DATE,

    /** Sort by [HouseHoldEntityWithLastActivity.lastMemberRegisteredAt] DESC
     *  (only household-member registrations) */
    LAST_MEMBER_REGISTRATION,

    /** Default fallback – sort by Household.id DESC */
    DEFAULT,
}
