package com.medtroniclabs.spice.ui.medicalreview.investigation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityInvestigationBinding
import com.medtroniclabs.spice.model.medicalreview.InvestigationModel
import com.medtroniclabs.spice.model.medicalreview.SearchLabTestResponse
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.DeleteReasonDialog
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel

class InvestigationActivity : BaseActivity(), AdapterView.OnItemClickListener,
    InvestigationListener, View.OnClickListener {

    lateinit var binding: ActivityInvestigationBinding

    private val investigationViewModel: InvestigationViewModel by viewModels()

    private val patientViewModel: PatientDetailViewModel by viewModels()

    private lateinit var investigationGenerator: InvestigationGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestigationBinding.inflate(layoutInflater)
        setMainContentView(binding.root, true, title = getString(R.string.investigation))
        initView()
        setListeners()
        attachObserver()
    }

    private fun attachObserver() {
        investigationViewModel.investigationSearchResponseListLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {

                }

                ResourceState.ERROR -> {

                }

                ResourceState.SUCCESS -> {
                    resourceState.data?.let {
                        loadAdapter(it)
                    }
                }
            }
        }

        investigationViewModel.investigationListLiveData.observe(this) { investigationList ->
            showAdapterList(investigationList)
        }

        patientViewModel.patientDetailsLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let { data ->
                        if (data.gender != null){
                            investigationGenerator.setPatientGender(data.gender)
                        }
                        data.id?.let {
                            investigationViewModel.getLabTestList(data)
                        } ?: kotlin.run {
                            hideLoading()
                        }
                    } ?: kotlin.run {
                        hideLoading()
                    }
                }
            }
        }

        investigationViewModel.labTestListLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let {
                        investigationViewModel.addExistingLabTestListToUI(it)
                    }
                }
            }
        }

        investigationViewModel.createLabTestLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let { map ->
                        hideLoading()
                        val intent = Intent()
                        if (map.containsKey(DefinedParams.EncounterId)) {
                            val value = map[DefinedParams.EncounterId]
                            if (value is String) {
                                intent.putExtra(DefinedParams.EncounterId, value)
                            }
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    } ?: kotlin.run {
                        hideLoading()
                    }
                }
            }
        }

        investigationViewModel.removeLabTestLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let { map ->
                        if (map.containsKey(DefinedParams.ID)) {
                            val id = map[DefinedParams.ID]
                            if (id is String) {
                                investigationViewModel.removeInvestigationByID(id)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showAdapterList(investigationList: ArrayList<InvestigationModel>) {
        binding.llInvestigationHolder.removeAllViews()
        if (investigationList.size > 0) {
            binding.llInvestigationHolder.visible()
            binding.tvNoInvestigationDataFound.gone()
            investigationGenerator.populateViews(investigationList)
        } else {
            binding.llInvestigationHolder.gone()
            binding.tvNoInvestigationDataFound.visible()
        }

    }

    private fun loadAdapter(data: ArrayList<SearchLabTestResponse>) {
        val adapter = InvestigationSearchAdapter(binding.root.context)
        adapter.setData(data)
        binding.searchView.setAdapter(adapter)
        binding.searchView.showDropDown()
    }

    private fun setListeners() {
        binding.searchView.addTextChangedListener {
            if (it.isNullOrEmpty()) {
                // default showing all medicines
            } else {
                investigationViewModel.searchInvestigationByName(it.toString())
            }
        }
        binding.searchView.onItemClickListener = this
        binding.btnSubmit.setOnClickListener(this)
    }

    private fun initView() {
        investigationViewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
        investigationViewModel.encounterId = intent.getStringExtra(DefinedParams.EncounterId)
        investigationGenerator = InvestigationGenerator(
            this@InvestigationActivity,
            binding.llInvestigationHolder,
            binding.nestedScrollView,
            false,
            this
        )
        investigationViewModel.patientId?.let {
            patientViewModel.getPatients(it)
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        investigationViewModel.investigationSearchResponseListLiveData.value?.let { resourceState ->
            resourceState.data?.let { investigationList ->
                val investigationResponse = investigationList[position]
                investigationViewModel.addInvestigationModelToUI(investigationResponse)
            }
        }
        binding.searchView.setText("")
    }

    override fun removeInvestigation(investigation: InvestigationModel) {
        val dialog = DeleteReasonDialog.newInstance(
            this,
            getString(R.string.confirmation),
            true,
            Pair(getString(R.string.ok), getString(R.string.cancel)),
            showComment = false,
            callback = { isPositiveResult, _ ->
                if (isPositiveResult) {
                    investigationViewModel.removeInvestigationModel(investigation)
                }
            },
            message = Pair(getString(R.string.delete_confirmation), null)
        )
        dialog.show(supportFragmentManager, DeleteReasonDialog.TAG)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnSubmit.id -> {
                if (investigationGenerator.onValidateInput()) {
                    patientViewModel.patientDetailsLiveData.value?.data?.let { data ->
                        investigationViewModel.createLabTest(
                            geyPayloadForLabTest(investigationGenerator.getResultFromInvestigation()),
                            data
                        )
                    }
                } else {
                    investigationGenerator.getResultFromInvestigation()?.let {
                        binding.llInvestigationHolder.removeAllViews()
                        investigationGenerator.populateViews(ArrayList(it))
                    }
                }
            }
        }
    }


    private fun geyPayloadForLabTest(resultFromInvestigation: List<InvestigationModel>?): List<InvestigationModel>? {
        val list = resultFromInvestigation?.filter { it.id == null || (it.resultHashMap != null && it.resultHashMap!!.size > 0) }
        return list
    }

}