package com.medtroniclabs.spice.ui.household.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentHouseHoldRegistrationBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.household.HouseholdActivity
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams
import com.medtroniclabs.spice.ui.household.viewmodel.HouseRegistrationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HouseHoldRegistrationFragment : Fragment(), View.OnClickListener, FormEventListener {

    lateinit var binding: FragmentHouseHoldRegistrationBinding

    private lateinit var formGenerator: FormGenerator

    private val householdRegistrationViewModel: HouseRegistrationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHouseHoldRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        householdRegistrationViewModel.getFormData(HouseholdDefinedParams.REGISTRATION)
        initializeFormGenerator()
        setListeners()
        attachObservers()
    }

    private fun attachObservers() {
        householdRegistrationViewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resources ->
            when (resources.state) {
                ResourceState.LOADING -> {
                    (activity as? BaseActivity)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as? BaseActivity)?.hideLoading()
                    resources.data?.let { data ->
                        val formFieldsType = object : TypeToken<FormResponse>() {}.type
                        val formFields:FormResponse = Gson().fromJson(data, formFieldsType)
                        formGenerator.populateViews(formFields.formLayout)
                    }
                }

                ResourceState.ERROR -> {
                    (activity as? BaseActivity)?.hideLoading()
                }
            }
        }
    }

    private fun initializeFormGenerator() {
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, null, this, binding.scrollView,
            translate = false
        )
    }


    private fun setListeners() {
        binding.btnNext.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnNext -> {
                 formGenerator.formSubmitAction(v)
                /*if (activity is HouseholdActivity) {
                    (activity as HouseholdActivity).loadFragment(2)
                }*/
            }
        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
    }

    override fun onPopulate(targetId: String) {
    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        serverViewModel: FormLayout,
        resultMap: Any?
    ) {
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?
    ) {
    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        resultMap?.let { map ->
            householdRegistrationViewModel.registerHousehold(map)
        }
    }
}