package com.medtroniclabs.spice.ui.household

import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityHouseholdRegistrationBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.ui.BaseActivity

class HouseholdRegistrationActivity : BaseActivity(), FormEventListener, View.OnClickListener {

    private lateinit var binding: ActivityHouseholdRegistrationBinding

    private lateinit var formGenerator: FormGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdRegistrationBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.household_registration)
        )
        formGenerator = FormGenerator(
            this, binding.llForm, null, this, binding.scrollView,
            translate = false
        )
        val objectList = Gson().fromJson(
            getStringFromAssets("house_hold_registration.json"),
            Array<FormLayout>::class.java
        ).asList()
        formGenerator.populateViews(objectList)
        setListeners()
    }

    private fun setListeners() {
        binding.btnNext.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnNext -> {

            }
        }
    }

    private fun getStringFromAssets(fileName: String): String {
        return assets.open(fileName).bufferedReader().use { it.readText() }
    }

}