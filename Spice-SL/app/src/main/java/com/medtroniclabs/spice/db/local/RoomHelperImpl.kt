package com.medtroniclabs.spice.db.local

import com.medtroniclabs.spice.db.dao.AssessmentDAO
import com.medtroniclabs.spice.db.dao.HouseholdDAO
import com.medtroniclabs.spice.db.dao.MemberDAO
import com.medtroniclabs.spice.db.dao.MetaDataDAO
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.FormEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.entity.UserProfileEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import javax.inject.Inject

class RoomHelperImpl @Inject constructor(
    private val householdDAO: HouseholdDAO,
    private val memberDAO: MemberDAO,
    private val assessmentDAO: AssessmentDAO,
    private val metaDataDAO: MetaDataDAO
) : RoomHelper {
    override suspend fun saveHouseHoldEntry(householdEntity: HouseholdEntity): Long {
        return householdDAO.insertHouseHold(householdEntity)
    }

    override suspend fun updateHousehold(householdEntity: HouseholdEntity) {
        return householdDAO.updateHouseHold(householdEntity)
    }

    override suspend fun getHouseHoldList(): ArrayList<HouseHoldEntityWithMemberCount> {
        return ArrayList(householdDAO.getAllHouseHold())
    }

    override suspend fun getLastHouseholdNo(villageId: Long): Long? {
        return householdDAO.getLastHouseholdNo(villageId)
    }

    override suspend fun searchByHouseholdNameOrNo(searchTerm: String): ArrayList<HouseHoldEntityWithMemberCount> {
        return ArrayList(householdDAO.searchByHouseholdNameOrNo(searchTerm))
    }

    override suspend fun getHouseHoldDetailsById(houseHoldId: Long): HouseholdEntity {
        return householdDAO.getHouseHoldDetailsById(houseHoldId)
    }

    override suspend fun registerMember(householdMemberEntity: HouseholdMemberEntity): Long {
        return memberDAO.insertMember(householdMemberEntity)
    }

    override suspend fun getAllHouseHoldMemberList(houseHoldId: Long): ArrayList<HouseholdMemberEntity> {
        return ArrayList(memberDAO.getAllHouseHoldMemberList(houseHoldId))
    }

    override suspend fun getMemberDetailsByID(memberId: Long): HouseholdMemberEntity {
        return memberDAO.getMemberDetailsById(memberId)
    }

    override suspend fun getMemberCountPerHouseHold(householdId: Long): Int {
        return memberDAO.getMemberCountPerHouseHold(householdId)
    }

    override suspend fun saveAssessment(assessmentEntity: AssessmentEntity): Long {
        return assessmentDAO.insertAssessment(assessmentEntity)
    }

    override suspend fun updateOtherAssessmentDetails(assessmentEntity: AssessmentEntity) {
        return assessmentDAO.updateOtherAssessmentDetails(assessmentEntity)
    }

    override suspend fun getLatestAssessmentForMember(memberId: Long): AssessmentEntity? {
        return assessmentDAO.getLatestAssessmentForMember(memberId)
    }

    override suspend fun insertSymptomList(symptoms: List<SignsAndSymptomsEntity>) {
        assessmentDAO.insertSymptoms(symptoms)
    }

    override suspend fun getSymptomListByType(type: String): List<SignsAndSymptomsEntity> {
        return assessmentDAO.getSymptomListByType(type)
    }
    override suspend fun saveHealthFacility(healthFacilityEntityList: HealthFacilityEntity) {
         metaDataDAO.insertHealthFacility(healthFacilityEntityList)
    }

    override suspend fun updateHeadCount(householdId: Long, newNoOfPeople: Int) {
        return householdDAO.updateHeadCount(householdId, newNoOfPeople)
    }

    override suspend fun deleteAllHealthFacility() {
        metaDataDAO.deleteAllHealthFacility()
    }

    override suspend fun saveVillage(villageEntityList: List<VillageEntity>) {
        metaDataDAO.insertVillages(villageEntityList)
    }

    override suspend fun getAllVillageName(): List<VillageEntity> {
        return metaDataDAO.getAllVillageName()
    }

    override suspend fun getDefaultHealthFacility(): HealthFacilityEntity? {
        return metaDataDAO.getDefaultHealthFacility()
    }

    override suspend fun deleteAllVillages() {
        metaDataDAO.deleteAllVillages()
    }

    override suspend fun saveMenus(menuEntity: MenuEntity) {
        metaDataDAO.insertMenus(menuEntity)
    }

    override suspend fun saveClinicalWorkflow(clinicalWorkflowEntity: ClinicalWorkflowEntity) {
        return metaDataDAO.saveClinicalWorkflow(clinicalWorkflowEntity)
    }

    override suspend fun deleteAllClinicalWorkflow() {
        return metaDataDAO.deleteAllClinicalWorkflow()
    }

    override suspend fun saveForm(form: FormEntity) {
        return metaDataDAO.saveForm(form)
    }

    override suspend fun deleteAllForms() {
        return metaDataDAO.deleteAllForms()
    }

    override suspend fun getAllClinicalWorkflowIds(): List<Int> {
        return metaDataDAO.getAllClinicalWorkflowIds()
    }

    override suspend fun insertSymptoms(symptomEntity: SignsAndSymptomsEntity) {
        metaDataDAO.insertSymptoms(symptomEntity)
    }

    override suspend fun deleteAllSymptoms() {
        metaDataDAO.deleteAllSymptoms()
    }


    override suspend fun getFormData(formType: String): String {
        return metaDataDAO.getFormData(formType)
    }

    override suspend fun getFomDataForWorkFlow(formType: String, workflowName: String): String {
        return metaDataDAO.getFormDataForWorkFlow(formType, workflowName)
    }

    override suspend fun deleteAllMenus() {
        metaDataDAO.deleteAllMenus()
    }

    override suspend fun saveUserProfileDetails(userProfileEntity: UserProfileEntity) {
        metaDataDAO.insertUserProfileDetails(userProfileEntity)
    }

    override suspend fun deleteAllUserProfileDetails() {
        metaDataDAO.deleteAllUserProfileDetails()
    }

    override suspend fun getMenus() : List<MenuEntity> {
        return metaDataDAO.getMenus()
    }

    override suspend fun getUserProfile(): UserProfileEntity {
        return metaDataDAO.getUserProfile()
    }

    override suspend fun getUserVillages(): List<VillageEntity> = metaDataDAO.getVillages()
}