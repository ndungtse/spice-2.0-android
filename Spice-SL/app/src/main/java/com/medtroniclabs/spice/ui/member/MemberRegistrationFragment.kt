package com.medtroniclabs.spice.ui.member

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentMemberRegistrationBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.home.ToolsActivity
import com.medtroniclabs.spice.ui.household.HouseholdActivity
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
                            intent.putExtra(DefinedParams.MemberID, it ?: -1)
                        }
                        startActivity(intent)
                        (activity as HouseholdActivity).finish()
                    } else {
                        if (!householdRegistrationViewModel.isMemberRegistration) {
                            val intent = Intent(
                                requireActivity(), HouseholdSummaryActivity::class.java
                            )
                            intent.putExtra(
                                DefinedParams.houseHoldID,
                                memberRegistrationViewModel.householdId
                            )
                            intent.putExtra(DefinedParams.isFromHouseHoldRegistration, true)
                            startActivity(intent)
                        }
                        (activity as HouseholdActivity).finish()
                    }
                }
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
        val objectList = Gson().fromJson(
            CommonUtils.getStringFromAssets(
                "house_hold_member_registration.json", requireActivity().assets
            ), Array<FormLayout>::class.java
        ).asList()
        formGenerator.populateViews(objectList)
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
            if (householdRegistrationViewModel.isMemberRegistration) {
                memberRegistrationViewModel.registerMember(map)
            } else {
                householdRegistrationViewModel.householdEntityDetail?.let { householdEntity ->
                    memberRegistrationViewModel.registerHouseThenMember(householdEntity, map)
                }
            }
        }
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