package com.medtroniclabs.spice.db.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.data.DiseaseConditionItems
import com.medtroniclabs.spice.data.offlinesync.model.FollowUpCallStatus
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import java.lang.reflect.Type

class OfflineStatusTypeConverter {

    @TypeConverter
    fun fromOfflineSyncStatus(syncStatus: OfflineSyncStatus): String {
        return syncStatus.name
    }

    @TypeConverter
    fun toOfflineSyncStatus(syncStatusString: String): OfflineSyncStatus {
        return OfflineSyncStatus.valueOf(syncStatusString)
    }

    @TypeConverter
    fun fromReferralStatus(referralStatus: ReferralStatus): String {
        return referralStatus.name
    }

    @TypeConverter
    fun toReferralStatus(referralStatusString: String): ReferralStatus {
        return ReferralStatus.valueOf(referralStatusString)
    }

    @TypeConverter
    fun fromString(value: String?): ArrayList<String?>? {
        val listType: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromDiseaseConditionString(value: String?): ArrayList<DiseaseConditionItems?>? {
        val listType: Type = object : TypeToken<ArrayList<DiseaseConditionItems?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromDiseaseConditionArrayList(list: ArrayList<DiseaseConditionItems?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromFollowUpCallStatus(callStatus: FollowUpCallStatus): String {
        return callStatus.name
    }

    @TypeConverter
    fun toFollowUpCallStatus(status: String): FollowUpCallStatus {
        return FollowUpCallStatus.valueOf(status)
    }
}