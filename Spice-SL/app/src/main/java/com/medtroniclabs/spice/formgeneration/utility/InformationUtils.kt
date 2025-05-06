package com.medtroniclabs.spice.formgeneration.utility

import android.content.Context
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.formgeneration.model.InformationModel
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Contraceptive
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.PostTestCounselling
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.PreTestCounselling
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.TestForHiv
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.chestInDrawing
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.hasOedemaOfBothFeet
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.isBreastfeed
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.isConvulsionPastFewDays
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.isUnusualSleepy
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.isVomiting
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.muacCode
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.rdtTest

class InformationUtils {

    fun getMuacInformationListItem(context: Context): ArrayList<InformationModel> {
        val informationList = ArrayList<InformationModel>()

        informationList.add(
            InformationModel(
                imageId = R.drawable.measuring_muac_1,
                inputText = context.getString(R.string.muac_info_1),
                type = muacCode
            )
        )
        informationList.add(
            InformationModel(
                imageId = R.drawable.measuring_muac_2,
                inputText = context.getString(R.string.muac_info_2),
                type = muacCode
            )
        )
        informationList.add(
            InformationModel(
                imageId = R.drawable.measuring_muac_3,
                inputText = context.getString(R.string.muac_info_3),
                type = muacCode
            )
        )
        informationList.add(
            InformationModel(
                imageId = R.drawable.measuring_muac_4,
                inputText = context.getString(R.string.muac_info_4),
                type = muacCode
            )
        )
        informationList.add(
            InformationModel(
                inputText = context.getString(R.string.muac_info_5),
                type = muacCode
            )
        )
        informationList.add(
            InformationModel(
                inputText = context.getString(R.string.muac_info_6),
                type = muacCode
            )
        )
        return informationList
    }

    fun getOedemaInformationList(context: Context): ArrayList<InformationModel>{
        val informationList = ArrayList<InformationModel>()

        informationList.add(
            InformationModel(
                imageId = R.drawable.oedema_2,
                inputText = context.getString(R.string.oedema_info_1),
                type = hasOedemaOfBothFeet
            )
        )
        informationList.add(
            InformationModel(
                inputText = context.getString(R.string.oedema_info_2),
            )
        )
        informationList.add(
            InformationModel(
                imageId = R.drawable.oedema_3,
                inputText = context.getString(R.string.oedema_info_3),
                type = hasOedemaOfBothFeet
            )
        )
        return informationList
    }

    fun getChestIndrawingInformation(context: Context) : ArrayList<InformationModel>{
        val informationList = ArrayList<InformationModel>()

        informationList.add(
            InformationModel(
                imageId = R.drawable.chest_in_drawing_1,
                inputText = context.getString(R.string.chest_info_1),
                type = chestInDrawing
            )
        )
        informationList.add(
            InformationModel(
                inputText = context.getString(R.string.chest_info_2),
                type = chestInDrawing
            )
        )
        return informationList
    }

    fun getRdtTest(context: Context): ArrayList<InformationModel> {
        val informationList = ArrayList<InformationModel>()
        informationList.add(
            InformationModel(
                imageId = R.drawable.rdt_test_positive,
                inputText = "",
                type = rdtTest
            )
        )
        informationList.add(
            InformationModel(
                imageId = R.drawable.rdt_test_negative,
                inputText = "",
                type = rdtTest
            )
        )
        return informationList
    }

    fun getFastBreathingInstructions(context: Context): ArrayList<String> {
        val informationList = ArrayList<String>()

        informationList.add(
            context.getString(R.string.fb_info_1)
        )
        informationList.add(
            context.getString(R.string.fb_info_2)
        )
        informationList.add(
            context.getString(R.string.fb_info_3)
        )
        informationList.add(
            context.getString(R.string.fb_info_4)
        )
        informationList.add(
            context.getString(R.string.fb_info_5)
        )
        informationList.add(
            context.getString(R.string.fb_info_6)
        )

        return informationList

    }
    fun getDangerSignsInstructions(context: Context, id: String?): ArrayList<InformationModel> {
        val informationList = ArrayList<InformationModel>()
        when (id) {
            isUnusualSleepy -> {
                informationList.add(
                    InformationModel(
                        imageId = R.drawable.ic_unusefulsleep,
                        inputText = context.getString(R.string.ask_is_the_child_very_sleepy),
                        type = isUnusualSleepy
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.clap_your_hands_close_to_the_child),
                        type = isUnusualSleepy
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.if_the_child_does_not),
                        type = isUnusualSleepy
                    )
                )
            }

            isVomiting -> {
                informationList.add(
                    InformationModel(
                        imageId = R.drawable.ic_isvomiting,
                        inputText = context.getString(R.string.ask_does_the_child_vomit_everything_he_she_eats_or_drinks),
                        type = isVomiting
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.give_the_child_clean_water_or_ask_the_mother_to_offer_her_breast_to_the_child),
                        type = isVomiting
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.if_the_child_vomits_the_water_or_breastmilk_refer_to_the_phu_immediately),
                        type = isVomiting
                    )
                )
            }

            isConvulsionPastFewDays -> {
                informationList.add(
                    InformationModel(
                        imageId = R.drawable.ic_convulsions,
                        inputText = context.getString(R.string.look_is_the_child_convulsing_now),
                        type = isConvulsionPastFewDays
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.ask_did_the_child_have_convulsions_at_home),
                        type = isConvulsionPastFewDays
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.if_the_child_is_convulsing_now_or_if_the_child_had_convulsions_at_home_refer_to_the_phu_immediately),
                        type = isConvulsionPastFewDays
                    )
                )
            }

            isBreastfeed -> {
                informationList.add(
                    InformationModel(
                        imageId = R.drawable.ic_unablebreastfeed,
                        inputText = context.getString(R.string.ask_if_the_child_is_able_to_drink_or_breastfeed),
                        type = isBreastfeed
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.give_the_child_clean_water_or_ask_the_mother_to_offer_her_breast_to_the_child),
                        type = isBreastfeed
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.if_the_child_is_not_able_to_drink_or_breastfeed_refer_to_the_phu_immediately),
                        type = isBreastfeed
                    )
                )
            }
        }
        return informationList
    }

    fun getContraceptiveInformation(context: Context): ArrayList<InformationModel>? {
        val informationList = ArrayList<InformationModel>()
        informationList.add(
            InformationModel(
                imageId = R.drawable.ic_contraceptive,
                inputText = context.getString(R.string.advise_to),
                type = Contraceptive
            )
        )
        informationList.add(
            InformationModel(
                inputText = context.getString(R.string.explain_that),
                type = Contraceptive
            )
        )
        return informationList
    }
    fun getHIVTestInformation(context: Context, id: String?): ArrayList<InformationModel> {
        val informationList = ArrayList<InformationModel>()
        when (id) {
            PreTestCounselling -> {
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.basic_information_about_hiv),
                        type = PreTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.the_benefits_of_hiv_testing),
                        type = PreTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.understanding_test_results),
                        type = PreTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.importance_of_hiv_prevention),
                        type = PreTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.confidentiality_and_rights),
                        type = PreTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.risk_assessment),
                        type = PreTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.explain_the_testing_procedure),
                        type = PreTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.addressing_misconceptions),
                        type = PreTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.support_services_for_hiv),
                        type = PreTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.consent_sought_for_the_individual_to_be_tested),
                        type = PreTestCounselling
                    )
                )
            }
            PostTestCounselling -> {
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.re_emphasise_hiv_prevention_methods_retesting),
                        type = PostTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.for_positive_clients),
                        type = PostTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.offer_prep_for_partners_in_discordant_couples),
                        type = PostTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.provide_counselling_risk_reduction_condom_use),
                        type = PostTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.art_and_hiv_care),
                        type = PostTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.offer_art_preparatory_counselling),
                        type = PostTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.post_test_doc_and_link),
                        type = PostTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.link_to_art),
                        type = PostTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.if_same_day_art),
                        type = PostTestCounselling
                    )
                )
                informationList.add(
                    InformationModel(
                        inputText = context.getString(R.string.provide_ongoing_follow_up),
                        type = PostTestCounselling
                    )
                )
            }
            TestForHiv -> {
                   informationList.add(
                    InformationModel(
                        imageId = R.drawable.ic_hiv_test,
                        inputText = "",
                        type = TestForHiv
                    )
                )
            }
        }
        return informationList
    }

}