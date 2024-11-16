package com.medtroniclabs.spice.ncd.medicalreview

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.ActivityNcdhrioBaseBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.formgeneration.extension.safePopupMenuClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.data.NCDPatientRemoveRequest
import com.medtroniclabs.spice.ncd.medicalreview.fragment.NCDScheduleDialog
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.patientDelete.NCDDeleteConfirmationDialog
import com.medtroniclabs.spice.ui.patientDelete.viewModel.NCDPatientDeleteViewModel
import com.medtroniclabs.spice.ui.patientEdit.NCDPatientEditActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDHrioBaseActivity : BaseActivity() {
    private lateinit var binding: ActivityNcdhrioBaseBinding
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val patientDeleteViewModel: NCDPatientDeleteViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdhrioBaseBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_details),
            homeAndBackVisibility = Pair(false, true),
            homeIcon = ContextCompat.getDrawable(this, R.drawable.ic_more_vertical)
        )
        init()
        attachObservers()
        showHideVerticalIcon(true)
    }

    fun init() {
        withNetworkAvailability(online = {
            getPatientId()?.let { id ->
                patientViewModel.getPatients(
                    id,
                    origin = getMenuOrigin()
                )
            }
        })
    }

    private fun showHideVerticalIcon(visibility: Boolean) {
        showVerticalMoreIcon(visibility) {
            onMoreIconClicked(it)
        }
    }

    private fun onMoreIconClicked(view: View) {
        val popupMenu = PopupMenu(this@NCDHrioBaseActivity, view)
        popupMenu.menuInflater.inflate(R.menu.ncd_menu_patient_edit, popupMenu.menu)
        popupMenu.menu.findItem(R.id.patient_delete).isVisible = true
        popupMenu.menu.findItem(R.id.schedule).isVisible =
            CommonUtils.canShowScheduleMenu()
        popupMenu.safePopupMenuClickListener(object :
            android.widget.PopupMenu.OnMenuItemClickListener,
            PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                onPatientEditMenuItemClick(menuItem.itemId)
                return true
            }
        })
        popupMenu.setForceShowIcon(true)
        popupMenu.show()
    }

    private fun onPatientEditMenuItemClick(itemId: Int) {
        when (itemId) {
            R.id.patient_delete -> {
                patientDeleteCreate()
            }

            R.id.patient_edit -> {
                val intent =
                    Intent(this@NCDHrioBaseActivity, NCDPatientEditActivity::class.java)
                intent.putExtra(NCDMRUtil.PATIENT_REFERENCE, patientViewModel.getPatientId())
                intent.putExtra(
                    NCDMRUtil.MEMBER_REFERENCE,
                    patientViewModel.getPatientFHIRId()
                )
                intent.putExtra(DefinedParams.ORIGIN, patientViewModel.origin)
                startActivity(intent)
            }

            R.id.schedule -> {
                displayScheduleDialog()
            }
        }
    }

    private fun displayScheduleDialog() {
        NCDScheduleDialog.newInstance(
            patientViewModel.getPatientId(),
            patientViewModel.getPatientFHIRId()
        ).show(supportFragmentManager, NCDScheduleDialog.TAG)
    }

    private fun patientDeleteCreate() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { model ->
            val deleteConfirmationDialog = NCDDeleteConfirmationDialog.newInstance(
                getString(R.string.alert),
                getString(
                    R.string.patient_delete_confirmation,
                    model.firstName,
                    model.lastName
                ),
                { _, reason, otherReason ->
                    val request = NCDPatientRemoveRequest(
                        patientId = model.patientId.toString(),
                        reason = reason,
                        otherReason = otherReason
                    )
                    patientDeleteViewModel.ncdPatientRemove(request)
                },
                this,
                true,
                okayButton = getString(R.string.yes),
                cancelButton = getString(R.string.no)
            )
            deleteConfirmationDialog.show(
                supportFragmentManager,
                NCDDeleteConfirmationDialog.TAG
            )
        }
    }

    fun attachObservers() {
        patientViewModel.patientDetailsLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let {
                        loadData(it)
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showError(true)
                }
            }
        }
    }

    private fun loadData(patientListRespModel: PatientListRespModel) {

        binding.patientName.tvKey.text = getString(R.string.name)
        binding.patientName.tvValue.text = patientListRespModel.name.takeUnless { it.isNullOrBlank() }?.capitalizeFirstChar() ?: getString(R.string.hyphen_symbol)

        binding.mobileNumber.tvKey.text = getString(R.string.mobile_number)
        binding.mobileNumber.tvValue.text = patientListRespModel.phoneNumber.takeUnless { it.isNullOrBlank() } ?: getString(R.string.hyphen_symbol)

        binding.mobileCategory.tvKey.text = getString(R.string.mobile_number)
        binding.mobileCategory.tvValue.text = patientListRespModel.phoneNumberCategory.takeUnless { it.isNullOrBlank() }?.capitalizeFirstChar() ?: getString(R.string.hyphen_symbol)

        binding.landmark.tvKey.text = getString(R.string.landmark)
        binding.landmark.tvValue.text = patientListRespModel.landmark.takeUnless { it.isNullOrBlank() }?.capitalizeFirstChar() ?: getString(R.string.hyphen_symbol)

        binding.occupation.tvKey.text = getString(R.string.occupation)
        binding.occupation.tvValue.text = patientListRespModel.occupation.takeUnless { it.isNullOrBlank() }?.capitalizeFirstChar() ?: getString(R.string.hyphen_symbol)

        binding.healthStatus.tvKey.text = getString(R.string.health_insurance_status)
        binding.healthStatus.tvValue.text = getString(R.string.hyphen_symbol)

        binding.healthType.tvKey.text = getString(R.string.health_insurance_type)
        binding.healthType.tvValue.text = getString(R.string.hyphen_symbol)

        binding.healthId.tvKey.text = getString(R.string.health_insurance_Id)
        binding.healthId.tvValue.text = getString(R.string.hyphen_symbol)

        binding.nextVisit.tvKey.text = getString(R.string.next_medical_review_date)
        binding.nextVisit.tvValue.text = getString(R.string.hyphen_symbol)
    }

    private fun showError(isActivityClosed: Boolean = false) {
        showErrorDialogue(
            title = getString(R.string.alert),
            message = getString(R.string.something_went_wrong_try_later),
            positiveButtonName = getString(R.string.ok),
        ) { isPositiveResult ->
            if (isPositiveResult && isActivityClosed) {
                onBackPressPopStack()
            }
        }
    }

    private fun onBackPressPopStack() {
        this@NCDHrioBaseActivity.finish()
    }

    private fun getMenuOrigin(): String? {
        return intent.getStringExtra(DefinedParams.ORIGIN)
    }

    private fun getPatientId(): String? {
        return intent.getStringExtra(DefinedParams.FhirId)
    }
}