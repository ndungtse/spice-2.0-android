package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.medtroniclabs.spice.appextensions.convertToString
import com.medtroniclabs.spice.appextensions.convertToUtcDateTime
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.offlinesync.model.Assessment
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.HouseHoldMember
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.data.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.data.offlinesync.model.ResponseInitialDownload
import com.medtroniclabs.spice.data.offlinesync.model.SyncEntityList
import com.medtroniclabs.spice.data.offlinesync.model.SyncResponse
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.data.resource.RequestAllEntities
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.EntitiesName
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import retrofit2.Response
import javax.inject.Inject

class OfflineSyncRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    suspend fun getAllUnSyncedHouseHolds(): List<HouseHold> {
        return roomHelper.getAllUnSyncedHouseHolds()
    }

    suspend fun getAllUnSyncedMembers(householdId: Long): List<HouseHoldMember> {
        return roomHelper.getAllUnSyncedHouseHoldMembers(householdId)
    }

    suspend fun getOtherHouseholdMembers(ids: List<Long>): List<HouseHoldMember> {
        return roomHelper.getOtherHouseholdMembers(ids)
    }

    suspend fun postOfflineHouseHolds(map: Map<String,Any>): Response<SyncResponse> {
        return apiHelper.postOfflineSync(map)
    }

    suspend fun getUnSyncedAssessmentByPatientId(patientId: String): List<Assessment> {
        return convertEntityToRequest(roomHelper.getUnSyncedAssessmentByPatientId(patientId))
    }

    suspend fun getOtherUnSyncedAssessments(patientIds: List<String>): List<Assessment> {
        return convertEntityToRequest(roomHelper.getOtherUnSyncedAssessments(patientIds))
    }

    private fun convertEntityToRequest(list: List<AssessmentEntity>): List<Assessment> {
        return list.map { entity ->
            Assessment(
                referenceId = entity.id,
                householdId = entity.householdId,
                memberId = entity.memberId,
                villageId = entity.villageId,
                assessmentType = entity.assessmentType,
                assessmentDetails = JsonParser.parseString(entity.assessmentDetails),
                patientId = entity.patientId,
                startTime = null,
                endTime = null,
                referred = entity.isReferred,
                referredDate = if (entity.isReferred) entity.createdAt.convertToString() else null,
                patientStatus = entity.referralStatus.name,
                referredReasons = entity.referredReason?.joinToString(", "),
                provenance = ProvanceDto(createdDateTime = entity.createdAt.convertToUtcDateTime()),
                latitude = entity.latitude,
                longitude = entity.longitude,
                summary = JsonParser.parseString(entity.otherDetails)
            )
        }
    }

    suspend fun getHouseholdAndMembers(liveData: MutableLiveData<Resource<Boolean>>) {
        liveData.postLoading()
        val villageNameId = mutableMapOf<String, Long>()
        roomHelper.getAllVillageEntity().forEach {
            villageNameId[it.name] = it.id
        }

        try {
            val syncedResponse = getSyncedEntities(villageNameId.values.toList())
            val unSyncedResponse = getUnSyncedEntities()
            if (syncedResponse.isSuccessful && unSyncedResponse.isSuccessful) {
                roomHelper.deleteAllHouseholds()
                roomHelper.deleteAllHouseholdMembers()
                // Insert Synced Entities
                val hhMapping = insertHouseholds(syncedResponse.body()?.entity?.households, villageNameId)
                insertHouseholdMembers(syncedResponse.body()?.entity?.members, hhMapping)


                // Insert UnSynced Entities
                val householdList =
                    unSyncedResponse.body()?.entityList?.filter { it.type == EntitiesName.HOUSEHOLD }
                val hhMap = insertFailedHouseholds(householdList, villageNameId)

                val householdMemberList =
                    unSyncedResponse.body()?.entityList?.filter { it.type == EntitiesName.HOUSEHOLD_MEMBER }
                insertFailedHouseholdMembers(householdMemberList, hhMap)

                liveData.postSuccess(true)
            } else {
                liveData.postError("Something went wrong")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postError(e.message)
        }
    }

    private suspend fun insertHouseholds(
        households: List<HouseHold>?,
        villageNameId: Map<String, Long>
    ): Map<String, Long> {
        // fhir id, local id
        val hhMap = mutableMapOf<String, Long>()

        households?.forEach { entity ->
            hhMap[entity.id!!] = roomHelper.saveHouseHoldEntry(
                entity.toHouseholdEntity(
                    villageNameId,
                    OfflineSyncStatus.Success
                )
            )
        }

        return hhMap
    }

    private suspend fun insertHouseholdMembers(householdMembers: List<HouseHoldMember>?, hhIdMap: Map<String, Long>) {
        householdMembers?.forEach { member ->
            hhIdMap[member.householdId]?.let {
                roomHelper.registerMember(member.toHouseholdMemberEntity(it, OfflineSyncStatus.Success))
            }
        }
    }

    private suspend fun insertFailedHouseholds(households: List<SyncEntityList>?, villageNameId : Map<String,Long>): Map<String, HouseHold> {
        // Response apiReferenceId, Household
        val hhMap = mutableMapOf<String, HouseHold>()
        households?.forEach { entity ->
            Gson().fromJson(entity.data, HouseHold::class.java)?.let { houseHold ->
                val apiRefId = houseHold.referenceId
                var dbHHId: Long?
                if (houseHold.id != null) { // Fhir id is not null - Success
                    dbHHId = roomHelper.getHouseholdIdByFhirId(houseHold.id)
                    if (dbHHId != null) { // Update Flow
                        roomHelper.updateHousehold(houseHold.toHouseholdEntity(villageNameId, OfflineSyncStatus.Success, dbHHId))
                    } else { // Insert Flow
                        dbHHId = roomHelper.saveHouseHoldEntry(houseHold.toHouseholdEntity(villageNameId, OfflineSyncStatus.Success))
                    }
                } else { // Fhir id is null - Failed
                    dbHHId = roomHelper.saveHouseHoldEntry(houseHold.toHouseholdEntity(villageNameId, OfflineSyncStatus.Failed))
                }

                houseHold.referenceId = dbHHId.toString()
                hhMap[apiRefId!!] = houseHold
            }
        }
        return hhMap
    }

    private suspend fun insertFailedHouseholdMembers(householdMemberList: List<SyncEntityList>?, hhMap: Map<String, HouseHold>) {
        householdMemberList?.forEach { entity ->
            Gson().fromJson(entity.data, HouseHoldMember::class.java)?.let { member ->
                val dbHHId = roomHelper.getHouseholdIdByFhirId(member.householdId) ?: hhMap[member.householdReferenceId]?.referenceId?.toLong()
                if (dbHHId != null) { // HouseholdId found in local
                    if (member.id != null) { //  Fhir id is not null - Success
                        val dbHHMId = roomHelper.getHouseholdMemberIdByFhirId(member.id)
                        if (dbHHMId != null) { // Update Flow
                            roomHelper.registerMember(member.toHouseholdMemberEntity(dbHHId, OfflineSyncStatus.Success, dbHHMId))
                        } else { // Insert Flow
                            roomHelper.registerMember(member.toHouseholdMemberEntity(dbHHId, OfflineSyncStatus.Success))
                        }
                    } else { // Fhir id is null - Failed
                        roomHelper.registerMember(member.toHouseholdMemberEntity(dbHHId, OfflineSyncStatus.Failed))
                    }
                }
            }
        }
    }

    private suspend fun getSyncedEntities(villageList: List<Long>): Response<APIResponse<ResponseInitialDownload>> {
        // Getting village name only. For mapping I have used following code
        val request = RequestAllEntities(villageList)
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
}