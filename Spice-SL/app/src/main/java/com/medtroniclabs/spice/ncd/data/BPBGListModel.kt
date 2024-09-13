package com.medtroniclabs.spice.ncd.data

data class BPBGListModel(
    var limit: Int? = null,
    var skip: Int? = null,
    var memberId: String? = null,
    var latestRequired: Boolean? = null,
    val bpLogList: ArrayList<BPLogList>? = null,
    val glucoseLogList: ArrayList<GlucoseLogList>? = null
)

data class BPLogList(
    val bpTakenOn: String? = null, val avgSystolic: Long? = null, val avgDiastolic: Long? = null
)

data class GlucoseLogList(
    val glucoseType: String? = null,
    val glucoseValue: Double? = null,
    val glucoseUnit: String? = null,
    val glucoseDateTime: String? = null,
    val hba1c: Double? = null,
    val hba1cUnit: String? = null
)