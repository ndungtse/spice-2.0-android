package com.medtroniclabs.spice.ui.common

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentLabourOrDeliveryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.MedicalReviewBaseViewModel

class LabourOrDeliveryFragment : BaseFragment() {

    private lateinit var binding: FragmentLabourOrDeliveryBinding
    private var datePickerDialog: DatePickerDialog? = null
    private val viewModel: MedicalReviewBaseViewModel by activityViewModels()
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
        spinnerForDeliveryType()
        initializeDatePicker()
        setListenerToTimeOfDelivery()
        setListenerToTimeOfLabourOnset()
    }

    private fun setListenerToTimeOfLabourOnset() {
        getDataFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.timeOfLabourOnsetMap,
                Pair(DefinedParams.TimeOfLabourOnset,null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                timeOfLabourOnsetSingleSelectionCallback
            )
            binding.timeOfLabourOnsetGroup.addView(view)
        }
    }

    private fun setListenerToTimeOfDelivery() {
        getDataFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.timeOfDeliveryMap,
                Pair(DefinedParams.TimeOfDelivery,null),
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

    private var timeOfDeliverySingleSelectionCallback: ((selectedID: Any?, elementId: Pair<String,String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.timeOfDeliveryMap[DefinedParams.TimeOfDelivery] = selectedID as String
        }

    private var timeOfLabourOnsetSingleSelectionCallback: ((selectedID: Any?, elementId: Pair<String,String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.timeOfLabourOnsetMap[DefinedParams.TimeOfLabourOnset] = selectedID as String
        }

    private fun getDataFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.am)))
        flowList.add(getOptionMap(getString(R.string.pm)))
        return flowList
    }

    private fun getOptionMap(value: String): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[DefinedParams.ID] = value
        map[DefinedParams.NAME] = value
        return map
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
                DateUtils.convertddMMMToddMM(textView.text.toString())
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                requireContext(),
                date = yearMonthDate,
                disableFutureDate = true,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
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

    private fun spinnerForDeliveryType() {
        val list = arrayListOf<Map<String, Any>>()
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to "Select",
                DefinedParams.ID to DefinedParams.DefaultSelectID
            )
        )
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to "Type 2",
                DefinedParams.ID to "1L"
            )
        )
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to "Type 3",
                DefinedParams.ID to "2L"
            )
        )
        setListenerToDeliveryType(list)
        setListenerToDeliveryAt(list)
        setListenerToDeliveryBy(list)
        setListenerToDeliveryStatus(list)
    }

    private fun setListenerToDeliveryStatus(list: ArrayList<Map<String, Any>>) {
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.etDeliveryStatus.adapter = adapter
    }

    private fun setListenerToDeliveryBy(list: ArrayList<Map<String, Any>>) {
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.etDeliveryBy.adapter = adapter
    }

    private fun setListenerToDeliveryAt(list: ArrayList<Map<String, Any>>) {
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.etDeliveryAt.adapter = adapter
    }

    private fun setListenerToDeliveryType(list: ArrayList<Map<String, Any>>) {
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.etDeliveryType.adapter = adapter
    }
}