package com.medtroniclabs.spice.formgeneration.utility

import android.content.Context
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.formgeneration.model.InformationModel
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.chestInDrawing
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.hasOedemaOfBothFeet
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.muacCode

class InformationUtils {

    fun getMuacInformationListItem(context: Context): ArrayList<InformationModel> {
        val informationList = ArrayList<InformationModel>()

        informationList.add(
            InformationModel(
                inputText = context.getString(R.string.muac_info_1),
                type = muacCode
            )
        )
        informationList.add(
            InformationModel(
                imageId = R.drawable.measuring_muac_1,
                inputText = context.getString(R.string.muac_info_2),
                type = muacCode
            )
        )
        informationList.add(
            InformationModel(
                imageId = R.drawable.measuring_muac_2,
                inputText = context.getString(R.string.muac_info_3),
                type = muacCode
            )
        )
        informationList.add(
            InformationModel(
                imageId = R.drawable.measuring_muac_3,
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
                imageId = R.drawable.oedema_1,
                inputText = context.getString(R.string.oedema_info_1),
                type = hasOedemaOfBothFeet
            )
        )
        informationList.add(
            InformationModel(
                imageId = R.drawable.oedema_2,
                inputText = context.getString(R.string.oedema_info_2),
                type = hasOedemaOfBothFeet
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
                imageId = R.drawable.chest_in_drawing_2,
                inputText = context.getString(R.string.chest_info_2),
                type = chestInDrawing
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

}