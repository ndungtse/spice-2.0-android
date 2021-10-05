package com.mdtlabs.ncd.data.model

data class LoginRequest(
    var username: String,
    var password: String,
    var options: OptionsModel
)

