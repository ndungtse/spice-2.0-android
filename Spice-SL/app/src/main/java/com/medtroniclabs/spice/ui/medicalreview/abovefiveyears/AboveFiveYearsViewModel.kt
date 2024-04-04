package com.medtroniclabs.spice.ui.medicalreview.abovefiveyears

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.data.ExaminationsComplaintItems
import com.medtroniclabs.spice.data.model.AboveFiveYearsSubmitRequest
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.repo.AboveFiveYearsRepository
import com.medtroniclabs.spice.repo.ExaminationComplaintsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboveFiveYearsViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO : CoroutineDispatcher,
    private var repository: AboveFiveYearsRepository,
    private var chipItemRepository: ExaminationComplaintsRepository
): ViewModel(){

    @Inject
    lateinit var connectivityManager: ConnectivityManager
    val aboveFiveYearsMetaLiveData = MutableLiveData<Resource<Boolean>>()
    val examinationsComplaintsList = MutableLiveData<Resource<List<ExaminationsComplaintItems>>>()
    var presentingComplaintsType : String = ""
    var systemicExaminationsType : String = ""
    var selectedPresentingComplaints = ArrayList<ChipViewItemModel>()
    var selectedSystemicExaminations = ArrayList<ChipViewItemModel>()
    var enteredClinicalNotes = ""
    var enteredExaminationNotes = ""
    var enteredComplaintNotes = ""
    val aboveFiveYearsCreateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()

    fun getStaticMetaData(menuType: String) {
        viewModelScope.launch(dispatcherIO){
            repository.getStaticMetaData(aboveFiveYearsMetaLiveData, menuType)
        }
    }

    fun getComplaintsList(type: String) {
        viewModelScope.launch(dispatcherIO) {
            chipItemRepository.getComplaintsListByType(type, examinationsComplaintsList)
        }
    }

    fun createAboveFiveYearsResult(request: AboveFiveYearsSubmitRequest){
        if (connectivityManager.isNetworkAvailable()){
            viewModelScope.launch(dispatcherIO) {
                repository.createAboveFiveYears(request, aboveFiveYearsCreateResponse)
            }
        }
    }
}