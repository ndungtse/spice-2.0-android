package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDFormsRepo
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDFormViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val ncdFormsRepo: NCDFormsRepo,
) : ViewModel() {

    val ncdFormResponse = MutableLiveData<Resource<List<FormLayout>>>()

    fun getNCDForm(type: String, workFlow: String? = null) {
        viewModelScope.launch(dispatcherIO) {
            ncdFormResponse.postLoading()
            try {
                val gson = Gson()
                val formLayouts = ArrayList<FormLayout>()
                ncdFormsRepo.getNCDForm(type, workFlow = workFlow).forEach { item ->
                    if (item.contains(DefinedParams.ViewScreens)) {
                        gson.fromJson(item, List::class.java)?.let { list ->
                            list.forEach { listItem ->
                                if (listItem is Map<*, *>) {
                                    (listItem[DefinedParams.ViewScreens] as? ArrayList<*>)?.let { viewScreens ->
                                        val hasVs =
                                            viewScreens.any { it.toString().equals(type, true) }
                                        if (hasVs) {
                                            (listItem[DefinedParams.FormInput] as? String)?.let { responseStr ->
                                                gson.fromJson(responseStr, FormResponse::class.java)
                                                    ?.let { formResponse ->
                                                        formLayouts.addAll(formResponse.formLayout)
                                                    }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        gson.fromJson(item, FormResponse::class.java)?.let { formResponse ->
                            formLayouts.addAll(formResponse.formLayout)
                        }
                    }
                }
                ncdFormResponse.postSuccess(formLayouts)
            } catch (e: Exception) {
                ncdFormResponse.postError()
            }
        }
    }
}