package com.medtroniclabs.spice.data.community

sealed class CommunitySummaryListItem {
    data class ProfileItem(val name:String?,val desc:String?,val registeredDate:String?):CommunitySummaryListItem()
    data class OtherItem(val label:String?,val value:String?,val isText:Boolean = true):CommunitySummaryListItem()
    data class EmergencyItem(
        val chcName:String?,
        val valuesMap:Map<String,String>?
        ):CommunitySummaryListItem()
    data class TitleItem(val title:String):CommunitySummaryListItem()
}