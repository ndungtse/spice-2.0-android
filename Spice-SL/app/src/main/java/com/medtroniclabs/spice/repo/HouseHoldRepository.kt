package com.medtroniclabs.spice.repo

import androidx.lifecycle.LiveData
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.ConsentFormType
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.data.model.HouseholdCardDetail
import com.medtroniclabs.spice.data.offlinesync.model.HouseHoldMember
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdMemberWithTb
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.entity.ConsentForm
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.db.response.HouseholdMemberCount
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration
import com.medtroniclabs.spice.model.household.HouseHoldFilterUiData
import com.medtroniclabs.spice.model.medicalreview.AddMemberRegRequest
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class HouseHoldRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    suspend fun getLastHouseholdNo(villageId: Long): Long? =
        roomHelper.getLastHouseholdNo(villageId)

    suspend fun checkHouseholdNumberExists(householdNo: Long): Boolean =
        roomHelper.checkHouseholdNumberExists(householdNo)

    suspend fun generateUniqueHouseholdNumber(): Long {
        var householdNumber: Long
        var attempts = 0
        val maxAttempts = 100
        
        do {
            // Generate random 10-digit number (1000000000 to 9999999999)
            householdNumber = (1000000000L..9999999999L).random()
            attempts++
        } while (checkHouseholdNumberExists(householdNumber) && attempts < maxAttempts)
        
        if (attempts >= maxAttempts) {
            // Fallback: use timestamp-based number if too many collisions
            householdNumber = System.currentTimeMillis() % 10000000000L
            if (householdNumber < 1000000000L) {
                householdNumber += 1000000000L
            }
        }
        
        return householdNumber
    }

    suspend fun getHouseHoldDetailsById(houseHoldId: Long) =
        roomHelper.getHouseHoldDetailsById(houseHoldId)

    suspend fun getAllHouseHoldMemberList(houseHoldId: Long): ArrayList<HouseholdMemberEntity> =
        roomHelper.getAllHouseHoldMemberList(houseHoldId)

    fun getMemberCountInHouseholdLiveData(houseHoldId: Long): LiveData<HouseholdMemberCount> {
        return roomHelper.getMemberCountInHouseholdLiveData(houseHoldId)
    }

    fun getFilteredHouseholdsLiveData(
        searchTerm: String,
        villageIds: List<Long>,
        ssIds: List<Long>,
        status: String
    ): LiveData<List<HouseHoldEntityWithMemberCount>> {
        return roomHelper.getFilteredHouseholdsLiveData(searchTerm, villageIds, ssIds, status)
    }

    suspend fun getFormData(
        formType: String,
    ): Resource<String> {
        return try {
            val response = roomHelper.getFormData(formType)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }


    suspend fun getHouseHoldFilterUiData(userId:Long): Resource<HouseHoldFilterUiData> {
        return try {
            val villages = roomHelper.getAllVillageEntity()
            val swasthyaSevikas = roomHelper.getShasthyaShebikaByShasthyaKormiId(userId)
            Resource(state = ResourceState.SUCCESS, HouseHoldFilterUiData(villages,swasthyaSevikas))
        }catch (_: Exception){
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun createOrUpdateHouseHoldEntity(map: HashMap<String, Any>, entity: HouseholdEntity? = null): HouseholdEntity {
        val householdEntity = entity ?: HouseholdEntity()

        val householdName = map[HouseHoldRegistration.householdName]
        var nameFromMap = CommonUtils.getStringOrEmptyString(householdName)
        // If updating and household name is not provided or empty, get it from household head member
        if (entity != null && nameFromMap.isEmpty()) {
            val householdHeadMember = getAllHouseHoldMemberList(entity.id)
                .firstOrNull { it.isHouseholdHead && it.isActive }
            householdHeadMember?.let {
                nameFromMap = it.name
            }
        }
        householdEntity.name = nameFromMap

        val villageID = map[HouseHoldRegistration.villageId]
        val villageLongID = CommonUtils.getLongOrNull(villageID) ?: 0
        householdEntity.villageId = villageLongID

        val shasthyaShebikaId = map[HouseHoldRegistration.shasthyaShebikaId]
        householdEntity.shasthyaShebikaId = CommonUtils.getLongOrNull(shasthyaShebikaId)

        val subVillageId = map[HouseHoldRegistration.subVillageId]
        householdEntity.subVillageId = CommonUtils.getLongOrNull(subVillageId)

        val householdType = map[HouseHoldRegistration.householdType]
        householdEntity.householdType = CommonUtils.getStringOrEmptyString(householdType).takeIf { it.isNotEmpty() }

        val monthlyIncome = map[HouseHoldRegistration.monthlyIncome]
        householdEntity.monthlyIncome = CommonUtils.getDoubleOrNull(monthlyIncome)

        if (entity != null) {
            householdEntity.updatedAt = System.currentTimeMillis()
            householdEntity.sync_status = OfflineSyncStatus.NotSynced

            val noOfPeople = map[HouseHoldRegistration.noOfPeople] ?: map[HouseHoldRegistration.totalMembers]
            householdEntity.noOfPeople = checkHeadCountOfHouseHold(CommonUtils.getIntegerOrNull(noOfPeople) ?: 0, getMemberCountPerHouseHold(entity.id))
        } else {
            // Use household number from form if provided, otherwise generate new one
            val householdNumberFromForm = map[HouseHoldRegistration.householdNumber]
            householdEntity.householdNo = if (householdNumberFromForm != null) {
                CommonUtils.getLongOrNull(householdNumberFromForm)
            } else {
                // Fallback: generate if not provided (shouldn't happen if form is populated correctly)
                generateUniqueHouseholdNumber()
            }
            val noOfPeople = map[HouseHoldRegistration.noOfPeople] ?: map[HouseHoldRegistration.totalMembers]
            householdEntity.noOfPeople = CommonUtils.getIntegerOrNull(noOfPeople) ?: 0
        }
        return householdEntity
    }

    suspend fun insertHouseHoldEntity(householdEntity: HouseholdEntity): Long {
        return roomHelper.saveHouseHoldEntry(householdEntity)
    }

    suspend fun getShasthyaShebikasByKormiId(shasthyaKormiId: Long): Resource<LocalSpinnerResponse> {
        return try {
            val response = roomHelper.getShasthyaShebikaByShasthyaKormiId(shasthyaKormiId)
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse("shasthya_shebika_id", response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getSubVillagesByShasthyaShebikaId(shasthyaShebikaId: Long): Resource<LocalSpinnerResponse> {
        return try {
            val response = roomHelper.getSubVillagesByShasthyaShebikaId(shasthyaShebikaId)
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse("sub_village_id", response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun updateHouseHoldEntity(householdEntity: HouseholdEntity) {
        roomHelper.updateHousehold(householdEntity)
    }

    private fun checkHeadCountOfHouseHold(
        givenHeadCount: Int,
        memberCount: Int
    ): Int {
        return if (memberCount > givenHeadCount) {
            memberCount
        } else {
            givenHeadCount
        }
    }

    suspend fun getUserVillages(
        tag: String
    ): Resource<LocalSpinnerResponse> {
        return try {
            val response = roomHelper.getAllVillageEntity()
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse(tag, response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getUserLinkedVillages(
        tag: String
    ): Resource<LocalSpinnerResponse> {
        return try {
            val response = roomHelper.getAllLinkedVillageEntity()
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse(tag, response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getVillageByID(villageId: Long): Resource<VillageEntity> {
        val response = roomHelper.getVillageByID(villageId)
        return Resource(state = ResourceState.SUCCESS, data = response)
    }

    suspend fun getMemberCountPerHouseHold(householdId: Long): Int {
        return roomHelper.getMemberCountPerHouseHold(householdId)
    }

    private suspend fun insertHouseholdMembers(householdMembers: List<HouseHoldMember>?, hhIdMap: Map<String, Long>) {
        householdMembers?.forEach { member ->
            hhIdMap[member.householdId]?.let {
                roomHelper.registerMember(
                    member.toHouseholdMemberEntity(
                        it,
                        OfflineSyncStatus.Success
                    )
                )
            }
        }
    }

    suspend fun getUnSyncedHouseholdCount(): Int {
        return roomHelper.getUnSyncedHouseholdCount()
    }

    suspend fun getUnSyncedHouseholdMemberCount(): Int {
        return roomHelper.getUnSyncedHouseholdMemberCount()
    }

    fun getHouseholdCardDetailLiveData(id: Long): LiveData<HouseholdCardDetail> {
        return roomHelper.getHouseholdCardDetailLiveData(id)
    }

    fun getAllHouseHoldMembersLiveData(hhId: Long) : LiveData<List<HouseholdMemberWithTb>> {
        return roomHelper.getAllHouseHoldMembersLiveData(hhId)
    }

    fun getAliveHouseHoldMembersLiveData(hhId: Long) : List<HouseholdMemberEntity> {
        return roomHelper.getAliveHouseHoldMembersLiveData(hhId)
    }

    suspend fun addNewMember(request: AddMemberRegRequest): Resource<String> {
        return try{
            val response = apiHelper.addNewMember(request)
            if (response.isSuccessful) {
                Resource(ResourceState.SUCCESS, response.body()?.entity)
            } else {
                val errorMessage = StringConverter.getErrorMessage(response.errorBody())
                Resource(state = ResourceState.ERROR, message = errorMessage)
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR, message = e.localizedMessage)
        }
    }

    suspend fun getConsentForm() : ConsentForm? {
        return roomHelper.getConsentFormByType(ConsentFormType.Household)
    }

    fun getAllHouseHoldMembersWithTbStatusLiveData(hhvId: Long) : LiveData<List<HouseholdMemberEntity>> {
        return roomHelper.householdMemberWithTbStatus(hhvId)
    }

    suspend fun updateHouseholdMemberTbContactTraceStatus(hhmId: Long,tbContactTracingStatus:Int) {
        return roomHelper.updateTBContactTraceStatus(hhmId,tbContactTracingStatus)
    }
}