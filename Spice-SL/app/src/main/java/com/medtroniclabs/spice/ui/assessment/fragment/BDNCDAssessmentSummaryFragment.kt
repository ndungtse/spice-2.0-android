package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.DefaultID
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.FragmentBdNcdSummaryBinding
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.findValueByKey
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.AVG_BLOOD_PRESSURE
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.BP_LOG_DETAILS
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.CULTURE_VALUE
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.FACILITY_TYPE_UPAZILA
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.GLUCOSE
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.GLUCOSE_TYPE
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.GLUCOSE_UNIT
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.HBA1CUnit
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.HEIGHT
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.NCD_SYMPTOMS
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.REFERRAL_FACILITY_TYPE
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.ReferredPHUSiteID
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.WEIGHT
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.hba1c
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import org.json.JSONArray
import org.json.JSONObject

class BDNCDAssessmentSummaryFragment : BaseFragment() {
    private val viewModel: AssessmentViewModel by activityViewModels()
    lateinit var binding: FragmentBdNcdSummaryBinding

    companion object {
        const val TAG = "BDNCDAssessmentSummaryFragment"

        fun newInstance(): BDNCDAssessmentSummaryFragment = BDNCDAssessmentSummaryFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBdNcdSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
        viewModel.setUserJourney(AnalyticsDefinedParams.TBSummaryAssessement)
    }

    private fun initView() {
        binding.btnDone.setOnClickListener {
            viewModel.updateOtherAssessmentDetails()
        }
    }

    private fun attachObservers() {
        viewModel.assessmentStringLiveData.value?.let {
            val json = JSONObject(it)
            updateStatusBar(json)
            val items = createNCDSummaryData(json)
            createSummaryView(items)
        }
    }

    private fun createSummaryView(listSummaryData: MutableList<AssessmentSummaryModel>?) {
        listSummaryData?.let { summaryData ->
            binding.parentLayout.removeAllViews()

            getStatus(viewModel.referralStatus)?.let {
                bindSummaryView(
                    getString(R.string.patient_status),
                    it,
                )
            }

            val isTranslationEnabled = SecuredPreference.getIsTranslationEnabled()

            summaryData.forEach { item ->
                bindSummaryView(if (isTranslationEnabled) item.cultureValue else item.title, item.value)
            }
        }
    }

    private fun getStatus(referralStatus: String?): String? =
        when (referralStatus) {
            ReferralStatus.Referred.name -> getString(R.string.referred)
            ReferralStatus.OnTreatment.name -> getString(R.string.on_treatment)
            ReferralStatus.Recovered.name -> getString(R.string.recovered)
            else -> {
                null
            }
        }

    private fun updateStatusBar(json: JSONObject) {
        when (viewModel.referralStatus) {
            ReferralStatus.Referred.name -> {
                val referralTypeSite = findValueByKey(json, REFERRAL_FACILITY_TYPE) as String

                viewModel.nearestFacilityLiveData.value?.data?.let { siteList ->
                    loadPhuSitesList(siteList)
                }
                binding.labelPhuReferred.visible()
                binding.etPhuChange.visible()
                binding.riskResultLayout.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.attention_color)

                if (referralTypeSite == FACILITY_TYPE_UPAZILA) {
                    binding.riskResultLayout.text = getString(R.string.referred_for_nfurther_assessment_Upazila)
                } else {
                    binding.riskResultLayout.text = getString(R.string.referred_for_nfurther_assessment_cc)
                }
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

    private fun createNCDSummaryData(json: JSONObject): MutableList<AssessmentSummaryModel>? =
        viewModel.formLayoutsLiveData.value
            ?.data
            ?.formLayout
            ?.filter { it.isSummary == true }
            ?.mapNotNull { formLayout ->
                val value = getValueFromJson(formLayout.id, json)

                value?.let {
                    AssessmentSummaryModel(
                        title = formLayout.titleSummary ?: formLayout.title,
                        id = formLayout.id,
                        cultureValue = formLayout.titleSummaryCulture ?: formLayout.titleCulture,
                        value = it,
                    )
                }
            }?.toMutableList()

    private fun getValueFromJson(
        id: String,
        jsonObject: JSONObject,
    ): String? {
        return when (id) {
            NCD_SYMPTOMS -> {
                val list = mutableListOf<String>()
                findValueByKey(jsonObject, id)?.let {
                    val jsonArray = it as JSONArray

                    for (i in 0 until jsonArray.length()) {
                        val sign = jsonArray.getJSONObject(i)

                        sign.optString(CULTURE_VALUE).let { value ->
                            list.add(value)
                        }
                    }

                    return list.joinToString(",")
                }
            }

            HEIGHT, WEIGHT -> findValueByKey(jsonObject, id)?.toString()

            GLUCOSE -> {
                val glucoseValue = findValueByKey(jsonObject, id)
                val unit = findValueByKey(jsonObject, GLUCOSE_UNIT)
                val type = findValueByKey(jsonObject, GLUCOSE_TYPE)
                return "${glucoseValue as Double} ${unit as String} (${type as String})"
            }

            hba1c -> {
                val hba1 = findValueByKey(jsonObject, id)
                val unit = findValueByKey(jsonObject, HBA1CUnit)
                return "${hba1 as Double} ${unit as String}"
            }

            BP_LOG_DETAILS -> {
                findValueByKey(jsonObject, AVG_BLOOD_PRESSURE) as? String
            }

            else -> findValueByKey(jsonObject, id)?.toString()
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
                    itemId: Long,
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        val selectedId = it[DefinedParams.id] as String?
                        if (selectedId != DefaultID) {
                            viewModel.otherAssessmentDetails[ReferredPHUSiteID] = selectedId.toString()
                        } else {
                            if (viewModel.otherAssessmentDetails.containsKey(ReferredPHUSiteID)) {
                                viewModel.otherAssessmentDetails.remove(ReferredPHUSiteID)
                            }
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

    private fun bindSummaryView(
        title: String?,
        value: String?,
        valueTextColor: Int? = null,
    ) {
        binding.parentLayout.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title,
                value,
                valueTextColor,
                requireContext(),
            ),
        )
    }
}
