package com.medtroniclabs.spice.ui.medicalreview.prescription

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.hideKeyboard
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.convertListToString
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.HIV
import com.medtroniclabs.spice.common.DefinedParams.SearchLengthPrescription
import com.medtroniclabs.spice.data.MedicationRequestObject
import com.medtroniclabs.spice.data.MedicationResponse
import com.medtroniclabs.spice.data.Prescription
import com.medtroniclabs.spice.data.RemovePrescriptionRequest
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.ActivityPrescriptionBinding
import com.medtroniclabs.spice.databinding.PrescriptionGroupingLayoutBinding
import com.medtroniclabs.spice.databinding.RowDiscontinedMedicationBinding
import com.medtroniclabs.spice.databinding.RowPrescriptionBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.DeleteReasonDialog
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.SignatureDialogFragment
import com.medtroniclabs.spice.ui.medicalreview.SignatureListener
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrescriptionActivity : BaseActivity(), AdapterView.OnItemClickListener, View.OnClickListener,
    SignatureListener {

    lateinit var binding: ActivityPrescriptionBinding
    private val prescriptionViewModel: PrescriptionViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrescriptionBinding.inflate(layoutInflater)
        setMainContentView(binding.root, true, title = getString(R.string.prescription))
        withNetworkCheck(connectivityManager,::initView,::finish)
        attachObserver()
        initListener()
        patientViewModel.setUserJourney(AnalyticsDefinedParams.PERSCRIPTIONSCREEN)
    }

    private fun initListener() {
        binding.btnPrescribe.safeClickListener(this)
        binding.tvDiscontinuedMedication.safeClickListener(this)
        binding.btnRenewAll.safeClickListener(this)
    }

    private fun attachObserver() {

        prescriptionViewModel.prescriptionListLiveData.observe(this) { resourceState ->
            when (resourceState.state) {

                ResourceState.LOADING -> {}

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState?.data?.let { data ->
                        if (data.size > 0) {
                            binding.btnRenewAll.visible()
                        } else {
                            binding.btnRenewAll.gone()
                        }
                        prescriptionViewModel.updateMedicationList(
                            prescriptionViewModel.constructMedicationRequestObjectList(ArrayList(data.filter { it.groupName.isNullOrEmpty() || it.groupUniqueId == null })),
                            true
                        )
                        prescriptionViewModel.updateMedicationGroupList(
                            prescriptionViewModel.constructMedicationRequestObjectList(ArrayList(data.filter { it.groupName!=null && it.groupUniqueId != null })),
                            true
                        )
                    }
                }

                ResourceState.ERROR -> { hideLoading() }
            }
        }

        prescriptionViewModel.medicationListLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                }

                ResourceState.SUCCESS -> {
                    resourceState.data?.let {
                        loadAdapter(it)
                    }
                }

                ResourceState.ERROR -> {}
            }
        }

        prescriptionViewModel.medicationGroupListLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    hideKeyboard(binding.root)
                    resourceState.data?.let { groupList ->
                        val list = ArrayList<MedicationRequestObject>()
                        groupList.forEach {
                            list.add(MedicationRequestObject(it))
                        }
                        prescriptionViewModel.updateMedicationGroupList(
                            medicationResponse = list,
                            false
                        )
                        prescriptionViewModel.selectedMedicationGroupLiveData.value?.let {
                            showMedicationGroupList(
                                it
                            )
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }

        prescriptionViewModel.selectedMedicationLiveData.observe(this) { list ->
            list?.let {
                hideKeyboard(binding.root)
                val selectedGroupMeds = prescriptionViewModel.selectedMedicationGroupLiveData.value ?: emptyList()
                binding.btnPrescribe.isEnabled = list.size > 0 || selectedGroupMeds.isNotEmpty()
                if (list.size > 0) {
                    binding.tvNoMedicationDataFound.gone()
                } else {
                    binding.tvNoMedicationDataFound.visible()
                }
                showMedicationList(list)
            }
        }

        prescriptionViewModel.selectedMedicationGroupLiveData.observe(this) { list ->
            list?.let {
                hideKeyboard(binding.root)
                val selectedMeds = prescriptionViewModel.selectedMedicationLiveData.value ?: emptyList()
                binding.btnPrescribe.isEnabled = list.isNotEmpty() || selectedMeds.isNotEmpty()
                showMedicationGroupList(list)
            }
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
                        data.id?.let {
                            prescriptionViewModel.getPrescriptionList(data)
                        } ?: kotlin.run {
                            hideLoading()
                        }
                    } ?: kotlin.run {
                        hideLoading()
                    }
                }
            }
        }

        prescriptionViewModel.createPrescriptionLiveData.observe(this) { resource ->
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
                        val intent = Intent()
                        if (map.containsKey(DefinedParams.EncounterId)) {
                            val value = map[DefinedParams.EncounterId]
                            if (value is String) {
                                intent.putExtra(DefinedParams.EncounterId, value)
                                intent.putExtra(DefinedParams.PRESCRIPTION,true)

                            }
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                        supportFragmentManager.setFragmentResult(
                            MedicalReviewDefinedParams.PRESCRIPTION, bundleOf(
                                MedicalReviewDefinedParams.Notes to true)
                        )
                    }
                }
            }
        }

        prescriptionViewModel.removePrescriptionLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    patientViewModel.patientDetailsLiveData.value?.data?.let {
                        prescriptionViewModel.getPrescriptionList(it)
                    }
                    binding.tvDiscontinuedMedication.text =
                        getText(R.string.view_discontinued_medication)
                    binding.cardDiscontinuedPrescriptionContainer.gone()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }

        prescriptionViewModel.discontinuedPrescriptionListLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let {
                        processDiscontinuedMedication(it)
                    }

                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }

        prescriptionViewModel.frequencyListLiveDate.observe(this) { _ ->
            prescriptionViewModel.patientId?.let {
                patientViewModel.getPatients(it)
            }
        }

        prescriptionViewModel.instructionListLiveDate.observe(this) { _ ->
            prescriptionViewModel.patientId?.let {
                patientViewModel.getPatients(it)
            }
        }
    }

    private fun processDiscontinuedMedication(prescriptions: ArrayList<Prescription>) {
        binding.tvDiscontinuedMedication.text = getString(R.string.hide_discontinued_medication)
        binding.cardDiscontinuedPrescriptionContainer.visible()
        if (prescriptions.isNotEmpty()) {
            binding.discontinuedMedicationHolder.visible()
            binding.tvNoDataFound.gone()
            showDiscontinuedMedication(prescriptions)
        } else {
            binding.discontinuedMedicationHolder.gone()
            binding.tvNoDataFound.visible()
        }
    }

    private fun showDiscontinuedMedication(prescriptions: ArrayList<Prescription>) {
        binding.discontinuedMedicationHolder.removeAllViews()
        prescriptions.forEachIndexed { index, data ->
            val discontinuedMedicationBinding =
                RowDiscontinedMedicationBinding.inflate(LayoutInflater.from(this))
            discontinuedMedicationBinding.tvMedicationName.text = data.medicationName
            discontinuedMedicationBinding.tvFrequency.text = getFrequencyName(
                data.frequency, getString(
                    R.string.hyphen_symbol
                )
            )
            discontinuedMedicationBinding.tvInstructions.text = data.instruction ?: getString(R.string.hyphen_symbol)
            discontinuedMedicationBinding.tvQuantity.text =
                ((data.prescribedDays ?: 0) * data.frequency).toString()
            discontinuedMedicationBinding.tvPrescribedDays.text = data.prescribedDays.toString()
            data.discontinuedDate?.let { endDate ->
                discontinuedMedicationBinding.tvDiscontinuedOn.text = DateUtils.convertDateFormat(
                    endDate,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DateUtils.DATE_ddMMyyyy
                )
            }
            if (index == prescriptions.size - 1) {
                discontinuedMedicationBinding.viewDiscontinuedMedication.gone()
            } else {
                discontinuedMedicationBinding.viewDiscontinuedMedication.visible()
            }
            binding.discontinuedMedicationHolder.addView(discontinuedMedicationBinding.root)
        }
    }

    private fun showMedicationGroupList(list: ArrayList<MedicationRequestObject>) {
        binding.llPrescriptionGroupContainer.removeAllViews()
        binding.llPrescriptionGroupContainer.visible()
        val groupNames = list.map { it.medicationResponse.groupName }.distinct()
        groupNames.forEach { groupName ->
            val medicationGroupBinding =
                PrescriptionGroupingLayoutBinding.inflate(LayoutInflater.from(this))
            medicationGroupBinding.prescriptionGroupName.text = groupName
            list.filter { it.medicationResponse.groupName == groupName }
                .forEachIndexed { index, data ->
                    val prescriptionBinding =
                        RowPrescriptionBinding.inflate(LayoutInflater.from(this))

                    if (data.medicationResponse.prescriptionId != null && !data.medicationResponse.isEditable) {
                        medicationGroupBinding.ivGroupEdit.visible()
                        medicationGroupBinding.ivGroupRemove.setImageResource(R.drawable.icon_delete_red)
                    } else {
                        medicationGroupBinding.ivGroupEdit.gone()
                        if (data.medicationResponse.isEditable) {
                            medicationGroupBinding.ivGroupRemove.setImageResource(R.drawable.icon_reset_grey)
                        } else {
                            medicationGroupBinding.ivGroupRemove.setImageResource(R.drawable.icon_remove_blue)
                        }
                    }

                    prescriptionBinding.ivRemoveMedication.gone()
                    prescriptionBinding.ivEditMedication.gone()
                    prescriptionBinding.medicationName.text = data.medicationResponse.name
                    prescriptionBinding.etPrescribedDays.addTextChangedListener { prescribedDays ->
                        data.medicationResponse.prescribedDays =
                            prescribedDays.toString().toLongOrNull()
                        prescriptionBinding.etQuantity.setText(
                            calculateQuantity(
                                data
                            )
                        )
                    }
                    if (data.medicationResponse.prescribedDays != null) {
                        prescriptionBinding.etPrescribedDays.setText(data.medicationResponse.prescribedDays.toString())
                    }
                    if (data.medicationResponse.prescribedSince != null && !data.medicationResponse.isEditable) {
                        prescriptionBinding.tvPrescribedSince.text = DateUtils.convertDateFormat(
                            data.medicationResponse.prescribedSince!!,
                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                            DateUtils.DATE_ddMMyyyy
                        )
                    } else {
                        prescriptionBinding.tvPrescribedSince.text =
                            getString(R.string.hyphen_symbol)
                    }
                    if (data.medicationResponse.showErrorMessage) {
                        prescriptionBinding.tvMedicineErrorMessage.visible()
                    } else {
                        prescriptionBinding.tvMedicineErrorMessage.gone()
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
                                prescriptionBinding.etQuantity.setText(
                                    calculateQuantity(
                                        data
                                    )
                                )
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }
                        }

                    val instructionAdapter = CustomSpinnerAdapter(prescriptionBinding.root.context, false)
                    instructionAdapter.setData(prescriptionViewModel.getInstructionMap())
                    prescriptionBinding.instruction.adapter = instructionAdapter
                    data.medicationResponse.instruction?.let { selectedMap ->
                        if (selectedMap.isNotEmpty()) {
                            prescriptionBinding.instruction.setSelection(
                                getInstructionPosition(
                                    prescriptionViewModel.getInstructionMap(),
                                    selectedMap
                                )
                            )
                        }
                    }

                    prescriptionBinding.instruction.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val selectedItem = instructionAdapter.getData(position = position)
                                selectedItem?.let { map ->
                                    if (data.medicationResponse.instruction.isNullOrEmpty()) {
                                        data.medicationResponse.instruction = null
                                    }
                                    data.medicationResponse.instruction = map.get(DefinedParams.Value) as? String
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }
                        }

                    if (data.medicationResponse.prescriptionId != null && !data.medicationResponse.isEditable) {
                        prescriptionBinding.etPrescribedDays.isEnabled = false
                        prescriptionBinding.frequency.isEnabled = false
                        prescriptionBinding.instruction.isEnabled = false
                    } else {
                        prescriptionBinding.etPrescribedDays.isEnabled = true
                        prescriptionBinding.frequency.isEnabled = true
                        prescriptionBinding.instruction.isEnabled = true
                        prescriptionBinding.ivEditMedication.gone()
                    }

                    medicationGroupBinding.ivGroupRemove.setOnClickListener {
                        data.medicationResponse.prescriptionId?.let { prescriptionId ->
                            if (data.medicationResponse.isEditable) {
                                resetGroupMedicationData(data)
                            } else {
                                val dialog = DeleteReasonDialog.newInstance(
                                    this,
                                    getString(R.string.confirmation),
                                    true,
                                    Pair(getString(R.string.ok), getString(R.string.cancel)),
                                    callback = { isPositiveResult, reason ->
                                        if (isPositiveResult) {
                                            if (CommonUtils.isCommunity()){
                                                val medicationList  = data.medicationResponse.groupName?.let { groupName ->
                                                    prescriptionViewModel.selectedMedicationGroupLiveData.value?.filter { it.medicationResponse.groupName == groupName }
                                                }?.mapNotNull { it.medicationResponse.prescriptionId }
                                                val removeMedicationsList = ArrayList<RemovePrescriptionRequest>()
                                                medicationList?.forEach {
                                                    removeMedicationsList.add(RemovePrescriptionRequest(
                                                        prescriptionId = it,
                                                        provenance = ProvanceDto(),
                                                        discontinuedReason = reason,
                                                    ))
                                                }
                                                if (medicationList?.isNotEmpty() == true){
                                                    prescriptionViewModel.removeCommunityPrescription(removeMedicationsList)
                                                }
                                            } else {
                                                prescriptionViewModel.removePrescription(
                                                    prescriptionId,
                                                    reason
                                                )
                                            }
                                        }
                                    },
                                    message = Pair(getString(R.string.delete_confirmation), null)
                                )
                                dialog.show(supportFragmentManager, DeleteReasonDialog.TAG)
                            }
                        } ?: kotlin.run {
                            val tempList =
                                prescriptionViewModel.selectedMedicationGroupLiveData.value
                                    ?: ArrayList()
                            data.medicationResponse.groupName?.let { group ->
                                tempList.removeAll { list -> list.medicationResponse.groupName == group }
                                prescriptionViewModel.selectedMedicationGroupLiveData.value =
                                    tempList
                            }
                        }
                    }

                    medicationGroupBinding.ivGroupEdit.setOnClickListener {
                        val tempList = prescriptionViewModel.selectedMedicationGroupLiveData.value ?: ArrayList()
                        data.medicationResponse.groupName?.let { group ->
                            tempList.forEach { item ->
                                if (item.medicationResponse.groupName == group) {
                                    item.medicationResponse.isEditable = true
                                }
                            }
                            prescriptionViewModel.selectedMedicationGroupLiveData.value = tempList
                        }
                    }

                    if (index == list.filter { it.medicationResponse.groupName == groupName }.size - 1) {
                        prescriptionBinding.viewMedication.gone()
                    } else {
                        prescriptionBinding.viewMedication.visible()
                    }
                    medicationGroupBinding.llPrescriptionGroupHolder.addView(prescriptionBinding.root)
                }
            binding.llPrescriptionGroupContainer.addView(medicationGroupBinding.root)
        }
    }


    private fun showMedicationList(list: ArrayList<MedicationRequestObject>) {
        binding.llPrescriptionHolder.removeAllViews()
        list.forEachIndexed { index, data ->
            val prescriptionBinding = RowPrescriptionBinding.inflate(LayoutInflater.from(this))
            prescriptionBinding.medicationName.text = data.medicationResponse.name
            prescriptionBinding.etPrescribedDays.addTextChangedListener { prescribedDays ->
                data.medicationResponse.prescribedDays = prescribedDays.toString().toLongOrNull()
                prescriptionBinding.etQuantity.setText(calculateQuantity(data))
            }
            if (data.medicationResponse.prescribedDays != null) {
                prescriptionBinding.etPrescribedDays.setText(data.medicationResponse.prescribedDays.toString())
            }

            if (data.medicationResponse.prescribedSince != null && !data.medicationResponse.isEditable) {
                prescriptionBinding.tvPrescribedSince.text = DateUtils.convertDateFormat(
                    data.medicationResponse.prescribedSince!!,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DateUtils.DATE_ddMMyyyy
                )
            } else {
                prescriptionBinding.tvPrescribedSince.text = getString(R.string.hyphen_symbol)
            }
            if (data.medicationResponse.showErrorMessage) {
                prescriptionBinding.tvMedicineErrorMessage.visible()
            } else {
                prescriptionBinding.tvMedicineErrorMessage.gone()
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
                        prescriptionBinding.etQuantity.setText(calculateQuantity(data))
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }

            val instructionAdapter = CustomSpinnerAdapter(prescriptionBinding.root.context, false)
            instructionAdapter.setData(prescriptionViewModel.getInstructionMap())
            prescriptionBinding.instruction.adapter = instructionAdapter
            data.medicationResponse.instruction?.let { selectedMap ->
                if (selectedMap.isNotEmpty()) {
                    prescriptionBinding.instruction.setSelection(
                        getInstructionPosition(
                            prescriptionViewModel.getInstructionMap(),
                            selectedMap
                        )
                    )
                }
            }

            prescriptionBinding.instruction.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedItem = instructionAdapter.getData(position = position)
                        selectedItem?.let { map ->
                            if (data.medicationResponse.instruction.isNullOrEmpty()) {
                                data.medicationResponse.instruction = null
                            }
                            data.medicationResponse.instruction = map[DefinedParams.Value] as? String
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }

            if (data.medicationResponse.prescriptionId != null && !data.medicationResponse.isEditable) {
                prescriptionBinding.etPrescribedDays.isEnabled = false
                prescriptionBinding.frequency.isEnabled = false
                prescriptionBinding.instruction.isEnabled = false
                prescriptionBinding.ivEditMedication.visible()
                prescriptionBinding.ivRemoveMedication.setImageResource(R.drawable.icon_delete_red)
            } else {
                prescriptionBinding.etPrescribedDays.isEnabled = true
                prescriptionBinding.frequency.isEnabled = true
                prescriptionBinding.instruction.isEnabled = true
                prescriptionBinding.ivEditMedication.gone()
                if (data.medicationResponse.isEditable) {
                    prescriptionBinding.ivRemoveMedication.setImageResource(R.drawable.icon_reset_grey)
                } else {
                    prescriptionBinding.ivRemoveMedication.setImageResource(R.drawable.icon_remove_blue)
                }
            }
            prescriptionBinding.ivRemoveMedication.setOnClickListener {
                data.medicationResponse.prescriptionId?.let { prescriptionId ->
                    if (data.medicationResponse.isEditable) {
                        resetDataInitialData(data)
                    } else {
                        val dialog = DeleteReasonDialog.newInstance(
                            this,
                            getString(R.string.confirmation),
                            true,
                            Pair(getString(R.string.ok), getString(R.string.cancel)),
                            callback = { isPositiveResult, reason ->
                                if (isPositiveResult) {
                                    if (CommonUtils.isCommunity()) {
                                        val removeMedicationsList =
                                            ArrayList<RemovePrescriptionRequest>()
                                        removeMedicationsList.add(
                                            RemovePrescriptionRequest(
                                                prescriptionId = prescriptionId,
                                                provenance = ProvanceDto(),
                                                discontinuedReason = reason,
                                            )
                                        )
                                        prescriptionViewModel.removeCommunityPrescription(
                                            removeMedicationsList
                                        )
                                    } else {
                                        prescriptionViewModel.removePrescription(
                                            prescriptionId,
                                            reason
                                        )
                                    }

                                }
                            },
                            message = Pair(getString(R.string.delete_confirmation), null)
                        )
                        dialog.show(supportFragmentManager, DeleteReasonDialog.TAG)
                    }
                } ?: kotlin.run {
                    val tempList =
                        prescriptionViewModel.selectedMedicationLiveData.value ?: ArrayList()
                    tempList.remove(data)
                    prescriptionViewModel.selectedMedicationLiveData.value = tempList
                }
            }

            prescriptionBinding.ivEditMedication.setOnClickListener {
                data.medicationResponse.isEditable = true
                val tempList = prescriptionViewModel.selectedMedicationLiveData.value ?: ArrayList()
                prescriptionViewModel.selectedMedicationLiveData.value = tempList
            }
            if (index == list.size - 1) {
                prescriptionBinding.viewMedication.gone()
            } else {
                prescriptionBinding.viewMedication.visible()
            }

            binding.llPrescriptionHolder.addView(prescriptionBinding.root)
        }
    }

    private fun resetDataInitialData(data: MedicationRequestObject) {
        val actualPrescription =
            prescriptionViewModel.prescriptionListLiveData.value?.data?.filter { it.prescriptionId == data.medicationResponse.prescriptionId }
        if (!actualPrescription.isNullOrEmpty()) {
            data.medicationResponse =
                prescriptionViewModel.constructMedicationRequestObject(actualPrescription[0])
            data.medicationResponse.isEditable = false
        }
        val tempList = prescriptionViewModel.selectedMedicationLiveData.value ?: ArrayList()
        prescriptionViewModel.selectedMedicationLiveData.value = tempList
    }

    private fun resetGroupMedicationData(data: MedicationRequestObject) {
        val groups = prescriptionViewModel.selectedMedicationGroupLiveData.value
            ?.filter { it.medicationResponse.groupName == data.medicationResponse.groupName }

        val prescriptionIdsInGroups = groups?.mapNotNull { it.medicationResponse.prescriptionId }?.toSet() ?: emptySet()

        val prescriptionList = prescriptionViewModel.prescriptionListLiveData.value?.data
            ?.filter { it.groupName == data.medicationResponse.groupName && it.prescriptionId in prescriptionIdsInGroups }
            ?: emptyList()

        prescriptionList.forEach { item ->
            groups?.forEach { group ->
                if (item.prescriptionId == group.medicationResponse.prescriptionId) {
                    group.medicationResponse = prescriptionViewModel.constructMedicationRequestObject(item)
                    group.medicationResponse.isEditable = false
                }
            }
        }

        prescriptionViewModel.selectedMedicationGroupLiveData.value =
            prescriptionViewModel.selectedMedicationGroupLiveData.value ?: ArrayList()
    }



    private fun getFrequencyPosition(
        frequencyMap: ArrayList<Map<String, Any>>,
        selectedMap: Map<String, Any>
    ): Int {
        frequencyMap.forEachIndexed { index, map ->
            if (map[DefinedParams.ID] == selectedMap[DefinedParams.ID]) {
                return index
            }
        }
        return 0
    }

    private fun getInstructionPosition(
        frequencyMap: ArrayList<Map<String, Any>>,
        selectedValue: String
    ): Int {
        frequencyMap.forEachIndexed { index, map ->
            if (map[DefinedParams.Value] == selectedValue) {
                return index
            }
        }
        return 0
    }

    private fun calculateQuantity(
        data: MedicationRequestObject
    ): String {
        val frequency = data.medicationResponse.selectedMap?.get(DefinedParams.Frequency) as? Int?
        if (data.medicationResponse.prescribedDays != null && frequency != null) {
            data.medicationResponse.quantity = data.medicationResponse.prescribedDays!! * frequency
        } else {
            data.medicationResponse.quantity = 0
        }
        return data.medicationResponse.quantity.toString()
    }

    private fun loadAdapter(data: ArrayList<MedicationResponse>) {
        val adapter = MedicationSearchAdapter(binding.root.context)
        adapter.setData(data)
        binding.searchView.setAdapter(adapter)
        binding.searchView.showDropDown()
    }

    private fun initView() {
        patientViewModel.encounterId = intent.getStringExtra(DefinedParams.EncounterId)
        prescriptionViewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
        prescriptionViewModel.getFrequencyList()
        prescriptionViewModel.getInstructionList()
        binding.searchView.addTextChangedListener {
            if (it.isNullOrEmpty()) {
                // default showing all medicines
            } else {
                if (it.length > SearchLengthPrescription) {
                    prescriptionViewModel.searchMedicationByName(it.toString())
                }
            }
        }
        binding.searchView.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        prescriptionViewModel.medicationListLiveData.value?.data?.let { medicationList ->
            val medicationResponse = medicationList[position]
            if (CommonUtils.isCommunity() && medicationResponse.isGroup) {
                medicationResponse.groupName?.let { name ->
                    prescriptionViewModel.getMedicationGroupByName(
                        name
                    )
                }
            } else {
                val list = ArrayList<MedicationRequestObject>()
                list.add(MedicationRequestObject(medicationResponse))
                prescriptionViewModel.updateMedicationList(
                    medicationResponse = list,
                    false
                )
            }
            binding.searchView.setText("")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnPrescribe.id -> {
                patientViewModel.setUserJourney(AnalyticsDefinedParams.DISPENSEMEDICATION)
                val status = checkValidation()
                if (status) {
                    val list =
                        prescriptionViewModel.selectedMedicationLiveData.value?.filter { ((it.medicationResponse.prescriptionId != null && it.medicationResponse.isEditable && it.medicationResponse.prescribedDays != null && it.medicationResponse.prescribedDays != 0L) || (it.medicationResponse.prescriptionId == null && it.medicationResponse.prescribedDays != null && it.medicationResponse.prescribedDays != 0L)) }
                    val groupList =
                        prescriptionViewModel.selectedMedicationGroupLiveData.value?.filter { ((it.medicationResponse.prescriptionId != null && it.medicationResponse.isEditable && it.medicationResponse.prescribedDays != null && it.medicationResponse.prescribedDays != 0L) || (it.medicationResponse.prescriptionId == null && it.medicationResponse.prescribedDays != null && it.medicationResponse.prescribedDays != 0L)) }
                    if ((list != null && list.isEmpty()) && ( groupList != null && groupList.isEmpty())) {
                        showErrorDialogue(
                            getString(R.string.alert),
                            getString(R.string.no_new_medicines_prescribed)
                        ) {

                        }
                    } else {
                        SignatureDialogFragment.newInstance(this)
                            .show(supportFragmentManager, SignatureDialogFragment.TAG)
                    }
                }
            }

            binding.tvDiscontinuedMedication.id -> {
                if (binding.tvDiscontinuedMedication.text.toString() == getString(R.string.hide_discontinued_medication)) {
                    binding.tvDiscontinuedMedication.text =
                        getText(R.string.view_discontinued_medication)
                    patientViewModel.setUserJourney(AnalyticsDefinedParams.HIDEDISCONTINUEDMEDICATION)
                    binding.cardDiscontinuedPrescriptionContainer.gone()
                } else {
                    patientViewModel.patientDetailsLiveData.value?.data?.let {
                        prescriptionViewModel.getPrescriptionList(it, false)
                    }
                    patientViewModel.setUserJourney(AnalyticsDefinedParams.VIEWDISCONTINUEDMEDICATION)
                }
            }

            binding.btnRenewAll.id -> {
                prescriptionViewModel.selectedMedicationLiveData.value?.filter { it.medicationResponse.prescriptionId != null }
                    ?.map { it.medicationResponse.isEditable = true }
                prescriptionViewModel.selectedMedicationLiveData.value =
                    prescriptionViewModel.selectedMedicationLiveData.value

                prescriptionViewModel.selectedMedicationGroupLiveData.value?.filter { it.medicationResponse.prescriptionId != null }
                    ?.map { it.medicationResponse.isEditable = true }
                prescriptionViewModel.selectedMedicationGroupLiveData.value =
                    prescriptionViewModel.selectedMedicationGroupLiveData.value
            }
        }

    }


    private fun checkValidation(): Boolean {

        val invalidList = ArrayList<MedicationRequestObject>()

        prescriptionViewModel.selectedMedicationLiveData.value?.forEach { data ->
            if (data.medicationResponse.prescribedDays == null || data.medicationResponse.prescribedDays == 0L || (data.medicationResponse.instruction == DefinedParams.DefaultIDLabel && !(data.medicationResponse.prescriptionId != null && !data.medicationResponse.isEditable))) {
                data.medicationResponse.showErrorMessage = true
                invalidList.add(data)
            } else {
                data.medicationResponse.showErrorMessage = false
            }

        }

        prescriptionViewModel.selectedMedicationLiveData.value =
            prescriptionViewModel.selectedMedicationLiveData.value

        prescriptionViewModel.selectedMedicationGroupLiveData.value?.forEach { data ->
            if (data.medicationResponse.prescribedDays == null || data.medicationResponse.prescribedDays == 0L || (data.medicationResponse.instruction == DefinedParams.DefaultIDLabel && !(data.medicationResponse.prescriptionId != null && !data.medicationResponse.isEditable))) {
                data.medicationResponse.showErrorMessage = true
                invalidList.add(data)
            } else {
                data.medicationResponse.showErrorMessage = false
            }

        }

        prescriptionViewModel.selectedMedicationGroupLiveData.value =
            prescriptionViewModel.selectedMedicationGroupLiveData.value

        return invalidList.size == 0
    }

    override fun applySignature(signature: Bitmap) {
        updatePrescriptions(signature)
    }

    private fun updatePrescriptions(signatureBitmap: Bitmap) {
        patientViewModel.patientDetailsLiveData.value?.data?.let { data ->
            prescriptionViewModel.patientId?.let { patientId ->
                val prescriptionCreateList = ArrayList<MedicationRequestObject>()
                prescriptionViewModel.selectedMedicationLiveData.value?.filter { ((it.medicationResponse.prescriptionId != null && it.medicationResponse.isEditable && it.medicationResponse.prescribedDays != null && it.medicationResponse.prescribedDays != 0L) || (it.medicationResponse.prescriptionId == null && it.medicationResponse.prescribedDays != null && it.medicationResponse.prescribedDays != 0L)) }
                    .let {list->
                        list?.forEach {
                            prescriptionCreateList.add(it)
                        }
                    }
                prescriptionViewModel.selectedMedicationGroupLiveData.value?.filter { ((it.medicationResponse.prescriptionId != null && it.medicationResponse.isEditable && it.medicationResponse.prescribedDays != null && it.medicationResponse.prescribedDays != 0L) || (it.medicationResponse.prescriptionId == null && it.medicationResponse.prescribedDays != null && it.medicationResponse.prescribedDays != 0L)) }
                    .let {list ->
                        list?.forEach {
                            prescriptionCreateList.add(it)
                        }
                    }
                if (prescriptionCreateList.any { it.medicationResponse.category?.name?.equals(
                        HIV, true) == true }){
                    val medications = prescriptionCreateList.filter { it.medicationResponse.category?.name?.equals(
                        HIV, true) == true
                           // && it.medicationResponse.regimenLine != null
                    }
                        .map { it.medicationResponse.name }
                    val medicationsRegimen = prescriptionCreateList
                        .filter { it.medicationResponse.category?.name?.equals(HIV, true) == true }
                        .mapNotNull { it.medicationResponse.regimenLine?.toIntOrNull() } // List<Int>
                        .maxOrNull()
                    val prescriptionId =
                        prescriptionCreateList.any { it.medicationResponse.prescriptionId != null }
                    ReasonForChangeDialogFragment.newInstance(
                        name = convertListToString(ArrayList(medications.filterNotNull())),
                        regimen = medicationsRegimen,
                        prescribedMedicine = prescriptionId,
                        callback = object : ReasonForChangeDialogFragment.ReasonChangeCallback {
                            override fun onReasonProvided(reason: String) {
                                prescriptionCreateList.let { list ->
                                    prescriptionViewModel.createPrescription(
                                        signatureBitmap,
                                        CommonUtils.getFilePath(
                                            patientId,
                                            context = this@PrescriptionActivity
                                        ),
                                        ArrayList(list),
                                        data,
                                        patientViewModel.encounterId,
                                        reason,
                                        medicationsRegimen?.apply { this+1 } ?: 1
                                    )
                                }
                            }
                        }
                    ).show(supportFragmentManager, ReasonForChangeDialogFragment.TAG)

                } else {
                    prescriptionCreateList.let { list ->
                        prescriptionViewModel.createPrescription(
                            signatureBitmap,
                            CommonUtils.getFilePath(
                                patientId,
                                context = this
                            ),
                            ArrayList(list),
                            data,
                            patientViewModel.encounterId,
                            null
                        )
                    }
                }
            }
        }
    }

    private fun getFrequencyName(frequency: Int, hypen: String): String {
        prescriptionViewModel.frequencyListLiveDate.value?.data?.let { list ->
            val selectedFrequency = list.filter { it.frequency == frequency }
            return if (selectedFrequency.isNotEmpty()) {
                selectedFrequency[0].name
            } else {
                hypen
            }
        } ?: kotlin.run {
            return hypen
        }
    }
}