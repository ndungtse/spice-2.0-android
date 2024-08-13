package com.medtroniclabs.spice.ui.medicalreview.motherneonate.labourdelivery.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.LabourDeliveryMetaEntity
import com.medtroniclabs.spice.databinding.FragmentLabourOrDeliveryBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.labourdelivery.viewmodel.LabourDeliveryViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums

class LabourOrDeliveryFragment : BaseFragment() {

    private lateinit var binding: FragmentLabourOrDeliveryBinding
    private var datePickerDialog: DatePickerDialog? = null
    private val viewModel: LabourDeliveryViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLabourOrDeliveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "LabourOrDeliveryFragment"

        fun newInstance(): LabourOrDeliveryFragment {
            return LabourOrDeliveryFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mandatoryFields()
        attachObserver()
        initListeners()
        initializeDatePicker()
        initializeTimeOfDelivery()
        initializeTimeOfLabourOnset()
    }

    private fun initListeners() {
        binding.etHrsTimeOfDelivery.doAfterTextChanged {
            val timeOfDeliveryInHour = it?.trim().toString()
            if (timeOfDeliveryInHour.isNotEmpty()) {
                viewModel.timeOfDeliveryInHour = it.toString()
                viewModel.validateSubmitButtonState()
            } else {
                viewModel.timeOfDeliveryInHour = null
                viewModel.validateSubmitButtonState()
            }

        }

        binding.etMinutesTimeOfDelivery.doAfterTextChanged {
            val timeOfDeliveryInMinutes = it?.trim().toString()
            if (timeOfDeliveryInMinutes.isNotEmpty()) {
                viewModel.timeOfDeliveryInMinute = it.toString()
                viewModel.validateSubmitButtonState()
            } else {
                viewModel.timeOfDeliveryInMinute = null
                viewModel.validateSubmitButtonState()
            }
        }

        binding.etHrsTimeOfLabourOnset.doAfterTextChanged {
            val labourOnSetInMinutes = it?.trim().toString()
            if (labourOnSetInMinutes.isNotEmpty()) {
                viewModel.timeOfLabourOnSetInHour = it.toString()
                viewModel.validateSubmitButtonState()
            } else {
                viewModel.timeOfLabourOnSetInHour = null
                viewModel.validateSubmitButtonState()
            }
        }

        binding.etMinutesTimeOfLabourOnset.doAfterTextChanged {
            val labourOnSetInMinutes = it?.trim().toString()
            if (labourOnSetInMinutes.isNotEmpty()) {
                viewModel.timeOfLabourOnSetInMinutes = it.toString()
                viewModel.validateSubmitButtonState()
            } else {
                viewModel.timeOfLabourOnSetInMinutes = null
                viewModel.validateSubmitButtonState()
            }
        }

        binding.etNoOfDeonates.doAfterTextChanged {
            val noOfNenonates = it?.trim().toString()
            if (noOfNenonates.isNotEmpty()) {
                viewModel.noOfNeonates = it.toString()
                viewModel.validateSubmitButtonState()
            } else {
                viewModel.noOfNeonates = null
                viewModel.validateSubmitButtonState()
            }

        }
    }

    fun mandatoryFields() {
        binding.tvDateOfDelivery.markMandatory()
        binding.tvTimeOfDelivery.markMandatory()
        binding.tvDateOfLabourOnset.markMandatory()
        binding.tvTimeOfLabourOnset.markMandatory()
        binding.tvDeliveryType.markMandatory()
        binding.tvDeliveryBy.markMandatory()
        binding.tvDeliveryAt.markMandatory()
        binding.tvDeliveryStatus.markMandatory()
        binding.tvNoOfDeonates.markMandatory()

    }


    private fun initializeTimeOfLabourOnset() {
        getDataFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.timeOfLabourOnsetMap,
                Pair(DefinedParams.TimeOfLabourOnset, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                timeOfLabourOnsetSingleSelectionCallback
            )
            binding.timeOfLabourOnsetGroup.addView(view)
        }
    }

    private fun initializeTimeOfDelivery() {
        getDataFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.timeOfDeliveryMap,
                Pair(DefinedParams.TimeOfDelivery, null),
                FormLayout(
                    viewType = "",
                    id = "",
                    title = "",
                    visibility = "",
                    optionsList = null
                ),
                timeOfDeliverySingleSelectionCallback
            )
            binding.timeOfDeliveryGroup.addView(view)
        }
    }

    private var timeOfDeliverySingleSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.timeOfDeliveryMap[DefinedParams.TimeOfDelivery] = selectedID as String
            viewModel.validateSubmitButtonState()
        }

    private var timeOfLabourOnsetSingleSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.timeOfLabourOnsetMap[DefinedParams.TimeOfLabourOnset] = selectedID as String
            viewModel.validateSubmitButtonState()
        }

    private fun getDataFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(CommonUtils.getOptionMap(getString(R.string.am), getString(R.string.am)))
        flowList.add(CommonUtils.getOptionMap(getString(R.string.pm), getString(R.string.pm)))
        return flowList
    }

    private fun initializeDatePicker() {
        binding.etDateOfDelivery.safeClickListener {
            setListenerToDatePicker(binding.etDateOfDelivery)
        }
        binding.etDateOfLabourOnset.safeClickListener {
            setListenerToDatePicker(binding.etDateOfLabourOnset)
        }
    }

    private fun setListenerToDatePicker(textView: AppCompatTextView) {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (textView.text.toString().isNotEmpty())
            yearMonthDate =
                DateUtils.convertedMMMToddMM(textView.text.toString())
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                requireContext(),
                date = yearMonthDate,
                disableFutureDate = true,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                if (textView.id == binding.etDateOfDelivery.id) {
                    viewModel.dateOfDelivery = Triple(year, month, dayOfMonth)
                    viewModel.setDate(year, month, dayOfMonth)
                } else {
                    viewModel.dateOfLabourOnset = Triple(year, month, dayOfMonth)
                }
                viewModel.validateSubmitButtonState()
                textView.text =
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DateUtils.DATE_FORMAT_ddMMyyyy,
                        DateUtils.DATE_ddMMyyyy
                    )
                datePickerDialog = null
            }
        }
    }


    private fun attachObserver() {
        viewModel.labourDeliveryMetaList.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let { listItems ->
                        initializeDeliveryTypeItem(listItems)
                        initializeDeliveryAtItem(listItems)
                        initializeDeliveryByItem(listItems)
                        initializeDeliveryStatusItem(listItems)
                    }
                    hideProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }

        }
    }

    private fun initializeDeliveryStatusItem(listItems: List<LabourDeliveryMetaEntity>) {
        val list = arrayListOf<Map<String, Any>>()
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.ID to DefinedParams.DefaultID
            )
        )
        listItems.filter { it.category == MedicalReviewTypeEnums.DeliveryStatus.name }.forEach {
            list.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to it.name,
                    DefinedParams.ID to it.id
                )
            )
        }
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.etDeliveryStatus.adapter = adapter


        binding.etDeliveryStatus.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        val selectedName = it[DefinedParams.NAME] as String?
                        if (selectedName != DefinedParams.DefaultIDLabel) {
                            viewModel.deliveryStatus = selectedName.toString()
                            viewModel.validateSubmitButtonState()
                        } else {
                            viewModel.deliveryStatus = null
                            viewModel.validateSubmitButtonState()
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

            }
    }

    private fun initializeDeliveryAtItem(listItems: List<LabourDeliveryMetaEntity>) {
        val list = arrayListOf<Map<String, Any>>()
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.ID to DefinedParams.DefaultID
            )
        )
        listItems.filter { it.category == MedicalReviewTypeEnums.DeliveryAt.name }.forEach {
            list.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to it.name,
                    DefinedParams.ID to it.id
                )
            )
        }
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.etDeliveryAt.adapter = adapter

        binding.etDeliveryAt.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                val selectedItem = adapter.getData(position = pos)
                selectedItem?.let {
                    val selectedName = it[DefinedParams.NAME] as String?
                    if (selectedName != DefinedParams.DefaultIDLabel) {
                        viewModel.deliveryAt = selectedName
                        viewModel.validateSubmitButtonState()
                    } else {
                        viewModel.deliveryAt = null
                        viewModel.validateSubmitButtonState()
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }


    }

    private fun initializeDeliveryByItem(listItems: List<LabourDeliveryMetaEntity>) {
        val list = arrayListOf<Map<String, Any>>()
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.ID to DefinedParams.DefaultID
            )
        )
        listItems.filter { it.category == MedicalReviewTypeEnums.DeliveryBy.name }.forEach {
            list.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to it.name,
                    DefinedParams.ID to it.id
                )
            )
        }
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.etDeliveryBy.adapter = adapter

        binding.etDeliveryBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                val selectedItem = adapter.getData(position = pos)
                selectedItem?.let {
                    val selectedName = it[DefinedParams.NAME] as String?
                    if (selectedName != DefinedParams.DefaultIDLabel) {
                        viewModel.deliveryBy = selectedName.toString()
                        viewModel.validateSubmitButtonState()
                    } else {
                        viewModel.deliveryBy = null
                        viewModel.validateSubmitButtonState()
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }
    }

    private fun initializeDeliveryTypeItem(listItems: List<LabourDeliveryMetaEntity>) {
        val list = arrayListOf<Map<String, Any>>()
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.ID to DefinedParams.DefaultID
            )
        )
        listItems.filter { it.category == MedicalReviewTypeEnums.DeliveryType.name }.forEach {
            list.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to it.name,
                    DefinedParams.ID to it.id
                )
            )
        }
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.etDeliveryType.adapter = adapter


        binding.etDeliveryType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        val selectedName = it[DefinedParams.NAME] as String?
                        if (selectedName != DefinedParams.DefaultIDLabel) {
                            viewModel.deliveryType = selectedName
                            viewModel.validateSubmitButtonState()
                        } else {
                            viewModel.deliveryType = null
                            viewModel.validateSubmitButtonState()
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

            }
    }

    private fun valid12ClockMinuteRange(minute: String?): Boolean {
        if (minute.isNullOrEmpty()) {
            return false

        }
        val minuteInt = minute.toInt()
        return minuteInt in 0..59
    }

    private fun valid12ClockHourRange(hour: String?): Boolean {
        if (hour.isNullOrEmpty()) {
            return false
        }
        val hourInt = hour.toInt()
        return hourInt in 1..12
    }

    fun validate(): Boolean {
        val isValidDateTimeDelivery = labourDeliveryValidation()
        return isValidDateTimeDelivery
    }

    private fun View.showIf(condition: Boolean) {
        this.isVisible = condition
    }

    private fun showLabourDeliveryErrors(status: Boolean) {
        with(binding) {
            tvDateOfLabourOnsetError.showIf(status)
            tvDateOfDeliveryError.showIf(status)
            tvTimeOfDeliveryError.showIf(status)
            tvTimeOfLabourOnsetError.showIf(status)
            tvDeliveryTypeError.showIf(status)
            tvDeliveryByError.showIf(status)
            tvDeliveryAtError.showIf(status)
            tvDeliveryStatusError.showIf(status)
            tvNoOfDeonatesError.showIf(status)
        }
    }

    private fun labourDeliveryValidation(): Boolean {
        showLabourDeliveryErrors(false)

        val etHourTimeOfDelivery = binding.etHrsTimeOfDelivery.text?.trim().toString()
        val etMinutesTimeOfDelivery = binding.etMinutesTimeOfDelivery.text?.trim().toString()
        val dateOfDelivery = viewModel.dateOfDelivery

        val etHourTimeOfLabourOnset = binding.etHrsTimeOfLabourOnset.text?.trim().toString()
        val etMinutesTimeOfLabourOnSet = binding.etMinutesTimeOfLabourOnset.text?.trim().toString()
        val dateOfLabourOnset = viewModel.dateOfLabourOnset
        val noOfNeonate = viewModel.noOfNeonates?.toInt()
        val isValidDelivery = etHourTimeOfDelivery.isNotEmpty() &&
                etMinutesTimeOfDelivery.isNotEmpty() &&
                dateOfDelivery != null &&
                viewModel.timeOfDeliveryMap[DefinedParams.TimeOfDelivery] != null &&
                etHourTimeOfLabourOnset.isNotEmpty() &&
                etMinutesTimeOfLabourOnSet.isNotEmpty() &&
                dateOfLabourOnset != null &&
                viewModel.timeOfLabourOnsetMap[DefinedParams.TimeOfLabourOnset] != null &&
                viewModel.deliveryType != null &&
                viewModel.deliveryBy != null &&
                viewModel.deliveryAt != null &&
                viewModel.deliveryStatus != null &&
                viewModel.noOfNeonates != null && noOfNeonate != 0

        if (!isValidDelivery) {
            showLabourDeliveryErrors(true)
        }

        with(binding) {
            if(etHourTimeOfDelivery.isNotEmpty() || etMinutesTimeOfDelivery.isNotEmpty()) {
                    if (etHourTimeOfDelivery.toInt() <= 12 && etMinutesTimeOfDelivery.toInt() <= 60 && viewModel.timeOfDeliveryMap[DefinedParams.TimeOfDelivery] != null) {
                        tvTimeOfDeliveryError.showIf(false)
                    } else {
                        tvTimeOfDeliveryError.showIf(true)
                    }
                }
            if (etHourTimeOfLabourOnset.isNotEmpty()||etMinutesTimeOfLabourOnSet.isNotEmpty()) {
                if ( etHourTimeOfLabourOnset.toInt() <= 12 && etMinutesTimeOfLabourOnSet.toInt() <= 60 && viewModel.timeOfLabourOnsetMap[DefinedParams.TimeOfLabourOnset] != null) {
                    tvTimeOfLabourOnsetError.showIf(false)
                } else {
                    tvTimeOfLabourOnsetError.showIf(true)
                }
            }
//            tvTimeOfDeliveryError.showIf(etHourTimeOfDelivery.isEmpty() || etMinutesTimeOfDelivery.isEmpty() || viewModel.timeOfDeliveryMap[DefinedParams.TimeOfDelivery] == null)
            tvDateOfDeliveryError.showIf(dateOfDelivery == null)
            tvDateOfLabourOnsetError.showIf(dateOfLabourOnset == null)
//            tvTimeOfLabourOnsetError.showIf(etHourTimeOfLabourOnset.isEmpty() || etMinutesTimeOfLabourOnSet.isEmpty() || viewModel.timeOfLabourOnsetMap[DefinedParams.TimeOfLabourOnset] == null)
            tvDeliveryTypeError.showIf(viewModel.deliveryType == null)
            tvDeliveryByError.showIf(viewModel.deliveryBy == null)
            tvDeliveryAtError.showIf(viewModel.deliveryAt == null)
            tvDeliveryStatusError.showIf(viewModel.deliveryStatus == null)
            if (viewModel.noOfNeonates == null || noOfNeonate == 0) {
                tvNoOfDeonatesError.showIf(true)
            }else{
                tvNoOfDeonatesError.showIf(false)
            }

        }
        return isValidDelivery
    }

}