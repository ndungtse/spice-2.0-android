package com.medtroniclabs.spice.formgeneration.listener

import com.medtroniclabs.spice.formgeneration.model.FormLayout

interface FormEventListener {

    fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long? = null)
    fun onPopulate(targetId: String)

    fun onCheckBoxDialogueClicked(
        id: String,
        serverViewModel: FormLayout,
        resultMap: Any?
    )

    fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>? = null,
        description: String? = null
    )

    fun onFormSubmit(resultMap: HashMap<String, Any>?)

}
