package com.medtroniclabs.spice.mappingkey

import android.content.Context
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

object MemberRegistration {
    const val name = "name"
    const val phoneNumber = "phone_number"
    const val phoneNumberCategory = "phone_number_category"
    const val villageId="village_id"
    const val dateOfBirth = "date_of_birth"
    const val gender = "gender"
    const val householdHeadRelationship = "household_head_relationship"
    var otherFamilyMember = "otherFamilyMember"
    const val householdId = "household_id"
    const val isPregnant= "isPregnant"

    const val WifeOrHusband = "Wife / Husband"
    private const val SonOrDaughter = "Son / Daughter"
    const val FatherOrMother = "Father / Mother"
    private const val BrotherOrSister = "Brother / Sister"
    private const val Grandchild = "Grandchild"
    const val Grandparent = "Grandparent"
    const val OtherRelation = "Other Family Member (specify)"

    fun isValidRelationAge(ctx: Context, dob: String, relation: String, headDob: String?): String? {
        // No Age Validation for Brother or Sister and Other relation
        if (relation.equals(BrotherOrSister, true)
            || relation.equals(OtherRelation, true)
        ) {
            return null
        }

        val memberDOB =
            LocalDate.parse(dob, DateTimeFormatter.ofPattern(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ))
        // Min Age validation for Wife or Husband
        if (relation.equals(WifeOrHusband, true)) {
            return if (isValidMinAge(memberDOB)) null else ctx.getString(R.string.age_validation_wife_husband)
        }

        val headDOB = LocalDate.parse(headDob, DateTimeFormatter.ofPattern(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ))
        val ageDifference = Period.between(headDOB, memberDOB).years

        return when (relation) {
            // Minimum +10 Age Difference for Son or Daughter
            SonOrDaughter -> if (ageDifference >= 10) null else ctx.getString(R.string.age_validation_son_daughter)

            // Minimum -10 Age Difference for Father or Mother
            FatherOrMother -> if (ageDifference <= -10) null else ctx.getString(R.string.age_validation_father_mother)

            // Minimum +30 Age Difference for Grandchild
            Grandchild -> if (ageDifference >= 30) null else ctx.getString(R.string.age_validation_grandchild)

            // Minimum -30 Age Difference for Grandparent
            Grandparent -> if (ageDifference <= -30) null else ctx.getString(R.string.age_validation_grandparent)
            else -> null
        }
    }

    fun isValidMinAge(dateOfBirth: LocalDate): Boolean {
        val today = LocalDate.now()
        val age = Period.between(dateOfBirth, today).years
        return age >= 10
    }

    fun isValidMinAgeForCbsMemberAdd(dob: String): Boolean {
        val memberDOB = LocalDate.parse(
            dob,
            DateTimeFormatter.ofPattern(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
        )
        val ageDifference = Period.between(memberDOB, LocalDate.now())
        return ageDifference.years == 0
    }
}