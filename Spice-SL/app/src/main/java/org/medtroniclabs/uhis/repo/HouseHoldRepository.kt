package org.medtroniclabs.uhis.repo

import androidx.lifecycle.LiveData
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.ConsentFormType
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.common.StringConverter
import org.medtroniclabs.uhis.data.LocalSpinnerResponse
import org.medtroniclabs.uhis.data.offlinesync.model.HouseHoldMember
import org.medtroniclabs.uhis.data.offlinesync.model.HouseholdMemberWithTb
import org.medtroniclabs.uhis.data.offlinesync.utils.OfflineSyncStatus
import org.medtroniclabs.uhis.db.dao.HouseholdSortOrder
import org.medtroniclabs.uhis.db.entity.ConsentForm
import org.medtroniclabs.uhis.db.entity.HouseholdEntity
import org.medtroniclabs.uhis.db.entity.HouseholdMemberEntity
import org.medtroniclabs.uhis.db.entity.SubVillageEntity
import org.medtroniclabs.uhis.db.entity.VillageEntity
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.db.response.HouseHoldEntityWithLastActivity
import org.medtroniclabs.uhis.db.response.HouseholdMemberCount
import org.medtroniclabs.uhis.mappingkey.HouseHoldRegistration
import org.medtroniclabs.uhis.model.household.HouseHoldFilterUiData
import org.medtroniclabs.uhis.model.medicalreview.AddMemberRegRequest
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import javax.inject.Inject

class HouseHoldRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper,
) {
    suspend fun getLastHouseholdNo(villageId: Long): Long? = roomHelper.getLastHouseholdNo(villageId)

    suspend fun checkHouseholdNumberExists(householdNo: Long): Boolean = roomHelper.checkHouseholdNumberExists(householdNo)

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

    suspend fun getHouseHoldDetailsById(houseHoldId: Long) = roomHelper.getHouseHoldDetailsById(houseHoldId)

    suspend fun getAllHouseHoldMemberList(houseHoldId: Long): ArrayList<HouseholdMemberEntity> = roomHelper.getAllHouseHoldMemberList(houseHoldId)

    fun getMemberCountInHouseholdLiveData(houseHoldId: Long): LiveData<HouseholdMemberCount> = roomHelper.getMemberCountInHouseholdLiveData(houseHoldId)

    fun getFilteredHouseholdsLiveData(
        searchTerm: String,
        ssIds: List<Long>,
        subVillageIds: List<Long> = emptyList(),
        sortOrder: HouseholdSortOrder = HouseholdSortOrder.DEFAULT,
    ): LiveData<List<HouseHoldEntityWithLastActivity>> =
        roomHelper.getFilteredHouseholdsLiveData(
            searchInput = searchTerm,
            filterBySs = ssIds,
            filterBySubVillages = subVillageIds,
            sortOrder = sortOrder,
        )

    suspend fun getFormData(formType: String): Resource<String> =
        try {
            val response = roomHelper.getFormData(formType)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getHouseHoldFilterUiData(userId: Long): Resource<HouseHoldFilterUiData> =
        try {
            val swasthyaSevikas = roomHelper.getShasthyaShebikaByShasthyaKormiId(userId)
            val subVillages = mutableListOf<SubVillageEntity>()
            if (swasthyaSevikas.isNotEmpty()) {
                subVillages.addAll(roomHelper.getSubVillagesByShasthyaShebikaIds(swasthyaSevikas.map { it.id }))
            }
            Resource(state = ResourceState.SUCCESS, HouseHoldFilterUiData(swasthyaSevikas, subVillages))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun createOrUpdateHouseHoldEntity(
        map: HashMap<String, Any>,
        entity: HouseholdEntity? = null,
    ): HouseholdEntity {
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

        val occupation = map[HouseHoldRegistration.householdHeadOccupation]
        householdEntity.householdHeadOccupation = CommonUtils.getStringOrEmptyString(occupation).takeIf { it.isNotEmpty() }

        val otherOccupation = map[HouseHoldRegistration.otherOccupation]
        householdEntity.otherOccupation = CommonUtils.getStringOrEmptyString(otherOccupation).takeIf { it.isNotEmpty() }

        if (entity != null) {
            householdEntity.updatedAt = System.currentTimeMillis()
            householdEntity.sync_status = OfflineSyncStatus.NotSynced

            val noOfPeople = map[HouseHoldRegistration.noOfPeople] ?: map[HouseHoldRegistration.totalMembers]
            householdEntity.noOfPeople = checkHeadCountOfHouseHold(CommonUtils.getIntegerOrNull(noOfPeople) ?: 0, getMemberCountPerHouseHold(entity.id))

            val disabilityPersonsCount = map[HouseHoldRegistration.ID_DISABILITY_PERSONS_COUNT]
            householdEntity.disabilityPersonsCount =
                checkHeadCountOfHouseHold(CommonUtils.getIntegerOrNull(disabilityPersonsCount) ?: 0, getDisabilityMembersCountPerHousehold(entity.id))
        } else {
            // Use household number from form if provided, otherwise generate new one
            val householdNumberFromForm = map[HouseHoldRegistration.householdNumber]
            householdEntity.householdNo = householdNumberFromForm as? String
                ?: // Fallback: generate if not provided (shouldn't happen if form is populated correctly)
                "HH${System.currentTimeMillis()}"
            val noOfPeople = map[HouseHoldRegistration.noOfPeople] ?: map[HouseHoldRegistration.totalMembers]
            householdEntity.noOfPeople = CommonUtils.getIntegerOrNull(noOfPeople) ?: 0

            val disabilityPersonsCount = map[HouseHoldRegistration.ID_DISABILITY_PERSONS_COUNT]
            householdEntity.disabilityPersonsCount = CommonUtils.getIntegerOrNull(disabilityPersonsCount) ?: 0
        }
        return householdEntity
    }

    suspend fun insertHouseHoldEntity(householdEntity: HouseholdEntity): Long = roomHelper.saveHouseHoldEntry(householdEntity)

    suspend fun getShasthyaShebikasByKormiId(shasthyaKormiId: Long): Resource<LocalSpinnerResponse> =
        try {
            val response = roomHelper.getShasthyaShebikaByShasthyaKormiId(shasthyaKormiId)
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse("shasthya_shebika_id", response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getSubVillagesByShasthyaShebikaId(shasthyaShebikaId: Long): Resource<LocalSpinnerResponse> =
        try {
            val response = roomHelper.getSubVillagesByShasthyaShebikaId(shasthyaShebikaId)
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse("sub_village_id", response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun updateHouseHoldEntity(householdEntity: HouseholdEntity) {
        roomHelper.updateHousehold(householdEntity)
    }

    private fun checkHeadCountOfHouseHold(
        givenHeadCount: Int,
        memberCount: Int,
    ): Int =
        if (memberCount > givenHeadCount) {
            memberCount
        } else {
            givenHeadCount
        }

    suspend fun getUserVillages(tag: String): Resource<LocalSpinnerResponse> =
        try {
            val response = roomHelper.getAllVillageEntity()
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse(tag, response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getUserLinkedVillages(tag: String): Resource<LocalSpinnerResponse> =
        try {
            val response = roomHelper.getAllLinkedVillageEntity()
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse(tag, response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getGuardianMembers(
        tag: String,
        hhId: Long,
        hhmId: Long,
    ): Resource<LocalSpinnerResponse> =
        try {
            val response = roomHelper.getOtherHouseholdExcludeTBPatient(hhId, hhmId)
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse(tag, response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getVillageByID(villageId: Long): Resource<VillageEntity> {
        val response = roomHelper.getVillageByID(villageId)
        return Resource(state = ResourceState.SUCCESS, data = response)
    }

    suspend fun getMemberCountPerHouseHold(householdId: Long): Int = roomHelper.getMemberCountPerHouseHold(householdId)

    suspend fun getDisabilityMembersCountPerHousehold(householdId: Long): Int = roomHelper.getDisabilityMembersCountForHousehold(householdId)

    private suspend fun insertHouseholdMembers(
        householdMembers: List<HouseHoldMember>?,
        hhIdMap: Map<String, Long>,
    ) {
        householdMembers?.forEach { member ->
            hhIdMap[member.householdId]?.let {
                roomHelper.registerMember(
                    member.toHouseholdMemberEntity(
                        it,
                        OfflineSyncStatus.Success,
                    ),
                )
            }
        }
    }

    suspend fun getUnSyncedHouseholdCount(): Int = roomHelper.getUnSyncedHouseholdCount()

    suspend fun getUnSyncedHouseholdMemberCount(): Int = roomHelper.getUnSyncedHouseholdMemberCount()

    fun getHouseholdCardDetailLiveData(id: Long): LiveData<List<HouseHoldEntityWithLastActivity>> =
        roomHelper.getFilteredHouseholdsLiveData(
            searchInput = "",
            filterByHhIds = listOf(id),
        )

    fun getAllHouseHoldMembersLiveData(hhId: Long): LiveData<List<HouseholdMemberWithTb>> = roomHelper.getAllHouseHoldMembersLiveData(hhId)

    fun getAliveHouseHoldMembersLiveData(hhId: Long): List<HouseholdMemberEntity> = roomHelper.getAliveHouseHoldMembersLiveData(hhId)

    suspend fun addNewMember(request: AddMemberRegRequest): Resource<String> =
        try {
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

    suspend fun getConsentForm(): ConsentForm? =
        if (CommonUtils.parseUserLocale() == DefinedParams.EN) {
            roomHelper.getConsentFormByType(ConsentFormType.Household)
        } else {
            roomHelper.getConsentFormByType(ConsentFormType.HouseHoldCulture)
        }

    suspend fun updateHouseholdMemberTbContactTraceStatus(
        hhmId: Long,
        tbContactTracingStatus: Int,
    ) = roomHelper.updateTBContactTraceStatus(hhmId, tbContactTracingStatus)

    suspend fun getHouseholdsCountBasedSubVillage(subVillageId: Long) = roomHelper.getHouseholdsCountBasedSubVillage(subVillageId)
}
