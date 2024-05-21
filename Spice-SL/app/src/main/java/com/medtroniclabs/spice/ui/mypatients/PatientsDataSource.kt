package com.medtroniclabs.spice.ui.mypatients

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.medtroniclabs.spice.common.DefinedParams.LIST_LIMIT
import com.medtroniclabs.spice.common.DefinedParams.PAGE_INDEX
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.model.MedicalReviewFilterModel
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.PatientsDataModel
import com.medtroniclabs.spice.model.SearchAndListResponse
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.ui.mypatients.repo.PatientRepository


class PatientsDataSource(
    private val apiHelper: ApiHelper,
    private val patientRepository: PatientRepository,
    private val searchText: String,
    private val filter:MedicalReviewFilterModel?,
    private val getPatientsCount: (String) -> Unit
) : PagingSource<Int, PatientListRespModel>() {

    private var loadedCount = 0
    private var totalCount = 0
    private var villages: List<Long> = mutableListOf()
    private var districtId: Long? = null
    private var referencePatientId: String? = null
    override fun getRefreshKey(state: PagingState<Int, PatientListRespModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PatientListRespModel> {
        val pageIndex = params.key ?: PAGE_INDEX
        return try {
            if (villages.isEmpty()) {
                val villageIdNameList = patientRepository.getVillageIdName()
                villages = villageIdNameList.map { it.id }
                districtId = districtId ?: villageIdNameList.firstOrNull()?.districtId
            }
            val response: APIResponse<SearchAndListResponse> = if (searchText.isEmpty()) {
                apiHelper.getPatients(
                    PatientsDataModel(
                        skip = loadedCount,
                        limit = LIST_LIMIT,
                        villageIds = villages,
                        referencePatientId = referencePatientId?.ifBlank { null },
                        filter = filter
                    )
                )
            } else {
                apiHelper.patientSearch(
                    PatientsDataModel(
                        skip = loadedCount,
                        limit = LIST_LIMIT,
                        searchText = searchText.ifEmpty { null },
                        districtId = districtId,
                        referencePatientId = referencePatientId?.ifBlank { null },
                        filter = filter
                    )
                )
            }
            /* Request construction - Ends */


            val patientList: List<PatientListRespModel> = response.entity?.patientList ?: emptyList()
            referencePatientId = response.entity?.referencePatientId

            response.totalCount?.let { count ->
                totalCount = count
            } ?: kotlin.run {
                totalCount += patientList.size
                getPatientsCount(totalCount.toString())
            }
            if (loadedCount == 0)
                getPatientsCount(totalCount.toString())
            loadedCount += patientList.size
            LoadResult.Page(
                data = patientList,
                prevKey = (pageIndex - 1).takeIf { pageIndex > PAGE_INDEX },
                nextKey = if (searchText.isEmpty()) {
                    (pageIndex + 1).takeIf { loadedCount < totalCount }
                } else (pageIndex + 1).takeIf { patientList.isNotEmpty() }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}