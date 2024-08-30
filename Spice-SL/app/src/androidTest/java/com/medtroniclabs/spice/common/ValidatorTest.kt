package com.medtroniclabs.spice.common


import org.junit.Test

class ValidatorTest {

    @Test
    fun isEmailValid_emptyString_returnsFalse(){
        assert(!Validator.isEmailValid(""))
    }

    @Test
    fun isEmailValid_invalidEmailValid_returnsFalse() {
        assert(!Validator.isEmailValid("faithkingi.spice.com"))
    }

    @Test
    fun isEmailValid_validEmailValid_returnsTrue() {
        assert(Validator.isEmailValid("demomail@gmail.com"))
    }

    @Test
    fun isValidMobileNumber_emptyString_returnsFalse() {
        assert(!Validator.isValidMobileNumber(""))
    }

    @Test
    fun isValidMobileNumber_invalidMobileNumber_returnsFalse() {
        assert(!Validator.isValidMobileNumber("demoogddf"))
    }
    @Test
    fun isValidMobileNumber_numberRepetition_returnsFalse() {
        assert(!Validator.isValidMobileNumber("7811111111"))
    }

    @Test
    fun isValidMobileNumber_validMobileNumber_returnsTrue() {
        assert(Validator.isValidMobileNumber("7865463548"))
    }


}