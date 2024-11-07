package com.medtroniclabs.spice.ncd.counseling.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.loadAsGif
import com.medtroniclabs.spice.appextensions.resetImageView
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.DialogNcdCounselingBinding
import com.medtroniclabs.spice.formgeneration.extension.hideKeyboard
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.counseling.model.NCDCounselingModel
import com.medtroniclabs.spice.ncd.counseling.viewmodel.CounselingViewModel
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.MEMBER_REFERENCE
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.PATIENT_REFERENCE
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.VISIT_ID
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NCDCounselingDialog(private val callback: (isPositiveResult: Boolean) -> Unit) :
    DialogFragment(), View.OnClickListener {

    private lateinit var binding: DialogNcdCounselingBinding

    private val viewModel: CounselingViewModel by activityViewModels()

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    companion object {
        const val TAG = "NCDCounselingDialog"
        fun newInstance(callback: (isPositiveResult: Boolean) -> Unit): NCDCounselingDialog {
            return NCDCounselingDialog(callback)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogNcdCounselingBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    private fun initView() {
        binding.apply {
            tvCounselorNotesLbl.markMandatory()
            btnSave.safeClickListener(this@NCDCounselingDialog)
            ivClose.safeClickListener(this@NCDCounselingDialog)
            btnCancel.safeClickListener(this@NCDCounselingDialog)
            loadingProgress.safeClickListener {}

            etCounselorNotes.addTextChangedListener {
                viewModel.counselorAssessment = it?.toString()
                updateView()
            }
        }
    }

    private fun updateView() {
        with(viewModel) {
            binding.btnSave.isEnabled = !counselorAssessment.isNullOrBlank()
        }
    }

    private fun attachObserver() {
        viewModel.createAssessmentLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    dismiss()
                    callback.invoke(true)
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.ivClose.id, binding.btnCancel.id -> {
                dismiss()
            }

            binding.btnSave.id -> {
                requireContext().hideKeyboard(v)
                if (connectivityManager.isNetworkAvailable())
                    viewModel.createAssessment(getCreateRequest(), false)
            }
        }
    }

    private fun getCreateRequest(): NCDCounselingModel {
        return with(viewModel) {
            NCDCounselingModel(
                patientReference = patientReference,
                memberReference = memberReference,
                visitId = encounterReference,
                patientVisitId = encounterReference,
                counselorAssessment = counselorAssessment,
                referredBy = NCDMRUtil.getUserName(),
                referredDate = DateUtils.getTodayDateDDMMYYYY(),
                assessedBy = NCDMRUtil.getUserName(),
                assessedDate = DateUtils.getTodayDateDDMMYYYY(),
                isCounselor = counselor
            )
        }
    }

    override fun onStart() {
        super.onStart()
        setDialogPercent(80, 25)
    }

    private fun showLoading() {
        binding.apply {
            loadingProgress.visibility = View.VISIBLE
            loaderImage.apply {
                loadAsGif(R.drawable.loader_spice)
            }
            btnSave.visibility = View.INVISIBLE
            btnCancel.visibility = View.INVISIBLE
        }
    }

    private fun hideLoading() {
        binding.apply {
            loadingProgress.visibility = View.GONE
            loaderImage.apply {
                resetImageView()
            }
            btnSave.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE
        }
    }
}