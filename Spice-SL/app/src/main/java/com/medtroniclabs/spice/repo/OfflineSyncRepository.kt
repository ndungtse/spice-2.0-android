package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.appextensions.convertToUtcDateTime
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.model.Assessment
import com.medtroniclabs.spice.data.offlinesync.model.AssessmentEncounter
import com.medtroniclabs.spice.data.offlinesync.model.FollowUpCriteria
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.HouseHoldMember
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.data.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.data.offlinesync.model.ResponseInitialDownload
import com.medtroniclabs.spice.data.offlinesync.model.SyncEntityList
import com.medtroniclabs.spice.data.offlinesync.model.SyncResponse
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineUtils
import com.medtroniclabs.spice.data.resource.RequestAllEntities
import com.medtroniclabs.spice.db.entity.EntitiesName
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.assessment.AssessmentDetails
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.lang.reflect.Type
import javax.inject.Inject

class OfflineSyncRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    private suspend fun getUnSyncedAssessmentByPatientId(patientId: String): List<Assessment> {
        return convertEntityToRequest(roomHelper.getUnSyncedAssessmentByPatientId(patientId))
    }

    private suspend fun getOtherUnSyncedAssessments(): List<Assessment> {
        return convertEntityToRequest(roomHelper.getOtherUnSyncedAssessments())
    }

    private fun convertEntityToRequest(list: List<AssessmentDetails>): List<Assessment> {
        return list.map { entity ->
            Assessment(
                referenceId = entity.id,
                villageId = entity.villageId,
                assessmentType = entity.assessmentType,
                assessmentDetails = JsonParser.parseString(entity.assessmentDetails),
                patientStatus = entity.referralStatus,
                referredReasons = entity.referredReason?.joinToString(", "),
                summary = entity.otherDetails?.let { JsonParser.parseString(it) },
                encounter = AssessmentEncounter(
                    householdId = entity.householdId,
                    memberId = entity.memberId,
                    referred = entity.isReferred,
                    patientId = entity.patientId,
                    provenance = ProvanceDto(modifiedDate = entity.createdAt.convertToUtcDateTime()),
                    latitude = entity.latitude,
                    longitude = entity.longitude,
                    visitNumber = getVisitCount(entity)
                ),
                followUpId = entity.followUpId,
                updatedAt = entity.createdAt
            )
        }
    }

    private fun getVisitCount(assessmentDetail: AssessmentDetails): Long? {
        when(assessmentDetail.assessmentType.lowercase()) {
            RMNCH.ANC_MENU.lowercase() -> return assessmentDetail.ancVisitNo
            RMNCH.pnc_mother_key.lowercase() -> return assessmentDetail.pncVisitNo
            RMNCH.CHILD_MENU.lowercase() -> return assessmentDetail.childVisitNo
            else -> return null
        }
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

            val villageIds = roomHelper.getAllVillageIds()
            // Fetch Synced Data
            val isInitialDataSuccess = fetchSyncedData(villageIds, null)

            // Need to check this to be added for downloading error and inprogress data
            /* if (!fetchUnSyncedData()) {
                 liveData.postError("Something went wrong")
             }*/

            if (isInitialDataSuccess) {
                SecuredPreference.putBoolean(
                    SecuredPreference.EnvironmentKey.ISLOGGEDIN.name,
                    true
                )
                SecuredPreference.putBoolean(
                    SecuredPreference.EnvironmentKey.ISMETALOADED.name,
                    true
                )
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

        // Insert follow up
        roomHelper.deleteAllFollowUps()
        roomHelper.deleteAllFollowUpCalls()
        val initialSyncStatus = OfflineSyncStatus.Success
        val initialSyncTime = System.currentTimeMillis()
        requestInitialDownload.followUps?.forEach {
            it.syncStatus = initialSyncStatus
            it.updatedAt = it.calledAt ?: 0
            roomHelper.insertFollowUp(it)
        }

        // Insert Pregnancy Information
        requestInitialDownload.pregnancyInfos?.forEach {
            roomHelper.insertUpdatePregnancyDetailFromBE(it)
        }

        // Set FollowUpCriteria in Preference
        requestInitialDownload.followUpCriteria?.let {
            SecuredPreference.putFollowUpCriteria(it)
        } ?: kotlin.run {
            val followUpCriteria = FollowUpCriteria(3, 5, 3, 7, 7, 2, 2, 2, 2, 5, 5)
            SecuredPreference.putFollowUpCriteria(followUpCriteria)
        }

        SecuredPreference.putString(SecuredPreference.EnvironmentKey.SERVER_LAST_SYNCED.name,
            requestInitialDownload.lastSyncTime)
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

    /*
    * It will post all un-synced changes from local database and returns List<String>?
    * 1. list size > 0 -> Posted un-synced local changes and API is success
    * 2. List size == 0 -> There are no local changes to post
    * 3. List is null -> Post un-synced local changes and API is failed
    * */
    suspend fun postOfflineUnSyncedChanges(): List<String>? {
        val householdIds = mutableListOf<String>()
        val householdMemberIds = mutableListOf<String>()
        val assessmentIds = mutableListOf<String>()

        val houseHoldList = roomHelper.getAllUnSyncedHouseHolds()
        householdIds.addAll(houseHoldList.map { it.referenceId!! })
        houseHoldList.forEach { householdEntity ->
            val memberList =
                roomHelper.getAllUnSyncedHouseHoldMembers((householdEntity.referenceId!!.toLong()))
            householdMemberIds.addAll(memberList.map { it.referenceId!! })

            //Assessment
            memberList.forEach { hhm ->
                hhm.motherPatientId?.let { hhm.isChild = true }
                hhm.assessments = getUnSyncedAssessmentByPatientId(hhm.patientId)
                assessmentIds.addAll(hhm.assessments.map { it.referenceId.toString() })
            }

            householdEntity.householdMembers.addAll(memberList)
        }

        val otherHouseholdMembers = roomHelper.getOtherHouseholdMembers(householdMemberIds)
        //Assessment
        otherHouseholdMembers.forEach { hhm ->
            householdMemberIds.add(hhm.referenceId!!)
            hhm.motherPatientId?.let { hhm.isChild = true }
            hhm.assessments = getUnSyncedAssessmentByPatientId(hhm.patientId)
            assessmentIds.addAll(hhm.assessments.map { it.referenceId.toString() })
        }

        val otherAssessments = getOtherUnSyncedAssessments()
        assessmentIds.addAll(otherAssessments.map { it.referenceId.toString() })

        //Followup
        val allFollowUps = roomHelper.getAllFollowUpRequests()
        allFollowUps.forEach { followUp ->
            followUp.id?.let {
                followUp.followUpDetails = roomHelper.getAllFollowUpCalls(it)
            }
        }

        val request = OfflineUtils.getRequestObject()
        request[OfflineConstant.HOUSE_HOLDS] = houseHoldList
        request[OfflineConstant.HOUSE_HOLD_MEMBERS] = otherHouseholdMembers
        request[OfflineConstant.ASSESSMENTS] = otherAssessments
        request[OfflineConstant.FOLLOWUPS] = allFollowUps

        // Nothing to Post anything
        if (houseHoldList.isEmpty()
            && otherHouseholdMembers.isEmpty()
            && otherAssessments.isEmpty()
            && allFollowUps.isEmpty()
        ) {
            return listOf()
        }

        try {
            val apiResponse = apiHelper.postOfflineSync(request)
            if (apiResponse.isSuccessful) {
                roomHelper.changeHouseholdStatus(householdIds) // Change Status to InProgress
                roomHelper.changeHouseholdMemberStatus(householdMemberIds) // Change Status to InProgress
                roomHelper.changeAssessmentStatus(assessmentIds) // Change status to InProgress
                return listOf(request[OfflineConstant.REQUEST_ID] as String)
            }
        } catch (e: Exception) {
            return null
        }

        return null
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
                            if (entity.type != null && entity.referenceId != null && entity.fhirId != null) {
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
}