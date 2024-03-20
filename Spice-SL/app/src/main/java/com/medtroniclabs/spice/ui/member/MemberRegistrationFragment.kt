package com.medtroniclabs.spice.ui.member

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.getYearMonthAndWeek
import com.medtroniclabs.spice.common.DefinedParams.HOUSEHOLD_MEMBER_REGISTRATION
import com.medtroniclabs.spice.common.DefinedParams.MemberID
import com.medtroniclabs.spice.common.DefinedParams.female
import com.medtroniclabs.spice.common.DefinedParams.male
import com.medtroniclabs.spice.databinding.FragmentMemberRegistrationBinding
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Month
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Week
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Year
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.mappingkey.MemberRegistration
import com.medtroniclabs.spice.mappingkey.MemberRegistration.gender
import com.medtroniclabs.spice.mappingkey.MemberRegistration.householdHeadRelationship
import com.medtroniclabs.spice.mappingkey.MemberRegistration.name
import com.medtroniclabs.spice.mappingkey.MemberRegistration.phoneNumber
import com.medtroniclabs.spice.mappingkey.MemberRegistration.phoneNumberCategory
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.home.ToolsActivity
import com.medtroniclabs.spice.ui.household.HouseholdActivity
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams
import com.medtroniclabs.spice.ui.household.summary.HouseholdSummaryActivity
import com.medtroniclabs.spice.ui.household.viewmodel.HouseRegistrationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MemberRegistrationFragment : Fragment(), FormEventListener, View.OnClickListener {

    private lateinit var binding: FragmentMemberRegistrationBinding
    private lateinit var formGenerator: FormGenerator
    private val memberRegistrationViewModel: MemberRegistrationViewModel by activityViewModels()
    private val householdRegistrationViewModel: HouseRegistrationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMemberRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        setListener()
        memberRegistrationViewModel.getFormData(HOUSEHOLD_MEMBER_REGISTRATION)
        attachObserver()
    }

    private fun attachObserver() {
        memberRegistrationViewModel.memberRegistrationLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as BaseActivity?)?.showLoading()
                }

                ResourceState.ERROR -> {
                    (activity as BaseActivity?)?.hideLoading()

                }

                ResourceState.SUCCESS -> {
                    (activity as BaseActivity?)?.hideLoading()
                    if (memberRegistrationViewModel.startAssessment != null && memberRegistrationViewModel.startAssessment!!) {
                        val intent = Intent(requireActivity(), ToolsActivity::class.java)
                        resourceState.data.let {
                            intent.putExtra(MemberID, it ?: -1)
                        }
                        startActivity(intent)
                        (activity as HouseholdActivity).finish()
                    } else {
                        if (!householdRegistrationViewModel.isMemberRegistration) {
                            val intent = Intent(
                                requireActivity(), HouseholdSummaryActivity::class.java
                            )
                            intent.putExtra(
                                HouseholdDefinedParams.ID,
                                memberRegistrationViewModel.selectedHouseholdId
                            )
                            intent.putExtra(HouseholdDefinedParams.isFromHouseHoldRegistration, memberRegistrationViewModel.memberDetailsLiveData.value?.data?.id == null)
                            startActivity(intent)
                        }
                        (activity as HouseholdActivity).finish()
                    }
                }
            }
        }
        memberRegistrationViewModel.formLayoutsLiveData.observe(viewLifecycleOwner) { resources ->
            when (resources.state) {
                ResourceState.LOADING -> {
                    (activity as? BaseActivity)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as? BaseActivity)?.hideLoading()
                    resources.data?.let { data ->
                        val formFieldsType = object : TypeToken<FormResponse>() {}.type
                        val formFields: FormResponse = Gson().fromJson(data, formFieldsType)
                        formGenerator.populateViews(formFields.formLayout)
                    }
                }

                ResourceState.ERROR -> {
                    (activity as? BaseActivity)?.hideLoading()
                }
            }
        }

        memberRegistrationViewModel.memberDetailsLiveData.observe(viewLifecycleOwner) {resourceState ->
            when(resourceState.state){
                ResourceState.LOADING ->{
                    (activity as BaseActivity?)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as BaseActivity?)?.hideLoading()
                    resourceState.data?.let { data ->
                        autoPopulateDetails(data)
                    }
                }

                ResourceState.ERROR -> {
                    (activity as BaseActivity?)?.hideLoading()
                }
            }
        }
    }

    private fun autoPopulateDetails(details: HouseholdMemberEntity) {
        details.householdId.let {id ->
            householdRegistrationViewModel.householdId = id
        }
        formGenerator.getViewByTag(name)?.let { view ->
            formGenerator.setValueForView(details.name, view)
        }
        formGenerator.getViewByTag(householdHeadRelationship)?.let { view ->
            if (details.householdHeadRelationship.isNotBlank()) {
                view.isEnabled = false
            }
            formGenerator.setValueForView(details.householdHeadRelationship, view)
        }
        formGenerator.getViewByTag(phoneNumber)?.let { view ->
            formGenerator.setValueForView(details.phoneNumber, view)
        }
        formGenerator.getViewByTag(phoneNumberCategory)?.let { view ->
            formGenerator.setValueForView(details.phoneNumberCategory, view)
        }
        details.gender.let {
            when (it) {
                male -> {
                    singleSelectValueOption(
                        male,
                        gender
                    )
                }

                female -> {
                    singleSelectValueOption(
                        female,
                        gender
                    )
                }

                else -> {}
            }
            if (details.gender.isNotBlank()) {
                formGenerator.disableSingleSelection(gender)
            }
        }
        details.dateOfBirth.let {
            val dateOfBirth = DateUtils.convertDateFormat(it, DATE_FORMAT_yyyyMMddHHmmssZZZZZ, DATE_ddMMyyyy)
            formGenerator.getViewByTag(MemberRegistration.dateOfBirth)?.let { view ->
                if (dateOfBirth.isNotBlank()) {
                    formGenerator.disableView(view, requireContext())
                }
                formGenerator.setValueForView(dateOfBirth, view)
            }

            val yearMonthWeeks = getYearMonthAndWeek(dateOfBirth, DATE_ddMMyyyy)
            formGenerator.getViewByTag(MemberRegistration.dateOfBirth + Year)?.let { view ->
                formGenerator.disableView(view, requireContext())
                formGenerator.setValueForView(yearMonthWeeks.first, view)
            }
            formGenerator.getViewByTag(MemberRegistration.dateOfBirth + Month)?.let { view ->
                formGenerator.disableView(view, requireContext())
                formGenerator.setValueForView(yearMonthWeeks.second, view)
            }
            formGenerator.getViewByTag(MemberRegistration.dateOfBirth + Week)?.let { view ->
                formGenerator.disableView(view, requireContext())
                formGenerator.setValueForView(yearMonthWeeks.third, view)
            }
        }

    }


    private fun singleSelectValueOption(value: String, key: String) {
        formGenerator.getViewByTag("${value}_${key}")
            ?.let { view ->
                if (view is TextView) {
                    view.isSelected = true
                    view.performClick()
                }
            }
    }


    private fun setListener() {
        binding.btnSubmit.setOnClickListener(this)
        binding.btnStartAssessment.setOnClickListener(this)
    }

    private fun initializeView() {
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, null, this, binding.scrollView, translate = false
        )
    }


    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
    }

    override fun onPopulate(targetId: String) {
    }

    override fun onCheckBoxDialogueClicked(
        id: String, serverViewModel: FormLayout, resultMap: Any?
    ) {
    }

    override fun onInstructionClicked(
        id: String, title: String, informationList: ArrayList<String>?, description: String?
    ) {
    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        resultMap?.let { map ->
            if (householdRegistrationViewModel.isMemberRegistration || householdRegistrationViewModel.memberID != -1L) {
                memberRegistrationViewModel.registerMember(map, householdRegistrationViewModel.householdId)
            } else {
                householdRegistrationViewModel.householdEntityDetail?.let { householdEntity ->
                    memberRegistrationViewModel.registerHouseThenMember(householdEntity, map, householdRegistrationViewModel.getCurrentLocation())
                }
            }
        }
    }

    override fun onRenderingComplete() {
        if (householdRegistrationViewModel.memberID != -1L){
            memberRegistrationViewModel.getMemberDetailsByID(householdRegistrationViewModel.memberID)
        }
    }

    override fun onUpdateInstruction(id: String, selectedId: Any?) {

    }

    override fun onInformationHandling(
        id: String,
        noOfDays: Int,
        enteredDays: Int,
        resultMap: HashMap<String, Any>?
    ) {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnStartAssessment -> {
                memberRegistrationViewModel.startAssessment = true
                formGenerator.formSubmitAction(v)
            }

            R.id.btnSubmit -> {
                memberRegistrationViewModel.startAssessment = false
                formGenerator.formSubmitAction(v)
            }
        }
    }
}