package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.getLocalDate
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.offlinesync.model.Prescription
import com.medtroniclabs.spice.databinding.FragmentTBTreatmentBinding
import com.medtroniclabs.spice.db.entity.TreatmentDetailsEntity
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class TBTreatmentFragment : BaseFragment() {
    private lateinit var binding: FragmentTBTreatmentBinding
    private val viewModel: AssessmentViewModel by activityViewModels()
    private val dateTimeFormatter = DateTimeFormatter.ofPattern(DateUtils.DATE_ddMMyyyy)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTBTreatmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addObserver()
    }

    private fun addObserver() {
        viewModel.treatmentDetailsLiveData.observe(viewLifecycleOwner){ td ->
            bindData(td)
        }
    }

    private fun bindData(treatmentDetails: TreatmentDetailsEntity?) {
        treatmentDetails?.let {
            createTreatmentDetailsCardView(createTreatmentDetailsData(treatmentDetails))
        }
    }

    private fun getFormattedDateString(date: String?): String {
        return if (date != null && date.trim().isNotEmpty()) {
            date.getLocalDate().format(dateTimeFormatter)
        } else {
            getString(R.string.hyphen_symbol)
        }
    }

    private fun createTreatmentDetailsData(td: TreatmentDetailsEntity): List<AssessmentSummaryModel>? {

        val drugsAndQuantity = getDrugAndQuantityDetails(td.prescriptions)
        return mutableListOf(
            AssessmentSummaryModel(
                title = getString(R.string.diagnoses),
                value = td.diagnoses ?: getString(R.string.hyphen_symbol)
            ),
            AssessmentSummaryModel(
                title = getString(R.string.date_diagnosed),
                value = getFormattedDateString(td.diagnosedDate)
            ),
            AssessmentSummaryModel(
                title = getString(R.string.treatment_start_date),
                value = getFormattedDateString(td.tbConfirmationDate)
            ),
            AssessmentSummaryModel(
                title = getString(R.string.health_unit_no),
                value = td.healthUnitNo?.toString() ?: getString(R.string.hyphen_symbol)
            ),
            AssessmentSummaryModel(
                title = getString(R.string.ic_district_tb_no),
                value = td.icDistrictTBNo?.toString() ?: getString(R.string.hyphen_symbol)
            ),

            AssessmentSummaryModel(
                title = getString(R.string.type_of_drug),
                value = drugsAndQuantity.first ?: getString(R.string.hyphen_symbol)
            ),
            AssessmentSummaryModel(
                title = getString(R.string.no_of_tablets_given_for_tb),
                value = drugsAndQuantity.second ?: getString(R.string.hyphen_symbol)
            )
        )
    }

    private fun getDrugAndQuantityDetails(json: String?): Pair<String?, String?> {
        val gson = Gson()
        val listType = object : TypeToken<List<Prescription>>() {}.type
        val prescriptionList: List<Prescription> = gson.fromJson(json, listType)

        val filteredList = prescriptionList.filter { it.isActive
                && it.categoryName != null
                && it.categoryName.equals(DefinedParams.TB, true)}

        if (filteredList.isNotEmpty()) {
            var totalTablets = 0L
            val tablets = getString(R.string.tablets)
            val drugsList = filteredList.map { prescription ->
                val tabletCount = prescription.prescribedDays * prescription.frequency
                totalTablets += tabletCount
                "${prescription.medicationName} / $tabletCount $tablets"
            }

            return Pair(drugsList.joinToString("\n"), totalTablets.toString())
        } else {
            return Pair(null, null)
        }
    }

    private fun createTreatmentDetailsCardView(
        listSummaryData: List<AssessmentSummaryModel>?
    ) {
        listSummaryData?.let {summaryData ->
            binding.emptyErrorMessage.visibility = View.GONE
            binding.llParentLayout.visibility = View.VISIBLE
            binding.llParentLayout.removeAllViews()
            composeTbSummaryView(summaryData)
        } ?: kotlin.run {
            showErrorInSummary()
        }
    }

    private fun composeTbSummaryView(listSummaryData: List<AssessmentSummaryModel>) {
        listSummaryData.forEach { item ->
            bindTbSummaryView(item.title,item.value)
        }
    }

    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.llParentLayout.visibility = View.GONE
    }

    private fun bindTbSummaryView(title: String?, value: String?, valueTextColor: Int? = null) {
        binding.llParentLayout.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title,
                value,
                valueTextColor,
                requireContext()
            )
        )
    }

    companion object {
        const val TAG = "TBTreatmentFragment"
    }
}