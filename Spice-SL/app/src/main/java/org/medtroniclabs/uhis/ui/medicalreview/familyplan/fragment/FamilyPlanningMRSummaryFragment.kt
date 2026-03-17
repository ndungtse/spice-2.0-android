package org.medtroniclabs.uhis.ui.medicalreview.familyplan.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.common.ViewUtils
import org.medtroniclabs.uhis.data.model.FamilyPlanningSummaryResponse
import org.medtroniclabs.uhis.databinding.FragmentFamilyPlanTreatmentBinding
import org.medtroniclabs.uhis.formgeneration.extension.markMandatory
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.medicalreview.familyplan.activity.FamilyPlanMedicalReviewActivity
import org.medtroniclabs.uhis.ui.medicalreview.familyplan.viewmodel.ContraceptivesViewModel
import org.medtroniclabs.uhis.ui.medicalreview.familyplan.viewmodel.FamilyPlanViewModel
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewDefinedParams

@AndroidEntryPoint
class FamilyPlanningMRSummaryFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentFamilyPlanTreatmentBinding
    private val viewModel: FamilyPlanViewModel by activityViewModels()
    private val contraceptivesViewModel: ContraceptivesViewModel by activityViewModels()
    private var datePickerDialog: DatePickerDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFamilyPlanTreatmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.summaryDetailsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let {
                        renderSummaryDetails(it)
                    }
                }
            }
            val swipeRefresh =
                (activity as FamilyPlanMedicalReviewActivity).findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
            if (swipeRefresh.isRefreshing) {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun renderSummaryDetails(data: FamilyPlanningSummaryResponse) {
        binding.tvClientTypeText.text =
            data.contraceptive?.clientType ?: getString(R.string.separator_double_hyphen)
        binding.tvPostPartumText.text =
            data.contraceptive?.postPartum ?: getString(R.string.separator_double_hyphen)
        binding.tvProgestinOnlyOralsText.text =
            if (contraceptivesViewModel.otherProgestinOnlyOralsComments.isNullOrEmpty()) {
                data.contraceptive?.progestinOnlyOrals
                    ?: getString(R.string.separator_double_hyphen)
            } else {
                "${data.contraceptive?.progestinOnlyOrals} - ${contraceptivesViewModel.otherProgestinOnlyOralsComments}"
            }
        binding.tvQuantityText.text = data.contraceptive?.microlutQuantity?.toString()
            ?: getString(R.string.separator_double_hyphen)
    }

    private fun initListeners() {
        binding.tvNextMedicalReviewLabelText.safeClickListener(this)
    }

    private fun initViews() {
        binding.tvNextMedicalReviewLabel.markMandatory()
        binding.tvClinicalName.text = requireContext().getString(
            R.string.firstname_lastname,
            SecuredPreference.getUserDetails()?.firstName,
            SecuredPreference.getUserDetails()?.lastName,
        )
        binding.tvDateOfReviewValue.text = DateUtils.convertDateTimeToDate(
            DateUtils.getTodayDateDDMMYYYY(),
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_ddMMyyyy,
        )
    }

    companion object {
        const val TAG = "FamilyPlanTreatmentSummaryFragment"
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvNextMedicalReviewLabelText -> {
                showDatePickerDialog()
            }
        }
    }

    private fun showDatePickerDialog() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.tvNextMedicalReviewLabelText.text.isNullOrBlank()) {
            yearMonthDate =
                DateUtils.convertedMMMToddMM(binding.tvNextMedicalReviewLabelText.text.toString())
        }

        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                minDate = DateUtils.getTomorrowDate(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null },
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.tvNextMedicalReviewLabelText.text =
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DateUtils.DATE_FORMAT_ddMMyyyy,
                        DateUtils.DATE_ddMMyyyy,
                    )
                viewModel.nextFollowupDate = binding.tvNextMedicalReviewLabelText.text.toString()
                datePickerDialog = null
                summaryListener()
            }
        }
    }

    private fun summaryListener() {
        setFragmentResult(
            MedicalReviewDefinedParams.SUMMARY_ITEM,
            bundleOf(
                MedicalReviewDefinedParams.SUMMARY_ITEM to true,
            ),
        )
    }
}
