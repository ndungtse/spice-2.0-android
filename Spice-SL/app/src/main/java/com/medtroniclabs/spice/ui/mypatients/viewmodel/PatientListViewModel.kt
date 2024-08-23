package com.medtroniclabs.spice.ui.mypatients.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.Active
import com.medtroniclabs.spice.common.DefinedParams.LIST_LIMIT
import com.medtroniclabs.spice.db.response.VillageBasicDetails
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.common.DefinedParams.OnHold
import com.medtroniclabs.spice.common.DefinedParams.OnTreatment
import com.medtroniclabs.spice.common.DefinedParams.REFERRED
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.model.MedicalReviewFilterModel
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.ui.BaseViewModel
import com.medtroniclabs.spice.ui.mypatients.PatientsDataSource
import com.medtroniclabs.spice.ui.mypatients.repo.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class PatientListViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val apiHelper: ApiHelper,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher
    ) : BaseViewModel(dispatcherIO) {

    //Patient list - Grid count
    var spanCount: Int = DefinedParams.span_count_1
    var totalPatientCount = MutableLiveData<String>()
    var searchText = ""
    var medicalReviewDueTag: List<ChipViewItemModel>? = null
    var patientStatusTag: List<ChipViewItemModel>? = null
    var filterLiveData = MutableLiveData<Boolean>()

    var origin: String? = null

    val patientsDataSource =
        Pager(config = PagingConfig(pageSize = LIST_LIMIT), pagingSourceFactory = {
            PatientsDataSource(
                apiHelper = apiHelper,
                patientRepository = patientRepository,
                searchText = searchText,
                filter = getFilter()
            ) { getPatientsCount ->
                totalPatientCount.postValue(getPatientsCount)
            }
        }).flow

    fun setFilter(trigger: Boolean) {
        filterLiveData.value = trigger
    }

    private fun getFilter(): MedicalReviewFilterModel? {
        return if (patientStatusTag?.isNullOrEmpty() == false || medicalReviewDueTag?.isNullOrEmpty() == false) {
            MedicalReviewFilterModel(
                patientStatus = patientStatusTag?.map {
                    if (it.name.equals(OnTreatment, true)) OnHold else if (it.name.equals(
                            REFERRED,
                            true
                        )
                    ) {
                        Active
                    } else ""
                },
                visitDate = medicalReviewDueTag?.map { it.name.lowercase() }
            )
        } else {
            null
        }
    }

    fun filterCount(): Int {
        return listOf(patientStatusTag, medicalReviewDueTag).count { !it.isNullOrEmpty() }
    }

}

