package com.medtroniclabs.spice.ncd.screening.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.RadioGroup
import androidx.fragment.app.activityViewModels
import com.askjeffreyliu.flexboxradiogroup.FlexBoxRadioGroup
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentGeneralDetailsBinding
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.Screening.Camp
import com.medtroniclabs.spice.mappingkey.Screening.Community
import com.medtroniclabs.spice.mappingkey.Screening.DoorToDoor
import com.medtroniclabs.spice.mappingkey.Screening.Facility
import com.medtroniclabs.spice.mappingkey.Screening.OPDTriage
import com.medtroniclabs.spice.mappingkey.Screening.Pharmacy
import com.medtroniclabs.spice.mappingkey.Screening.inpatient
import com.medtroniclabs.spice.mappingkey.Screening.outpatient
import com.medtroniclabs.spice.ncd.screening.viewmodel.GeneralDetailsViewModel
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil

class GeneralDetailsFragment : BaseFragment(), View.OnClickListener,
    RadioGroup.OnCheckedChangeListener, FlexBoxRadioGroup.OnCheckedChangeListener {

    private lateinit var binding: FragmentGeneralDetailsBinding
    private val viewModel: GeneralDetailsViewModel by activityViewModels()

    private val adapter by lazy { CustomSpinnerAdapter(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGeneralDetailsBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    companion object {
        const val TAG = "GeneralDetailsFragment"
        fun newInstance() = GeneralDetailsFragment() // Optimized: Simplified function.
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
        setListeners()
    }

    private fun attachObservers() {
        viewModel.getSitesLiveData.observe(viewLifecycleOwner) {
            loadSiteDetails(ArrayList(it))
        }
    }

    private fun loadSiteDetails(data: ArrayList<HealthFacilityEntity>?) {
        val list = arrayListOf<Map<String, Any>>(
            hashMapOf(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.ID to DefinedParams.DefaultSelectID
            )
        )
        var defaultPosition = 0
        data?.mapIndexed { index, site ->
            hashMapOf(
                DefinedParams.ID to site.id,
                DefinedParams.NAME to site.name,
                DefinedParams.TenantId to site.tenantId,
                DefinedParams.FhirId to (site.fhirId ?: 0)
            ).also {
                if (site.isDefault) defaultPosition = index + 1
            }
        }?.let { list.addAll(it) }
        adapter.setData(list)
        binding.etSiteChange.post {
            binding.etSiteChange.setSelection(defaultPosition, false)
        }
        binding.etSiteChange.adapter = adapter
    }

    private fun setListeners() {
        binding.etSiteChange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?, view: View?, pos: Int, itemId: Long
            ) {
                adapter.getData(pos)?.let {
                    processSiteSelection(it)
                    validateToEnableNext()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun processSiteSelection(map: Map<String, Any>) {
        viewModel.siteDetail.apply {
            siteName = map[DefinedParams.NAME] as? String ?: ""
            siteId = map[DefinedParams.ID]?.toString()?.toLongOrNull() ?: -1L
            tenantId = map[DefinedParams.TenantId]?.toString()?.toLongOrNull() ?: -1L
            // TODO: ask backend for it
            referredSiteId = map[DefinedParams.FhirId]?.toString()?.toLongOrNull() ?: -1L
        }
    }

    private fun initView() {
        binding.apply {
            tvTitleCategory.markMandatory()
            tvTitleType.markMandatory()
            btnNext.safeClickListener(this@GeneralDetailsFragment)
            rgCategoryRow.setOnCheckedChangeListener(this@GeneralDetailsFragment)
            rgType.setOnCheckedChangeListener(this@GeneralDetailsFragment)
        }
        binding.rbFacility.isChecked = true
        viewModel.getSites(true)
        MotherNeonateUtil.initTextWatcherForString(binding.etOthers) {
            viewModel.siteDetail.categoryDisplayType = it.ifBlank { null }
            validateToEnableNext()
        }
    }

    override fun onClick(view: View?) {
        // Implement the click event as needed
        when (view?.id) {
            R.id.btnNext -> {
                replaceFragmentIfExists<StatsFragment>(
                    R.id.screeningParentLayout,
                    bundle = null,
                    tag = StatsFragment.TAG
                )
            }
        }
    }

    override fun onCheckedChanged(radioGroup: RadioGroup?, radioButton: Int) {
        when (radioGroup?.id) {
            R.id.rgCategoryRow -> {
                viewModel.siteDetail.category = when (radioButton) {
                    R.id.rbFacility -> Facility.also {
                        viewModel.siteDetail.categoryDisplayName = getString(R.string.clinic)
                    }

                    R.id.rbCommunity -> Community.also {
                        viewModel.siteDetail.categoryDisplayName = getString(R.string.community)
                    }

                    else -> return
                }
                changeScreenTypeDetails(radioButton)
            }
        }
        validateToEnableNext()
    }

    private fun changeScreenTypeDetails(category: Int) {
        binding.apply {
            rgType.clearCheck()
            rbTypeBtn3.gone()
            rbTypeBtn4.gone()
            rbTypeBtn5.gone()
            etOthers.gone()

            if (category == R.id.rbFacility) {
                rbTypeBtn1.visible()
                rbTypeBtn3.visible()
                rbTypeBtn4.visible()
                rbTypeBtn5.visible()
                rbTypeBtn1.setText(R.string.opd_triage)
                rbTypeBtn2.setText(R.string.outpatient)
                rbTypeBtn3.setText(R.string.inpatient)
                rbTypeBtn4.setText(R.string.pharmacy)
                rbTypeBtn5.setText(R.string.Other)
            } else if (category == R.id.rbCommunity) {
                rbTypeBtn1.visible()
                rbTypeBtn1.setText(R.string.door_to_door)
                rbTypeBtn1.isChecked = true
                rbTypeBtn2.setText(R.string.camp)
            }
        }
    }

    override fun onCheckedChanged(radioGroup: FlexBoxRadioGroup?, radioButton: Int) {
        val isFacilitySelected = viewModel.siteDetail.category == Facility
        when (radioGroup?.checkedRadioButtonId) {
            R.id.rbTypeBtn1 -> configureCategoryType(
                if (isFacilitySelected) R.string.opd_triage else R.string.door_to_door,
                if (isFacilitySelected) OPDTriage else DoorToDoor
            )

            R.id.rbTypeBtn2 -> configureCategoryType(
                if (isFacilitySelected) R.string.outpatient else R.string.camp,
                if (isFacilitySelected) outpatient else Camp
            )

            R.id.rbTypeBtn3 -> configureCategoryType(
                R.string.inpatient,
                inpatient
            )

            R.id.rbTypeBtn4 -> configureCategoryType(
                R.string.pharmacy,
                Pharmacy
            )

            R.id.rbTypeBtn5 -> {
                if (isFacilitySelected) {
                    binding.etOthers.visible()
                    binding.etOthers.text = null
                    viewModel.siteDetail.categoryDisplayType = getString(R.string.Other)
                    viewModel.siteDetail.categoryType = DefinedParams.Other
                }
            }
        }
        validateToEnableNext()
    }

    private fun configureCategoryType(
        displayResId: Int,
        type: String
    ) {
        binding.etOthers.gone()
        viewModel.siteDetail.apply {
            categoryDisplayType = getString(displayResId)
            categoryType = type
        }
    }

    private fun validateToEnableNext() {
        val categorySelected = binding.rgCategoryRow.checkedRadioButtonId != -1
        var typeSelected = binding.rgType.checkedRadioButtonId != -1
        if (viewModel.siteDetail.categoryType.equals(DefinedParams.Other, true)) {
            typeSelected = !binding.etOthers.text?.trim().isNullOrBlank()
        }
        binding.btnNext.isEnabled =
            categorySelected && typeSelected && viewModel.siteDetail.referredSiteId != -1L
    }
}
