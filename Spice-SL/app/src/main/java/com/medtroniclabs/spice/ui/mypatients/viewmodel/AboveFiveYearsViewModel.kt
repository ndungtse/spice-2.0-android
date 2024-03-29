package com.medtroniclabs.spice.ui.mypatients.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.AboveFiveYearsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboveFiveYearsViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO : CoroutineDispatcher,
    private var repository: AboveFiveYearsRepository
): ViewModel(){

    val aboveFiveYearsMetaLiveData = MutableLiveData<Resource<Boolean>>()

    fun getStaticMetaData() {
        viewModelScope.launch(dispatcherIO){
            repository.getStaticMetaData(aboveFiveYearsMetaLiveData)
        }
    }
}