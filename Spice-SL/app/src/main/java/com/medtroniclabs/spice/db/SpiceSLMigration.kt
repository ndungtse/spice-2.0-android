package com.medtroniclabs.spice.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object SpiceSLMigration {

    val MIGRATION_1_2 = object : Migration(1, 2) {

        override fun migrate(database: SupportSQLiteDatabase) {
            /* 1. Household Entity Migration
            *   1.1. Changing household_no from NOT Null field to Nullable Field
            * */
            database.execSQL(
                """
            CREATE TABLE Household_temp (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                household_no INTEGER,
                name TEXT NOT NULL,
                village_id INTEGER NOT NULL,
                landmark TEXT,
                head_phone_number TEXT,
                no_of_people INTEGER NOT NULL,
                is_owned_an_improved_latrine INTEGER NOT NULL,
                is_owned_hand_washing_facility_with_soap INTEGER NOT NULL,
                is_owned_a_treated_bed_net INTEGER NOT NULL,
                bed_net_count INTEGER,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                version TEXT,
                lastUpdated TEXT,
                fhir_id TEXT,
                sync_status TEXT NOT NULL DEFAULT 'NotSynced',
                created_by INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                created_at INTEGER NOT NULL
            )
        """
            )

            database.execSQL(
                """
            INSERT INTO Household_temp (id, household_no, name, village_id, landmark,
                                                head_phone_number, no_of_people, is_owned_an_improved_latrine,
                                                is_owned_hand_washing_facility_with_soap, is_owned_a_treated_bed_net,
                                                bed_net_count, latitude, longitude, version, lastUpdated, fhir_id,
                                                sync_status, created_by, updated_at, created_at)
            SELECT id, household_no, name, village_id, landmark,
                   head_phone_number, no_of_people, is_owned_an_improved_latrine,
                   is_owned_hand_washing_facility_with_soap, is_owned_a_treated_bed_net,
                   bed_net_count, latitude, longitude, version, lastUpdated, fhir_id,
                   sync_status, created_by, updated_at, created_at
            FROM Household
            """
            )

            database.execSQL("DROP TABLE Household")
            database.execSQL("ALTER TABLE Household_temp RENAME TO Household")

            /* 2. Assessment Entity Migration
            *   2.1. Added householdMemberLocalId - for keeping local id as foreign key for assessment
            *   2.2. Changing patientId from NOT Null field to Nullable Field
            *   2.3. Setting householdMemberLocalId by using member patient id and member fhir id
            * */

            database.execSQL("ALTER TABLE Assessment ADD COLUMN householdMemberLocalId INTEGER NOT NULL DEFAULT 0")
            database.execSQL(
                """
            UPDATE Assessment
            SET householdMemberLocalId = (
                SELECT member.id
                FROM HouseholdMember AS member
                WHERE member.patient_id = Assessment.patientId AND (member.fhir_id IS NULL OR member.fhir_id = Assessment.memberId)
            )
            WHERE patientId IS NOT NULL 
            """.trimIndent()
            )

            database.execSQL(
                """
            CREATE TABLE IF NOT EXISTS Assessment_new (
                 id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                 householdMemberLocalId INTEGER NOT NULL DEFAULT 0,
                 memberId TEXT,
                 householdId TEXT,
                 patientId TEXT,
                 villageId TEXT NOT NULL,
                 assessmentType TEXT NOT NULL,
                 assessmentDetails TEXT NOT NULL,
                 otherDetails TEXT,
                 isReferred INTEGER NOT NULL,
                 referralStatus TEXT NOT NULL,
                 referredReason TEXT,
                 followUpId INTEGER,
                 latitude REAL NOT NULL,
                 longitude REAL NOT NULL,
                 fhir_id TEXT,
                 sync_status TEXT NOT NULL,
                 created_by INTEGER NOT NULL,
                 updated_at INTEGER NOT NULL,
                 created_at INTEGER NOT NULL
                 )
            """.trimIndent()
            )

            database.execSQL(
                """
            INSERT INTO Assessment_new (id, householdMemberLocalId, memberId, householdId, patientId, villageId, assessmentType, assessmentDetails, otherDetails, isReferred, referralStatus, referredReason, followUpId, latitude, longitude, fhir_id, sync_status, created_by, updated_at, created_at)
            SELECT id, householdMemberLocalId, memberId, householdId, patientId, villageId, assessmentType, assessmentDetails, otherDetails, isReferred, referralStatus, referredReason, followUpId, latitude, longitude, fhir_id, sync_status, created_by, updated_at, created_at
            FROM Assessment
            """.trimIndent()
            )

            database.execSQL("DROP TABLE Assessment")
            database.execSQL("ALTER TABLE Assessment_new RENAME TO Assessment")

            /* 3. PregnancyDetail Entity Migration
            *   3.1. Added householdMemberLocalId - for keeping local id as foreign key for PregnancyDetail
            *   3.2. Added neonateHouseholdMemberLocalId - for keeping neonate local id  as foreign key for PregnancyDetail
            *   3.3. Setting householdMemberLocalId by using member patient id and member fhir id
            *   3.4. Setting neonateHouseholdMemberLocalId by using member patient id
            * */

            database.execSQL("ALTER TABLE PregnancyDetail ADD COLUMN householdMemberLocalId INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE PregnancyDetail ADD COLUMN neonateHouseholdMemberLocalId INTEGER")

            database.execSQL(
                """
            UPDATE PregnancyDetail
            SET householdMemberLocalId = (
                SELECT member.id
                FROM HouseholdMember AS member
                WHERE member.patient_id = PregnancyDetail.patientId AND (member.fhir_id IS NULL OR member.fhir_id = PregnancyDetail.householdMemberId)
            )
            WHERE patientId IS NOT NULL
            """.trimIndent()
            )

            database.execSQL(
                """
            UPDATE PregnancyDetail
            SET neonateHouseholdMemberLocalId = (
                SELECT member.id
                FROM HouseholdMember AS member
                WHERE member.patient_id = PregnancyDetail.neonatePatientId
            )
            WHERE neonatePatientId IS NOT NULL
            """.trimIndent()
            )

            // Create newly added table LinkHouseholdMember
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS LinkHouseholdMember (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    memberId TEXT NOT NULL,
                    currentStatus TEXT NOT NULL,
                    syncStatus TEXT
                )
                """.trimIndent()
            )

            // Create newly added table CallHistory
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS CallHistory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    type TEXT NOT NULL,
                    referenceId TEXT NOT NULL,
                    callStartTime INTEGER NOT NULL,
                    callEndTime INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL
                )
                """.trimIndent())

        }
    }
}