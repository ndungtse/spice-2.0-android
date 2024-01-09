package com.medtroniclabs.spice.ui.member

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityHouseholdRegistrationBinding
import com.medtroniclabs.spice.databinding.ActivityMemberRegistrationBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout

class MemberRegistrationActivity : AppCompatActivity(), FormEventListener {

    private lateinit var binding: ActivityMemberRegistrationBinding

    private lateinit var formGenerator: FormGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemberRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        formGenerator = FormGenerator(
            this, binding.llForm, null, this, binding.scrollView,
            translate = false
        )
        val objectList = Gson().fromJson("[{\"familyOrder\":0,\"id\":\"bioData\",\"title\":\"BioData\",\"viewType\":\"CardView\"},{\"errorMessage\":\"Nameisrequiredandmustbelengthof1to100\",\"family\":\"bioData\",\"fieldName\":\"Name\",\"hint\":\"EnterName\",\"id\":\"name\",\"onlyAlphabets\":true,\"inputType\":96,\"isEnabled\":true,\"isMandatory\":true,\"isNeededDefault\":true,\"isNotDefault\":false,\"isSummary\":true,\"maxLength\":100,\"minLength\":1,\"orderId\":1,\"title\":\"Name\",\"viewType\":\"EditText\",\"visibility\":\"visible\"},{\"condition\":[],\"defaultValue\":null,\"errorMessage\":\"Pleaseselectanrelationship\",\"family\":\"bioData\",\"fieldName\":\"RelationhsiptoHousehold\",\"id\":\"relationshipToHousehold\",\"isEditable\":true,\"isEnabled\":true,\"isMandatory\":true,\"isNeededDefault\":true,\"optionsList\":[{\"id\":\"HouseholdHead\",\"name\":\"HouseholdHead\"},{\"id\":\"Wife\",\"name\":\"Wife\"},{\"id\":\"Son\",\"name\":\"Son\"},{\"id\":\"Daughter\",\"name\":\"Daughter\"}],\"title\":\"RelationhsiptoHousehold\",\"viewType\":\"Spinner\",\"orderId\":2,\"visibility\":\"visible\"},{\"errorMessage\":\"MobileNumberisrequiredandmustbelengthof1to100\",\"family\":\"bioData\",\"fieldName\":\"MobileNumber\",\"hint\":\"EnterMobileNumber\",\"id\":\"mobileNumber\",\"inputType\":3,\"isEnabled\":true,\"isMandatory\":true,\"isNeededDefault\":true,\"isNotDefault\":false,\"isSummary\":true,\"maxLength\":100,\"minLength\":1,\"orderId\":3,\"title\":\"MobileNumber\",\"viewType\":\"EditText\",\"visibility\":\"visible\"},{\"condition\":[],\"defaultValue\":null,\"errorMessage\":\"PleaseselectanMobilenumbercategory\",\"family\":\"bioData\",\"fieldName\":\"MobileNumberCategory\",\"id\":\"mobileNumberCategory\",\"isEditable\":true,\"isEnabled\":true,\"isMandatory\":true,\"isNeededDefault\":true,\"optionsList\":[{\"id\":\"Personal\",\"name\":\"Personal\"},{\"id\":\"FamilyMember\",\"name\":\"FamilyMember\"},{\"id\":\"Friend\",\"name\":\"Friend\"}],\"title\":\"MobileNumberCategory\",\"orderId\":4,\"viewType\":\"Spinner\",\"visibility\":\"visible\"},{\"disableFutureDate\":true,\"family\":\"bioMetrics\",\"fieldName\":\"Age\",\"id\":\"age\",\"isEnabled\":true,\"isMandatory\":true,\"isNeededDefault\":true,\"isSummary\":true,\"title\":\"Age\",\"viewType\":\"Age\",\"orderId\":5,\"visibility\":\"visible\"},{\"condition\":[],\"errorMessage\":\"NationalIDisrequired\",\"family\":\"bioData\",\"fieldName\":\"NationalID\",\"hint\":\"EnterNationalID\",\"id\":\"nationalId\",\"inputType\":-1,\"isEnabled\":true,\"isMandatory\":true,\"isNeedAction\":true,\"isNeededDefault\":true,\"isNotDefault\":false,\"isSummary\":true,\"maxLength\":50,\"minLength\":1,\"orderId\":6,\"title\":\"NationalID\",\"viewType\":\"EditText\",\"visibility\":\"visible\"},{\"family\":\"bioData\",\"fieldName\":\"Gender\",\"id\":\"gender\",\"isEnabled\":true,\"isMandatory\":true,\"isNeededDefault\":true,\"isNotDefault\":false,\"isSummary\":true,\"orderId\":7,\"optionsList\":[{\"name\":\"Male\",\"id\":\"male\"},{\"name\":\"Female\",\"id\":\"female\"}],\"title\":\"Gender\",\"viewType\":\"SingleSelectionView\",\"visibility\":\"visible\"}]", Array<FormLayout>::class.java).asList()
        formGenerator.populateViews(objectList)
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        TODO("Not yet implemented")
    }

    override fun onPopulate(targetId: String) {
        TODO("Not yet implemented")
    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        serverViewModel: FormLayout,
        resultMap: Any?
    ) {
        TODO("Not yet implemented")
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?
    ) {
        TODO("Not yet implemented")
    }
}