package com.medtroniclabs.spice.ui.mypatient.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.appextensions.setError
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowUpViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    var spanCount: Int = DefinedParams.span_count_1
    var isAssessmentType: String = ""
    val callResultHashMap = HashMap<String, Any>()
    val patientStatusHashMap = HashMap<String, Any>()
    val unSuccessfulHashMap = HashMap<String, Any>()
    var setPatientDateList = MutableLiveData<List<FollowUpPatientModel>>()

    val followUpPatientList = MutableLiveData<Resource<List<FollowUpPatientModel>>>()
    fun getFollowUpPatientList(type: String? = null, searchKey: String? = "") {
        viewModelScope.launch(dispatcherIO) {
            try {
                followUpPatientList.postLoading()
                type?.let { type ->
                    isAssessmentType = type
                }
                followUpPatientList.postSuccess(getMockData())
            } catch (e: Exception) {
                followUpPatientList.setError()
            }
        }
    }


    fun getMockData(): List<FollowUpPatientModel> {

        return listOf(
            FollowUpPatientModel(
                id = 1,
                reason = "Follow-up visit",
                firstName = "John",
                lastName = "Doe",
                patientStatus = "Active",
                startDate = "2024-01-23",
                village = "Sample Village",
                landMark = "Near Landmark",
                hhName = "Doe Household",
                memberId = 101,
                totalCall = 3,
                callsMade = 2,
                dateOfBirth = "1990-05-15",
                age = 32,
                gender = "Male"
            ),
            FollowUpPatientModel(
                id = 2,
                reason = "Routine checkup",
                firstName = "Jane",
                lastName = "Smith",
                patientStatus = "Inactive",
                startDate = "2024-02-10",
                village = "Another Village",
                landMark = "Main Street",
                hhName = "Smith Household",
                memberId = 102,
                totalCall = 2,
                callsMade = 1,
                dateOfBirth = "1985-08-20",
                age = 36,
                gender = "Female"
            )
        )

    }
}