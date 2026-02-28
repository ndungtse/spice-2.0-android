package com.medtroniclabs.spice.db.dao

/**
 * Controls how [HouseholdDAO.getHouseholdsWithLastActivity] orders its results.
 *
 * All directions are **descending** (most recent / largest value first).
 */
enum class HouseholdSortOrder {
    /** Sort by [HouseHoldEntityWithMemberCount.householdNo] DESC */
    HOUSEHOLD_NO,

    /** Sort by [HouseHoldEntityWithMemberCount.lastActivityAt] DESC
     *  (latest member registration OR assessment, whichever is newer) */
    LAST_VISIT_DATE,

    /** Sort by [HouseHoldEntityWithMemberCount.lastMemberRegisteredAt] DESC
     *  (only household-member registrations) */
    LAST_MEMBER_REGISTRATION,

    /** Default fallback – sort by Household.id DESC */
    DEFAULT,
}
