package com.medtroniclabs.spice.formgeneration.ui

import android.content.Context
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.formgeneration.config.ViewType
import com.medtroniclabs.spice.formgeneration.model.FormLayout

class FormResultComposer {

    private var groupedResultMap: HashMap<String, Any> = HashMap()

    fun groupValues(
        context: Context,
        serverData: List<FormLayout?>,
        resultMap: HashMap<String, *>,
        menutype: String?
    ): Pair<String?, HashMap<String, Any>> {
        serverData.forEach { serverViewModel ->
           when (serverViewModel?.viewType) {
                ViewType.VIEW_TYPE_FORM_CARD_FAMILY -> createGroup(serverViewModel.id)
                else -> {
                    addToGroup(
                        serverViewModel?.family,
                        serverViewModel?.id!!,
                        resultMap[serverViewModel.id]
                    )
                    resultMap.remove(serverViewModel.id)
                }
            }
        }
        return Pair(StringConverter.convertGivenMapToString(groupedResultMap), addToMenuGroup(groupedResultMap, menutype))
    }

    private fun addToMenuGroup(groupedResultMap: HashMap<String, Any>, menuType: String?): HashMap<String, Any> {
        val menuGroupMap = HashMap<String, Any>()
        menuType?.let {
            menuGroupMap[menuType] = groupedResultMap
            return menuGroupMap
        }
        return groupedResultMap
    }

    private fun createGroup(id: String) {
        val tempMap = HashMap<String, Any>()
        if (!groupedResultMap.containsKey(id))
            groupedResultMap[id] = tempMap
    }

    private fun addToGroup(family: String?, id: String, any: Any?) {

        family?.let {
            if (!groupedResultMap.containsKey(it))
                createGroup(it)
            val subMap = groupedResultMap[it] as HashMap<String, Any>
            any?.let { value ->
                subMap.put(id, value)
            }
        }
    }
}