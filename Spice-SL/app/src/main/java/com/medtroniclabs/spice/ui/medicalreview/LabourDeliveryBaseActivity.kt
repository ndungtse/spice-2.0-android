package com.medtroniclabs.spice.ui.medicalreview

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.data.MedicalReviewSummarySubmitRequest
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.ActivityMedicalReviewLabourDeliveryactivityBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.common.LabourOrDeliveryFragment
import com.medtroniclabs.spice.ui.dialog.MedicalReviewSuccessDialogFragment
import com.medtroniclabs.spice.ui.medicalreview.labourDelivery.LabourDeliveryViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.prescription.PrescriptionActivity
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MotherFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.MotherSummaryFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.NeonateFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.NeonateSummaryFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.ReferPatientFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.ReferPatientViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LabourDeliveryBaseActivity : BaseActivity(), View.OnClickListener, AncVisitCallBack {

    private lateinit var binding: ActivityMedicalReviewLabourDeliveryactivityBinding
    private val viewModel: LabourDeliveryViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val referPatientViewModel: ReferPatientViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityMedicalReviewLabourDeliveryactivityBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.labour_delivery_medical_review),
            homeAndBackVisibility = Pair(true, true),
            callback = {
                backNavigation()
            }
        )
        initializeView()
        attachObserver()
        initializeListener()
        getCurrentLocation()
        viewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
    }

    private fun backNavigation() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                onBackPressPopStack()
            }
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backNavigation()
            }
        }

    private fun attachObserver() {
        referPatientViewModel.referPatientResultLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    val fragment = supportFragmentManager.findFragmentByTag(ReferPatientFragment.TAG) as? ReferPatientFragment
                    fragment?.dismiss()
                    MedicalReviewSuccessDialogFragment.newInstance().show(supportFragmentManager, MedicalReviewSuccessDialogFragment.TAG)
                }
                ResourceState.ERROR -> hideLoading()
            }
        }

        viewModel.summaryCreateResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok)
                    ) {}
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    MedicalReviewSuccessDialogFragment.newInstance().show(supportFragmentManager, MedicalReviewSuccessDialogFragment.TAG)
                }
            }
        }

        patientViewModel.patientDetailsLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    if (binding.refreshLayout.isRefreshing) {
                        binding.refreshLayout.isRefreshing = false
                    }
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok)
                    ) {
                        if (it) {
                            onBackPressPopStack()
                        }
                    }
                }
            }
        }

        referPatientViewModel.referPatientResultLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    val fragment = supportFragmentManager.findFragmentByTag(ReferPatientFragment.TAG) as? ReferPatientFragment
                    fragment?.dismiss()
                    MedicalReviewSuccessDialogFragment.newInstance().show(supportFragmentManager, MedicalReviewSuccessDialogFragment.TAG)
                }
                ResourceState.ERROR -> hideLoading()
            }
        }

        viewModel.labourDeliveryMetaLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> {
                    viewModel.getLabourDeliveryMetaList()
                    initializePatientDetailsFragments()
                }
            }
        }

        viewModel.submitButtonState.observe(this) {
            binding.btnSubmit.isEnabled = it
        }

        viewModel.createLabourDeliveryMedicalReviewResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let {
                        viewModel.getLabourDeliverySummaryDetails(
                            it.motherId,
                            it.patientReference,
                            it.childPatientReference,
                            it.neonateId
                        )
                    }
                    showLabourDeliverySummary()
                }
            }
        }
    }

    private fun initializeView() {
        if (!SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_LABOUR_DELIVERY_LOADED.name)) {
            if (connectivityManager.isNetworkAvailable()) {
                viewModel.getStaticMetaData()
            } else {
                showErrorDialogue(getString(R.string.error), getString(R.string.no_internet_error), isNegativeButtonNeed = false) {}
            }
        } else {
            viewModel.getLabourDeliveryMetaList()
            initializePatientDetailsFragments()
        }

        supportFragmentManager.setFragmentResultListener(MedicalReviewDefinedParams.SUMMARY_ITEM, this) { _, _ ->
            enableReferralDoneBtn()
        }
    }

    private fun enableReferralDoneBtn() {
        binding.btnDone.isEnabled = viewModel.nextFollowupDate != null
    }

    private fun initializeListener() {
        binding.btnSubmit.safeClickListener(this)
        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
        binding.btnRefer.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
        binding.ivPrescription.safeClickListener(this)
    }

    private fun swipeRefresh() {
        viewModel.isRefresh = true
        if (connectivityManager.isNetworkAvailable()) {
            supportFragmentManager.findFragmentById(R.id.patientDetailFragment)?.let {
                patientViewModel.getPatientId()?.let { id ->
                    patientViewModel.getPatients(id)
                }
            }
        } else {
            showErrorDialogue(getString(R.string.error), getString(R.string.no_internet_error), isNegativeButtonNeed = false) {
                if (binding.refreshLayout.isRefreshing) {
                    binding.refreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun initializeFragment() {
        binding.btnSubmit.isEnabled = true
        replaceFragmentInId<LabourOrDeliveryFragment>(
            binding.labourDeliveryContainer.id,
            tag = LabourOrDeliveryFragment.TAG
        )
        replaceFragmentInId<MotherFragment>(
            binding.motherContainer.id,
            tag = MotherFragment.TAG
        )
        replaceFragmentInId<NeonateFragment>(
            binding.neonateContainer.id,
            tag = NeonateFragment.TAG
        )
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnSubmit -> handleSubmitClick()
            binding.btnRefer.id -> handleReferClick()
            binding.btnDone.id -> handleDoneClick()
            binding.ivPrescription.id -> handlePrescriptionClick()
        }
    }

    private fun handleSubmitClick() {
        if (validateLabourOrDelivery()) {
            if (connectivityManager.isNetworkAvailable()) {
                viewModel.createLabourDeliveryRequest()
            } else {
                showErrorDialogue(
                    getString(R.string.error),
                    getString(R.string.no_internet_error),
                    isNegativeButtonNeed = false
                ) {}
            }
        }
    }

    private fun handleReferClick() {
        viewModel.createLabourDeliveryMedicalReviewResponse.value?.data?.let {
            ReferPatientFragment.newInstance(
                MedicalReviewTypeEnums.LabourDelivery.name,
                it.patientReference,
                it.motherId
            ).show(supportFragmentManager, ReferPatientFragment.TAG)
        }
    }

    private fun handleDoneClick() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { patientDetails ->
            viewModel.createLabourDeliveryMedicalReviewResponse.value?.data?.let {
                val request = MedicalReviewSummarySubmitRequest(
                    id = it.motherId,
                    nextVisitDate = DateUtils.convertDateTimeToDate(
                        viewModel.nextFollowupDate,
                        DateUtils.DATE_ddMMyyyy,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        inUTC = true
                    ),
                    memberId = patientDetails.memberId,
                    patientReference = it.patientReference,
                    provenance = ProvanceDto(),
                    referralTicketType = MedicalReviewTypeEnums.RMNCH.name
                )
                viewModel.labourDeliverySummaryCreate(request)
            }
        }
    }

    private fun handlePrescriptionClick() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { data ->
            val intent = Intent(this, PrescriptionActivity::class.java).apply {
                putExtra(DefinedParams.PatientId, data.patientId)
                putExtra(DefinedParams.EncounterId, patientViewModel.encounterId)
            }
            startActivity(intent)
        }
    }


    private fun showLabourDeliverySummary() {
        viewModel.isRefresh = true
        binding.bottomNavigationView.visibility = View.INVISIBLE
        binding.referalBottomView.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.labourDeliveryContainer, MotherSummaryFragment()).commit()
        supportFragmentManager.beginTransaction()
            .replace(R.id.motherContainer, NeonateSummaryFragment()).commit()
        binding.neonateContainer.isVisible = false
    }

    private fun validateLabourOrDelivery(): Boolean {
        var isValid = true
        supportFragmentManager.findFragmentById(R.id.labourDeliveryContainer)
            ?.let { sessionFragment ->
                if (sessionFragment is LabourOrDeliveryFragment) {
                    isValid = sessionFragment.validate()
                    return isValid
                }
            }
        return isValid
    }

    private fun validateNeonate(): Boolean {
        var isValid = true
        supportFragmentManager.findFragmentById(R.id.neonateContainer)
            ?.let { sessionFragment ->
                if (sessionFragment is NeonateFragment) {
                    isValid = sessionFragment.validateInput()
                    return isValid
                }
            }
        return isValid
    }

    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(this)
        locationManager.getCurrentLocation {
            viewModel.lastLocation = it
        }
    }

    private fun initializePatientDetailsFragments() {
        val patientId = intent.getStringExtra(DefinedParams.PatientId)
        val fragment = PatientInfoFragment.newInstance(patientId).apply {
            setDataCallback(this@LabourDeliveryBaseActivity)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.patientDetailFragment, fragment)
            .commit()
    }

    override fun onDataLoaded(detail: PatientListRespModel) {
        viewModel.patientDetailModel = detail
        if (!viewModel.isRefresh) {
            initializeFragment()
        }
    }

    private fun onBackPressPopStack() {
        this@LabourDeliveryBaseActivity.finish()
    }
}
