package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.BuildConfig
import com.medtroniclabs.spice.appextensions.convertToUtcDateTime
import com.medtroniclabs.spice.appextensions.imgFileNameExtension
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.appextensions.signatureFolder
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.CBS
import com.medtroniclabs.spice.common.DefinedParams.COMMUNITY_REGISTERED_DATE
import com.medtroniclabs.spice.common.DefinedParams.Description
import com.medtroniclabs.spice.common.DefinedParams.FollowUp
import com.medtroniclabs.spice.common.DefinedParams.Provenance
import com.medtroniclabs.spice.common.DefinedParams.ReferenceId
import com.medtroniclabs.spice.common.DefinedParams.UnAssigned
import com.medtroniclabs.spice.common.DefinedParams.VillageId
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.offlinesync.model.Assessment
import com.medtroniclabs.spice.data.offlinesync.model.AssessmentEncounter
import com.medtroniclabs.spice.data.offlinesync.model.CallRegisterDetail
import com.medtroniclabs.spice.data.offlinesync.model.FollowUpCriteria
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.HouseHoldMember
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdMemberLinkCallDetails
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.data.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.data.offlinesync.model.ResponseInitialDownload
import com.medtroniclabs.spice.data.offlinesync.model.ResponseRxBuddy
import com.medtroniclabs.spice.data.offlinesync.model.RxBuddy
import com.medtroniclabs.spice.data.offlinesync.model.RxBuddyFollowUp
import com.medtroniclabs.spice.data.offlinesync.model.RxBuddyMember
import com.medtroniclabs.spice.data.offlinesync.model.RxBuddyRegister
import com.medtroniclabs.spice.data.offlinesync.model.SyncEntityList
import com.medtroniclabs.spice.data.offlinesync.model.SyncResponse
import com.medtroniclabs.spice.data.offlinesync.model.TreatmentDetails
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineUtils
import com.medtroniclabs.spice.data.resource.RequestAllEntities
import com.medtroniclabs.spice.db.entity.CommunityProfile
import com.medtroniclabs.spice.db.entity.EntitiesName
import com.medtroniclabs.spice.db.entity.EntitiesName.COMMUNITY_PROFILE
import com.medtroniclabs.spice.db.entity.LinkHouseholdMember
import com.medtroniclabs.spice.db.entity.RxBuddyDetails
import com.medtroniclabs.spice.db.entity.TreatmentDetailsEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.mappingkey.RxBuddy.RX_BUDDY_TYPE_HOUSEHOLD_MEMBER
import com.medtroniclabs.spice.mappingkey.RxBuddy.hadReactionToYourMedications
import com.medtroniclabs.spice.mappingkey.RxBuddy.isSymptomsGettingWorse
import com.medtroniclabs.spice.mappingkey.RxBuddy.rxBuddyMonitoringDates
import com.medtroniclabs.spice.model.assessment.AssessmentDetails
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.MenuConstants.ICCM_MENU_ID
import com.medtroniclabs.spice.ui.MenuConstants.TB_MENU_ID
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ChildHoodVisit
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.NeonatePatientId
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.NeonatePatientReferenceId
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PNC
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PNCNeonatal
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.visitNo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.lang.reflect.Type
import javax.inject.Inject

class OfflineSyncRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    private val mutex = Mutex()

    private suspend fun getUnSyncedAssessmentByPatientId(hhmId: Long): List<Assessment> {
        return convertEntityToRequest(roomHelper.getUnSyncedAssessmentByHHMId(hhmId))
    }

    private suspend fun getOtherUnSyncedAssessments(addedAssessmentIds: List<String>): List<Assessment> {
        return convertEntityToRequest(roomHelper.getOtherUnSyncedAssessments(addedAssessmentIds))
    }

    private fun convertEntityToRequest(list: List<AssessmentDetails>): List<Assessment> {
        val peerSupervisorId = SecuredPreference.getLong(SecuredPreference.EnvironmentKey.PEER_SUPERVISOR_ID.name)
        return list.map { entity ->
            val assessmentDetail = getAssessmentDetails(entity)
            Assessment(
                referenceId = entity.id,
                villageId = entity.villageId,
                assessmentType = entity.assessmentType,
                assessmentDetails = assessmentDetail,
                patientStatus = entity.referralStatus,
                referredReasons = entity.referredReason?.joinToString(", "),
                summary = entity.otherDetails?.let { JsonParser.parseString(it) },
                peerSupervisorId = peerSupervisorId,
                encounter = AssessmentEncounter(
                    householdId = entity.householdId,
                    memberId = entity.memberId,
                    referred = entity.isReferred,
                    patientId = entity.patientId,
                    provenance = ProvanceDto(modifiedDate = entity.createdAt.convertToUtcDateTime()),
                    latitude = entity.latitude,
                    longitude = entity.longitude,
                    visitNumber = getVisitNumber(entity.assessmentType, assessmentDetail, entity.neonatePatientId, entity.neonatePatientReferenceId)
                ),
                followUpId = entity.followUpId,
                updatedAt = entity.createdAt
            )
        }
    }

    private fun getAssessmentDetails(assessment: AssessmentDetails): JsonElement {
       val assessmentDetails =  JsonParser.parseString(assessment.assessmentDetails)
        val followUpDetails = assessment.callResult?.let { JsonParser.parseString(it) }

        val assessmentType = assessment.assessmentType.lowercase()

        // CBS Registration changes
        if (assessmentType == CBS.lowercase()) {
            val assessmentObject =
                assessmentDetails.asJsonObject.get(CBS.lowercase()).asJsonObject
            followUpDetails?.let {
                assessmentObject.add(FollowUp, it)
            }
        }

        // CBS changes for ICCM
        if (assessmentType == ICCM_MENU_ID.lowercase()) {
            val assessmentObject =
                assessmentDetails.asJsonObject.get(ICCM_MENU_ID.lowercase()).asJsonObject
            followUpDetails?.let {
                assessmentObject.add(FollowUp, it)
            }
        }

        // CBS changes for ANC
        if (assessmentType == RMNCH.ANC_MENU.lowercase()) {
            updateCbsForRMNCH(assessmentDetails, followUpDetails, ANC)
        }

        // CBS changes for PNC Neonate
        if (assessmentType == RMNCH.pnc_neonate_key.lowercase()) {
            updateCbsForRMNCH(assessmentDetails, followUpDetails, PNCNeonatal)
        }

        // CBS changes for Childhood Visit
        if (assessmentType == RMNCH.CHILD_MENU.lowercase()) {
            updateCbsForRMNCH(assessmentDetails, followUpDetails, ChildHoodVisit)
        }

        return assessmentDetails
    }

    private fun updateCbsForRMNCH(assessmentDetails: JsonElement, followUpDetails: JsonElement?, key: String ) {
        val mainObject = assessmentDetails.asJsonObject
        val asst = mainObject.get(key).asJsonObject
        mainObject.get(CBS.lowercase())?.let { 
            val cbs = it.asJsonObject
            followUpDetails?.let {
                cbs.add(FollowUp, followUpDetails)
            }
            asst.add(CBS.lowercase(), cbs)

            mainObject.remove(CBS.lowercase())
        }
    }

    private fun getVisitNumber(assessmentType: String, assessmentDetails: JsonElement, neonatePatientId: String?, neonatePatientReferenceId: Long?): Long? {
        when (assessmentType.lowercase()) {
            RMNCH.ANC_MENU.lowercase() -> return getRMNCHVisitNumber(ANC, assessmentDetails)
            RMNCH.pnc_mother_key.lowercase() -> return getRMNCHPNCVisitNumber(PNC, assessmentDetails, neonatePatientId, neonatePatientReferenceId)
            RMNCH.pnc_neonate_key.lowercase() -> return getRMNCHVisitNumber(PNCNeonatal, assessmentDetails)
            RMNCH.CHILD_MENU.lowercase() -> return getRMNCHVisitNumber(
                ChildHoodVisit,
                assessmentDetails
            )

            else -> return null
        }
    }

    private fun getRMNCHVisitNumber(key: String, assessmentDetails: JsonElement): Long {
        return assessmentDetails.asJsonObject.get(key).asJsonObject.get(visitNo).asLong
    }

    private fun getRMNCHPNCVisitNumber(key: String, assessmentDetails: JsonElement, neonatePatientId: String?, neonatePatientReferenceId: Long?): Long {
        val assessmentObject = assessmentDetails.asJsonObject.get(key).asJsonObject
        assessmentObject.addProperty(NeonatePatientId, neonatePatientId)
        assessmentObject.addProperty(NeonatePatientReferenceId, neonatePatientReferenceId)
        return assessmentObject.get(visitNo).asLong
    }

    suspend fun getSyncStatus(request: RequestGetSyncStatus): Response<SyncResponse> {
        return apiHelper.getOfflineSyncStatus(request)
    }

    private suspend fun updateFhirId(tableName: String, id: String, fhirId: String?, status: String) {
        roomHelper.updateFhirId(tableName, id, fhirId, status)
    }

    suspend fun getInsertOrUpdateLocalData(liveData: MutableLiveData<Resource<Boolean>>) {
        // Check and Delete local data
        val lastSyncedAt = SecuredPreference.getString(SecuredPreference.EnvironmentKey.SERVER_LAST_SYNCED.name)
        if (lastSyncedAt == null) {
            roomHelper.deleteAllHouseholds()
            roomHelper.deleteAllHouseholdMembers()
            roomHelper.deleteAllPregnancyDetails()
            roomHelper.deleteAllAssessments()
            roomHelper.deleteAllFollowUpCalls()
            roomHelper.deleteAllFollowUps()
            roomHelper.deleteAllUnAssignedMember()
            roomHelper.deleteAllCallHistory()
            roomHelper.deleteAllCommunityProfiles()
            roomHelper.deleteAllRxBuddyDetails()
            roomHelper.deleteAllRxBuddyFollowUp()

            val villageIds = roomHelper.getAllVillageIds()
            // Fetch Synced Data
            val isInitialDataSuccess = fetchSyncedData(villageIds, null)

            // Need to check this to be added for downloading error and inprogress data
            /* if (!fetchUnSyncedData()) {
                 liveData.postError("Something went wrong")
             }*/

            if (isInitialDataSuccess) {
                liveData.postSuccess(true)
            } else {
                liveData.postError("Something went wrong")
            }
        } else {
            liveData.postSuccess(true)
        }
    }

    suspend fun fetchSyncedData(villageIds: List<Long> = listOf(), serverLastSyncedAt: String? = null): Boolean {
        val syncedResponse = getSyncedEntities(villageIds, serverLastSyncedAt)
        if (syncedResponse.isSuccessful) {
            val response = syncedResponse.body()?.string()
            response?.let {
                try {
                    val gson = Gson()
                    val type: Type = object : TypeToken<ResponseInitialDownload>() {}.type
                    val responseInitialDownload: ResponseInitialDownload? = gson.fromJson(it, type)
                    if (responseInitialDownload == null) {
                        return false
                    } else {
                        saveRequestInitialDownload(responseInitialDownload)
                        return true
                    }

                } catch (e: Exception) {
                    Timber.d("Exception ${e.localizedMessage}")
                    return false
                }
            }
        } else {
            return false
        }
        return false
    }

    private suspend fun saveRequestInitialDownload(requestInitialDownload: ResponseInitialDownload) {
        val hhMapping = insertHouseholds(requestInitialDownload.households)
        insertHouseholdMembers(requestInitialDownload.members, hhMapping)

        // List of changes for Followup incremental
        // 1. Delete All Followup where Followup id is null and SyncStatus is InProgress -> Because it is created from Mobile
        // 2. Update record when sync status is Not NotSynced
        // 3. Delete All FollowUp where isCompleted true and syncStatus is Success
        // Insert follow up
        requestInitialDownload.followUps?.forEach { followUp ->
            followUp.patientStatus = followUp.patientStatus ?: ""
            followUp.syncStatus = OfflineSyncStatus.Success
            roomHelper.insertOrUpdateFollowUp(followUp)
        }
        roomHelper.deleteCompletedFollowUp()

        /*Insert Link household member details*/
        requestInitialDownload.householdMemberLinks?.let {
            val insertList = mutableListOf<LinkHouseholdMember>()
            val deleteListIds = mutableListOf<String>()
            it.forEach { linkHouseholdMember ->
                if (linkHouseholdMember.status == UnAssigned) { // Insert
                    linkHouseholdMember.syncStatus = OfflineSyncStatus.Success
                    insertList.add(linkHouseholdMember)
                } else { // Delete
                    deleteListIds.add(linkHouseholdMember.memberId)
                }
            }
            roomHelper.insertLinkHouseholdMembers(insertList)
            roomHelper.deleteLinkHouseholdMembersById(deleteListIds)
        }


        // Insert Pregnancy Information
        requestInitialDownload.pregnancyInfos?.forEach {
            roomHelper.insertUpdatePregnancyDetailFromBE(it)
        }

        // Save Community profiles
        requestInitialDownload.communityProfiles?.forEach { item ->
            val id = item.get(DefinedParams.ID).asString
            val villageId = item.get(VillageId).asLong
            val description = item.get(Description).asString
            val date = item.get(COMMUNITY_REGISTERED_DATE).asString
            item.remove(DefinedParams.ID)
            item.remove(VillageId)
            item.remove(Description)
            item.remove(COMMUNITY_REGISTERED_DATE)
            val communityProfileEntity = CommunityProfile(id = 0, villageId, description, date, payload = item.toString())
            communityProfileEntity.fhirId = id
            communityProfileEntity.sync_status = OfflineSyncStatus.Success
            roomHelper.insertOrUpdateFromBE(communityProfileEntity)
        }

        /*Save Treatment details*/
        requestInitialDownload.treatmentDetails?.let {
            insertOrUpdateTreatmentDetails(it)
        }

        /*Save Rx Buddy Register Details*/
        requestInitialDownload.rxBuddies?.let {
            insertOrUpdateRxBuddyData(it)
        }

        // Set FollowUpCriteria in Preference
        requestInitialDownload.followUpCriteria?.let {
            SecuredPreference.putFollowUpCriteria(it)
        } ?: kotlin.run {
            val followUpCriteria = FollowUpCriteria(3, 5, 3, 7, 7, 2, 2, 2, 2, 5, 5, 5, 5)
            SecuredPreference.putFollowUpCriteria(followUpCriteria)
        }

        SecuredPreference.putString(SecuredPreference.EnvironmentKey.SERVER_LAST_SYNCED.name,
            requestInitialDownload.lastSyncTime)
    }

    private suspend fun insertOrUpdateTreatmentDetails(treatmentDetails: List<TreatmentDetails>) {
        val gson = Gson()
        roomHelper.deleteAllTreatmentDetails()
        treatmentDetails.forEach { treatmentDetail ->
            if (treatmentDetail.isTbConfirmed == true) {
                val prescriptionList = gson.toJson(treatmentDetail.prescriptions)
                val entity = TreatmentDetailsEntity(
                    memberId = treatmentDetail.memberId,
                    type = TB_MENU_ID.lowercase(),
                    treatmentStartDate = treatmentDetail.treatmentStartDate,
                    diagnoses = treatmentDetail.diagnosis,
                    diagnosedDate = treatmentDetail.dateDiagnosed ?: "",
                    prescriptions = prescriptionList
                )

                roomHelper.insertTreatmentDetails(entity)
            }
        }
    }

    private suspend fun insertOrUpdateRxBuddyData(rxBuddies: List<ResponseRxBuddy>) {
        val disableIds = mutableListOf<Long>()
        rxBuddies.forEach { item ->
            if (item.isActive) {
                val registry = item.registry
                val rxBuddy = RxBuddyDetails(
                    id = 0,
                    rxBuddyId = item.id,
                    patientMemberId = item.patientMemberId,
                    relationship = registry.relationShip,
                    isMonitorSheetProvider = registry.isMonitorSheetProvided,
                    nextVisitDate = "",
                    otherRelationship = registry.otherRelationship
                )

                if (item.type == RX_BUDDY_TYPE_HOUSEHOLD_MEMBER && registry.householdMemberId != null) {
                    rxBuddy.householdMemberId =
                        roomHelper.getHouseholdMemberIdByFhirId(registry.householdMemberId)
                } else {
                    rxBuddy.name = registry.rxBuddyDetails?.name
                    rxBuddy.phoneNumber = registry.rxBuddyDetails?.phoneNumber
                }

                roomHelper.insertOrUpdateRxBuddyFromBE(rxBuddy)
            } else {
                disableIds.add(item.id)
            }
        }
        roomHelper.deleteDisableRxBuddies(disableIds)
    }

    private suspend fun fetchUnSyncedData(): Boolean {
        val villageNameId = mutableMapOf<String, Long>()
        roomHelper.getAllVillageEntity().forEach {
            villageNameId[it.name] = it.id
        }
        val unSyncedResponse = getUnSyncedEntities()
        if (unSyncedResponse.isSuccessful) {
            // Insert UnSynced Entities
            val householdList =
                unSyncedResponse.body()?.entityList?.filter { it.type == EntitiesName.HOUSEHOLD }
            val hhMap = insertFailedHouseholds(householdList, villageNameId)

            val householdMemberList =
                unSyncedResponse.body()?.entityList?.filter { it.type == EntitiesName.HOUSEHOLD_MEMBER }
            insertFailedHouseholdMembers(householdMemberList, hhMap)
            return true
        } else {
            return false
        }
    }

    private suspend fun insertHouseholds(households: List<HouseHold>?): Map<String, Long> {
        // fhir id, local id
        val hhMap = mutableMapOf<String, Long>()

        households?.forEach { entity ->
            hhMap[entity.id!!] = roomHelper.insertOrUpdateHHFromBE(
                entity.toHouseholdEntity(
                    OfflineSyncStatus.Success
                )
            )
        }

        return hhMap
    }

    private suspend fun insertHouseholdMembers(
        householdMembers: List<HouseHoldMember>?,
        hhIdMap: Map<String, Long>
    ) {
        householdMembers?.forEach { member ->
            if (hhIdMap.containsKey(member.householdId)) {
                roomHelper.insertOrUpdateHHMFromBE(member.toHouseholdMemberEntity(
                    hhIdMap[member.householdId]!!,
                    OfflineSyncStatus.Success
                ))
            } else {
                if (member.householdId != null) {
                    roomHelper.getHouseholdIdByFhirId(member.householdId)?.let {
                        roomHelper.insertOrUpdateHHMFromBE(
                            member.toHouseholdMemberEntity(
                                it,
                                OfflineSyncStatus.Success
                            )
                        )
                    }
                } else {
                    roomHelper.insertOrUpdateHHMFromBE(
                        member.toHouseholdMemberEntity(
                            null,
                            OfflineSyncStatus.Success
                        )
                    )
                }
            }
        }
    }

    private suspend fun insertFailedHouseholds(
        households: List<SyncEntityList>?,
        villageNameId: Map<String, Long>
    ): Map<String, HouseHold> {
        // Response apiReferenceId, Household
        val hhMap = mutableMapOf<String, HouseHold>()
        households?.forEach { entity ->
            Gson().fromJson(entity.data, HouseHold::class.java)?.let { houseHold ->
                val apiRefId = houseHold.referenceId
                var dbHHId: Long?
                if (houseHold.id != null) { // Fhir id is not null - Success
                    dbHHId = roomHelper.getHouseholdIdByFhirId(houseHold.id)
                    if (dbHHId != null) { // Update Flow
                        roomHelper.updateHousehold(
                            houseHold.toHouseholdEntity(
                                OfflineSyncStatus.Success,
                                dbHHId
                            )
                        )
                    } else { // Insert Flow
                        dbHHId = roomHelper.saveHouseHoldEntry(
                            houseHold.toHouseholdEntity(
                                OfflineSyncStatus.Success
                            )
                        )
                    }
                } else { // Fhir id is null - Failed
                    dbHHId = roomHelper.saveHouseHoldEntry(
                        houseHold.toHouseholdEntity(
                            OfflineSyncStatus.Failed
                        )
                    )
                }

                houseHold.referenceId = dbHHId.toString()
                hhMap[apiRefId!!] = houseHold
            }
        }
        return hhMap
    }

    private suspend fun insertFailedHouseholdMembers(
        householdMemberList: List<SyncEntityList>?,
        hhMap: Map<String, HouseHold>
    ) {
        householdMemberList?.forEach { entity ->
            Gson().fromJson(entity.data, HouseHoldMember::class.java)?.let { member ->
                val dbHHId = roomHelper.getHouseholdIdByFhirId(member.householdId)
                    ?: hhMap[member.householdReferenceId]?.referenceId?.toLong()
                if (dbHHId != null) { // HouseholdId found in local
                    if (member.id != null) { //  Fhir id is not null - Success
                        val dbHHMId = roomHelper.getHouseholdMemberIdByFhirId(member.id)
                        if (dbHHMId != null) { // Update Flow
                            roomHelper.registerMember(
                                member.toHouseholdMemberEntity(
                                    dbHHId,
                                    OfflineSyncStatus.Success,
                                    dbHHMId
                                )
                            )
                        } else { // Insert Flow
                            roomHelper.registerMember(
                                member.toHouseholdMemberEntity(
                                    dbHHId,
                                    OfflineSyncStatus.Success
                                )
                            )
                        }
                    } else { // Fhir id is null - Failed
                        roomHelper.registerMember(
                            member.toHouseholdMemberEntity(
                                dbHHId,
                                OfflineSyncStatus.Failed
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun getSyncedEntities(
        villageList: List<Long>,
        lastSyncedAt: String? = null
    ): Response<ResponseBody> {
        // Getting village name only. For mapping I have used following code
        val request = RequestAllEntities(villageList, lastSyncedAt)
        return apiHelper.fetchSyncedData(request)
    }

    private suspend fun getUnSyncedEntities(): Response<SyncResponse> {
        val req = RequestGetSyncStatus(
            userId = SecuredPreference.getUserId(),
            dataRequired = true,
            statuses = listOf(OfflineSyncStatus.InProgress.name, OfflineSyncStatus.Failed.name),
            types = listOf(EntitiesName.HOUSEHOLD, EntitiesName.HOUSEHOLD_MEMBER)
        )

        return apiHelper.getOfflineSyncStatus(req)
    }


    suspend fun uploadAllSignatures(): Boolean {
        val hhSignatureDetails = roomHelper.getHHSignatureDetails()

        if (hhSignatureDetails.isEmpty())
            return true

        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)

        hhSignatureDetails.forEach { hhSignatureDetail ->
            getRenamedFile(hhSignatureDetail.signatureName, hhSignatureDetail.fhirId)?.let { file ->
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                builder.addFormDataPart("signatureFile", file.name, requestFile)
            }
        }

        val dataRequest = Gson().toJson(ProvanceDto())
        builder.addFormDataPart("provenance", dataRequest)

        return try {
            val response = apiHelper.uploadAllConsentSignatures(builder.build())
            if (response.isSuccessful)
                deleteAllSyncedImages()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getRenamedFile(oldFileName: String, newFileName: String): File? {
        val signatureDirPath = "/data/data/${BuildConfig.APPLICATION_ID}/files/${signatureFolder}"
        val signatureDir = File(signatureDirPath)

        if (signatureDir.exists()) {
            val oldFileNameWithExtension = "$oldFileName.$imgFileNameExtension"
            val newFileNameWithExtension = "$newFileName.$imgFileNameExtension"
            val oldFile = File(signatureDir, oldFileNameWithExtension)
            val newFile = File(signatureDir, newFileNameWithExtension)
            if (oldFile.exists()) {
                oldFile.renameTo(newFile)
                return newFile
            }
        }

        return null
    }

    private fun deleteAllSyncedImages(): Boolean {
        val imagesDirPath = "/data/data/${BuildConfig.APPLICATION_ID}/files/${signatureFolder}"
        val imagesDir = File(imagesDirPath)
        return deleteDirectory(imagesDir)
    }

    private fun deleteDirectory(directory: File): Boolean {
        if (directory.exists()) {
            directory.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    deleteDirectory(file)
                } else {
                    file.delete()
                }
            }
        }

        return directory.delete()
    }

    private suspend fun getCallRegisterEntityAsRequest(householdLinkCallsMemberIds : MutableList<String>): List<HouseholdMemberLinkCallDetails> {
        val list = roomHelper.getUnSyncedCallHistoryForHHMLink()
        val callDetails = mutableListOf<HouseholdMemberLinkCallDetails>()
        list.groupBy { it.memberId }.forEach { (key, value) ->
            householdLinkCallsMemberIds.add(key)
            val callRegisters = value.map { CallRegisterDetail(it.callDate.convertToUtcDateTime()) }
            callDetails.add(
                HouseholdMemberLinkCallDetails(
                    memberId = key,
                    patientId = value.first().patientId,
                    villageId = value.first().villageId,
                    callRegisterDetails = callRegisters
                )
            )
        }
        return callDetails
    }

    private suspend fun formatMemberForPnc(
        input: List<HouseHoldMember>,
        memberIds: MutableList<String>,
        assessmentIds: MutableList<String>
    ): List<HouseHoldMember> {
        val motherIds = mutableSetOf<String>()

        input.forEach { hhm ->
            memberIds.add(hhm.referenceId!!)
            hhm.motherReferenceId?.let {
                hhm.isChild = true
                motherIds.add(it)
            }
            hhm.assessments = getUnSyncedAssessmentByPatientId(hhm.referenceId!!.toLong())
            assessmentIds.addAll(hhm.assessments.map { it.referenceId.toString() })
        }


        val childIds = mutableListOf<String>()
        motherIds.forEach { motherId ->
            val children = input.filter { it.motherReferenceId == motherId && it.id == null }
            val mother = input.find { it.referenceId == motherId }
            if (!children.isNullOrEmpty()) {
                if (mother != null) {
                    mother.children = children
                } else {
                   children.forEach {// Un mapped child
                       memberIds.remove(it.referenceId!!)
                   }
                }
                childIds.addAll(children.map { it.referenceId!! })
            }
        }

        return input.filter { !childIds.contains(it.referenceId) }
    }


    private suspend fun getRxBuddiesRequest(hhmIds: MutableList<String>,
                                            rxBuddyRegisterIds: MutableList<Long>,
                                            rxBuddyFollowUpIds: MutableList<Long>): List<RxBuddy> {
        /*Construct Rx Buddy details*/
        /*
        * 1. Get Un Synced Rx Buddy Register
        * 2. Construct Register object with member id / new rx buddy details
        * 3. Get Un Synced Rx Buddy Followup with Rx Buddy Id
        * */
        val rxBuddies = mutableListOf<RxBuddy>()
        val rxBuddyRegisters = roomHelper.getAllUnSyncedRxBuddyRegister()
        rxBuddyRegisters.forEach { rx ->
            rxBuddyRegisterIds.add(rx.id)
            val register = RxBuddyRegister(
                referenceId = rx.id,
                relationShip = rx.relationship,
                otherRelationship = rx.otherRelationship,
                isMonitorSheetProvided = rx.isMonitorSheetProvider,
                nextVisitDate = rx.nextVisitDate,
                followUpId = rx.followUpId,
                latitude = rx.latitude,
                longitude = rx.longitude,
                provenance = ProvanceDto(modifiedDate = rx.updatedAt.convertToUtcDateTime())
            )

            rx.householdMemberId?.let { hhmId ->
                val member = roomHelper.getHouseholdMemberForRxBuddy(hhmId)
                if (member.id != null)
                    register.householdMemberId = member.id
                else {
                    hhmIds.add(member.referenceId!!)
                    val assessments = getUnSyncedAssessmentByPatientId(member.referenceId.toLong())
                    member.assessments = assessments
                    register.householdMember = member
                }
            } ?: run {
                register.rxBuddyDetails = RxBuddyMember(rx.name, rx.phoneNumber)
            }

            val followUpList = mutableListOf<RxBuddyFollowUp>()
            val followUps = roomHelper.getUnSyncedRxBuddyFollowUpWithoutRxBuddyId(rx.id)
            followUps.forEach { item ->
                rxBuddyFollowUpIds.add(item.rxBuddyLocalId)
                item.followUp?.let { strFollowUp ->
                    followUpList.add(
                        getRxBuddyFollowUp(
                            item.id,
                            item.rxBuddyId,
                            strFollowUp,
                            item.nextVisitDate,
                            item.updatedAt,
                            item.followUpId,
                            item.latitude,
                            item.longitude
                        )
                    )
                }
            }
            rxBuddies.add(RxBuddy(rx.patientMemberId, rx.patientId, rx.villageId, rx.householdId, null, rx.id, register, followUpList))
        }

        val rxBuddyFollowUps = roomHelper.getUnSyncedRxBuddyFollowUpWithRxBuddyId().groupBy { it.patientMemberId }
        val followUpList = mutableListOf<RxBuddyFollowUp>()
        rxBuddyFollowUps.forEach { (patientMemberId, followUps) ->
            followUps.forEach { item ->
                rxBuddyFollowUpIds.add(item.rxBuddyLocalId)
                item.followUp?.let { strFollowUp ->
                    followUpList.add(
                        getRxBuddyFollowUp(
                            item.id,
                            item.rxBuddyId,
                            strFollowUp,
                            item.nextVisitDate,
                            item.updatedAt,
                            item.followUpId,
                            item.latitude,
                            item.longitude
                        )
                    )
                }
            }
            val rx = followUps.first()
            rxBuddies.add(RxBuddy(patientMemberId, rx.patientId, rx.villageId, rx.householdId, followUps[0].rxBuddyId, rx.rxBuddyLocalId, null, followUpList))
        }

        return rxBuddies
    }

    private fun getRxBuddyFollowUp(
        id: Long,
        rxBuddyId: Long?,
        strFollowUp: String,
        nextVisitDate: String,
        updatedAt: Long,
        followUpId: Long? = null,
        latitude: Double,
        longitude: Double
    ): RxBuddyFollowUp {
        val map = StringConverter.stringToMap(strFollowUp)
        return RxBuddyFollowUp(
            id = id,
            rxBuddyId = rxBuddyId,
            monitoringSheet = map[rxBuddyMonitoringDates] as List<String>,
            isSymptomsGettingWorse = map[isSymptomsGettingWorse] as Boolean,
            hadReactionToYourMedications = map[hadReactionToYourMedications] as Boolean,
            nextVisitDate = nextVisitDate,
            followUpId = followUpId,
            latitude = latitude,
            longitude = longitude,
            provenance = ProvanceDto(modifiedDate = updatedAt.convertToUtcDateTime())
        )
    }

    /*
    * It will post all un-synced changes from local database and returns List<String>?
    * 1. list size > 0 -> Posted un-synced local changes and API is success
    * 2. List size == 0 -> There are no local changes to post
    * 3. List is null -> Post un-synced local changes and API is failed
    * */
    private suspend fun postOfflineUnSyncedChanges(syncMode: String): List<String>? {
        val householdIds = mutableListOf<String>()
        val householdMemberIds = mutableListOf<String>()
        val assessmentIds = mutableListOf<String>()
        val followUpIds = mutableListOf<Long>()
        val followUpCallIds = mutableListOf<Long>()
        val householdLinkCallsMemberIds = mutableListOf<String>()
        val communityProfileIds = mutableListOf<Long>()
        val rxBuddyRegisterIds = mutableListOf<Long>()
        val rxBuddyFollowUpIds = mutableListOf<Long>()

        //uploadAllSignatures()
        val rxBuddies = getRxBuddiesRequest(householdMemberIds, rxBuddyRegisterIds, rxBuddyFollowUpIds)

        val houseHoldList = roomHelper.getAllUnSyncedHouseHolds() /*Hot Fix change - Done*/
        householdIds.addAll(houseHoldList.map { it.referenceId!! })
        houseHoldList.forEach { householdEntity ->
            val memberList =
                roomHelper.getAllUnSyncedHouseHoldMembers((householdEntity.referenceId!!.toLong())) /*Hot Fix Change - Need to check*/

            householdEntity.householdMembers.addAll(formatMemberForPnc(memberList, householdMemberIds, assessmentIds))
        }

        val members = roomHelper.getOtherHouseholdMembers(householdMemberIds) /*Hot Fix Change - Need to check*/
        val otherMembers = formatMemberForPnc(members, householdMemberIds, assessmentIds)

        val assignedMemberIds = otherMembers.filter { it.assignHousehold == true && it.id != null }.map { it.id!! }

        //Assessment
        val otherAssessments = getOtherUnSyncedAssessments(assessmentIds) /*Hot Fix change - Done*/
        assessmentIds.addAll(otherAssessments.map { it.referenceId.toString() })

        //Followup
        val allFollowUps = roomHelper.getAllFollowUpRequests()
        allFollowUps.forEach { followUp ->
            followUpIds.add(followUp.referenceId)
            followUp.id?.let {
                val followUpDetails = roomHelper.getAllFollowUpCalls(it)
                followUp.followUpDetails = followUpDetails
                followUpCallIds.addAll(followUpDetails.map { call -> call.id })
            }
        }

        val householdMemberLink = getCallRegisterEntityAsRequest(householdLinkCallsMemberIds)

        // Community profiles
        val communityProfiles = roomHelper.getUnSyncedCommunityDetails()
        val communityProfilesRequests = mutableListOf<JsonObject>()
        communityProfiles.forEach { community ->
            val provenance = ProvanceDto(modifiedDate = community.updatedAt.convertToUtcDateTime())
            val json =  JsonParser.parseString(community.payload).asJsonObject
            community.fhirId?.let {
                json.addProperty(DefinedParams.ID, it)
            }
            json.addProperty(Description, community.communityDescription)
            json.addProperty(COMMUNITY_REGISTERED_DATE, community.registeredDate)
            json.addProperty(VillageId, community.villageId)
            json.addProperty(ReferenceId, community.id.toString())
            json.add(Provenance,  Gson().toJsonTree(provenance))
            communityProfilesRequests.add(json)
            communityProfileIds.add(community.villageId)
        }


        // Nothing to Post anything
        if (houseHoldList.isEmpty()
            && otherMembers.isEmpty()
            && otherAssessments.isEmpty()
            && allFollowUps.isEmpty()
            && householdMemberLink.isEmpty()
            && communityProfilesRequests.isEmpty()
            && rxBuddies.isEmpty()
        ) {
            return listOf()
        }

        val request = OfflineUtils.getRequestObject()
        request[OfflineConstant.SYNC_MODE] = syncMode
        request[OfflineConstant.HOUSE_HOLDS] = houseHoldList
        request[OfflineConstant.HOUSE_HOLD_MEMBERS] = otherMembers
        request[OfflineConstant.ASSESSMENTS] = otherAssessments
        request[OfflineConstant.FOLLOWUPS] = allFollowUps
        request[OfflineConstant.HOUSEHOLD_MEMBER_LINK] = householdMemberLink
        request[OfflineConstant.COMMUNITY_PROFILES] = communityProfilesRequests
        request[OfflineConstant.RX_BUDDIES] = rxBuddies

        try {
            val apiResponse = apiHelper.postOfflineSync(request)
            if (apiResponse.isSuccessful) {
                roomHelper.changeHouseholdStatus(householdIds, OfflineSyncStatus.InProgress.name) // Change Status to InProgress
                roomHelper.changeHouseholdMemberStatus(householdMemberIds, OfflineSyncStatus.InProgress.name) // Change Status to InProgress
                roomHelper.changeAssessmentStatus(assessmentIds, OfflineSyncStatus.InProgress.name) // Change status to InProgress
                roomHelper.changeFollowUpStatus(followUpIds, OfflineSyncStatus.InProgress.name) // Change status to InProgress
                roomHelper.changeFollowUpCallStatus(followUpCallIds) // Change isSynced Status to True
                roomHelper.changeAssignHHMStatus(assignedMemberIds, OfflineSyncStatus.InProgress.name)
                roomHelper.changeHHMLinkCallStatus(householdLinkCallsMemberIds, OfflineSyncStatus.InProgress.name)
                roomHelper.changeCommunityProfileStatus(communityProfileIds, OfflineSyncStatus.InProgress.name)
                roomHelper.updateRxBuddyRegisterSyncStatus(rxBuddyRegisterIds, OfflineSyncStatus.InProgress.name)
                roomHelper.updateRxBuddyFollowUpSyncStatus(rxBuddyFollowUpIds, OfflineSyncStatus.InProgress.name)
                return listOf(request[OfflineConstant.REQUEST_ID] as String)
            }
        } catch (e: Exception) {
            roomHelper.changeHouseholdStatus(householdIds, OfflineSyncStatus.NetworkError.name) // Change Status to InProgress
            roomHelper.changeHouseholdMemberStatus(householdMemberIds, OfflineSyncStatus.NetworkError.name) // Change Status to InProgress
            roomHelper.changeAssessmentStatus(assessmentIds, OfflineSyncStatus.NetworkError.name) // Change status to InProgress
            roomHelper.changeFollowUpStatus(followUpIds, OfflineSyncStatus.NetworkError.name) // Change status to InProgress
            roomHelper.changeAssignHHMStatus(assignedMemberIds, OfflineSyncStatus.NetworkError.name)
            roomHelper.changeHHMLinkCallStatus(householdLinkCallsMemberIds, OfflineSyncStatus.NetworkError.name)
            roomHelper.changeCommunityProfileStatus(communityProfileIds, OfflineSyncStatus.NetworkError.name)
            roomHelper.updateRxBuddyRegisterSyncStatus(rxBuddyRegisterIds, OfflineSyncStatus.NetworkError.name)
            roomHelper.updateRxBuddyFollowUpSyncStatus(rxBuddyFollowUpIds, OfflineSyncStatus.NetworkError.name)
            return listOf(request[OfflineConstant.REQUEST_ID] as String)
        }

        return listOf(request[OfflineConstant.REQUEST_ID] as String)
    }

    suspend fun getSyncStatusForOffline(id: String): Boolean {
        val req = RequestGetSyncStatus(requestId = id)
        try {
            // Get Sync Status
            val response = getSyncStatus(req)
            if (response.isSuccessful) {
                var isAllEntitiesSynced = true
                response.body()?.entityList?.forEach { entity ->
                    when(entity.status) {
                        OfflineSyncStatus.Success.name -> {
                            if (entity.referenceId != null && (entity.type == COMMUNITY_PROFILE || entity.type != null && entity.fhirId != null)) {
                                updateFhirId(
                                    entity.type,
                                    entity.referenceId,
                                    entity.fhirId,
                                    OfflineSyncStatus.Success.name
                                )
                            }
                        }

                        OfflineSyncStatus.Failed.name -> {
                            if (entity.type != null && entity.referenceId != null) {
                                updateFhirId(
                                    entity.type,
                                    entity.referenceId,
                                    entity.fhirId,
                                    OfflineSyncStatus.Failed.name
                                )
                            }
                        }

                        OfflineSyncStatus.InProgress.name -> {
                            isAllEntitiesSynced = false
                        }
                    }
                }

                return isAllEntitiesSynced
            } else {
                return false
            }
        } catch (e: Exception) {
            return false
        }
    }


    suspend fun postOfflineUnSyncedChangesWithMutex(syncMode: String): List<String>? {
        mutex.withLock {
            return postOfflineUnSyncedChanges(syncMode)
        }
    }
}