package com.medtroniclabs.spice.formgeneration.model

sealed class ConditionModelConfig {
    object VISIBILITY : ConditionModelConfig()

    object ENABLED : ConditionModelConfig()
}
