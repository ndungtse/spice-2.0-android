package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UnderTwoMonthsRepository @Inject constructor(
    private val roomHelper: RoomHelper,
    private val apiHelper: ApiHelper
) {

    suspend fun getStaticMetaData(underTwoMonthsMetaLiveData: MutableLiveData<Resource<Boolean>>){
        try {
            underTwoMonthsMetaLiveData.postLoading()
            withContext(Dispatchers.IO){
                val response = apiHelper.getUnderTwoMonthsMetaData()
                if (response.isSuccessful){
                    response.body()?.entity?.apply {
                        roomHelper.deleteDiagnosisList()
                        roomHelper.saveDiagnosisList(diseaseCategories)
                        roomHelper.deleteExaminationsList()
                        roomHelper.saveExaminationsList(examinations)
                    }
                    SecuredPreference.putBoolean(
                        SecuredPreference.EnvironmentKey.IS_UNDER_TWO_MONTHS_LOADED.name,
                        true
                    )
                    underTwoMonthsMetaLiveData.postSuccess()
                }
            }
        } catch (e:Exception){
            e.printStackTrace()
            underTwoMonthsMetaLiveData.postError()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_UNDER_TWO_MONTHS_LOADED.name,
                false
            )
        }
    }

}