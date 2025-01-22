package com.medtroniclabs.spice.ui.landing.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.data.CulturesEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.CultureLocaleModel
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseViewModel
import com.medtroniclabs.spice.ui.boarding.repo.MetaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguagePreferenceViewModel @Inject constructor(
    private val metaRepository: MetaRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher
) : BaseViewModel(dispatcherIO) {
    var selectedCultureId: Long? = null

    val cultureList = MutableLiveData<Resource<List<CulturesEntity>>>()
    val cultureUpdateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()

    fun getCultures() {
        viewModelScope.launch(dispatcherIO) {
            cultureList.postValue(metaRepository.getCultures())
        }
    }

    fun cultureLocaleUpdate(request: CultureLocaleModel) {
        viewModelScope.launch(dispatcherIO) {
            try {
                cultureUpdateResponse.postLoading()
                val response = metaRepository.cultureLocaleUpdate(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true) {
                        cultureUpdateResponse.postSuccess(res.entityList)
                    } else {
                        cultureUpdateResponse.postError()
                    }
                } else {
                    cultureUpdateResponse.postError()
                }
            } catch (e: Exception) {
                cultureUpdateResponse.postError()
            }
        }
    }
}