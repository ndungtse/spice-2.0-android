package com.medtroniclabs.spice.ui.registration.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.EntityMapper
import com.medtroniclabs.spice.common.FormAutofill
import com.medtroniclabs.spice.data.model.RecommendedDosageListModel
import com.medtroniclabs.spice.databinding.FragmentRegistrationFormBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.FormResultComposer
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.registration.RegistrationActivity
import com.medtroniclabs.spice.ui.registration.viewmodel.RegistrationFormViewModel

class RegistrationFormFragment : BaseFragment(), View.OnClickListener, FormEventListener {
    private lateinit var binding: FragmentRegistrationFormBinding
    private val viewModel: RegistrationFormViewModel by activityViewModels()
    private val patientViewModel: PatientDetailViewModel by activityViewModels()
    private lateinit var formGenerator: FormGenerator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistrationFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        loadJson()
        attachObservers()
    }

    private fun initViews() {
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, null, this, binding.scrollView,
            translate = false
        )

        binding.btnSubmit.safeClickListener(this)
    }

    private fun loadJson() {
        viewModel.getFormData(DefinedParams.Registration)
    }

    private fun attachObservers() {
        viewModel.registrationFormLayoutsLiveData.observe(viewLifecycleOwner) { resources ->
            when (resources.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resources.data?.let { data ->
                        formGenerator.populateViews(data.formLayout)
                        prePopulate()
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
        viewModel.countrySpinnerLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        formGenerator.spinnerDataInjection(
                            data,
                            EntityMapper.getResultSpinnerMapList(data)
                        )
                    }
                }

                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
        viewModel.districtSpinnerLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        formGenerator.spinnerDataInjection(
                            data,
                            EntityMapper.getResultSpinnerMapList(data)
                        )
                    }
                }

                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
        viewModel.chiefdomSpinnerLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        formGenerator.spinnerDataInjection(
                            data,
                            EntityMapper.getResultSpinnerMapList(data)
                        )
                    }
                }

                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
        viewModel.villageSpinnerLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        formGenerator.spinnerDataInjection(
                            data,
                            EntityMapper.getResultSpinnerMapList(data)
                        )
                    }
                }

                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
        viewModel.programsSpinnerLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { data ->
                        formGenerator.spinnerDataInjection(
                            data,
                            EntityMapper.getResultSpinnerMapList(data)
                        )
                    }
                }

                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
        viewModel.registrationResponseLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState?.data?.let {
                        (activity as RegistrationActivity?)?.loadRegistrationSummaryFragment()
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                    resourceState.message?.let {
                        (activity as? BaseActivity)?.showErrorDialogue(
                            getString(R.string.error),
                            it,
                            isNegativeButtonNeed = false
                        ) {}
                    }
                }
            }
        }
    }

    private fun prePopulate() {
        patientViewModel.patientDetailsLiveData.value?.data.let {
            FormAutofill.start(formGenerator, it)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.btnSubmit.id -> {
                formGenerator.formSubmitAction(view)
            }
        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        if (localDataCache is String) {
            viewModel.loadDataCacheByType(id, localDataCache, selectedParent)
        }
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
        description: String?,
        dosageListModel: ArrayList<RecommendedDosageListModel>?
    ) {
    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?, serverData: List<FormLayout?>?) {
        withNetworkAvailability(online = {
            resultMap?.let { map ->
                val result = serverData?.let {
                    FormResultComposer().groupValues(
                        context = requireContext(),
                        serverData = it,
                        map
                    )
                }
                result?.second?.let {
                    viewModel.registerPatient(it)
                }
            }
        })
    }

    override fun onRenderingComplete() {
    }

    override fun onUpdateInstruction(id: String, selectedId: Any?) {
    }

    override fun onInformationHandling(
        id: String,
        noOfDays: Int,
        enteredDays: Int?,
        resultMap: HashMap<String, Any>?
    ) {
    }

    override fun onAgeCheckForPregnancy() {
    }

    companion object {
        const val TAG = "RegistrationFormFragment"
    }
}