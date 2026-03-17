package org.medtroniclabs.uhis.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object SpiceAfricaMigration {
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
                head_phone_number_category TEXT,
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
        """,
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
            """,
            )

            database.execSQL("DROP TABLE Household")
            database.execSQL("ALTER TABLE Household_temp RENAME TO Household")
            database.execSQL(
                "Update Household SET head_phone_number_category = (SELECT member.phone_number_category  From HouseholdMember AS member WHERE member.household_id = Household.id AND member.household_head_relationship = 'HouseholdHead')",
            )

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
                """.trimIndent(),
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
                """.trimIndent(),
            )

            database.execSQL(
                """
                INSERT INTO Assessment_new (id, householdMemberLocalId, memberId, householdId, patientId, villageId, assessmentType, assessmentDetails, otherDetails, isReferred, referralStatus, referredReason, followUpId, latitude, longitude, fhir_id, sync_status, created_by, updated_at, created_at)
                SELECT id, householdMemberLocalId, memberId, householdId, patientId, villageId, assessmentType, assessmentDetails, otherDetails, isReferred, referralStatus, referredReason, followUpId, latitude, longitude, fhir_id, sync_status, created_by, updated_at, created_at
                FROM Assessment
                """.trimIndent(),
            )

            database.execSQL("DROP TABLE Assessment")
            database.execSQL("ALTER TABLE Assessment_new RENAME TO Assessment")

            /*
             * Mother Reference Id for kid
             * */

            database.execSQL("ALTER TABLE HouseholdMember ADD COLUMN motherReferenceId INTEGER")

            database.execSQL(
                """
                UPDATE HouseholdMember
                    SET motherReferenceId = (
                    SELECT member.id
                    FROM HouseholdMember AS member
                    WHERE member.patient_id = HouseholdMember.parentId
                )
                WHERE parentId IS NOT NULL
                """.trimIndent(),
            )

            database.execSQL(
                "UPDATE HouseholdMember SET household_head_relationship = \"Wife / Husband\" WHERE household_head_relationship = \"Spouse / Partner\"",
            )

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
                """.trimIndent(),
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
                """.trimIndent(),
            )

            // Create newly added table LinkHouseholdMember
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS LinkHouseholdMember (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    memberId TEXT NOT NULL,
                    status TEXT NOT NULL,
                    syncStatus TEXT
                )
                """.trimIndent(),
            )

            // Create newly added table CallHistory
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS CallHistory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    type TEXT NOT NULL,
                    referenceId TEXT NOT NULL,
                    callStartTime INTEGER NOT NULL,
                    callEndTime INTEGER NOT NULL,
                    syncStatus TEXT NOT NULL
                )
                """.trimIndent(),
            )

            database.execSQL("ALTER TABLE DiagnosisEntity ADD COLUMN cultureValue TEXT")
            database.execSQL("ALTER TABLE FrequencyEntity ADD COLUMN displayValue TEXT")
            database.execSQL("ALTER TABLE ClinicalWorkflowConditionEntity ADD COLUMN groupName TEXT")
            database.execSQL("ALTER TABLE ClinicalWorkflowConditionEntity ADD COLUMN category TEXT")

            // Create a new table with the updated schema
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS VillageEntity_new (
                    id INTEGER PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    villagecode TEXT,
                    chiefdomId INTEGER,
                    countryId INTEGER NOT NULL,
                    districtId INTEGER,
                    chiefdomCode TEXT,
                    districtCode TEXT
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                INSERT INTO VillageEntity_new (id, name, villagecode, chiefdomId, countryId, districtId, chiefdomCode, districtCode)
                SELECT id, name, villagecode, chiefdomId, countryId, districtId, chiefdomCode, districtCode FROM VillageEntity
                """.trimIndent(),
            )

            database.execSQL("DROP TABLE VillageEntity")
            database.execSQL("ALTER TABLE VillageEntity_new RENAME TO VillageEntity")

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS ProgramEntity (
                    id INTEGER PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    health_facilities TEXT NOT NULL,
                    tenant_id INTEGER NOT NULL
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS CulturesEntity (
                    id INTEGER PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS ConsentEntity (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    formType TEXT NOT NULL,
                    formInput TEXT NOT NULL
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `MentalHealthEntity` (
                    `formType` TEXT NOT NULL,
                    `formInput` TEXT,
                    PRIMARY KEY(`formType`)
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS MedicalComplianceEntity (
                    id INTEGER PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    display_order INTEGER,
                    display_value TEXT,
                    parent_compliance_id INTEGER,
                    child_exists INTEGER NOT NULL DEFAULT 0,
                    culture_value TEXT
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS ChiefDomEntity (
                    id INTEGER PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    code TEXT,
                    districtId INTEGER NOT NULL
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS DistrictEntity (
                    id INTEGER PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    code TEXT,
                    countryId INTEGER NOT NULL
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `ScreeningEntity` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `screeningDetails` TEXT NOT NULL,
                    `generalDetails` TEXT NOT NULL,
                    `uploadStatus` INTEGER NOT NULL DEFAULT 0,
                    `createdAt` INTEGER NOT NULL DEFAULT 0,
                    `userId` TEXT,
                    `signature` BLOB,
                    `isReferred` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `RiskFactorEntity` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `nonLabEntity` TEXT NOT NULL,
                    `uploadStatus` INTEGER NOT NULL DEFAULT 0,
                    `createdAt` INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `LifestyleEntity` (
                    `id` INTEGER PRIMARY KEY NOT NULL,
                    `name` TEXT NOT NULL,
                    `displayValue` TEXT,
                    `value` TEXT,
                    `answers` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `displayOrder` INTEGER NOT NULL
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `NCDMedicalReviewMetaEntity` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `displayValue` TEXT NOT NULL,
                    `displayOrder` INTEGER NOT NULL,
                    `type` TEXT,
                    `category` TEXT,
                    `value` TEXT
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `AssessmentNCDEntity` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `assessmentDetails` TEXT NOT NULL,
                    `uploadStatus` INTEGER NOT NULL DEFAULT 0,
                    `createdAt` INTEGER NOT NULL,
                    `userId` INTEGER
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `unit_metric_entity` (
                    `id` INTEGER PRIMARY KEY NOT NULL,
                    `unit` TEXT NOT NULL,
                    `type` TEXT,
                    `displayOrder` INTEGER NOT NULL,
                    `description` TEXT,
                    `status` INTEGER NOT NULL,
                    `displayValue` TEXT NOT NULL,
                    `isDefault` INTEGER NOT NULL,
                    `answerDependent` INTEGER NOT NULL,
                    `childExists` INTEGER NOT NULL
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `dosage_frequency_entity` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `displayOrder` INTEGER NOT NULL,
                    `frequency` INTEGER NOT NULL DEFAULT 1,
                    `description` TEXT,
                    `status` INTEGER NOT NULL,
                    `displayValue` TEXT,
                    `isDefault` INTEGER NOT NULL,
                    `answerDependent` INTEGER NOT NULL,
                    `childExists` INTEGER NOT NULL
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS NCDDiagnosisEntity (
                    id INTEGER PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    displayOrder INTEGER NOT NULL,
                    value TEXT,
                    type TEXT,
                    gender TEXT
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `TreatmentPlanEntity` (
                    `id` INTEGER PRIMARY KEY NOT NULL,
                    `name` TEXT NOT NULL,
                    `displayValue` TEXT,
                    `displayOrder` INTEGER NOT NULL,
                    `duration` TEXT NOT NULL,
                    `period` TEXT NOT NULL,
                    `riskLevel` TEXT,
                    `type` TEXT
                )
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `shortageReason` (
                    `id` INTEGER NOT NULL PRIMARY KEY,
                    `name` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `displayOrder` INTEGER NOT NULL,
                    `displayValue` TEXT NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

    val MIGRATION_2_5 = object : Migration(2, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Step 1: Create new table with updated schema
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS NCDMedicalReviewMetaEntity_new (
                    primaryId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    displayValue TEXT NOT NULL,
                    displayOrder INTEGER NOT NULL,
                    type TEXT,
                    category TEXT,
                    value TEXT
                )
                """.trimIndent(),
            )

            // Step 2: Copy data from old table to new table
            db.execSQL(
                """
                INSERT INTO NCDMedicalReviewMetaEntity_new (id, name, displayValue, displayOrder, type, category, value)
                SELECT id, name, displayValue, displayOrder, type, category, value FROM NCDMedicalReviewMetaEntity
                """.trimIndent(),
            )

            // Step 3: Drop the old table
            db.execSQL("DROP TABLE NCDMedicalReviewMetaEntity")

            // Step 4: Rename the new table to the original name
            db.execSQL("ALTER TABLE NCDMedicalReviewMetaEntity_new RENAME TO NCDMedicalReviewMetaEntity")

            // 2.1.1 migration
            // In Assessment callResult is Newly added
            db.execSQL("ALTER TABLE Assessment ADD COLUMN callResult TEXT DEFAULT NULL")

            // CommunityProfile new  entity is Added
            // Step 1: Create new table with full schema
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS CommunityProfile_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    villageId INTEGER NOT NULL,
                    communityDescription TEXT,
                    registeredDate TEXT,
                    payload TEXT,
                    latitude REAL NOT NULL DEFAULT 0.0,
                    longitude REAL NOT NULL DEFAULT 0.0,
                    created_at INTEGER NOT NULL DEFAULT 0,
                    updated_at INTEGER NOT NULL DEFAULT 0,
                    created_by INTEGER NOT NULL DEFAULT 0,
                    sync_status TEXT NOT NULL DEFAULT 'NotSynced',
                    fhir_id TEXT
                )
                """.trimIndent(),
            )

            // Step 2: Check if the old table exists
            val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='CommunityProfile'")
            val tableExists = cursor.count > 0
            cursor.close()

            if (tableExists) {
                try {
                    // Step 3: Copy existing data (set default values for new columns)
                    db.execSQL(
                        """
                        INSERT INTO CommunityProfile_new (id, villageId, communityDescription, registeredDate, payload, latitude, longitude, created_at, updated_at, created_by, sync_status, fhir_id)
                        SELECT id, villageId, communityDescription, registeredDate, payload, latitude, longitude, 0, 0, 0, 'NotSynced', NULL
                        FROM CommunityProfile
                        """.trimIndent(),
                    )
                } catch (e: Exception) {
                    e.printStackTrace() // Logs migration errors
                }

                // Step 4: Drop old table
                db.execSQL("DROP TABLE CommunityProfile")
            }

            // Step 5: Rename new table to original name
            db.execSQL("ALTER TABLE CommunityProfile_new RENAME TO CommunityProfile")

            // HealthFacilityEntity phoneNumber is Newly added
            db.execSQL("ALTER TABLE HealthFacilityEntity ADD COLUMN phoneNumber TEXT DEFAULT NULL")

            // HouseholdMemberEntity deceasedReason is Newly added
            db.execSQL("ALTER TABLE HouseholdMember ADD COLUMN deceasedReason TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE HouseholdMember ADD COLUMN latitude REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE HouseholdMember ADD COLUMN longitude REAL NOT NULL DEFAULT 0.0")

            // LinkHouseholdMemberEntity is Added
            // Step 1: Create the new table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS LinkedVillageEntity_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    villageId INTEGER NOT NULL,
                    tenantId INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    villagecode TEXT,
                    chiefdomId INTEGER,
                    countryId INTEGER NOT NULL,
                    districtId INTEGER,
                    isUserVillage INTEGER NOT NULL DEFAULT 0,
                    chiefdomCode TEXT,
                    districtCode TEXT
                )
                """.trimIndent(),
            )

            // Step 2: Check if the old table exists
            val cursorLink = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='LinkedVillageEntity'")
            val tableExistsLink = cursorLink.count > 0
            cursor.close()

            if (tableExistsLink) {
                db.execSQL(
                    """
                    INSERT INTO LinkedVillageEntity_new (id, villageId, tenantId, name, villagecode, chiefdomId, countryId, districtId, isUserVillage, chiefdomCode, districtCode)
                    SELECT id, villageId, tenantId, name, villagecode, chiefdomId, countryId, districtId, isUserVillage, chiefdomCode, districtCode FROM LinkedVillageEntity
                    """.trimIndent(),
                )

                // Step 3: Drop the old table
                db.execSQL("DROP TABLE LinkedVillageEntity")
            }

            // Step 4: Rename new table to the original name
            db.execSQL("ALTER TABLE LinkedVillageEntity_new RENAME TO LinkedVillageEntity")

            // MemberClinicalEntity is Added
            // Check if table exists
            val cursorMemberClinicalEntity = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='MemberClinicalEntity'")
            val tableExistsMemberClinicalEntity = cursorMemberClinicalEntity.count > 0
            cursorMemberClinicalEntity.close()

            if (!tableExistsMemberClinicalEntity) {
                // Create the table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS MemberClinicalEntity (
                        patientId TEXT,
                        visitCount INTEGER NOT NULL,
                        clinicalDate TEXT,
                        numberOfNeonate INTEGER,
                        isDeliveryAtHome INTEGER,
                        neonateHouseholdMemberLocalId INTEGER DEFAULT NULL, -- Newly Added Column
                        isNeonateDeathRecordedByPHU INTEGER
                    )
                    """.trimIndent(),
                )
            } else {
                // Only alter if table exists
                db.execSQL("ALTER TABLE MemberClinicalEntity ADD COLUMN neonateHouseholdMemberLocalId INTEGER DEFAULT NULL;")
                db.execSQL("ALTER TABLE MemberClinicalEntity ADD COLUMN isNeonateDeathRecordedByPHU INTEGER DEFAULT NULL;")
            }

            // PregnancyDetailEntity  isNeonateDeathRecordedByPHU Newly Added
            db.execSQL("ALTER TABLE PregnancyDetail ADD COLUMN isNeonateDeathRecordedByPHU INTEGER DEFAULT NULL")

            // VillageEntity healthFacilityId is Newly Added
            db.execSQL("ALTER TABLE VillageEntity ADD COLUMN healthFacilityId INTEGER DEFAULT NULL")
        }
    }
}
