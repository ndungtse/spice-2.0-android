package com.medtroniclabs.spice.common


import org.junit.Test

class ValidatorTest {

    @Test
    fun isEmptyEmailValid() {
        assert(!Validator.isEmailValid(""))
    }

    @Test
    fun isInvalidEmailValid() {
        assert(!Validator.isEmailValid("faithkingi.spice.com"))
    }

    @Test
    fun isValidEmailValid() {
        assert(Validator.isEmailValid("demomail@gmail.com"))
    }

    @Test
    fun isEmptyMobileNumber() {
        assert(!Validator.isValidMobileNumber(""))
    }

    @Test
    fun isInvalidMobileNumber() {
        assert(!Validator.isValidMobileNumber("345434547"))
    }

    @Test
    fun isValidMobileNumber() {
        assert(Validator.isValidMobileNumber("7865463548"))
    }


}