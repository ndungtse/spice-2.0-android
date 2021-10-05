package com.mdtlabs.ncd.data.model

data class LoginResponse(
    val email: String?,
    val first_name: String?,
    val username: String?,
    val last_name: String?,
    val is_active: Boolean,
    val is_blocked: Boolean,
    val token: String
)