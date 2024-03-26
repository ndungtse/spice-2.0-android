package com.medtroniclabs.spice.ui.mypatients.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.LIST_LIMIT
import com.medtroniclabs.spice.db.response.VillageBasicDetails
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.ui.mypatients.PatientsDataSource
import com.medtroniclabs.spice.ui.mypatients.repo.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PatientListViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val apiHelper: ApiHelper
) : ViewModel() {

    //Patient list - Grid count
    var spanCount: Int = DefinedParams.span_count_1
    var totalPatientCount = MutableLiveData<String>()
    var searchText = ""

    val patientsDataSource =
        Pager(config = PagingConfig(pageSize = LIST_LIMIT), pagingSourceFactory = {
            PatientsDataSource(
                apiHelper = apiHelper,
                patientRepository = patientRepository,
                searchText = searchText
            ) { getPatientsCount ->
                totalPatientCount.postValue(getPatientsCount)
            }
        }).flow

}