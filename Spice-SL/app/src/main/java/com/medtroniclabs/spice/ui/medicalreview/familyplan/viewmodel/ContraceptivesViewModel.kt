package com.medtroniclabs.spice.ui.medicalreview.familyplan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.model.FamilyPlanningSummaryResponse
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.familyplan.FamilyPlanningRepository
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.CombinedOralContraceptiveComments
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.MicrolutQuantity
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherImplantComments
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherInjectableComments
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherPermanentMethodComments
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherProgestinOnlyOralsComments
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContraceptivesViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val repository: FamilyPlanningRepository
):ViewModel(){
    var selectedIUCD = ArrayList<ChipViewItemModel>()
    val resultHashMap = HashMap<String, Any>()
    var quantity: Long? =null
    var otherInjectableComments :String? = null
    var otherImplantComments :String? = null
    var otherPermanentMethodComments :String? = null
    var otherProgestinOnlyOralsComments :String? = null
    var combinedOralContraceptiveComments :String? = null
    val contraceptiveMetaList = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()

    fun getMetaList(type: String) {
        viewModelScope.launch(dispatcherIO) {
            contraceptiveMetaList.postLoading()
            contraceptiveMetaList.postValue(repository.getMetaListByType(type))
        }
    }

    fun getContraceptivesResult(): Pair<HashMap<String, Any>, List<ChipViewItemModel>> {
        resultHashMap[OtherInjectableComments ] = otherInjectableComments ?: ""
        resultHashMap[OtherImplantComments  ] = otherImplantComments ?: ""
        resultHashMap[OtherPermanentMethodComments] = otherPermanentMethodComments ?: ""
        resultHashMap[OtherProgestinOnlyOralsComments] = otherProgestinOnlyOralsComments ?: ""
        resultHashMap[CombinedOralContraceptiveComments] = combinedOralContraceptiveComments ?: ""
        resultHashMap[MicrolutQuantity] = quantity ?: -1L
        return Pair(resultHashMap, selectedIUCD)
    }


}