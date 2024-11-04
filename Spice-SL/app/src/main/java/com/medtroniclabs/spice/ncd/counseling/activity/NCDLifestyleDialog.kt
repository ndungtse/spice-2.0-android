package com.medtroniclabs.spice.ncd.counseling.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.loadAsGif
import com.medtroniclabs.spice.appextensions.resetImageView
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.DialogNcdLifestyleBinding
import com.medtroniclabs.spice.formgeneration.extension.hideKeyboard
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.counseling.model.NCDCounselingModel
import com.medtroniclabs.spice.ncd.counseling.viewmodel.CounselingViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.ui.TagListCustomView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NCDLifestyleDialog(private val callback: (isPositiveResult: Boolean) -> Unit) :
    DialogFragment(), View.OnClickListener {

    private lateinit var binding: DialogNcdLifestyleBinding

    private val viewModel: CounselingViewModel by viewModels()
    private lateinit var tagListCustomView: TagListCustomView

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    companion object {
        const val TAG = "NCDLifestyleDialog"
        fun newInstance(callback: (isPositiveResult: Boolean) -> Unit): NCDLifestyleDialog {
            return NCDLifestyleDialog(callback)
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
        binding = DialogNcdLifestyleBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    private fun initView() {
        binding.apply {
            tvLifestyleAssessmentLbl.markMandatory()
            tvOtherNotesLbl.markMandatory()
            btnSave.safeClickListener(this@NCDLifestyleDialog)
            ivClose.safeClickListener(this@NCDLifestyleDialog)
            btnCancel.safeClickListener(this@NCDLifestyleDialog)
            loadingProgress.safeClickListener {}

            etLifestyleAssessment.addTextChangedListener {
                viewModel.lifestyleAssessment = it?.toString()
                updateView()
            }

            etOtherNotes.addTextChangedListener {
                viewModel.otherNote = it?.toString()
            }
        }

        tagListCustomView =
            TagListCustomView(requireContext(), binding.chipGroup) { _, _, _ ->
                viewModel.lifestyles =
                    tagListCustomView.getSelectedTags().map { it.name }.ifEmpty { null }
                updateView()
            }
        viewModel.getChips()
    }

    private fun updateView() {
        with(viewModel) {
            binding.btnSave.isEnabled =
                !(lifestyles.isNullOrEmpty() || lifestyleAssessment.isNullOrBlank())
        }
    }

    private fun attachObserver() {
        viewModel.getChipItems.observe(this) {
            val complaintList = it.map { item ->
                ChipViewItemModel(
                    id = item.id, name = item.displayValue, type = item.type, value = item.value
                )
            } as ArrayList<ChipViewItemModel>
            tagListCustomView.addChipItemList(complaintList)
        }
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
                    viewModel.createAssessment(getCreateRequest(), true)
            }
        }
    }

    private fun getCreateRequest(): NCDCounselingModel {
        return with(viewModel) {
            NCDCounselingModel(
                patientReference = patientReference,
                memberReference = memberReference,
                visitId = encounterReference,
                lifestyles = lifestyles,
                lifestyleAssessment = lifestyleAssessment,
                otherNote = otherNote,
                referredBy = SecuredPreference.getUserFhirId(),
                referredDate = DateUtils.getTodayDateDDMMYYYY(),
                assessedBy = SecuredPreference.getUserFhirId(),
                assessedDate = DateUtils.getTodayDateDDMMYYYY(),
                isNutritionist = nutritionist
            )
        }
    }

    override fun onStart() {
        super.onStart()
        setDialogPercent(80, 50)
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