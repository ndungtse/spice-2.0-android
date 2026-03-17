package org.medtroniclabs.uhis.ui.peersupervisor.adapter

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.medtroniclabs.uhis.common.DefinedParams.LIST_LIMIT
import org.medtroniclabs.uhis.common.DefinedParams.PAGE_INDEX
import org.medtroniclabs.uhis.data.performance.CHWPerformanceMonitoring
import org.medtroniclabs.uhis.data.performance.PerformanceReportRequest
import org.medtroniclabs.uhis.network.ApiHelper

class PerformanceReportDataSource(
    private val apiService: ApiHelper,
    private val request: PerformanceReportRequest,
) : PagingSource<Int, CHWPerformanceMonitoring>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CHWPerformanceMonitoring> {
        try {
            val currentPage = params.key ?: PAGE_INDEX

            val skip = currentPage * LIST_LIMIT
            request.skip = skip

            val response = apiService.getPeerSupervisorReport(request)
            val data = response.body()?.entityList ?: listOf()

            return LoadResult.Page(
                data = data,
                prevKey = if (currentPage == PAGE_INDEX) null else currentPage - 1,
                nextKey = if (data.isEmpty()) null else currentPage + 1,
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CHWPerformanceMonitoring>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
}
