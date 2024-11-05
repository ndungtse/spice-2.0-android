package com.medtroniclabs.spice.ui.medicalreview.pharmacist.activity

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.DispenseUpdatePrescriptionRequest
import com.medtroniclabs.spice.data.DispenseUpdateRequest
import com.medtroniclabs.spice.data.DispenseUpdateResponse
import com.medtroniclabs.spice.databinding.ActivityNcdPharmacistBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.dialog.GeneralSuccessDialog
import com.medtroniclabs.spice.ui.medicalreview.pharmacist.fragment.NCDPharmacistFragment
import com.medtroniclabs.spice.ui.medicalreview.pharmacist.fragment.NCDPrescriptionHistoryDialogFragment
import com.medtroniclabs.spice.ui.medicalreview.pharmacist.fragment.NCDQuantityDifferenceDialogueFragment
import com.medtroniclabs.spice.ui.medicalreview.pharmacist.viewModel.NCDPharmacistViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDPharmacistActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityNcdPharmacistBinding
    private val patientDetailViewModel: PatientDetailViewModel by viewModels()
    private val viewModel: NCDPharmacistViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdPharmacistBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.pharmacist_title),
            homeAndBackVisibility = Pair(false, true),
        )
        initView()
        getPatientDetails()
        attachObserver()
    }


    private fun initView() {
        viewModel.patient_visit_id = intent.getStringExtra(NCDMRUtil.EncounterReference)
        binding.bottomView.btnDone.safeClickListener(this)
        binding.bottomView.btnCancel.safeClickListener(this)
        replaceFragmentInId<NCDPharmacistFragment>(binding.prescriptionRefillFragment.id)
    }


    private fun attachObserver() {

        viewModel.updatePrescriptionLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        showErrorDialogue(getString(R.string.error), it) {}
                    }
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data.let { data ->
                        showDialogue(data)
                    }
                }
            }
        }

        patientDetailViewModel.patientDetailsLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    loadPatientInfo(resourceState.data)
                    viewModel.getPrescriptionDispenseList(
                        DispenseUpdateRequest(patientReference = patientDetailViewModel.getPatientId())
                    )
                    viewModel.patientReference = patientDetailViewModel.getPatientId()
                    viewModel.memberId = patientDetailViewModel.getPatientFHIRId()
                    viewModel.last_refill_visit_id = patientDetailViewModel.getLastRefillVisitId()
                }
            }
        }

        viewModel.prescriptionDispenseLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let {
                        if (it.size > 0) {
                            binding.bottomView.btnDone.visibility = View.VISIBLE
                        } else {
                            binding.bottomView.btnDone.visibility = View.GONE
                        }
                    } ?: kotlin.run {
                        binding.bottomView.btnDone.visibility = View.GONE
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()

                }
            }
        }

        viewModel.prescriptionDispenseHistoryLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        showErrorDialogue(getString(R.string.error), message) {}
                    }
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { historyList ->
                        NCDPrescriptionHistoryDialogFragment.newInstance(historyList)
                            .show(supportFragmentManager, NCDPrescriptionHistoryDialogFragment.TAG)
                    }
                }
            }
        }

    }

    private fun loadPatientInfo(data: PatientListRespModel?) {
        data?.let {
            binding.tvProgramId.text = it.programId ?: "-"
            binding.tvNationalId.text = it.identityValue ?: "-"
            data.firstName?.let {
                val text = StringConverter.appendTexts(firstText = it, data.lastName)
                setTitle(
                    StringConverter.appendTexts(
                        firstText = text,
                        data.age.toString(),
                        data.gender,
                        separator = "-"
                    )
                )
            }
            data.prescribedDetails?.let { prescriberDetails ->
                val name = StringBuffer()
                prescriberDetails.firstName?.let { firstName ->
                    name.append(firstName)
                }
                prescriberDetails.lastName?.let { lastName ->
                    name.append(" $lastName")
                }
                binding.tvPrescriberName.text = name.ifBlank { getString(R.string.separator_hyphen) }
                prescriberDetails.phoneNumber?.let { prescriberNumber ->
                    binding.tvPrescriberNumber.text = prescriberNumber
                }
                prescriberDetails.lastRefillDate?.let { lastRefillDate ->
                    DateUtils.convertDateTimeToDate(
                        lastRefillDate,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DateUtils.DATE_FORMAT_ddMMMyyyy
                    ).let { formattedDate ->
                        val spannableString = SpannableString(formattedDate)
                        spannableString.setSpan(UnderlineSpan(), 0, formattedDate.length, 0)
                        binding.tvLastRefillDate.text = spannableString
                        binding.tvLastRefillDate.setTextAppearance(R.style.MR_Field_Style)
                        binding.tvLastRefillDate.setTextColor(this.getColor(R.color.cobalt_blue))
                        binding.tvLastRefillDate.safeClickListener {
                            viewModel.getDispensePrescriptionHistory(
                                DispenseUpdateRequest(
                                    patientVisitId = viewModel.last_refill_visit_id ?: "",
                                    patientReference = patientDetailViewModel.getPatientId() ?: "",
                                    requestFrom = MenuConstants.DISPENSE
                                )
                            )
                        }
                    }
                }
            }
        }
    }


    private fun getPatientDetails() {
        intent?.let {
            patientDetailViewModel.origin = it.getStringExtra(DefinedParams.ORIGIN)
            it.getStringExtra(DefinedParams.FhirId)?.let { id ->
                patientDetailViewModel.getPatients(
                    id,
                    origin = patientDetailViewModel.origin?.lowercase()
                )
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.bottomView.btnDone.id -> {
                validateCountDifference()
            }

            binding.bottomView.btnCancel.id -> {
                finish()
            }
        }
    }

    private fun validateCountDifference() {
        viewModel.prescriptionDispenseLiveData.value?.data?.let { list ->
            val differedQuantityList =
                list.filter { it.prescriptionFilledDays != it.prescriptionRemainingDays }
            if (differedQuantityList.isNotEmpty()) {
                NCDQuantityDifferenceDialogueFragment.newInstance()
                    .show(supportFragmentManager, NCDQuantityDifferenceDialogueFragment.TAG)
            } else {
                val filledValues =
                    list.filter { it.prescriptionFilledDays != null && it.prescriptionFilledDays != 0 }
                if (filledValues.isNotEmpty()) {
                    viewModel.patient_visit_id?.let { patientVisitId ->
                        viewModel.patientReference?.let { patient_refernce ->
                            viewModel.memberId?.let { memberId ->
                                viewModel.updateDispensePrescription(
                                    patientVisitId = viewModel.patient_visit_id ?: "",
                                    patientReference = viewModel.patientReference ?: "",
                                    memberId = viewModel.memberId ?: "",
                                    request = getReqBody()
                                )
                            }
                        }
                    }
                } else {
                    showErrorDialogue(
                        getString(R.string.error),
                        getString(R.string.days_are_required)
                    ) {}
                }
            }
        }
    }

    private fun getReqBody(): List<DispenseUpdatePrescriptionRequest> {
        val prescriptionList = ArrayList<DispenseUpdatePrescriptionRequest>()
        viewModel.prescriptionDispenseLiveData.value?.data?.forEach {
            prescriptionList.add(
                DispenseUpdatePrescriptionRequest(
                    medicationName = it.medicationName,
                    dosageFrequencyName = it.dosageFrequencyName,
                    prescriptionId = it.prescriptionId,
                    instructionNote = it.instructionNote,
                    prescriptionFilledDays = it.prescriptionFilledDays,
                    reason = it.discontinuedReason
                )
            )
        }
        return prescriptionList
    }

    private fun showDialogue(data: DispenseUpdateResponse?) {
        if (data != null) {
            GeneralSuccessDialog.newInstance(
                title = getString(R.string.prescription),
                message = getString(R.string.prescription_dispensed_successfully),
                okayButton = getString(R.string.done)
            ) { finish() }.show(supportFragmentManager, GeneralSuccessDialog.TAG)
        }
    }
}