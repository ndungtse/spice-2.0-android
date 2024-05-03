package com.medtroniclabs.spice.ui.medicalreview.prescription

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.medtroniclabs.spice.data.MedicationResponse
import com.medtroniclabs.spice.databinding.ActivityPrescriptionBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrescriptionActivity : BaseActivity(), AdapterView.OnItemClickListener {

    lateinit var binding: ActivityPrescriptionBinding
    private val prescriptionViewModel: PrescriptionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        attachObserver()
    }

    private fun attachObserver() {
        prescriptionViewModel.medicationListLiveData.observe(this) { resoruceState ->
            when (resoruceState.state) {
                ResourceState.LOADING -> {
                }

                ResourceState.SUCCESS -> {
                    resoruceState.data?.entityList?.let {
                        loadAdapter(it)
                    }
                }

                ResourceState.ERROR -> {

                }
            }
        }
    }

    private fun loadAdapter(data: ArrayList<MedicationResponse>) {
        val adapter = MedicationSearchAdapter(binding.root.context)
        adapter.setData(data)
        binding.searchView.setAdapter(adapter)
        binding.searchView.showDropDown()
    }

    private fun initView() {
        binding.searchView.addTextChangedListener {
            if (it.isNullOrEmpty()) {
                // default showing all medicines
            } else {
                prescriptionViewModel.searchMedicationByName(it.toString())
            }
        }
        binding.searchView.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        prescriptionViewModel.medicationListLiveData.value?.data?.entityList?.let { medicationList ->
            val medicationResponse = medicationList[position]
            binding.searchView.setText(medicationResponse.name)
        }
    }
}