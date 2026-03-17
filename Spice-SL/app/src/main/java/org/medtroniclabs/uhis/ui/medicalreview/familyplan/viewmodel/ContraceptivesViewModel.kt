package org.medtroniclabs.uhis.ui.medicalreview.familyplan.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.medicalreview.familyplan.FamilyPlanningRepository
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewDefinedParams.CombinedOralContraceptiveComments
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewDefinedParams.MicrolutQuantity
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherImplantComments
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherInjectableComments
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherPermanentMethodComments
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherProgestinOnlyOralsComments
import javax.inject.Inject

@HiltViewModel
class ContraceptivesViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val repository: FamilyPlanningRepository,
) : ViewModel() {
    var selectedIUCD = ArrayList<ChipViewItemModel>()
    val resultHashMap = HashMap<String, Any>()
    var quantity: Long? = null
    var otherInjectableComments: String? = null
    var otherImplantComments: String? = null
    var otherPermanentMethodComments: String? = null
    var otherProgestinOnlyOralsComments: String? = null
    var combinedOralContraceptiveComments: String? = null
    val contraceptiveMetaList = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()

    fun getMetaList(type: String) {
        viewModelScope.launch(dispatcherIO) {
            contraceptiveMetaList.postLoading()
            contraceptiveMetaList.postValue(repository.getMetaListByType(type))
        }
    }

    fun getContraceptivesResult(): Pair<HashMap<String, Any>, List<ChipViewItemModel>> {
        resultHashMap[OtherInjectableComments ] = otherInjectableComments ?: ""
        resultHashMap[OtherImplantComments ] = otherImplantComments ?: ""
        resultHashMap[OtherPermanentMethodComments] = otherPermanentMethodComments ?: ""
        resultHashMap[OtherProgestinOnlyOralsComments] = otherProgestinOnlyOralsComments ?: ""
        resultHashMap[CombinedOralContraceptiveComments] = combinedOralContraceptiveComments ?: ""
        resultHashMap[MicrolutQuantity] = quantity ?: -1L
        return Pair(resultHashMap, selectedIUCD)
    }
}
