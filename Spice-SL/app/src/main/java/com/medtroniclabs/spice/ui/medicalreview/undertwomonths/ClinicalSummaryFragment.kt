package com.medtroniclabs.spice.ui.medicalreview.undertwomonths

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.getOptionMap
import com.medtroniclabs.spice.common.DefinedParams.DefaultID
import com.medtroniclabs.spice.common.DefinedParams.DefaultIDLabel
import com.medtroniclabs.spice.common.DefinedParams.ID
import com.medtroniclabs.spice.common.DefinedParams.NAME
import com.medtroniclabs.spice.common.DefinedParams.Yes
import com.medtroniclabs.spice.databinding.FragmentClinicalSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.BREAST_FEEDING_TAG
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.EXCLUSIVE_BREAST_FEED_TAG
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.MOTHER_VITAMIN_TAG

class ClinicalSummaryFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentClinicalSummaryBinding
    val viewModel: UnderTwoMonthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentClinicalSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (super.onViewCreated(view, savedInstanceState))
        initView()
        initializeDeliveryTypeItem()
        clickListener()
    }

    private fun initializeEditText() {
        with(binding) {
            etHeight.addTextChangedListener(textWatcher)
            etWeight.addTextChangedListener(textWatcher)
            etTemperature.addTextChangedListener(textWatcher)
            etRespirationRate.addTextChangedListener(textWatcher)
            etRepeat.addTextChangedListener(textWatcher)
            etWAZ.addTextChangedListener(textWatcher)
            etWHZ.addTextChangedListener(textWatcher)
        }
    }

    private fun clickListener() {
        binding.etVitaminAForMother.safeClickListener(this)
        binding.llExclusiveBreastFeedingStatus.safeClickListener(this)
        binding.llBreastFeedingStatus.safeClickListener(this)
    }

    fun initView() {
        initializeEditText()
        getBreastFeedingFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = BREAST_FEEDING_TAG
            view.addViewElements(
                it,
                false,
                viewModel.resultBreastFeedingHashMap,
                Pair(BREAST_FEEDING_TAG, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                breastFeedingSelectionCallback
            )
            binding.llBreastFeedingStatus.addView(view)
        }

        getVitaminAMotherFlowData().let {
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

        getExclusiveBreastFeedingFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = EXCLUSIVE_BREAST_FEED_TAG
            view.addViewElements(
                it,
                false,
                viewModel.exclusiveBreastFeedHashMap,
                Pair(EXCLUSIVE_BREAST_FEED_TAG, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                exclusiveBreastFeedingSelectionCallBack
            )
            binding.llExclusiveBreastFeedingStatus.addView(view)
        }

    }

    private var breastFeedingSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultBreastFeedingHashMap[BREAST_FEEDING_TAG] = selectedID as String
            (requireActivity() as? UnderTwoMonthsBaseActivity)?.updateNextButtonState()
            enableExclusiveBreastFeeding(selectedID)
        }

    private var vitaminAMotherSelectionCallBack: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultMotherVitaminHashMap[MOTHER_VITAMIN_TAG] = selectedID as String
            (requireActivity() as? UnderTwoMonthsBaseActivity)?.updateNextButtonState()
        }

    private var exclusiveBreastFeedingSelectionCallBack: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.exclusiveBreastFeedHashMap[EXCLUSIVE_BREAST_FEED_TAG] = selectedID as String
            (requireActivity() as? UnderTwoMonthsBaseActivity)?.updateNextButtonState()
        }

    private fun enableExclusiveBreastFeeding(selectedID: Any?) {
        if (selectedID == Yes) {
            binding.exclusiveBreastFeedingGroup.visible()
        } else {
            binding.exclusiveBreastFeedingGroup.gone()
        }
    }

    private fun getBreastFeedingFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.yes), getString(R.string.yes)))
        flowList.add(getOptionMap(getString(R.string.no), getString(R.string.no)))
        return flowList
    }

    private fun getVitaminAMotherFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.yes), getString(R.string.yes)))
        flowList.add(getOptionMap(getString(R.string.no), getString(R.string.no)))
        return flowList
    }

    private fun getExclusiveBreastFeedingFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.yes), getString(R.string.yes)))
        flowList.add(getOptionMap(getString(R.string.no), getString(R.string.no)))
        return flowList
    }

    private fun initializeDeliveryTypeItem() {
        val list = arrayListOf<Map<String, Any>>()
        list.add(
            hashMapOf<String, Any>(
                NAME to DefaultIDLabel,
                ID to DefaultID
            )
        )
        list.add(
            hashMapOf<String, Any>(
                NAME to "Status 1",
                ID to "1L"
            )
        )
        list.add(
            hashMapOf<String, Any>(
                NAME to "Status 2",
                ID to "2L"
            )
        )
        setListenerToDeliveryStatus(list)
    }

    private fun setListenerToDeliveryStatus(list: ArrayList<Map<String, Any>>) {
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.etImmunisationStatus.adapter = adapter
        binding.etImmunisationStatus.setSelection(0, false)
        binding.etImmunisationStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = adapter.getData(position = position)
                selectedItem?.let {
                    val selectedId = it[ID] as String?
                    val selectedImmunisationStatus = it[NAME] as String?
                    if (selectedId != DefaultID) {
                            viewModel.selectedImmunisationStatus = selectedImmunisationStatus
                        (requireActivity() as? UnderTwoMonthsBaseActivity)?.updateNextButtonState()
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

    override fun onClick(v: View?) {

    }

    fun isAnyEditTextFilled(): Boolean {
        return binding.etWeight.text?.isNotBlank() == true &&
                binding.etHeight.text?.isNotBlank() == true &&
                binding.etTemperature.text?.isNotBlank() == true &&
                binding.etRepeat.text?.isNotBlank() == true &&
                binding.etRespirationRate.text?.isNotBlank() == true &&
                viewModel.selectedImmunisationStatus != null &&
                viewModel.resultMotherVitaminHashMap[MOTHER_VITAMIN_TAG] != null &&
                viewModel.resultBreastFeedingHashMap[BREAST_FEEDING_TAG] != null ||
                viewModel.exclusiveBreastFeedHashMap[EXCLUSIVE_BREAST_FEED_TAG] != null
    }

    fun validateEditFields(): Boolean {
        val weight = weightValidate()
        val height = heightValidate()
        val temperature = temperatureValidate()
        val respirationRate = respirationRateValidate()
        val repeat = repeatValidate()
        return weight && height && temperature && respirationRate && repeat
    }

    private fun heightValidate(): Boolean {
        return isValidInput(
            binding.etHeight.text.toString(),
            binding.etHeight,
            binding.tvHeightError,
            50.0..300.0,
            R.string.height_error
        )
    }

    private fun respirationRateValidate(): Boolean {
        return isValidInput(
            binding.etHeight.text.toString(),
            binding.etHeight,
            binding.tvHeightError,
            0.0..60.0,
            R.string.height_error
        )
    }

    private fun repeatValidate(): Boolean {
        return isValidInput(
            binding.etHeight.text.toString(),
            binding.etHeight,
            binding.tvHeightError,
            0.0..60.0,
            R.string.height_error
        )
    }

    private fun temperatureValidate(): Boolean {
        return isValidInput(
            binding.etHeight.text.toString(),
            binding.etHeight,
            binding.tvHeightError,
            10.0..300.0,
            R.string.height_error
        )
    }

    private fun isValidInput(
        inputText: String,
        editText: EditText,
        errorTextView: TextView,
        validRange: ClosedRange<Double>,
        errorMessageResId: Int
    ): Boolean {
        val input = inputText.toDoubleOrNull()
        if (editText.text.isNullOrBlank()) {
            errorTextView.gone()
            return true
        }
        if (!(input != null && input in validRange)) {
            errorTextView.visible()
            errorTextView.text = editText.context.getString(errorMessageResId)
            return false
        }
        errorTextView.gone()
        return true
    }

    private fun weightValidate(): Boolean {
        return isValidInput(
            binding.etWeight.text.toString(),
            binding.etWeight,
            binding.tvWeightError,
            10.0..400.0,
            R.string.weight_error
        )
    }

    private val textWatcher = object  : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            /**
             * this method is not used
             */
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            /**
             * this method is not used
             */
        }

        override fun afterTextChanged(s: Editable?) {
            (requireActivity() as? UnderTwoMonthsBaseActivity)?.updateNextButtonState()
        }

    }

}