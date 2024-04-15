package com.medtroniclabs.spice.ui.common

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.LabourDeliveryMetaEntity
import com.medtroniclabs.spice.databinding.FragmentLabourOrDeliveryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.labourDelivery.LabourDeliveryViewModel
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
        attachObserver()
        initializeDatePicker()
        initializeTimeOfDelivery()
        initializeTimeOfLabourOnset()
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
        }

    private var timeOfLabourOnsetSingleSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.timeOfLabourOnsetMap[DefinedParams.TimeOfLabourOnset] = selectedID as String
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
    }
}