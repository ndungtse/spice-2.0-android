package org.medtroniclabs.uhis.db.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "ShasthyaShebikaLinkedVillageEntity",
    primaryKeys = ["shasthyaShebikaId", "subVillageId"],
    indices = [
        Index(value = ["shasthyaShebikaId"], name = "idx_linked_shasthya_shebika_id"),
        Index(value = ["subVillageId"], name = "idx_linked_subvillage_id"),
    ],
)
data class ShasthyaShebikaLinkedVillageEntity(
    val shasthyaShebikaId: Long,
    val subVillageId: Long,
)
