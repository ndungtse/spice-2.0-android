package com.medtroniclabs.spice.ui.household.search.viewmodel

import androidx.lifecycle.ViewModel
import com.medtroniclabs.spice.formgenerator.definedproperties.DefinedParams
import javax.inject.Inject

class HouseholdListViewModel @Inject constructor() : ViewModel() {

    //Patient list - Grid count
    var spanCount: Int = DefinedParams.span_count_1

}