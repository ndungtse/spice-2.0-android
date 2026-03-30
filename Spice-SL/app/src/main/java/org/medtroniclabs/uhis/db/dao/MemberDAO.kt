package org.medtroniclabs.uhis.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.data.offlinesync.model.HHSignatureDetail
import org.medtroniclabs.uhis.data.offlinesync.model.HouseHoldMember
import org.medtroniclabs.uhis.data.offlinesync.model.HouseholdMemberStatus
import org.medtroniclabs.uhis.data.offlinesync.model.HouseholdMemberWithTb
import org.medtroniclabs.uhis.data.offlinesync.utils.OfflineSyncStatus
import org.medtroniclabs.uhis.db.entity.AssessmentEntity
import org.medtroniclabs.uhis.db.entity.HouseholdEntity
import org.medtroniclabs.uhis.db.entity.HouseholdMemberEntity
import org.medtroniclabs.uhis.db.entity.MemberAssessmentHistoryEntity
import org.medtroniclabs.uhis.mappingkey.MemberRegistration
import org.medtroniclabs.uhis.model.MemberDobGenderModel
import org.medtroniclabs.uhis.model.assessment.AssessmentMemberDetails
import org.medtroniclabs.uhis.model.services.ServiceStaticFilter

@Dao
interface MemberDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(memberEntity: HouseholdMemberEntity): Long

    @Query("SELECT * FROM HouseHoldMember WHERE household_id = :houseHoldId")
    suspend fun getAllHouseHoldMemberList(houseHoldId: Long): List<HouseholdMemberEntity>

    @Query(
        """
                    SELECT
                        hhm.*, td.diagnoses,
                        mah.serviceProvided AS recent_service,
                        (strftime('%s', mah.visitDate) * 1000) AS recent_service_date
                    FROM householdmember AS hhm
                    LEFT JOIN TreatmentDetailsEntity AS td ON hhm.fhir_id = td.memberId

                    LEFT JOIN MemberAssessmentHistory AS mah
                        ON mah.memberId = hhm.id
                        AND mah.visitDate = (
                            SELECT MAX(mah2.visitDate)
                            FROM MemberAssessmentHistory AS mah2
                            WHERE mah2.memberId = hhm.id
                        )

                    WHERE hhm.household_id = :houseHoldId
    """,
    )
    fun getAllHouseHoldMembersLiveData(houseHoldId: Long): LiveData<List<HouseholdMemberWithTb>>

    @Query("SELECT * FROM HouseHoldMember WHERE household_id = :houseHoldId AND isActive =:aliveStatus")
    fun getAliveHouseHoldMembers(
        houseHoldId: Long,
        aliveStatus: Boolean,
    ): List<HouseholdMemberEntity>

    @Query("SELECT * FROM HouseHoldMember WHERE id = :memberId")
    suspend fun getMemberDetailsById(memberId: Long): HouseholdMemberEntity

    @Query("SELECT * FROM HouseHoldMember WHERE patient_id = :patientId")
    suspend fun getMemberDetailsByPatientId(patientId: String): HouseholdMemberEntity?

    @Query("SELECT * FROM HouseHoldMember WHERE motherReferenceId = :memberId ORDER BY fhir_id IS NULL, fhir_id ASC")
    suspend fun getMemberDetailsByParentId(memberId: String): List<HouseholdMemberEntity>

    @Query("SELECT id, fhir_id AS fhirId, '' AS signatureName FROM HouseHoldMember WHERE fhir_id IS NOT NULL")
    suspend fun getHHSignatureDetails(): List<HHSignatureDetail>

    @Query("SELECT COUNT(household_id) FROM HouseHoldMember WHERE household_id = :householdId")
    suspend fun getMemberCountPerHouseHold(householdId: Long): Int

    @Query("SELECT patient_id FROM HouseholdMember WHERE patient_id LIKE :patientIdStarts ORDER BY patient_id DESC LIMIT 1")
    suspend fun getLastPatientId(patientIdStarts: String): String?

    @Query("SELECT date_of_birth,gender FROM HouseHoldMember WHERE id = :memberId")
    suspend fun getDobAndGenderById(memberId: Long): MemberDobGenderModel

    @Query("SELECT hhm.*, hh.fhir_id as household_fhir_id, hh.village_id as village_id, ve.name as village_name, hh.sub_village_id as sub_village_id, sv.name as sub_village_name, CASE WHEN lhhm.memberId IS NOT NULL AND lhhm.syncStatus IN (:status) THEN 1 ELSE NULL END AS assignHousehold FROM HouseHoldMember AS hhm INNER JOIN Household as hh ON hh.id = hhm.household_id INNER JOIN VillageEntity AS ve ON hh.village_id = ve.id LEFT JOIN SubVillageEntity AS sv ON hh.sub_village_id = sv.id LEFT JOIN LinkHouseholdMember AS lhhm ON lhhm.memberId = hhm.fhir_id WHERE hhm.id NOT IN (:memberIds) AND (hh.fhir_id IS NULL OR hhm.fhir_id IS NULL) AND hhm.household_id = :houseHoldId AND hhm.sync_status IN (:status)")
    suspend fun getAllUnSyncedHouseHoldMembers(
        houseHoldId: Long,
        memberIds: List<Long>,
        status: List<String> = listOf(
            OfflineSyncStatus.NotSynced.name,
            OfflineSyncStatus.NetworkError.name,
        ),
    ): List<HouseHoldMember>

    @Query("SELECT hhm.*, hh.fhir_id as household_fhir_id, COALESCE(hh.village_id, hhm.villageId) as village_id, ve.name as village_name, COALESCE(hh.sub_village_id, hhm.sub_village_id) as sub_village_id, sv.name as sub_village_name, CASE WHEN lhhm.memberId IS NOT NULL AND lhhm.syncStatus IN (:status) THEN 1 ELSE NULL END AS assignHousehold FROM HouseHoldMember AS hhm LEFT JOIN Household as hh ON hh.id = hhm.household_id LEFT JOIN VillageEntity AS ve ON ve.id = COALESCE(hh.village_id, hhm.villageId) LEFT JOIN SubVillageEntity AS sv ON sv.id = COALESCE(hh.sub_village_id, hhm.sub_village_id) LEFT JOIN LinkHouseholdMember AS lhhm ON lhhm.memberId = hhm.fhir_id WHERE hhm.id NOT IN (:memberIds) AND (hh.id IS NULL OR hh.fhir_id IS NOT NULL) AND hhm.sync_status IN (:status)")
    suspend fun getOtherHouseholdMembers(
        memberIds: List<String>,
        status: List<String> = listOf(
            OfflineSyncStatus.NotSynced.name,
            OfflineSyncStatus.NetworkError.name,
        ),
    ): List<HouseHoldMember>

    @Query("SELECT COUNT(id) FROM HouseholdMember where sync_status IN (:syncStatus)")
    suspend fun getUnSyncedCount(
        syncStatus: List<String> = listOf(
            OfflineSyncStatus.NotSynced.name,
            OfflineSyncStatus.NetworkError.name,
        ),
    ): Int

    @Query("SELECT id FROM HouseholdMember WHERE fhir_id =:fhirId")
    suspend fun getHouseholdMemberIdByFhirId(fhirId: String): Long?

    @Query("SELECT hhm.name, hhm.gender, hhm.date_of_birth AS dateOfBirth, hhm.patient_id AS patientId, CASE WHEN hh.sub_village_id IS NOT NULL THEN CAST(hh.sub_village_id AS TEXT) WHEN hhm.sub_village_id IS NOT NULL THEN CAST(hhm.sub_village_id AS TEXT) ELSE '0' END as subVillageId, CASE WHEN hh.village_id IS NOT NULL THEN CAST(hh.village_id AS TEXT) WHEN hhm.villageId IS NOT NULL THEN CAST(hhm.villageId AS TEXT) ELSE '0' END as villageId, hhm.fhir_id AS memberId, hh.household_no as householdNo, hh.fhir_id AS householdId, hhm.id AS id, COALESCE(hh.id, 0) AS householdLocalId, NULL AS contactTracingStatus, NULL AS isPregnant, hhm.phone_number AS phoneNumber FROM HouseholdMember AS hhm LEFT JOIN Household AS hh ON hh.id = hhm.household_id WHERE hhm.id=:id")
    suspend fun getAssessmentMemberDetails(id: Long): AssessmentMemberDetails

    @Query("DELETE FROM HouseholdMember")
    suspend fun deleteAllHouseholdMembers()

    @Query("SELECT patient_id FROM HouseholdMember WHERE fhir_id =:fhirId")
    suspend fun getPatientIdByFhirId(fhirId: String): String?

    @Query("SELECT patient_id FROM HouseholdMember WHERE id =:id")
    suspend fun getPatientIdById(id: Long): String

    @Query("SELECT * FROM HouseholdMember WHERE fhir_id = :fhirId LIMIT 1")
    suspend fun getByUniqueField(fhirId: String): HouseholdMemberEntity?

    @Transaction
    suspend fun insertOrUpdateFromBE(entity: HouseholdMemberEntity): Long {
        if (!entity.isActive && entity.fhirId != null) {
            deleteRxBuddyOnDeceased(entity.fhirId!!)
        }
        val existingEntity = entity.fhirId?.let { getByUniqueField(it) }
        if (existingEntity?.sync_status != OfflineSyncStatus.NotSynced) {
            val entityToInsert = existingEntity?.let { entity.copy(id = it.id) } ?: entity
            entityToInsert.sync_status = existingEntity?.sync_status ?: OfflineSyncStatus.Success
            entityToInsert.fhirId = entity.fhirId
            return insertMember(entityToInsert)
        } else {
            return existingEntity.id
        }
    }

    @Query("DELETE FROM RxBuddyDetails WHERE patientMemberId = :memberId")
    suspend fun deleteRxBuddyOnDeceased(memberId: String)

    @Query("UPDATE HouseholdMember SET sync_status =:syncStatus, updated_at =:updatedAt WHERE id IN (:memberIds)")
    suspend fun updateInProgress(
        memberIds: List<String>,
        syncStatus: String,
        updatedAt: Long = System.currentTimeMillis(),
    )

    @Query("UPDATE HouseholdMember SET sync_status =:syncStatus WHERE id = :id")
    suspend fun changeMemberDetailsToNotSynced(
        id: Long,
        syncStatus: OfflineSyncStatus = OfflineSyncStatus.NotSynced,
    )

    @Query("UPDATE HouseholdMember SET isActive = :status, sync_status =:syncStatus  WHERE id = :id")
    suspend fun updateMemberDeceasedStatus(
        id: Long,
        status: Boolean,
        syncStatus: OfflineSyncStatus,
    )

    @Query("UPDATE HouseholdMember SET isActive = :status, sync_status =:syncStatus , deceasedReason=:deceasedReason ,updated_at =:updatedAt WHERE id = :id")
    suspend fun updateMemberDeceasedReason(
        id: Long,
        status: Boolean,
        syncStatus: OfflineSyncStatus,
        deceasedReason: String?,
        updatedAt: Long = System.currentTimeMillis(),
    )

    @Query("UPDATE householdmember SET phone_number = :phoneNumber, sync_status =:syncStatus, updated_at =:updatedAt  WHERE household_id = :householdId AND is_house_hold_head = 1")
    suspend fun updatePhoneNumberForHouseholdHead(
        householdId: Long,
        phoneNumber: String?,
        syncStatus: String = OfflineSyncStatus.NotSynced.name,
        updatedAt: Long = System.currentTimeMillis(),
    )

    @Query("UPDATE HouseholdMember SET household_id = :householdId, sync_status =:syncStatus, updated_at =:updatedAt  WHERE fhir_id IN (:memberIds)")
    suspend fun updateHouseholdHeadAndRelationShip(
        memberIds: List<String>,
        householdId: Long,
        syncStatus: String = OfflineSyncStatus.NotSynced.name,
        updatedAt: Long = System.currentTimeMillis(),
    )

    @Query("SELECT date_of_birth FROM HouseholdMember WHERE household_id = :householdId AND is_house_hold_head = 1 LIMIT 1")
    suspend fun getHouseholdHeadDob(
        householdId: Long,
    ): String

    @Query("SELECT hm.* FROM HouseholdMember AS hm WHERE hm.household_id = :hhId")
    fun getHouseholdMemberWithTBContactTraceStatus(hhId: Long): LiveData<List<HouseholdMemberEntity>>

    @Query("UPDATE PregnancyDetail SET tbContactTraceStatus = :tbContactTraceStatus WHERE householdMemberLocalId = :householdMemberId")
    suspend fun updateTBContactTraceStatus(
        householdMemberId: Long,
        tbContactTraceStatus: Int,
    )

    @Query("UPDATE HouseholdMember SET sync_status =:syncStatus WHERE id = :memberId AND (:isPregnant IS NULL OR :isPregnant IS NOT NULL)")
    suspend fun updatePregnantStatus(
        memberId: Long,
        isPregnant: Boolean,
        syncStatus: String = OfflineSyncStatus.NotSynced.name,
    )

    @Query("SELECT * FROM HouseholdMember WHERE household_id = :householdId AND id != :patientId AND isActive=1 AND substr(date_of_birth, 1, 10) < date('now','-10 years') ")
    suspend fun getOtherHouseholdExcludeTBPatient(
        householdId: Long,
        patientId: Long,
    ): List<HouseholdMemberEntity>

    @Query("SELECT hhm.*, hh.fhir_id as household_fhir_id, hh.village_id as village_id, ve.name as village_name FROM HouseHoldMember AS hhm INNER JOIN Household as hh ON hh.id = hhm.household_id INNER JOIN VillageEntity AS ve ON hh.village_id = ve.id Where hhm.id = :hhmId")
    suspend fun getHouseholdMemberForRxBuddy(hhmId: Long): HouseHoldMember

    @Query("SELECT id, isActive FROM HouseholdMember WHERE fhir_id =:fhirId")
    suspend fun getHouseholdMemberIdAndStatusByFhirId(fhirId: String): HouseholdMemberStatus?

    @Query("SELECT fhir_id FROM HouseholdMember WHERE id =:hhmId")
    suspend fun getMemberFhirIdByLocalId(hhmId: Long): String?

    @Query("SELECT hhm.id FROM HouseholdMember AS hhm JOIN TreatmentDetailsEntity AS td ON hhm.fhir_id = td.memberId WHERE hhm.household_id =:householdId")
    suspend fun getTbPatientLocalIdByHouseholdId(householdId: Long): MutableList<Long>

    @Query("UPDATE HouseholdMember SET sync_status =:syncStatus, updated_at =:updatedAt WHERE id = :memberId AND (:status IS NULL OR :status IS NOT NULL)")
    suspend fun updateContactTracingStatus(
        memberId: Long,
        status: Int?,
        syncStatus: String = OfflineSyncStatus.NotSynced.name,
        updatedAt: Long = System.currentTimeMillis(),
    )

    @Query("UPDATE HouseholdMember SET sync_status =:syncStatus, updated_at =:updatedAt WHERE household_id = :householdId AND id != :tbHHMId")
    suspend fun updateContactTracingForLinkTbPatient(
        tbHHMId: Long,
        householdId: Long,
        syncStatus: String = OfflineSyncStatus.NotSynced.name,
        updatedAt: Long = System.currentTimeMillis(),
    )

    @Query("SELECT COUNT(id) FROM HouseholdMember WHERE disability='present' AND household_id=:householdId")
    suspend fun getDisabilityMembersCountForHousehold(householdId: Long): Int

    @Query(
        """
        UPDATE HouseholdMember
        SET guardian_hh_member_id = (
            SELECT guardian.id
            FROM HouseholdMember AS guardian
            WHERE guardian.fhir_id = HouseholdMember.guardian_hh_member_fhir_id
        )
        WHERE guardian_hh_member_fhir_id IS NOT NULL
        """,
    )
    suspend fun updateGuardianHhIds(): Int

    /**
     * Internal raw-query entry point. Use [getServiceMembers] instead.
     *
     * **observedEntities** ensures Room re-delivers LiveData whenever any of the
     * three underlying tables change.
     */
    @RawQuery(observedEntities = [HouseholdEntity::class, HouseholdMemberEntity::class, AssessmentEntity::class])
    fun getServiceMembersRaw(query: SimpleSQLiteQuery): LiveData<List<HouseholdMemberWithTb>>

    /**
     * Returns a live list of members with last-activity info.
     *
     * **Filters** (all optional):
     * @param searchInput match against household name or number; blank = no filter
     * @param filterBySs whitelist of Shasthya Shebika IDs; null/empty = no filter
     * @param filterBySubVillages whitelist of sub-village IDs; null/empty = no filter
     *
     */
    fun getServiceMembers(
        searchInput: String,
        filterBySs: List<Long>,
        filterBySubVillages: List<Long>,
        staticFilter: ServiceStaticFilter,
    ): LiveData<List<HouseholdMemberWithTb>> {
        val args = mutableListOf<Any>()
        val conditions = mutableListOf<String>()

        // Check if this is an external-members scoped filter
        val isExternalMember =
            staticFilter == ServiceStaticFilter.EXTERNAL_MEMBERS ||
                staticFilter == ServiceStaticFilter.EXTERNAL_PREGNANT_WOMEN

        if (searchInput.isNotBlank()) {
            conditions += "(hhm.name LIKE ? OR hhm.phone_number LIKE ?)"
            val pattern = "%${searchInput.trim()}%"
            args += pattern
            args += pattern
        }

        // Apply SS filter - for external members, use hhm.shasthya_shebika_id; for regular members, use hh.shasthya_shebika_id
        if (filterBySs.isNotEmpty()) {
            if (isExternalMember) {
                conditions += "hhm.shasthya_shebika_id IN (${filterBySs.joinToString(",") { "?" }})"
            } else {
                conditions += "hh.shasthya_shebika_id IN (${filterBySs.joinToString(",") { "?" }})"
            }
            args.addAll(filterBySs)
        }

        // Apply SubVillage filter - for external members, use hhm.sub_village_id; for regular members, use hh.sub_village_id
        if (filterBySubVillages.isNotEmpty()) {
            if (isExternalMember) {
                conditions += "hhm.sub_village_id IN (${filterBySubVillages.joinToString(",") { "?" }})"
            } else {
                conditions += "hh.sub_village_id IN (${filterBySubVillages.joinToString(",") { "?" }})"
            }
            args.addAll(filterBySubVillages)
        }

        when (staticFilter) {
            ServiceStaticFilter.FAMILY_PLANNING_COUNSELLING_ELIGIBLE -> {
                conditions += "hhm.gender = ?"
                args += DefinedParams.GENDER_FEMALE
                conditions += "hhm.marital_status = ?"
                args += MemberRegistration.MaritalStatus.MARRIED.value
                conditions += "substr(hhm.date_of_birth, 1, 10) <= date('now', '-${MemberRegistration.MIN_AGE_PREGNANCY} years')"
                conditions += "substr(hhm.date_of_birth, 1, 10) >= date('now', '-${MemberRegistration.MAX_AGE_PREGNANCY} years')"
                conditions +=
                    """
                    NOT EXISTS (
                        SELECT 1 FROM (
                            SELECT pd.dateOfDelivery, pd.lastMenstrualPeriod, pd.estimatedDeliveryDate
                            FROM PregnancyDetail AS pd
                            WHERE pd.householdMemberLocalId = hhm.id
                            ORDER BY pd.id DESC
                            LIMIT 1
                        ) AS latest_pregnancy
                        WHERE (latest_pregnancy.dateOfDelivery IS NULL OR latest_pregnancy.dateOfDelivery = '')
                        AND (latest_pregnancy.lastMenstrualPeriod IS NOT NULL AND latest_pregnancy.lastMenstrualPeriod != '')
                        AND (latest_pregnancy.estimatedDeliveryDate IS NULL OR substr(latest_pregnancy.estimatedDeliveryDate, 1, 10) >= date('now', '-45 days'))
                    )
                    """.trimIndent()
            }

            ServiceStaticFilter.PREGNANT_WOMEN -> {
                conditions +=
                    """
                    EXISTS (
                        SELECT 1 FROM (
                            SELECT pd.dateOfDelivery, pd.lastMenstrualPeriod, pd.estimatedDeliveryDate
                            FROM PregnancyDetail AS pd
                            WHERE pd.householdMemberLocalId = hhm.id
                            ORDER BY pd.id DESC
                            LIMIT 1
                        ) AS latest_pregnancy
                        WHERE (latest_pregnancy.dateOfDelivery IS NULL OR latest_pregnancy.dateOfDelivery = '')
                        AND (latest_pregnancy.lastMenstrualPeriod IS NOT NULL AND latest_pregnancy.lastMenstrualPeriod != '')
                        AND (latest_pregnancy.estimatedDeliveryDate IS NULL OR substr(latest_pregnancy.estimatedDeliveryDate, 1, 10) >= date('now', '-45 days'))
                    )
                    """.trimIndent()
            }

            ServiceStaticFilter.POSTNATAL_CARE_MOTHERS -> {
                conditions +=
                    """
                    EXISTS (
                        SELECT 1 FROM (
                            SELECT pd.dateOfDelivery
                            FROM PregnancyDetail AS pd
                            WHERE pd.householdMemberLocalId = hhm.id
                            ORDER BY pd.id DESC
                            LIMIT 1
                        ) AS latest_pregnancy
                        WHERE (latest_pregnancy.dateOfDelivery IS NOT NULL AND latest_pregnancy.dateOfDelivery != '')
                        AND substr(latest_pregnancy.dateOfDelivery, 1, 10) >= date('now', '-42 days')
                    )
                    """.trimIndent()
            }

            ServiceStaticFilter.CHILDREN_UNDER_TWO_YEARS -> {
                conditions += "substr(hhm.date_of_birth, 1, 10) > date('now', '-2 years')"
            }

            ServiceStaticFilter.EXPECTED_DELIVERIES -> {
                conditions +=
                    """
                    EXISTS (
                        SELECT 1 FROM (
                            SELECT pd.dateOfDelivery, pd.estimatedDeliveryDate
                            FROM PregnancyDetail AS pd
                            WHERE pd.householdMemberLocalId = hhm.id
                            ORDER BY pd.id DESC
                            LIMIT 1
                        ) AS latest_pregnancy
                        WHERE (latest_pregnancy.dateOfDelivery IS NULL OR latest_pregnancy.dateOfDelivery = '')
                        AND (latest_pregnancy.estimatedDeliveryDate IS NOT NULL AND latest_pregnancy.estimatedDeliveryDate != '')
                        AND substr(latest_pregnancy.estimatedDeliveryDate, 1, 10) BETWEEN date('now') AND date('now', '+30 days')
                    )
                    """.trimIndent()
            }

            ServiceStaticFilter.PENDING_DELIVERIES -> {
                conditions +=
                    """
                    EXISTS (
                        SELECT 1 FROM (
                            SELECT pd.dateOfDelivery, pd.estimatedDeliveryDate
                            FROM PregnancyDetail AS pd
                            WHERE pd.householdMemberLocalId = hhm.id
                            ORDER BY pd.id DESC
                            LIMIT 1
                        ) AS latest_pregnancy
                        WHERE (latest_pregnancy.dateOfDelivery IS NULL OR latest_pregnancy.dateOfDelivery = '')
                        AND (latest_pregnancy.estimatedDeliveryDate IS NOT NULL AND latest_pregnancy.estimatedDeliveryDate != '')
                        AND substr(latest_pregnancy.estimatedDeliveryDate, 1, 10) < date('now', '-45 days')
                    )
                    """.trimIndent()
            }

            ServiceStaticFilter.HIGH_RISK_PREGNANT_WOMEN -> {
                conditions +=
                    """
                    EXISTS (
                        SELECT 1 FROM (
                            SELECT pd.dateOfDelivery, pd.lastMenstrualPeriod, pd.estimatedDeliveryDate, pd.highRiskPregnantWoman
                            FROM PregnancyDetail AS pd
                            WHERE pd.householdMemberLocalId = hhm.id
                            ORDER BY pd.id DESC
                            LIMIT 1
                        ) AS latest_pregnancy
                        WHERE (latest_pregnancy.dateOfDelivery IS NULL OR latest_pregnancy.dateOfDelivery = '')
                        AND (latest_pregnancy.lastMenstrualPeriod IS NOT NULL AND latest_pregnancy.lastMenstrualPeriod != '')
                        AND (latest_pregnancy.estimatedDeliveryDate IS NULL OR substr(latest_pregnancy.estimatedDeliveryDate, 1, 10) >= date('now', '-45 days'))
                        AND (latest_pregnancy.highRiskPregnantWoman IS NOT NULL AND latest_pregnancy.highRiskPregnantWoman != '')
                    )
                    """.trimIndent()
            }

            ServiceStaticFilter.EXTERNAL_MEMBERS -> {
                conditions += "hhm.household_id IS NULL"
            }

            ServiceStaticFilter.EXTERNAL_PREGNANT_WOMEN -> {
                conditions += "hhm.household_id IS NULL"
                conditions += "hhm.isActive = 1"
                conditions +=
                    """
                    EXISTS (
                        SELECT 1 FROM (
                            SELECT pd.dateOfDelivery, pd.lastMenstrualPeriod, pd.estimatedDeliveryDate
                            FROM PregnancyDetail AS pd
                            WHERE pd.householdMemberLocalId = hhm.id
                            ORDER BY pd.id DESC
                            LIMIT 1
                        ) AS latest_pregnancy
                        WHERE (latest_pregnancy.dateOfDelivery IS NULL OR latest_pregnancy.dateOfDelivery = '')
                        AND (latest_pregnancy.lastMenstrualPeriod IS NOT NULL AND latest_pregnancy.lastMenstrualPeriod != '')
                        AND (latest_pregnancy.estimatedDeliveryDate IS NULL OR substr(latest_pregnancy.estimatedDeliveryDate, 1, 10) >= date('now', '-45 days'))
                    )
                    """.trimIndent()
            }

            else -> {}
        }

        val whereClause = if (conditions.isEmpty()) {
            ""
        } else {
            "WHERE ${conditions.joinToString(" AND ")}"
        }

        // For external members, use LEFT JOIN since household_id is NULL
        val householdJoin = if (isExternalMember) {
            "LEFT JOIN Household AS hh ON hh.id = hhm.household_id"
        } else {
            "INNER JOIN Household AS hh ON hh.id = hhm.household_id"
        }

        val ssJoin = if (isExternalMember) {
            "LEFT JOIN ShasthyaShebikaEntity AS ss ON hhm.shasthya_shebika_id = ss.id"
        } else {
            "INNER JOIN ShasthyaShebikaEntity AS ss ON hh.shasthya_shebika_id = ss.id"
        }

        val svJoin = if (isExternalMember) {
            "LEFT JOIN SubVillageEntity AS sv ON hhm.sub_village_id = sv.id"
        } else {
            "INNER JOIN SubVillageEntity AS sv ON hh.sub_village_id = sv.id"
        }

        val query =
            """
            SELECT
                hhm.*, td.diagnoses,
                mah.serviceProvided AS recent_service,
                (strftime('%s', mah.visitDate) * 1000) AS recent_service_date,
                COALESCE(ss.name, '') AS shasthya_shebika_name,
                COALESCE(ss.ssId, '') AS shasthya_shebika_ssId,
                COALESCE(sv.name, '') AS sub_village_name
            FROM householdmember AS hhm

            $householdJoin

            $ssJoin

            $svJoin

            LEFT JOIN TreatmentDetailsEntity AS td
                ON hhm.fhir_id = td.memberId

            LEFT JOIN MemberAssessmentHistory AS mah
                ON mah.memberId = hhm.id
                AND mah.visitDate = (
                    SELECT MAX(mah2.visitDate)
                    FROM MemberAssessmentHistory AS mah2
                    WHERE mah2.memberId = hhm.id
                )

            $whereClause
            ORDER BY hhm.id DESC
            """.trimIndent()
        return getServiceMembersRaw(SimpleSQLiteQuery(query, args.toTypedArray()))
    }

    /**
     * Retrieves a member and their associated assessment history, sorted by visit date in descending order.
     * Uses a LEFT JOIN to combine [HouseholdMemberEntity] and [MemberAssessmentHistoryEntity] in a single query.
     *
     * @param memberId The local ID of the member to retrieve.
     * @return A map where the key is the member entity and the value is a list of their assessment histories.
     */
    @Transaction
    @Query("SELECT * FROM HouseHoldMember AS hhm LEFT JOIN memberassessmenthistory AS mah ON hhm.id = mah.memberId WHERE hhm.id = :memberId ORDER BY mah.visitDate DESC")
    fun getMemberWithAssessmentHistory(memberId: Long): LiveData<Map<HouseholdMemberEntity, List<MemberAssessmentHistoryEntity>>?>
}
