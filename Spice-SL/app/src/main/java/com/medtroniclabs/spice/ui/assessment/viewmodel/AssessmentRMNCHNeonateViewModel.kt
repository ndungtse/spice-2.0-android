package com.medtroniclabs.spice.ui.assessment.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.AssessmentRepository
import com.medtroniclabs.spice.repo.HouseholdMemberRepository
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.referrallogic.ReferralResultGenerator
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssessmentRMNCHNeonateViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var householdMemberRepository: HouseholdMemberRepository,
    private var assessmentRepository: AssessmentRepository,
) : ViewModel() {

    var memberMap: HashMap<String, Any>? = null

    val formLayoutsLiveData = MutableLiveData<Resource<FormResponse>>()

    val memberFormLayoutsLiveData = MutableLiveData<Resource<FormResponse>>()

    val assessmentStringSaveLiveData = MutableLiveData<String?>()

    val assessmentSaveLiveData = MutableLiveData<Resource<AssessmentEntity>>()

    val childMemberDetailsLiveData = MutableLiveData<Resource<List<HouseholdMemberEntity>>>()
    var referralStatus: String? = null
    fun getFormData(formType: String) {
        viewModelScope.launch(dispatcherIO) {
            formLayoutsLiveData.postLoading()
            formLayoutsLiveData.postValue(assessmentRepository.getFormData(formType))
        }
    }


    fun savePNCDetail(
        childDetailMap: HashMap<String, Any>,
        householdId: Long,
        motherDetailMap: HashMap<String, Any>,
        memberDetail: AssessmentMemberDetails) {
        viewModelScope.launch(dispatcherIO) {
            if (memberMap != null) {
                householdMemberRepository.registerMember(
                    memberMap!!,
                    householdId,
                    null,
                    memberDetail.patientId
                )
                savePNCDetails(motherDetailMap, childDetailMap, memberDetail)
            } else {
                savePNCDetails(motherDetailMap, childDetailMap, memberDetail)
            }
        }
    }

    private suspend fun savePNCDetails(
        motherDetailMap: HashMap<String, Any>,
        childDetailMap: HashMap<String, Any>,
        memberDetail: AssessmentMemberDetails) {
        val groupMap = HashMap<String, Any>()
        groupMap[RMNCH.PNC] = motherDetailMap[RMNCH.PNC] as Any
        groupMap[RMNCH.PNCNeonatal] = childDetailMap
        val resultGenerator = ReferralResultGenerator()
        val referralResult = resultGenerator.calculateRMNCHReferralResult(groupMap)
        val assessmentDetail =
            getAssessmentDetails(groupMap as HashMap<Any, Any>)
        referralStatus = referralResult.first
        assessmentStringSaveLiveData.postValue(assessmentDetail.first)
        assessmentSaveLiveData.postValue(assessmentRepository.saveAssessment(
            assessmentDetail.second,
            memberDetail,
            RMNCH.PNC_MENU,
            referralResult,
            null,
        ))
    }

    private fun getAssessmentDetails(
        map: HashMap<Any, Any>
    ): Pair<String, String> {
        val assessmentDetail = StringConverter.convertGivenMapToString(map) ?: ""

        // Request modification for syncing PNC Mother to Backend
        if (map.containsKey(RMNCH.PNC)) {
            val pnc = map[RMNCH.PNC] as HashMap<Any, Any>
            if (pnc.containsKey(RMNCH.pncMotherSigns)) {
                val signsList = mutableListOf<String>()
                val list = pnc[RMNCH.pncMotherSigns] as List<*>
                list.forEach { it ->
                    if (it is HashMap<*, *>) {
                        signsList.add(it[DefinedParams.NAME] as String)
                    }
                }
                pnc[RMNCH.pncMotherSigns] = signsList
            }

            if (pnc.containsKey(RMNCH.otherPncMotherSigns)) {
                val os = pnc[RMNCH.otherPncMotherSigns] as Any
                pnc.remove(RMNCH.otherPncMotherSigns)
                pnc[RMNCH.otherSigns] = os
            }
        }

        // Request modification for syncing PNC neonatal to Backend
        if (map.containsKey(RMNCH.PNCNeonatal)) {
            val pncNeonate = map[RMNCH.PNCNeonatal] as HashMap<Any, Any>
            if (pncNeonate.containsKey(RMNCH.pncNeonateSigns)) {
                val signsList = mutableListOf<String>()
                val list = pncNeonate[RMNCH.pncNeonateSigns] as List<*>
                list.forEach { it ->
                    if (it is HashMap<*, *>) {
                        signsList.add(it[DefinedParams.NAME] as String)
                    }
                }

                pncNeonate.remove(RMNCH.pncNeonateSigns)
                pncNeonate[RMNCH.pncNeonatalSigns] = signsList
            }

            if (pncNeonate.containsKey(RMNCH.otherPncNeonateSigns)) {
                val os = pncNeonate[RMNCH.otherPncNeonateSigns] as Any
                pncNeonate.remove(RMNCH.otherPncNeonateSigns)
                pncNeonate[RMNCH.otherSigns] = os
            }
        }

        val assessmentDetailBE = StringConverter.convertGivenMapToString(map) ?: ""
        return Pair(assessmentDetail, assessmentDetailBE)
    }

    fun getMemberDetailsByParentId(memberId: String) {
        viewModelScope.launch(dispatcherIO) {
            childMemberDetailsLiveData.postLoading()
            childMemberDetailsLiveData.postValue(householdMemberRepository.getMemberDetailsByParentId(memberId))
        }
    }

    fun updateOtherAssessmentDetails(
        otherAssessmentDetails: HashMap<String, Any>,
        lastLocation: Location?,
        assessmentUpdateLiveData:MutableLiveData<Resource<String>>
    ) {
        viewModelScope.launch(dispatcherIO) {
            if (otherAssessmentDetails.containsKey(AssessmentDefinedParams.IsClinicTaken)) {
                val isTakenToClinical = otherAssessmentDetails[AssessmentDefinedParams.IsClinicTaken] as String
                otherAssessmentDetails[AssessmentDefinedParams.IsClinicTaken] =
                    (isTakenToClinical.equals(DefinedParams.Yes,true))
            }

            assessmentUpdateLiveData.postValue(assessmentRepository.updateOtherAssessmentDetails(
                assessmentSaveLiveData.value?.data,
                otherAssessmentDetails,
                lastLocation
            ))
        }
    }
}