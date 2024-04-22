package com.medtroniclabs.spice.ui.medicalreview.undertwomonths

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.UnderTwoMonthsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnderTwoMonthViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO : CoroutineDispatcher,
    private var repository: UnderTwoMonthsRepository
): ViewModel() {

    val underTwoMonthsMetaLiveData = MutableLiveData<Resource<Boolean>>()
    val resultBreastFeedingHashMap = HashMap<String, Any>()
    val resultMotherVitaminHashMap = HashMap<String, Any>()
    val exclusiveBreastFeedHashMap = HashMap<String, Any>()
    var nextVisitDateHashMap = HashMap<String, Any>()
    var selectedImmunisationStatus: String? = null

    fun getStaticMetaData() {
        viewModelScope.launch(dispatcherIO){
            repository.getStaticMetaData(underTwoMonthsMetaLiveData)
        }
    }

}