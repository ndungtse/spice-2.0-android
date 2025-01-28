package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.DefaultID
import com.medtroniclabs.spice.common.DefinedParams.TB
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentAssessmentTBSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getValueOfKeyFromMap
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.HasCoughLastedLonger
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.HasNightSweatsTB
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.HasWeightLoss
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.ReferredPHUSite
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.hasCough
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.hasFever
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class AssessmentTBSummaryFragment : Fragment(), View.OnClickListener {
    private val viewModel: AssessmentViewModel by activityViewModels()
    lateinit var binding: FragmentAssessmentTBSummaryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentTBSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
        viewModel.setUserJourney(AnalyticsDefinedParams.TBSummaryAssessement)
    }


    private fun initView() {
        binding.btnDone.safeClickListener(this)
    }

    override fun onClick(view: View) {
        when(view.id) {
            binding.btnDone.id -> {
                viewModel.updateOtherAssessmentDetails()
            }
        }
    }

    private fun attachObservers() {
        viewModel.assessmentStringLiveData.value?.let {
            updateStatusBar()
            createSummaryView(createTBListSummaryData(it))
        }
    }

    private fun createTBListSummaryData(data: String): MutableList<AssessmentSummaryModel>? {
        return viewModel.formLayoutsLiveData.value?.data?.formLayout?.filter { it.isSummary == true }?.map { formLayout ->
            AssessmentSummaryModel(
                title = formLayout.title,
                id = formLayout.id,
                cultureValue = formLayout.titleCulture,
                value = getValueOfKeyFromMap(
                    StringConverter.stringToMap(data),
                    formLayout.id,
                    TB
                )
            )
        }?.toMutableList()
    }

    private fun createSummaryView(
        listSummaryData: MutableList<AssessmentSummaryModel>?
    ) {
        listSummaryData?.let {summaryData ->
            binding.emptyErrorMessage.visibility = View.GONE
            binding.parentLayout.visibility = View.VISIBLE
            binding.parentLayout.removeAllViews()
            composeTbSummaryView(summaryData)
        } ?: kotlin.run {
            showErrorInSummary()
        }
    }

    private fun composeTbSummaryView(listSummaryData: MutableList<AssessmentSummaryModel>) {
        getStatus(viewModel.referralStatus)?.let {
            bindTbSummaryView(
                getString(R.string.patient_status),
                it
            )
        }

        listSummaryData.forEach { item ->
            item.value?.let {
                when (item.id) {
                    hasCough -> bindTbSummaryView(item.title, capitalizeYesNo(it))
                    HasCoughLastedLonger, HasNightSweatsTB, hasFever, HasWeightLoss ->
                        bindTbSummaryView(removeLastChar(item.title), it)
                }
            }
        }

        bindTbSummaryView(getString(R.string.presumptive_tb_no), getString(R.string.seperator_hyphen))
    }

    private fun removeLastChar(input: String?): String {
        return input?.let {
            if (it.isNotEmpty()) it.dropLast(1) else ""
        } ?: ""
    }

    private fun capitalizeYesNo(value: String): String {
        return value.lowercase().replaceFirstChar { it.uppercase() }
    }

    private fun bindTbSummaryView(title: String?, value: String?, valueTextColor: Int? = null) {
        binding.parentLayout.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title,
                value,
                valueTextColor,
                requireContext()
            )
        )
    }

    private fun updateStatusBar() {
        when (viewModel.referralStatus) {
            ReferralStatus.Referred.name -> {
                viewModel.nearestFacilityLiveData.value?.data?.let { siteList ->
                    loadPhuSitesList(siteList)
                }
                binding.labelPhuReferred.visible()
                binding.etPhuChange.visible()
                binding.riskResultLayout.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.attention_color)
                binding.riskResultLayout.text = getString(R.string.referred_for_further_assessment)
            }

            else -> {
                binding.labelPhuReferred.gone()
                binding.etPhuChange.gone()
                binding.riskResultLayout.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.green_attention_color)
                binding.riskResultLayout.text = getString(R.string.no_refferral_treatment_required)
            }
        }
    }

    private fun getStatus(referralStatus: String?): String? {
        return when (referralStatus) {
            ReferralStatus.Referred.name -> getString(R.string.referred)
            ReferralStatus.OnTreatment.name -> getString(R.string.on_treatment)
            ReferralStatus.Recovered.name -> getString(R.string.recovered)
            else -> {
                null
            }
        }
    }

    private fun loadPhuSitesList(healthFacilityList: ArrayList<Map<String, Any>>) {
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(healthFacilityList)
        binding.etPhuChange.adapter = adapter
        binding.etPhuChange.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        val selectedId = it[DefinedParams.id] as String?
                        if (selectedId != DefaultID) {
                            viewModel.otherAssessmentDetails[ReferredPHUSite] = selectedId.toString()
                        } else {
                            if (viewModel.otherAssessmentDetails.containsKey(ReferredPHUSite))
                                viewModel.otherAssessmentDetails.remove(ReferredPHUSite)
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    companion object {
        const val TAG = "AssessmentTBSummaryFragment"
        fun newInstance(): AssessmentTBSummaryFragment {
            return AssessmentTBSummaryFragment()
        }
    }

    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.parentLayout.visibility = View.GONE
    }

    fun getCurrentAnsweredStatus():Boolean {
        return viewModel.otherAssessmentDetails.isNotEmpty()
    }
}