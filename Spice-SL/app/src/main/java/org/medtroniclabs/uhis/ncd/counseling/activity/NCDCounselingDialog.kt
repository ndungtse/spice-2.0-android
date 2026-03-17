package org.medtroniclabs.uhis.ncd.counseling.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.loadAsGif
import org.medtroniclabs.uhis.appextensions.resetImageView
import org.medtroniclabs.uhis.appextensions.setDialogPercent
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.databinding.DialogNcdCounselingBinding
import org.medtroniclabs.uhis.formgeneration.extension.hideKeyboard
import org.medtroniclabs.uhis.formgeneration.extension.markMandatory
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ncd.counseling.viewmodel.CounselingViewModel
import org.medtroniclabs.uhis.ncd.data.NCDCounselingModel
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.network.utils.ConnectivityManager
import javax.inject.Inject

@AndroidEntryPoint
class NCDCounselingDialog(private val callback: (isPositiveResult: Pair<String, String>?) -> Unit) :
    DialogFragment(), View.OnClickListener {
    private lateinit var binding: DialogNcdCounselingBinding

    private val viewModel: CounselingViewModel by activityViewModels()

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    companion object {
        const val TAG = "NCDCounselingDialog"

        const val PatientReference = "patientReference"
        const val MemberReference = "memberReference"
        const val EncounterReference = "encounterReference"

        fun newInstance(
            patientReference: String?,
            memberReference: String?,
            encounterReference: String?,
            callback: (isPositiveResult: Pair<String, String>?) -> Unit,
        ): NCDCounselingDialog {
            val dialog = NCDCounselingDialog(callback)
            val bundle = Bundle()
            bundle.putString(PatientReference, patientReference)
            bundle.putString(MemberReference, memberReference)
            bundle.putString(EncounterReference, encounterReference)
            dialog.arguments = bundle
            return dialog
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogNcdCounselingBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    private fun initView() {
        arguments?.let {
            viewModel.apply {
                patientReference = it.getString(PatientReference)
                memberReference = it.getString(MemberReference)
                encounterReference = it.getString(EncounterReference)
            }
        }

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
                    resourceState.data?.message?.let { message ->
                        if (message.isNotEmpty()) {
                            callback.invoke(
                                Pair(
                                    getString(R.string.psychological_assessment),
                                    message,
                                ),
                            )
                        }
                    }
                    dismiss()
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
                if (connectivityManager.isNetworkAvailable()) {
                    viewModel.createAssessment(getCreateRequest(), false)
                }
            }
        }
    }

    private fun getCreateRequest(): NCDCounselingModel =
        with(viewModel) {
            NCDCounselingModel(
                patientReference = patientReference,
                memberReference = memberReference,
                visitId = encounterReference,
                patientVisitId = encounterReference,
                counselorAssessment = counselorAssessment,
                referredBy = NCDMRUtil.currentUserId(),
                referredByDisplay = NCDMRUtil.getUserName(),
                referredDate = DateUtils.getTodayDateDDMMYYYY(),
                assessedBy = NCDMRUtil.currentUserId(),
                assessedByDisplay = NCDMRUtil.getUserName(),
                assessedDate = DateUtils.getTodayDateDDMMYYYY(),
                isCounselor = counselor,
            )
        }

    override fun onStart() {
        super.onStart()
        setDialogPercent(80, 35)
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
