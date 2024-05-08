package com.medtroniclabs.spice.ui.medicalreview.prescription

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.MedicationRequestObject
import com.medtroniclabs.spice.data.MedicationResponse
import com.medtroniclabs.spice.databinding.ActivityPrescriptionBinding
import com.medtroniclabs.spice.databinding.RowPrescriptionBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.SignatureDialogFragment
import com.medtroniclabs.spice.ui.medicalreview.SignatureListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrescriptionActivity : BaseActivity(), AdapterView.OnItemClickListener, View.OnClickListener,
    SignatureListener {

    lateinit var binding: ActivityPrescriptionBinding
    private val prescriptionViewModel: PrescriptionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrescriptionBinding.inflate(layoutInflater)
        setMainContentView(binding.root, true, title = getString(R.string.prescription))
        initView()
        attachObserver()
        initListener()
    }

    private fun initListener() {
        binding.btnPrescribe.safeClickListener(this)
    }

    private fun attachObserver() {
        prescriptionViewModel.medicationListLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                }

                ResourceState.SUCCESS -> {
                    resourceState.data?.entityList?.let {
                        loadAdapter(it)
                    }
                }

                ResourceState.ERROR -> {

                }
            }
        }

        prescriptionViewModel.selectedMedicationLiveDate.observe(this) { list ->
            list?.let {
                binding.btnPrescribe.isEnabled = list.size > 0
                showMedicationList(list)
            }
        }
    }

    private fun showMedicationList(list: ArrayList<MedicationRequestObject>) {
        binding.llPrescriptionHolder.removeAllViews()
        list.forEach { data ->
            val prescriptionBinding = RowPrescriptionBinding.inflate(LayoutInflater.from(this))
            prescriptionBinding.medicationName.text = data.medicationResponse.name
            prescriptionBinding.etPrescribedDays.addTextChangedListener { prescribedDays ->
                data.medicationResponse.prescribedDays = prescribedDays.toString().toLongOrNull()
                calculateQuantity(data, prescriptionBinding.etQuantity)
            }
            if (data.medicationResponse.prescribedDays != null) {
                prescriptionBinding.etPrescribedDays.setText(data.medicationResponse.prescribedDays.toString())
            }
            val adapter = CustomSpinnerAdapter(prescriptionBinding.root.context, false)
            adapter.setData(prescriptionViewModel.getFrequencyMap())
            prescriptionBinding.frequency.adapter = adapter
            data.medicationResponse.selectedMap?.let { selectedMap ->
                if (selectedMap.isNotEmpty()) {
                    prescriptionBinding.frequency.setSelection(
                        getFrequencyPosition(
                            prescriptionViewModel.getFrequencyMap(),
                            selectedMap
                        )
                    )
                }
            }
            prescriptionBinding.frequency.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedItem = adapter.getData(position = position)
                        selectedItem?.let { map ->
                            if (data.medicationResponse.selectedMap.isNullOrEmpty()) {
                                data.medicationResponse.selectedMap = HashMap()
                            }
                            data.medicationResponse.selectedMap?.putAll(map)
                        }
                        calculateQuantity(data, prescriptionBinding.etQuantity)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                }
            binding.llPrescriptionHolder.addView(prescriptionBinding.root)
        }
    }

    private fun getFrequencyPosition(
        frequencyMap: ArrayList<Map<String, Any>>,
        selectedMap: HashMap<String, Any>
    ): Int {
        frequencyMap.forEachIndexed { index, map ->
            if (map[DefinedParams.ID] == selectedMap[DefinedParams.ID]) {
                return index
            }
        }
        return 0
    }

    private fun calculateQuantity(
        data: MedicationRequestObject,
        quantityEditText: AppCompatEditText
    ) {
        val frequency = data.medicationResponse.selectedMap?.get(DefinedParams.Frequency) as? Int?
        if (data.medicationResponse.prescribedDays != null && frequency != null) {
            data.medicationResponse.quantity = data.medicationResponse.prescribedDays!! * frequency
        } else {
            data.medicationResponse.quantity = 0
        }
        quantityEditText.setText(data.medicationResponse.quantity.toString())
    }

    private fun loadAdapter(data: ArrayList<MedicationResponse>) {
        val adapter = MedicationSearchAdapter(binding.root.context)
        adapter.setData(data)
        binding.searchView.setAdapter(adapter)
        binding.searchView.showDropDown()
    }

    private fun initView() {
        prescriptionViewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
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
            prescriptionViewModel.updateMedicationList(medicationResponse = medicationResponse)
            binding.searchView.setText("")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnPrescribe.id -> {
                val status = checkValidation()
                if (status.first) {
                    SignatureDialogFragment.newInstance(this)
                        .show(supportFragmentManager, SignatureDialogFragment.TAG)
                }
            }
        }

    }

    private fun checkValidation(): Pair<Boolean, List<MedicationRequestObject>?> {
        val invalidList =
            prescriptionViewModel.selectedMedicationLiveDate.value?.filter { it.medicationResponse.prescribedDays == null || it.medicationResponse.prescribedDays == 0L }
        if (!invalidList.isNullOrEmpty()) {
            return Pair(false, invalidList)
        }
        return Pair(true, null)
    }

    override fun applySignature(signature: Bitmap) {
        updatePrescriptions(signature)
    }

    private fun updatePrescriptions(signatureBitmap: Bitmap) {

        prescriptionViewModel.patientId?.let { patientId ->
            prescriptionViewModel.selectedMedicationLiveDate.value?.let { list ->
                prescriptionViewModel.createPrescription(
                    signatureBitmap,
                    CommonUtils.getFilePath(
                        patientId,
                        context = this,
                        list
                    ),
                    list
                )
            }
        }
    }
}