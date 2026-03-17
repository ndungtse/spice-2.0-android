package org.medtroniclabs.uhis.formgeneration.model

sealed class ConditionModelConfig {
    object VISIBILITY : ConditionModelConfig()

    object ENABLED : ConditionModelConfig()
}
