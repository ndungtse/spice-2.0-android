package com.medtroniclabs.spice.ncd.medicalreview.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentNcdMedicalReviewSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.data.MRSummaryResponse
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewSummaryViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.NCDMedicalReviewViewModel

class NCDMedicalReviewSummaryFragment : BaseFragment(),View.OnClickListener {

    private lateinit var binding: FragmentNcdMedicalReviewSummaryBinding
    private val viewModel: NCDMedicalReviewSummaryViewModel by activityViewModels()
    private val medicalReviewViewModel: NCDMedicalReviewViewModel by activityViewModels()
    private var datePickerDialog: DatePickerDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNcdMedicalReviewSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "NCDMedicalReviewSummaryFragment"
        fun newInstance(): NCDMedicalReviewSummaryFragment {
            return NCDMedicalReviewSummaryFragment()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.summaryResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    // navigate to summary
                    resourceState.data?.let {
                        populateData(it)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                    (activity as? BaseActivity)?.showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {
                    }
                }
            }
        }
    }

    private fun initView() {
        SecuredPreference.getUserDetails()?.let {
            binding.tvClinicalName.text = requireContext().getString(
                R.string.firstname_lastname,
                it.firstName?.capitalizeFirstChar()
                    ?: getString(R.string.empty),
                it.lastName ?: getString(R.string.empty)
            )
        }
        binding.tvDateOfReviewValue.text = DateUtils.convertDateTimeToDate(
            DateUtils.getTodayDateDDMMYYYY(),
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_ddMMyyyy
        )
        medicalReviewViewModel.createMedicalReview.value?.data?.let {
            withNetworkAvailability(online = {
                viewModel.fetchSummaryResponse(it)
            })
        }
        binding.tvNextMedicalReviewLabelText.safeClickListener(this)
    }

    private fun populateData(data: MRSummaryResponse) {
        binding.apply {
            tvObstetricsExaminationText.text = CommonUtils.combineText(
                data.physicalExams,
                data.physicalExamComments,
                getString(R.string.hyphen_symbol)
            )
            tvChiefComplaintsText.text = CommonUtils.combineText(
                data.complaints,
                data.compliantComments,
                getString(R.string.hyphen_symbol)
            )
            tvClinicalNotesText.text =
                data.clinicalNote?.takeIf { it.isNotBlank() } ?: getString(R.string.hyphen_symbol)
        }
    }

    private fun showDatePickerDialog() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.tvNextMedicalReviewLabelText.text.isNullOrBlank())
            yearMonthDate =
                DateUtils.convertedMMMToddMM(binding.tvNextMedicalReviewLabelText.text.toString())

        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                minDate = DateUtils.getTomorrowDate(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.tvNextMedicalReviewLabelText.text = DateUtils.convertDateTimeToDate(
                    stringDate,
                    DateUtils.DATE_FORMAT_ddMMyyyy,
                    DateUtils.DATE_ddMMyyyy
                )
                datePickerDialog = null
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.tvNextMedicalReviewLabelText.id -> {
                showDatePickerDialog()
            }
        }
    }

    fun validateInput(): Boolean {
        val value = binding.tvNextMedicalReviewLabelText.text?.trim().toString()
        if (value.isBlank()) {
            binding.tvNextMedicalReviewError.visible()
            binding.tvNextMedicalReviewLabelText.requestFocus()
            return false
        }
        binding.tvNextMedicalReviewError.invisible()
        return true
    }
}