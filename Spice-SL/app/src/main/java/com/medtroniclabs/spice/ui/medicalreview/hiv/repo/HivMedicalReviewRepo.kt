package com.medtroniclabs.spice.ui.medicalreview.hiv.repo

import androidx.lifecycle.LiveData
import com.medtroniclabs.spice.common.ConsentFormType
import com.medtroniclabs.spice.common.DefinedParams.emtctVisitStatus
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.HivClinicalInfoResponse
import com.medtroniclabs.spice.data.HivVitalDetailsRequest
import com.medtroniclabs.spice.data.HivVitalDetailsResponse
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.PregnancyDetailsModel
import com.medtroniclabs.spice.data.WhoClinicalStageCreateRequest
import com.medtroniclabs.spice.data.model.HivCreateScreeningSummaryResponse
import com.medtroniclabs.spice.data.model.HivMedicalReviewSummaryRequest
import com.medtroniclabs.spice.data.model.HivMedicalReviewSummaryResponse
import com.medtroniclabs.spice.data.model.HivRequestData
import com.medtroniclabs.spice.data.model.HivScreeningRequest
import com.medtroniclabs.spice.data.model.HivScreeningResponse
import com.medtroniclabs.spice.data.model.ViralLoadRequest
import com.medtroniclabs.spice.data.model.ViralLoadResponse
import com.medtroniclabs.spice.data.model.HivSummaryResponse
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.data.model.PatientEncounterResponse
import com.medtroniclabs.spice.data.resource.CD4DetailsRequest
import com.medtroniclabs.spice.data.resource.CD4DetailsResponse
import com.medtroniclabs.spice.db.entity.ConsentForm
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.ARTResponse
import com.medtroniclabs.spice.model.ArtRequest
import com.medtroniclabs.spice.model.PregnancySummaryRequest
import com.medtroniclabs.spice.model.medicalreview.EMTCTVisitStatusRequest
import com.medtroniclabs.spice.model.medicalreview.EMTCTVisitStatusResponse
import com.medtroniclabs.spice.model.medicalreview.HivVitalsRequest
import com.medtroniclabs.spice.model.medicalreview.HivVitalsResponse
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class HivMedicalReviewRepo @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {
    suspend fun getStaticMetaData(): Resource<Boolean> {
        return try {
            val response = apiHelper.getHivStaticData()
            if (response.isSuccessful) {
                response.body()?.entity?.apply {
                    roomHelper.deleteExaminationsComplaintsForAnc(MedicalReviewTypeEnums.HIV.name)
                    roomHelper.insertExaminationsComplaint(
                        generateChipItemByType(
                            hivHistory = hivHistory,
                            populationType = populationType,
                            hivTestDuration = hivTestDurations,
                            entryPoint = entryPoint,
                            patientStatus = patientStatus,
                            comorbidities = comorbidities,
                            presentingComplaints = presentingComplaints,
                            systemicExaminations = systemicExaminations,
                            hivPreganancyBreastFeedingStatus = hivPreganancyBreastFeedingStatus,
                            ahdStatus = ahdStatus,
                            dsdStatus = dsdStatus,
                            nonEstablishedModels = nonEstablishedModels,
                            emtctVisitStatus = emtctVisitStatus,
                            whoClinicalStage = whoClinicalStage,
                            emtctEntryPoint = emtctEntryPoint,
                            tbStatus = tbStatus,
                            maternalOutcome = maternalOutcome,
                            obstetricExaminations = obstetricExaminations
                        )
                    )
                    roomHelper.deleteDiagnosisList(MedicalReviewTypeEnums.HIV_REVIEW.name)
                    roomHelper.saveDiagnosisList(diseaseCategories)
                }
                SecuredPreference.putBoolean(SecuredPreference.EnvironmentKey.IS_HIV_DATA_LOADED.name, true)
                Resource(state = ResourceState.SUCCESS, true)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            SecuredPreference.putBoolean(SecuredPreference.EnvironmentKey.IS_HIV_DATA_LOADED.name, false)
            Resource(state = ResourceState.ERROR)
        }
    }

    private fun generateChipItemByType(
        hivHistory: List<MedicalReviewMetaItems>,
        populationType: List<MedicalReviewMetaItems>,
        hivTestDuration: List<MedicalReviewMetaItems>,
        entryPoint: List<MedicalReviewMetaItems>,
        patientStatus: List<MedicalReviewMetaItems>,
        comorbidities: List<MedicalReviewMetaItems>,
        presentingComplaints: List<MedicalReviewMetaItems>,
        systemicExaminations: List<MedicalReviewMetaItems>,
        hivPreganancyBreastFeedingStatus: List<MedicalReviewMetaItems>,
        ahdStatus: List<MedicalReviewMetaItems>,
        dsdStatus: List<MedicalReviewMetaItems>,
        nonEstablishedModels: List<MedicalReviewMetaItems>,
        emtctVisitStatus: List<MedicalReviewMetaItems>,
        whoClinicalStage: List<MedicalReviewMetaItems>,
        emtctEntryPoint: List<MedicalReviewMetaItems>,
        tbStatus: List<MedicalReviewMetaItems>,
        maternalOutcome: List<MedicalReviewMetaItems>,
        obstetricExaminations: List<MedicalReviewMetaItems>

    ): List<MedicalReviewMetaItems> {
        val chipItemList = ArrayList<MedicalReviewMetaItems>()
        chipItemList.addAll(hivHistory.map { it.apply { type = MedicalReviewTypeEnums.HIV.name } })
        chipItemList.addAll(populationType.map {
            it.apply {
                type = MedicalReviewTypeEnums.HIV.name
            }
        })
        chipItemList.addAll(hivTestDuration.map {
            it.apply {
                type = MedicalReviewTypeEnums.HIV.name
            }
        })
        chipItemList.addAll(entryPoint.map { it.apply { type = MedicalReviewTypeEnums.HIV.name } })
        chipItemList.addAll(patientStatus.map {
            it.apply {
                type = MedicalReviewTypeEnums.HIV.name
                category = MedicalReviewTypeEnums.patient_status.name
            }
        })
        chipItemList.addAll(comorbidities.map {
            it.apply {
                category = MedicalReviewTypeEnums.comorbidities.name
                type = MedicalReviewTypeEnums.HIV.name
            }
        })
        chipItemList.addAll(presentingComplaints.map {
            it.apply {
                category = MedicalReviewTypeEnums.PresentingComplaints.name
                type = MedicalReviewTypeEnums.HIV.name
            }
        })
        chipItemList.addAll(systemicExaminations.map {
            it.apply {
                category = MedicalReviewTypeEnums.SystemicExaminations.name
                type = MedicalReviewTypeEnums.HIV.name
            }
        })
        chipItemList.addAll(ahdStatus.map {
            it.apply {
                type = MedicalReviewTypeEnums.HIV.name
                category = MedicalReviewTypeEnums.ahdStatus.name
            }
        })
        chipItemList.addAll(hivPreganancyBreastFeedingStatus.map {
            it.apply {
                type = MedicalReviewTypeEnums.HIV.name
                category = MedicalReviewTypeEnums.hivPreganancyBreastFeedingStatus.name
            }
        })
        chipItemList.addAll(dsdStatus.map {
            it.apply {
                type = MedicalReviewTypeEnums.HIV.name
                category = MedicalReviewTypeEnums.dsdStatus.name
            }
        })
        chipItemList.addAll(nonEstablishedModels.map {
            it.apply {
                type = MedicalReviewTypeEnums.HIV.name
                category = MedicalReviewTypeEnums.nonEstablishedModels.name
            }
        })
        chipItemList.addAll(whoClinicalStage.map {
            it.apply {
                type = MedicalReviewTypeEnums.HIV.name
                category = MedicalReviewTypeEnums.whoClinicalStage.name
            }
        })
        chipItemList.addAll(emtctVisitStatus)
        chipItemList.addAll(emtctEntryPoint.map {
            it.apply {
                type = MedicalReviewTypeEnums.HIV.name
                category = MedicalReviewTypeEnums.emtctEntryPoint.name
            }
        })

        chipItemList.addAll(tbStatus.map {
            it.apply {
                type = MedicalReviewTypeEnums.HIV.name
                category = MedicalReviewTypeEnums.tbStatus.name
            }
        })
        chipItemList.addAll((emtctVisitStatus.map {
            it.apply {
                type = MedicalReviewTypeEnums.HIV.name
                category = MedicalReviewTypeEnums.emtct_visit_status.name
            }
        }))
        chipItemList.addAll((maternalOutcome.map {
            it.apply {
                type = MedicalReviewTypeEnums.HIV.name
                category = MedicalReviewTypeEnums.maternal_outcome.name
            }
        }))
        chipItemList.addAll(obstetricExaminations.map {
            it.apply {
                type = MedicalReviewTypeEnums.HIV.name
                category = MedicalReviewTypeEnums.obstetricExaminations.name
            }
        })

        return chipItemList
    }

    suspend fun getHivMetaItems(): Resource<List<MedicalReviewMetaItems>> {
        return try {
            val response = roomHelper.getHivMetaData()
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getConsentForm(): ConsentForm? {
        return roomHelper.getConsentFormByType(ConsentFormType.HIV)
    }

    suspend fun createHivScreening(request: HivScreeningRequest): Resource<HivScreeningResponse> {
        return try {
            val response = apiHelper.createHivScreening(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                val errorMessage = StringConverter.getErrorMessage(response.errorBody())
                Resource(state = ResourceState.ERROR, message = errorMessage)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getHivScreeningDetails(request: HivScreeningResponse): Resource<HivCreateScreeningSummaryResponse> {
        return try {
            val response = apiHelper.getHivScreeningDetails(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                val errorMessage = StringConverter.getErrorMessage(response.errorBody())
                Resource(state = ResourceState.ERROR, message = errorMessage)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun createHivSummary(request: HivMedicalReviewSummaryRequest): Resource<HivMedicalReviewSummaryResponse> {
        return try {
            val res = apiHelper.createHivSummary(request)
            if (res.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = res.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR, message = res.message())
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    fun getHivPatientStatus (
        category: String,
        type: String
    ): LiveData<List<MedicalReviewMetaItems>> {
        return roomHelper.getExaminationsComplaintsForAnc(category, type)
    }

    suspend fun getComplaintsListByType(type: String): Resource<List<MedicalReviewMetaItems>> {
        return try {
            val response = roomHelper.getExaminationsComplaintByType(type)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    fun getExaminationsComplaints(
        category: String,
        type: String
    ): LiveData<List<MedicalReviewMetaItems>> {
        return roomHelper.getExaminationsComplaintsForAnc(category, type)
    }

    suspend fun saveHIVMedicalReview(
        request: HivRequestData
    ): Resource<PatientEncounterResponse> {
        return try {
            val response = apiHelper.createHivImrCmr(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun fetchHivSummaryDetails(motherNeonateAncRequest: MotherNeonateAncRequest): Resource<HivSummaryResponse> {
        return try {
            apiHelper.fetchHivSummaryDetails(motherNeonateAncRequest).let { response ->
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        if (body.status) {
                            Resource(ResourceState.SUCCESS, body.entity)
                        } else {
                            Resource(ResourceState.ERROR)
                        }
                    } ?: Resource(ResourceState.ERROR)
                } else {
                    Resource(ResourceState.ERROR)
                }
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }
    }

    suspend fun createEMTCT(request: EMTCTVisitStatusRequest): Resource<EMTCTVisitStatusResponse> {
        return try {
            val res = apiHelper.createEMTCT(request)
            if (res.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = res.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR, message = res.message())
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getHivVitalsDetails(request: HivVitalsRequest): Resource<HivVitalsResponse> {
        return try {
            val res = apiHelper.getHivVitalsDetails(request)
            if (res.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = res.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR, message = res.message())
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }


    suspend fun getOpportunisticInfection(motherNeonateAncRequest: MotherNeonateAncRequest): Resource<HashMap<String, HashMap<String, String>?>> {
        return try {
            apiHelper.getOpportunisticInfection(motherNeonateAncRequest).let { response ->
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        if (body.status) {
                            Resource(ResourceState.SUCCESS, body.entity)
                        } else {
                            Resource(ResourceState.ERROR)
                        }
                    } ?: Resource(ResourceState.ERROR)
                } else {
                    Resource(ResourceState.ERROR)
                }
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }
    }

    suspend fun createWhoClinicalStage(request: WhoClinicalStageCreateRequest): Resource<HivClinicalInfoResponse> {
        return try {
            val response = apiHelper.createWhoClinicalStage(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                val errorMessage = StringConverter.getErrorMessage(response.errorBody())
                Resource(state = ResourceState.ERROR, message = errorMessage)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getHivVitalsDetails(hivVitalDetailsRequest: HivVitalDetailsRequest): Resource<HivVitalDetailsResponse> {
        return try {
            val response = apiHelper.getHivVitalDetails(hivVitalDetailsRequest)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                val errorMessage = StringConverter.getErrorMessage(response.errorBody())
                Resource(state = ResourceState.ERROR, message = errorMessage)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getHivCD4Details(request: CD4DetailsRequest): Resource<ArrayList<CD4DetailsResponse>> {
        return try {
            val response = apiHelper.getHivCD4Details(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                val errorMessage = StringConverter.getErrorMessage(response.errorBody())
                Resource(state = ResourceState.ERROR, message = errorMessage)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun checkRecommendationRInvestigations(motherNeonateAncRequest: MotherNeonateAncRequest): Resource<HashMap<String, Boolean?>?> {
        return try {
            apiHelper.checkRecommendationInvestigations(motherNeonateAncRequest).let { response ->
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        if (body.status) {
                            Resource(ResourceState.SUCCESS, body.entity)
                        } else {
                            Resource(ResourceState.ERROR)
                        }
                    } ?: Resource(ResourceState.ERROR)
                } else {
                    Resource(ResourceState.ERROR)
                }
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }
    }

    suspend fun getViralLoadData(request: ViralLoadRequest): Resource<List<ViralLoadResponse>> {
        return try {
            val res = apiHelper.getViralLoadData(request)
            if (res.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = res.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR, message = res.message())
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }
    suspend fun getARTData(request: ArtRequest): Resource<List<ARTResponse>> {
        return try {
            val res = apiHelper.getARTData(request)
            if (res.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = res.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR, message = res.message())
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }
    suspend fun getPatientSummaryDetails(request: PregnancySummaryRequest): Resource<PregnancyDetailsModel> {
        return try {
            val res = apiHelper.getPatientSummaryDetails(request)
            if (res.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = res.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR, message = res.message())
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }
}