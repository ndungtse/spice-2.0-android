package com.medtroniclabs.spice.ui.medicalreview.underfiveyears


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.DefaultID
import com.medtroniclabs.spice.common.DefinedParams.DefaultIDLabel
import com.medtroniclabs.spice.common.DefinedParams.ID
import com.medtroniclabs.spice.common.DefinedParams.NAME
import com.medtroniclabs.spice.common.DefinedParams.value
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.databinding.FragmentUnderFiveYearClinicalSummarryBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.isValidInput
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.MOTHER_VITAMIN_TAG

class ClinicalSummaryUnderFiveYearsFragment : BaseFragment() {
    private lateinit var binding: FragmentUnderFiveYearClinicalSummarryBinding
    private val viewModel: UnderFiveYearsClinicalSummaryViewModel by activityViewModels()

    companion object {
        const val TAG = "ClinicalSummaryUnderFiveYearsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUnderFiveYearClinicalSummarryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListeners()
        attachObserver()
    }

    private fun initListeners() {
        binding.etWeight.doAfterTextChanged {
            viewModel.updateWeight(it.toString())
        }
        binding.etHeight.doAfterTextChanged {
            viewModel.updateHeight(it.toString())
        }
        binding.etTemperature.doAfterTextChanged {
            viewModel.updateTemperature(it.toString())
        }
        binding.etRespirationRate.doAfterTextChanged {
            val respiration = it?.trim().toString()
            viewModel.updateRespiratoryRate(
                respiration, binding.etRepeat.text?.trim().toString()
            )
            showHideRepeatField(respiration.toDoubleOrNull())
        }
        binding.etRepeat.doAfterTextChanged {
            viewModel.updateRespiratoryRate(
                binding.etRespirationRate.text?.trim().toString(), it?.trim().toString()
            )
        }
        binding.etWAZ.doAfterTextChanged {
            viewModel.updateWaz(it?.trim().toString())
        }
        binding.etWHZ.doAfterTextChanged {
            viewModel.updateWhz(it?.trim().toString())
        }

    }

    private fun initializeImmunisationStatus(list: List<MedicalReviewMetaItems>) {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                NAME to DefaultIDLabel, ID to DefaultID
            )
        )
        for (item in list) {
            dropDownList.add(
                hashMapOf<String, Any>(
                    NAME to item.name,
                    DefinedParams.id to item.id.toString(),
                    value to (item.value ?: item.name)
                )
            )
        }
        setImmunisationStatus(dropDownList)
    }

    private fun initializeMuacStatus(list: List<MedicalReviewMetaItems>) {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                NAME to DefaultIDLabel, ID to DefaultID
            )
        )
        for (item in list) {
            dropDownList.add(
                hashMapOf<String, Any>(
                    NAME to item.name,
                    DefinedParams.id to item.id.toString(),
                    value to (item.value ?: item.name)
                )
            )
        }
        setListenerInitializeMuacStatus(dropDownList)
    }


    private fun setImmunisationStatus(list: ArrayList<Map<String, Any>>) {
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.etImmunisation.adapter = adapter
        binding.etImmunisation.setSelection(0, false)
        binding.etImmunisation.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    val selectedItem = adapter.getData(position = position)
                    selectedItem?.let {
                        val selectedId = it[ID] as String?
                        val selectedImmunisationStatus = it[DefinedParams.value] as String?
                        if (selectedId != DefaultID) {
                            selectedImmunisationStatus?.let {
                                viewModel.selectedImmunisationStatus = it
                                viewModel.updateImmunisationStatus()
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    private fun initView() {
        binding.tvHeightLabel.markMandatory()
        binding.tvWeightLabel.markMandatory()
        binding.tvWAZLabel.markMandatory()
        binding.tvWHZLabel.markMandatory()
        getFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = MOTHER_VITAMIN_TAG
            view.addViewElements(
                it,
                false,
                viewModel.resultMotherVitaminHashMap,
                Pair(MOTHER_VITAMIN_TAG, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                vitaminAMotherSelectionCallBack
            )
            binding.etVitaminAForMother.addView(view)
        }
        getFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context).apply {
                tag = MedicalReviewDefinedParams.Albendazole
                addViewElements(
                    it,
                    false,
                    viewModel.albendazoleHashMap,
                    Pair(MedicalReviewDefinedParams.Albendazole, null),
                    FormLayout(
                        viewType = "", id = "", title = "", visibility = "", optionsList = null
                    ),
                    albendazoleSelectionCallBack
                )
            }
            binding.llAlbendazoleStatus.addView(view)
        }

        viewModel.getImmunisationStatusMetaItems()
        viewModel.getMuAcStatusMetaItems()

    }

    private fun attachObserver() {
        viewModel.summaryMuacMetaItems.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { list ->
                        initializeMuacStatus(list)

                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
        viewModel.summaryMetaListItems.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { list ->
                        initializeImmunisationStatus(list)

                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private var vitaminAMotherSelectionCallBack: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultMotherVitaminHashMap[MOTHER_VITAMIN_TAG] = selectedID as String
            viewModel.updateVitaminAForMother()
        }

    private var albendazoleSelectionCallBack: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.albendazoleHashMap[MedicalReviewDefinedParams.Albendazole] =
                selectedID as String
            viewModel.updateAlbendazole()
        }

    private fun getFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(CommonUtils.getOptionMap(getString(R.string.yes), getString(R.string.yes)))
        flowList.add(CommonUtils.getOptionMap(getString(R.string.no), getString(R.string.no)))
        return flowList
    }

    fun validateEditFields(): Boolean {
        val weight = weightValidate()
        val height = heightValidate()
        val temperature = temperatureValidate()
        val respirationRate = respirationRateValidate()
        val repeat = repeatValidate()
        val whz = whzValidate()
        val waz = wazValidate()
        return weight && height && temperature && respirationRate && repeat && whz && waz
    }

    private fun whzValidate(): Boolean {
        return if (binding.etWHZ.text?.isEmpty() == true) {
            binding.tvWHZError.visible()
            binding.tvWHZError.text = getString(R.string.error_label)
            false
        } else {
            binding.tvWHZError.gone() // Clear error message if needed
            true
        }
    }

    private fun wazValidate(): Boolean {
        return if (binding.etWAZ.text?.isEmpty() == true) {
            binding.tvWAZError.visible()
            binding.tvWAZError.text = getString(R.string.error_label)
            false
        } else {
            binding.tvWAZError.gone() // Clear error message if needed
            true
        }
    }


    private fun heightValidate(): Boolean {
        return isValidInput(
            binding.etHeight.text.toString(),
            binding.etHeight,
            binding.tvHeightError,
            50.0..300.0,
            R.string.height_error,
            true,
            requireContext()
        )
    }

    private fun respirationRateValidate(): Boolean {
        return isValidInput(
            binding.etRespirationRate.text.toString(),
            binding.etRespirationRate,
            binding.tvRespirationRateError,
            0.0..100.0,
            (R.string.respiratory_error),
            false,
            requireContext()
        )
    }

    private fun repeatValidate(): Boolean {
        return isValidInput(
            binding.etRepeat.text.toString(),
            binding.etRepeat,
            binding.tvRepeatError,
            0.0..100.0,
            (R.string.please_enter_repeat_between_0_to_100),
            false,
            requireContext()
        )
    }

    private fun temperatureValidate(): Boolean {
        return isValidInput(
            binding.etTemperature.text.toString(),
            binding.etTemperature,
            binding.tvTemperatureLabelError,
            10.0..300.0,
            (R.string.temperature),
            false,
            requireContext()
        )
    }

    private fun weightValidate(): Boolean {
        return isValidInput(
            binding.etWeight.text.toString(),
            binding.etWeight,
            binding.tvWeightError,
            10.0..400.0,
            R.string.weight_error,
            true,
            requireContext()
        )
    }

    private fun setListenerInitializeMuacStatus(list: ArrayList<Map<String, Any>>) {

        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.etMUACStatus.adapter = adapter
        binding.etMUACStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selectedItem = adapter.getData(position = position)
                val selectedValue = selectedItem?.get(value) as String?
                val selectedId = selectedItem?.get(ID) as String?
                if (selectedId != DefaultID) {
                    selectedValue?.let { value ->
                        binding.muacStatusGroup.visible()
                        viewModel.selectedMuacStatus = value
                        viewModel.updateMuac()
                        setMuacStatus()
                    }
                } else {
                    binding.muacStatusGroup.gone()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                /**
                 * this method is not used
                 */
            }
        }
    }

    private fun setMuacStatus() {
        val muacText: String
        val muacInput: String

        when (viewModel.selectedMuacStatus) {
            getString(R.string.green) -> {
                muacText = getString(R.string.muac_green)
                muacInput = getString(R.string.normal)
            }

            getString(R.string.red) -> {
                muacText = getString(R.string.muac_red)
                muacInput = getString(R.string.severe_nutrition)
            }

            getString(R.string.yellow) -> {
                muacText = getString(R.string.muac_yellow)
                muacInput = getString(R.string.moderate_malnutrition)
            }

            else -> {
                binding.muacStatusGroup.gone()
                muacText = getString(R.string.empty__)
                muacInput = getString(R.string.empty__)
            }
        }
        binding.tvMUACText.text = muacText
        binding.tvMUACInput.text = muacInput
    }

    private fun showHideRepeatField(respiration: Double?) {
        if (respiration != null && respiration > 60.0) {
            binding.repeatGroup.visible()
        } else {
            binding.repeatGroup.invisible()
        }
    }

}